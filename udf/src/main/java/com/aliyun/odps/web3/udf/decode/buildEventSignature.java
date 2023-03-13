package com.aliyun.odps.web3.udf.decode;

import com.aliyun.odps.udf.UDF;
import org.web3j.abi.EventEncoder;

public class buildEventSignature extends UDF {

    public String evaluate(String s) {
        return EventEncoder.buildEventSignature(s);
    }

    public static void main(String[] args) {
        String sign = EventEncoder.buildEventSignature("OrdersMatched(bytes32,bytes32,address,address,uint256,bytes32)");
        System.out.println(sign);
    }
}