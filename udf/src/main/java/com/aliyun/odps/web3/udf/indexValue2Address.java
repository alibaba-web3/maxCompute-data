package com.aliyun.odps.web3.udf;

import com.aliyun.odps.udf.UDF;
import org.web3j.utils.Strings;

public class indexValue2Address extends UDF {
    public String evaluate(String s) {
        if (Strings.isEmpty(s) || s.length() <= 40) {
            return s;
        }
        return "0x" + s.substring(s.length() - 40, s.length());
    }
}
