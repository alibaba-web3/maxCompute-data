package com.aliyun.odps.web3.mr.uniswap.v2;

import com.aliyun.odps.OdpsException;
import com.aliyun.odps.data.Record;
import com.aliyun.odps.data.TableInfo;
import com.aliyun.odps.mapred.JobClient;
import com.aliyun.odps.mapred.Mapper;
import com.aliyun.odps.mapred.MapperBase;
import com.aliyun.odps.mapred.conf.JobConf;
import com.aliyun.odps.mapred.utils.InputUtils;
import com.aliyun.odps.mapred.utils.OutputUtils;
import org.web3j.abi.EventEncoder;

import java.io.IOException;
import java.util.LinkedHashMap;

/**
 * @Author: cxw
 * @Date: 2023/3/13
 */
public class pairs {
    public static class UniSwapV2PairsMapper extends MapperBase {
        private String factoryAddress = "0x5c69bee701ef814a2b6a3edd4b1652cb9cc5aa6f";
        private String eventSignature = EventEncoder.buildEventSignature("PairCreated(address,address,address,uint256)");

        @Override
        public void map(long key, Record record, Mapper.TaskContext ctx) throws IOException {
            String contractAddress = (String) record.get("contract_address");
            String topic1 = (String) record.get("topic_1");
            String topic2 = (String) record.get("topic_2");
            String topic3 = (String) record.get("topic_3");
            String data = (String) record.get("data");
            if (contractAddress.equals(factoryAddress) && topic1.equals(eventSignature)) {
                Record result = ctx.createOutputRecord();
                result.set("pair_index", Integer.parseInt(data.substring(data.length() - 64), 16));
                result.set("pair_address", "0x" + data.substring(26,66));
                result.set("token0_address", "0x" + topic2.substring(topic2.length() - 40));
                result.set("token1_address", "0x" + topic3.substring(topic3.length() - 40));
                result.set("created_block_number", record.get("block_number"));
                ctx.write(result);
            }
        }
    }

    public static void main(String[] args) throws OdpsException {
        if (args.length != 1) {
            System.err.println("Program arguments: <pt>");
            System.exit(2);
        }

        LinkedHashMap<String, String> pt = new LinkedHashMap();
        pt.put("pt", args[0]);

        JobConf job = new JobConf();
        InputUtils.addTable(TableInfo.builder().tableName("ethereum_logs_di").partSpec(pt).build(), job);
        OutputUtils.addTable(TableInfo.builder().tableName("ethereum_uniswap_v2_pairs_di").partSpec(pt).build(), job);
        job.setMapperClass(UniSwapV2PairsMapper.class);
        job.setNumReduceTasks(0);
        JobClient.runJob(job);
    }
}
