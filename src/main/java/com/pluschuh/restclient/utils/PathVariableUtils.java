package com.pluschuh.restclient.utils;

import com.pluschuh.restclient.valueobject.PathVariableValueObject;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 动态URL路径类，其中url形如/api/v1/product/{productId}
 *
 * @author pluschuh
 */
public final class PathVariableUtils {

    /**
     * 左括号
     */
    private static final char LEFT = '{';

    /**
     * 右括号
     */
    private static final char RIGHT = '}';


    public static String replacePathVariables(String path, List<PathVariableValueObject> pathVariables) {
        if (Objects.isNull(pathVariables) || pathVariables.isEmpty()) {
            return path;
        }
        for (PathVariableValueObject pathVariableValueObject : pathVariables) {
            String replace = "{" + pathVariableValueObject.getName() + "}";
            path = StringUtils.replace(path, replace, pathVariableValueObject.getValue());
        }
        return path;
    }

    /**
     * 按顺序用传入的参数替换调请求路径中的动态参数，
     * 如原始定义的路径为/api/v1/product/{productId}，传入"123"则得到/api/v1/product/123
     *
     * @param pathVariables 更多用于替换请求路径中动态参数的值
     * @return 替换后的请求路径
     */
    public static String replacePathVariables(String path, ArrayList<String> pathVariables) {
        if (Objects.isNull(pathVariables) || pathVariables.isEmpty()) {
            return path;
        }

        StringBuilder result = new StringBuilder();
        int indexReplace = -1;
        boolean startIgnore = false;
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            //遇到左括号
            if (c == LEFT && !startIgnore) {
                startIgnore = true;
                indexReplace++;
                if (indexReplace > pathVariables.size() - 1) {
                    result.append(path.substring(i));
                    break;
                }
            }
            //遇到右括号
            else if (c == RIGHT && startIgnore) {
                //忽略右括号并用入参中的第indexReplace个参数拼接
                result.append(pathVariables.get(indexReplace));
                //重置ignore状态
                startIgnore = false;
            }
            //在括号中则忽略
            else if (startIgnore) {
                //DO NOTHING
            }
            //不在括号中则拼接
            else {
                result.append(c);
            }
        }
        return result.toString().trim();
    }

    /**
     * 尝试获取路径中的参数名集合
     * @param path  原始路径
     * @return  取路径中的参数名集合，可能为空但不会是null。传入 /api/project/{projectId}/offline/task/{taskId}/detail，则返回 [projectId,taskId]
     */
    public static List<String> tryFindPathVariableNames(String path) {
        List<String> ans = new ArrayList<>();
        if (StringUtils.isBlank(path)) return ans;

        StringBuilder pathVariableName = new StringBuilder();
        boolean startIgnore = false;
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            //遇到左括号
            if (c == LEFT && !startIgnore) {
                startIgnore = true;
            }
            //遇到右括号
            else if (c == RIGHT && startIgnore) {
                //忽略右括号并用入参中的第indexReplace个参数拼接
                //重置ignore状态
                startIgnore = false;
                ans.add(String.valueOf(pathVariableName));
                pathVariableName = new StringBuilder();
            }
            //在括号中则拼接
            else if (startIgnore) {
                pathVariableName.append(c);
            }
            //不在括号中则拼接
            else {
                //DO NOTHING
            }
        }
        return ans;
    }

}
