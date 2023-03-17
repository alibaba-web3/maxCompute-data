package com.aliyun.odps.web3.udf;

import com.aliyun.odps.udf.UDFException;
import com.aliyun.odps.udf.annotation.Resolve;

import java.util.*;

/**
 * @Author: cxw
 * @Date: 2023/3/17
 */
@Resolve({"string,string->string,string,string,string"})
public class decodeLogData_udtf_4 extends decodeLogData_udtf {
    @Override
    public void process(Object[] args) throws UDFException {
        super.process(args);
    }

    // for test
    public static void main(String[] args) {
        System.out.println(
            Arrays.toString((new decodeLogData_udtf_4()).getValues(
                "0x000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000d529ae9e86000000000000000000000000000000000000000000000005466d0aff4d18ef0081500000000000000000000000000000000000000000000000000000000000000000",
                "uint256 a, uint256 b, uint256 c, uint256 d"
            ))
        );
    }
}
