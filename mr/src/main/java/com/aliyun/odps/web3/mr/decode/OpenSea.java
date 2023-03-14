package com.aliyun.odps.web3.mr.decode;

import com.aliyun.odps.OdpsException;
import com.aliyun.odps.data.Record;
import com.aliyun.odps.data.TableInfo;
import com.aliyun.odps.mapred.JobClient;
import com.aliyun.odps.mapred.Mapper;
import com.aliyun.odps.mapred.MapperBase;
import com.aliyun.odps.mapred.conf.JobConf;
import com.aliyun.odps.mapred.utils.InputUtils;
import com.aliyun.odps.mapred.utils.OutputUtils;
import com.aliyun.odps.web3.common.ContractUtils;
import com.aliyun.odps.web3.common.TypeConvertUtils;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.datatypes.Type;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Author: smy
 * @Date: 2023/3/9 1:48 PM
 */
public class OpenSea {
    public static class openSeaMapper extends MapperBase {

        private String methodId;
        private String contractAddress;
        private String methodSignature;

        @Override
        public void setup(TaskContext context) throws IOException {
            methodSignature = context.getJobConf().get("methodSignature");
            methodId = EventEncoder.buildEventSignature(methodSignature);
            contractAddress = context.getJobConf().get("contractAddress").toLowerCase();
        }

        @Override
        public void map(long key, Record record, Mapper.TaskContext context) throws IOException {
            String topic1 = (String) record.get("topic_1");
            if (!methodId.equals(topic1) || !contractAddress.equals(record.get("contract_address"))) {
                return;
            }
            if (methodSignature.startsWith("OrdersMatched(")) {
                ordersMatched(record, context);
            }
        }

        private void ordersMatched(Record record, Mapper.TaskContext context) throws IOException {
            String maker = (String) record.get("topic_2");
            String taker = (String) record.get("topic_3");
            String metadata = (String) record.get("topic_4");

            Record result = context.createOutputRecord();
            result.set("contract_address", record.get("contract_address"));
            result.set("block_number", record.get("block_number"));
            result.set("block_timestamp", record.get("block_timestamp"));
            result.set("transaction_index", record.get("transaction_index"));
            result.set("transaction_hash", record.get("transaction_hash"));
            result.set("log_index", record.get("log_index"));
            result.set("maker", TypeConvertUtils.convert2String(ContractUtils.decodeIndexedValue(maker, "address")));
            result.set("taker", TypeConvertUtils.convert2String(ContractUtils.decodeIndexedValue(taker, "address")));

            List<String> list = TypeConvertUtils.convert2String(ContractUtils.decodeParameter((String) record.get("data"), "bytes32,bytes32,uint256"));
            result.set("metadata", metadata);
            result.set("buyHash", list.get(0));
            result.set("sellHash", list.get(1));
            result.set("price", list.get(2));
            context.write(result);
        }
    }


    /**
     * Wyven exchange v1 contract: 0x7be8076f4ea4a4ad08075c2508e481d6c946d12b
     * Wyven exchange v2 contract: 0x7f268357A8c2552623316e2562D90e642bB538E5
     * @param args
     * @throws OdpsException
     */
    public static void main(String[] args) throws OdpsException {
        if (args.length != 4) {
            System.err.println("Usage: OnlyMapper <contractAddress> <methodSignature> <out_table> <pt> ");
            System.exit(2);
        }

        JobConf job = new JobConf();
        job.set("contractAddress", args[0]);
        job.set("methodSignature", args[1]);

        // TODO: specify map output types
        //job.setMapOutputKeySchema(SchemaUtils.fromString( ?));
        //job.setMapOutputValueSchema(SchemaUtils.fromString( ?));

        // TODO: specify input and output tables
        LinkedHashMap<String, String> pt = new LinkedHashMap<String, String>();
        pt.put("pt", args[3]);
        InputUtils.addTable(TableInfo.builder().tableName("ethereum_logs_di").partSpec(pt).build(), job);
        OutputUtils.addTable(TableInfo.builder().tableName(args[2]).partSpec(pt).build(), job);
        //OutputUtils.addTable(TableInfo.builder().tableName(args[2]).partSpec(pt).build(), job);

        // TODO: specify a mapper
        job.setMapperClass(openSeaMapper.class);
        // TODO: specify a reducer
        job.setNumReduceTasks(0);
        //job.setReducerClass();

        JobClient.runJob(job);
        //RunningJob rj = JobClient.runJob(job);
        //rj.waitForCompletion();

    }
}
