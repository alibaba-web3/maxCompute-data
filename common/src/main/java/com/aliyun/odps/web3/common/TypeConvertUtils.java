package com.aliyun.odps.web3.common;

import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.*;
import org.web3j.utils.Numeric;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: smy
 * @Date: 2023/3/13 9:46 PM
 */
public class TypeConvertUtils {

    public static String convert2String(Type parameter) {
        if (parameter instanceof NumericType) {
            return String.valueOf(parameter.getValue());
        } else if (parameter instanceof Address) {
            return String.valueOf(parameter.getValue());
        } else if (parameter instanceof DynamicBytes) {
            return Numeric.toHexString(((DynamicBytes) parameter).getValue());
        } else if (parameter instanceof StaticArray) {
            List<Type> list = ((StaticArray) parameter).getValue();
            return convert2String(list).toString();
        } else if (parameter instanceof DynamicArray) {
            List<Type> list = ((DynamicArray) parameter).getValue();
            return convert2String(list).toString();
        } else {
            return TypeEncoder.encode(parameter);
        }

    }

    public static List<String> convert2StringList(Type parameter) {
        List<Type> list;
        if (parameter instanceof StaticArray) {
            list = ((StaticArray) parameter).getValue();
        } else if (parameter instanceof DynamicArray) {
            list = ((DynamicArray) parameter).getValue();
        } else {
            throw new UnsupportedOperationException(
                    "Type cannot be encoded by 'convert2StringList': " + parameter.getClass());
        }
        List<String> result = new ArrayList<>(list.size());
        for (Type type : list) {
            result.add(convert2String(type));
        }
        return result;
    }

    public static List<String> convert2String(List<Type> list) {
        return list.stream().map(TypeConvertUtils::convert2String).collect(Collectors.toList());
    }
}
