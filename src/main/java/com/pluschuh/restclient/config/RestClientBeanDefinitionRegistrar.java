package com.pluschuh.restclient.config;

import com.pluschuh.restclient.annotation.RestClient;
import com.pluschuh.restclient.support.RestClientFactory;
import com.pluschuh.restclient.utils.EnvironmentPropUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Semaphore;

/**
 * REST客户端注册器
 *
 * @author pluschuh
 */
class RestClientBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestClientBeanDefinitionRegistrar.class);

    private static final Semaphore PERMIT = new Semaphore(1);

    private static final Set<String> SCANNED_PACKAGES = new ConcurrentSkipListSet<>();

    private static final Map<Class<?>, String> registeredBeans = new ConcurrentHashMap<>();

    private Environment environment;

    private ResourceLoader resourceLoader;

    public static final String scannedPackagesAttributeName = "scannedPackages";

    public RestClientBeanDefinitionRegistrar() {
        LOGGER.info("RestClientBeanDefinitionRegistrar constructed");
    }

    @Override
    public void registerBeanDefinitions(@Nonnull AnnotationMetadata importingClassMetadata, @Nonnull BeanDefinitionRegistry registry) {
        try {
            PERMIT.acquire();

            Set<BeanDefinition> restClientInterfaceBeanDefinitions = scanRestClientInterfaces(importingClassMetadata, registry);
            if (restClientInterfaceBeanDefinitions.isEmpty()) {
                LOGGER.debug("has none scanned rest clients, will do nothing");
                return;
            }

            for (BeanDefinition restClientInterfaceBeanDefinition : restClientInterfaceBeanDefinitions) {
                ScannedGenericBeanDefinition scannedRestClientInterfaceBeanDefinition = (ScannedGenericBeanDefinition) restClientInterfaceBeanDefinition;
                String beanClassName = scannedRestClientInterfaceBeanDefinition.getBeanClassName();
                Class<?> restClientClz = Class.forName(beanClassName);
                if (registeredBeans.containsKey(restClientClz)) {
                    LOGGER.debug("the class {} has been already registered, will skip", restClientClz);
                    continue;
                }
                RestClient restClientAnnotation = restClientClz.getAnnotation(RestClient.class);
                String conditionalOnProperty = restClientAnnotation.conditionalOnProperty();
                if (StringUtils.isNotBlank(conditionalOnProperty) && !environment.containsProperty(conditionalOnProperty)) {
                    LOGGER.info("{} not exist, will skip {}", conditionalOnProperty, restClientClz);
                    continue;
                }

                RootBeanDefinition restClientFactoryBeanDefinition = new RootBeanDefinition(RestClientFactory.class);
                ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
                constructorArgumentValues.addIndexedArgumentValue(0, restClientClz);
                constructorArgumentValues.addIndexedArgumentValue(1, restClientAnnotation);
                restClientFactoryBeanDefinition.setConstructorArgumentValues(constructorArgumentValues);

                String beanName = restClientAnnotation.name();
                if (StringUtils.isBlank(beanName)) {
                    String clzSimpleName = restClientClz.getSimpleName();
                    if (clzSimpleName.length() == 1) {
                        beanName = clzSimpleName.toLowerCase();
                    } else {
                        beanName = String.valueOf(clzSimpleName.charAt(0)).toLowerCase() + clzSimpleName.substring(1);
                    }
                } else {
                    beanName = EnvironmentPropUtils.tryFindRealVal(beanName, environment);
                }
                registry.registerBeanDefinition(beanName, restClientFactoryBeanDefinition);
                registeredBeans.put(restClientClz, beanName);
            }

        } catch (InterruptedException | ClassNotFoundException e) {
            LOGGER.error("hit exception when try to register rest client bean", e);
        } finally {
            PERMIT.release();
        }

    }

    @Override
    public void setEnvironment(@Nonnull Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(@Nonnull ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    private String[] findBasePackages(AnnotationMetadata importingClassMetadata) {
        String[] basePackagesVal = null;
        MergedAnnotations annotations = importingClassMetadata.getAnnotations();
        for (MergedAnnotation<Annotation> annotation : annotations) {
            Class<Annotation> annotationType = annotation.getType();
            if (annotationType.equals(EnableRestClients.class)) {
                Optional<String[]> basePackages = annotation.getValue(scannedPackagesAttributeName, String[].class);
                basePackagesVal = basePackages.filter(strings -> strings.length > 0).orElseGet(() -> {
                    String metaClassName = importingClassMetadata.getClassName();
                    String metaPackageName = metaClassName;
                    int lastIndexOf = metaClassName.lastIndexOf(".");
                    if (lastIndexOf > 0) {
                        metaPackageName = metaClassName.substring(0, lastIndexOf);
                    }
                    LOGGER.debug("basePackages in annotation is empty, will set the basePackages as the meta class package {}", metaPackageName);
                    return new String[]{metaPackageName};
                });
            }
        }
        return basePackagesVal;
    }

    private List<String> excludeScanned(String[] basePackages) {
        List<String> unScannedBasePackages = new ArrayList<>();
        if (Objects.isNull(basePackages) || basePackages.length == 0)
            return unScannedBasePackages;
        for (String basePackage : basePackages) {
            if (StringUtils.isBlank(basePackage)) continue;
            boolean shouldExclude = false;
            for (String scannedPackage : SCANNED_PACKAGES) {
                if (basePackage.startsWith(scannedPackage)) {
                    shouldExclude = true;
                    break;
                }
            }
            if (!shouldExclude) {
                unScannedBasePackages.add(basePackage);
            } else {
                LOGGER.debug("package {} has already be scanned, will skip", basePackage);
            }
        }
        return unScannedBasePackages;
    }

    private Set<BeanDefinition> scanRestClientInterfaces(@Nonnull AnnotationMetadata importingClassMetadata, @Nonnull BeanDefinitionRegistry registry) {
        Set<BeanDefinition> allCandidateComponents = new LinkedHashSet<>();
        String[] basePackagesFromAnnotation = findBasePackages(importingClassMetadata);
        List<String> basePackages = excludeScanned(basePackagesFromAnnotation);
        if (basePackages.isEmpty()) {
            LOGGER.debug("basePackages is empty, will do nothing");
            return allCandidateComponents;
        }

        RestClientClassPathBeanDefinitionScanner classPathBeanDefinitionScanner = new RestClientClassPathBeanDefinitionScanner(registry, environment, resourceLoader);
        for (String basePackageVal : basePackages) {
            Set<BeanDefinition> candidateComponents = classPathBeanDefinitionScanner.findCandidateComponents(basePackageVal);
            allCandidateComponents.addAll(candidateComponents);
            SCANNED_PACKAGES.add(basePackageVal);
        }
        return allCandidateComponents;
    }

}
