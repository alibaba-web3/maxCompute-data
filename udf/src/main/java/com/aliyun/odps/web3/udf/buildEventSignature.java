package com.aliyun.odps.web3.udf;

import com.aliyun.odps.udf.UDF;
import org.web3j.abi.EventEncoder;

public class buildEventSignature extends UDF {
    public String evaluate(String s) {
        return EventEncoder.buildEventSignature(s);
    }

    // for test
    public static void main(String[] args) {
        System.out.println(
            (new buildEventSignature()).evaluate("OrdersMatched(bytes32,bytes32,address,address,uint256,bytes32)")
        );
    }
}
