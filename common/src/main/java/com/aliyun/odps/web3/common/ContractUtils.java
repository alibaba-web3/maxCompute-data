package com.aliyun.odps.web3.common;

import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.AbiTypes;
import org.web3j.abi.datatypes.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: smy
 * @Date: 2023/3/13 3:26 PM
 */
public class ContractUtils {


    /**
     * decode nonIndex data
     *
     * Example:
     *      decodeParameter(log.data, "bytes32,bytes32,uint")
     * @param data
     * @param nonIndexParameters
     * @return
     */
    public static List<Type> decodeParameter(String data, String nonIndexParameters) {
        String[] types = nonIndexParameters.split(",");
        List<TypeReference<Type>> parameters = new ArrayList<>();
        Arrays.stream(types).forEach(t -> {
            parameters.add((TypeReference<Type>) TypeReference.create(AbiTypes.getType(t)));
        });
        return FunctionReturnDecoder.decode(data, parameters);
    }

    /**
     * decode index data
     *
     * Example:
     *      decodeIndexedValue(log.topic2, "bytes32")
     * @param data
     * @param type
     * @return
     */
    public static Type decodeIndexedValue(String data, String type) {
        return FunctionReturnDecoder.decodeIndexedValue(data, TypeReference.create(AbiTypes.getType(type)));
    }

    public static void main(String[] args) {
    }
}
