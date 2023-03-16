package com.aliyun.odps.web3.udf;

import com.aliyun.odps.udf.UDFException;
import com.aliyun.odps.udf.UDTF;

import java.util.*;

/**
 * @Author: cxw
 * @Date: 2023/3/17
 */
public abstract class decodeLogData_udtf extends UDTF {
  @Override
  public void process(Object[] args) throws UDFException {
    forward((Object) getValues((String) args[0], (String) args[1]));
  }

  public String[] getValues(String data, String signature) {
    Collection<String> values = (new decodeLogData()).getMap(data, signature).values();
    return values.toArray(new String[0]);
  }
}
