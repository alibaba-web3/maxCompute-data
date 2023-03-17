package com.aliyun.odps.web3.udf;

import com.alibaba.fastjson.JSON;
import com.aliyun.odps.udf.UDF;
import com.aliyun.odps.web3.common.TypeConvertUtils;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.AbiTypes;
import org.web3j.abi.datatypes.Type;

import java.util.*;

/**
 * @Author: cxw
 * @Date: 2023/3/16
 */
public class decodeLogData extends UDF {
    /**
     * Decode log data
     * Example: evaluate(log.data, "bytes32 column_1, bytes32 column_2, uint256 column_3")
     */
    public String evaluate(String data, String signature) {
        return JSON.toJSONString(getMap(data, signature));
    }

    public Map<String, String> getMap(String data, String signature) {
        String[] params = Arrays.stream(signature.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);

        // 获取字段名
        String[] keys = Arrays.stream(params).map(p -> p.split(" ")[1].trim()).filter(s -> !s.isEmpty()).toArray(String[]::new);

        // 获取字段值
        List<TypeReference<Type>> typeReferences = new ArrayList<>();
        Arrays.stream(params)
                .map(p -> p.split(" ")[0].trim()).filter(s -> !s.isEmpty())
                .forEach(t -> typeReferences.add((TypeReference<Type>) TypeReference.create(AbiTypes.getType(t))));
        String[] values = FunctionReturnDecoder.decode(data, typeReferences)
                .stream().map(TypeConvertUtils::convert2String).toArray(String[]::new);

        Map<String, String> map = new HashMap<>();
        for(int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return map;
    }

    // for test
    public static void main(String[] args) {
        System.out.println((new decodeLogData()).evaluate(
            "0x00000000000000000000000000000000000000000000000000000000000000000555c04a3f195d6efe57355e9dc6fec376e80d4f463d8d998db143a4decf9d570000000000000000000000000000000000000000000000000186cc6acd4b0000",
            "bytes32 a, bytes32 b, uint256 c"
        ));
        System.out.println((new decodeLogData()).evaluate(
            "0x000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000d529ae9e86000000000000000000000000000000000000000000000005466d0aff4d18ef0081500000000000000000000000000000000000000000000000000000000000000000",
            "uint256 a, uint256 b, uint256 c, uint256 d"
        ));
    }
}
