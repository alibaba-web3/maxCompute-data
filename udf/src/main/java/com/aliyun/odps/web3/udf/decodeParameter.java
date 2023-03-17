package com.aliyun.odps.web3.udf;

import com.aliyun.odps.udf.UDF;
import com.aliyun.odps.web3.common.TypeConvertUtils;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.AbiTypes;
import org.web3j.abi.datatypes.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class decodeParameter extends UDF {
    public List<String> evaluate(String data, String nonIndexParameters) {
        String[] types = nonIndexParameters.split(",");
        if (types.length == 0) {
            return null;
        }
        List<TypeReference<Type>> parameters = new ArrayList<>();
        Arrays.stream(types).forEach(t -> {
            parameters.add((TypeReference<Type>) TypeReference.create(AbiTypes.getType(t)));
        });
        List<Type> list = FunctionReturnDecoder.decode(data, parameters);
        return list.stream().map(d -> TypeConvertUtils.convert2String(d)).collect(Collectors.toList());
    }

    // for test
    public static void main(String[] args) throws Exception {
        String data = "0x00000000000000000000000000000000000000000000000000000000000000000555c04a3f195d6efe57355e9dc6fec376e80d4f463d8d998db143a4decf9d570000000000000000000000000000000000000000000000000186cc6acd4b0000";
        decodeParameter decodeParameter = new decodeParameter();
        List<String> list = decodeParameter.evaluate(data, "bytes32,bytes32,uint256");
        list.forEach(d -> System.out.println(d));
    }
}
