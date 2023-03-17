package com.aliyun.odps.web3.udf;

import com.aliyun.odps.udf.UDF;
import com.aliyun.odps.web3.common.TypeConvertUtils;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.AbiTypes;

public class decodeIndexedValue extends UDF {
  public String evaluate(String data, String type) {
    return TypeConvertUtils.convert2String(FunctionReturnDecoder.decodeIndexedValue(data, TypeReference.create(AbiTypes.getType(type))));
  }
}
