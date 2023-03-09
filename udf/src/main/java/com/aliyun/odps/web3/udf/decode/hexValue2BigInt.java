package com.aliyun.odps.web3.udf.decode;

import com.aliyun.odps.udf.UDF;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

public class hexValue2BigInt extends UDF {
    // TODO define parameters and return type, e.g:  public String evaluate(String a, String b)
    public String evaluate(String s) {
        return Numeric.toBigInt(s).toString();
    }

}