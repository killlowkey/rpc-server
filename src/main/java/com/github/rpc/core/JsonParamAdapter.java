package com.github.rpc.core;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.github.rpc.utils.JsonUtil;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Ray
 * @date created in 2022/3/5 16:48
 */
public class JsonParamAdapter {

    private final ObjectMapper mapper = new ObjectMapper();

    public Object[] adapt(Method method, Object[] params) throws Exception {

        JsonNode jsonNode = mapper.valueToTree(params);
        if (jsonNode instanceof NullNode) {
            return new Object[]{};
        }

        if (!(jsonNode instanceof ArrayNode)) {
            return new Object[]{};
        }
        ArrayNode arrayNode = (ArrayNode) jsonNode;
        Iterator<JsonNode> iterator = arrayNode.iterator();
        ArrayList<JsonNode> jsonNodeList = new ArrayList<>();
        while (iterator.hasNext()) {
            jsonNodeList.add(iterator.next());
        }

        if (method.getGenericParameterTypes().length == 1 && method.isVarArgs()) {
            Class<?> componentType = method.getParameterTypes()[0].getComponentType();
            if (componentType.isPrimitive()) {
                Object convertParam = adaptPrimitiveVarargs(jsonNodeList, componentType);
                return new Object[]{convertParam};
            } else {
                Object[] convertParams = adaptNonPrimitiveVarargs(jsonNodeList, componentType);
                return new Object[]{convertParams};
            }
        } else {
            return convertJsonToParameters(method, jsonNodeList);
        }
    }

    private Object adaptPrimitiveVarargs(List<JsonNode> params, Class<?> componentType) {
        Object convertedParams = Array.newInstance(componentType, params.size());

        for (int i = 0; i < params.size(); i++) {
            JsonNode jsonNode = params.get(i);
            Class<?> type = JsonUtil.getJavaTypeForJsonType(jsonNode);
            Object object = mapper.convertValue(jsonNode, type);
            Array.set(convertedParams, i, object);
        }

        return convertedParams;
    }

    private Object[] adaptNonPrimitiveVarargs(List<JsonNode> params, Class<?> componentType) {
        Object[] convertedParams = (Object[]) Array.newInstance(componentType, params.size());

        for (int i = 0; i < params.size(); i++) {
            JsonNode jsonNode = params.get(i);
            Class<?> type = JsonUtil.getJavaTypeForJsonType(jsonNode);
            Object object = mapper.convertValue(jsonNode, type);
            convertedParams[i] = object;
        }

        return convertedParams;
    }

    private Object[] convertJsonToParameters(Method m, List<JsonNode> params) throws IOException {
        Object[] convertedParams = new Object[params.size()];
        Type[] parameterTypes = m.getGenericParameterTypes();

        for (int i = 0; i < parameterTypes.length; i++) {
            JsonParser paramJsonParser = mapper.treeAsTokens(params.get(i));
            JavaType paramJavaType = mapper.getTypeFactory().constructType(parameterTypes[i]);

            convertedParams[i] = mapper.readerFor(paramJavaType)
                    .with(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                    .readValue(paramJsonParser);
        }
        return convertedParams;
    }

}
