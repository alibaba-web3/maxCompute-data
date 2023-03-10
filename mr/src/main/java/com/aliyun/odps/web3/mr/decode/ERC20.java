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
import org.web3j.abi.EventEncoder;
import org.web3j.utils.Numeric;
import org.web3j.utils.Strings;

import java.io.IOException;
import java.util.LinkedHashMap;

/**
 * @Author: smy
 * @Date: 2023/3/9 1:48 PM
 */
public class ERC20 {
    public static class erc20Mapper extends MapperBase {

        private String transferSign;

        @Override
        public void setup(TaskContext context) {
            String methodSignature = context.getJobConf().get("methodSignature");
            transferSign = EventEncoder.buildEventSignature(methodSignature);
        }

        @Override
        public void map(long key, Record record, Mapper.TaskContext context) throws IOException {
            String topic1 = (String) record.get("topic_1");
            String data = (String) record.get("data");
            if (!transferSign.equals(topic1) || data.length() <=2) {
                return;
            }
            String topic2 = (String) record.get("topic_2");
            String topic3 = (String) record.get("topic_3");

            Record result = context.createOutputRecord();
            result.set("contract_address", record.get("contract_address"));
            result.set("from", indexValue2Address(topic2));
            result.set("to", indexValue2Address(topic3));
            result.set("value", Numeric.toBigInt(data).toString());
            result.set("block_number", record.get("block_number"));
            result.set("block_timestamp", record.get("block_timestamp"));
            result.set("transaction_index", record.get("transaction_index"));
            result.set("transaction_hash", record.get("transaction_hash"));
            result.set("log_index", record.get("log_index"));
            context.write(result);
        }

        public String indexValue2Address(String s) {
            if (Strings.isEmpty(s) || s.length() <= 40) {
                return s;
            }
            return "0x" + s.substring(s.length() - 40, s.length());
        }
    }



    public static void main(String[] args) throws OdpsException {
        if (args.length != 3) {
            System.err.println("Usage: OnlyMapper <methodSignature> <out_table> <pt> ");
            System.exit(2);
        }

        JobConf job = new JobConf();
        job.set("methodSignature", args[0]);

        // TODO: specify map output types
        //job.setMapOutputKeySchema(SchemaUtils.fromString( ?));
        //job.setMapOutputValueSchema(SchemaUtils.fromString( ?));

        // TODO: specify input and output tables
        LinkedHashMap<String, String> pt = new LinkedHashMap<String, String>();
        pt.put("pt", args[2]);
        InputUtils.addTable(TableInfo.builder().tableName("ethereum_logs_di").partSpec(pt).build(), job);
        OutputUtils.addTable(TableInfo.builder().tableName(args[1]).partSpec(pt).build(), job);

        // TODO: specify a mapper
        job.setMapperClass(erc20Mapper.class);
        // TODO: specify a reducer
        job.setNumReduceTasks(0);
        //job.setReducerClass();

        JobClient.runJob(job);
        //RunningJob rj = JobClient.runJob(job);
        //rj.waitForCompletion();

    }
}
