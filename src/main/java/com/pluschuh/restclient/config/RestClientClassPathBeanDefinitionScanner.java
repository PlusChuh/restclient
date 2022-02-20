package com.pluschuh.restclient.config;

import com.pluschuh.restclient.annotation.RestClient;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import javax.annotation.Nonnull;

/**
 * REST客户端类路径扫描器
 *
 * @author pluschuh
 */
class RestClientClassPathBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {

    public RestClientClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, Environment environment, ResourceLoader resourceLoader) {
        super(registry, false, environment, resourceLoader);
        init();
    }

    private void init() {
        this.addIncludeFilter(new RestClientTypeFilter());
    }

    @Override
    protected boolean isCandidateComponent(@Nonnull AnnotatedBeanDefinition beanDefinition) {
        AnnotationMetadata metadata = beanDefinition.getMetadata();
        return metadata.isInterface() && metadata.hasAnnotation(RestClient.class.getName());
    }

    private static class RestClientTypeFilter implements TypeFilter {

        @Override
        public boolean match(MetadataReader metadataReader, @Nonnull MetadataReaderFactory metadataReaderFactory) {
            boolean anInterface = metadataReader.getClassMetadata().isInterface();
            boolean hasAnnotation = metadataReader.getAnnotationMetadata().hasAnnotation(RestClient.class.getName());
            return anInterface && hasAnnotation;
        }
    }
}
