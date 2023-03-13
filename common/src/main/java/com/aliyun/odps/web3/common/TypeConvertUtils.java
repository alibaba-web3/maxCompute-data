package com.aliyun.odps.web3.common;

import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.BytesType;
import org.web3j.abi.datatypes.Type;

/**
 * @Author: smy
 * @Date: 2023/3/13 9:46 PM
 */
public class TypeConvertUtils {

    public static String convert2String(Type parameter) {
        if (parameter instanceof BytesType) {
            return TypeEncoder.encode(parameter);
        } else {
            return String.valueOf(parameter.getValue());
        }

    }
}
