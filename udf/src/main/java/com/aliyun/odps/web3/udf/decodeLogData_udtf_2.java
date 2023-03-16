package com.aliyun.odps.web3.udf;

import com.aliyun.odps.udf.annotation.Resolve;

import java.util.*;

/**
 * @Author: cxw
 * @Date: 2023/3/17
 */
@Resolve({"string,string->string,string"})
public class decodeLogData_udtf_2 extends decodeLogData_udtf {
    // for test
    public static void main(String[] args) {
        System.out.println(
            Arrays.toString((new decodeLogData_udtf_2()).getValues(
                "0x00000000000000000000000000000000000000000000000000000000000000c80000000000000000000000003d97ad696af3b577c31e703bbe266bf965e000fc",
                "int24 a, address b"
            ))
        );
    }
}
