package com.aliyun.odps.web3.udf.decode;

import com.aliyun.odps.udf.UDF;
import org.web3j.abi.EventEncoder;

public class buildEventSignature extends UDF {
    // TODO define parameters and return type, e.g:  public String evaluate(String a, String b)
    public String evaluate(String s) {
        return EventEncoder.buildEventSignature(s);
    }
}