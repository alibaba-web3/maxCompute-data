package com.aliyun.odps.web3.udf;

import com.aliyun.odps.udf.UDF;
import org.web3j.utils.Numeric;

public class hexValue2BigInt extends UDF {
    public String evaluate(String s) {
        return Numeric.toBigInt(s).toString();
    }
}
