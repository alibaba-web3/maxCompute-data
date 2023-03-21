package com.aliyun.odps.web3.mr.decode.opensea;

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
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


/**
 * @Author: smy
 * @Date: 2023/3/15 9:37 AM
 */
public class Trade {

    public static class openSeaMapper extends MapperBase {

        private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

        private List<String> exchangeContractAddresses = Arrays.asList("0x7be8076f4ea4a4ad08075c2508e481d6c946d12b","0x7f268357a8c2552623316e2562d90e642bb538e5");

        private String wyvernAtomicizerContractAddress = "0xC99f70bFD82fb7c8f8191fdfbFB735606b15e5c5";
        private String merkleValidatorContractAddress = "0xBAf2127B49fC93CbcA6269FAdE0F7F31dF4c88a7";
        @Override
        public void setup(TaskContext context) throws IOException {
        }

        @Override
        public void map(long key, Record record, Mapper.TaskContext context) throws IOException {
            if (!exchangeContractAddresses.contains(record.get("to"))) {
                return;
            }
            String success = (String) record.get("success");
            if ("0".equals(success.trim())) {
                return;
            }
            try {
                atomicMatch(record, context);
            } catch (Exception ignore) {
                // ignore not standard contract
                System.out.println(String.format("fail to decode txn:{%s}, errors:{%s}", record.get("transaction_hash"), ignore.getMessage()));
            }
        }

        private void atomicMatch(Record record, Mapper.TaskContext context) throws IOException {
            /*
             *  address[14] addrs,
             *  uint[18] uints,
             *  uint8[8] feeMethodsSidesKindsHowToCalls,
             *  bytes calldataBuy,
             *  bytes calldataSell,
             *  bytes replacementPatternBuy,
             *  bytes replacementPatternSell,
             *  bytes staticExtradataBuy,
             *  bytes staticExtradataSell,
             *  uint8[2] vs,
             *  bytes32[5] rssMetadata
             */
            List<Type> parameters;
            String input = ((String) record.get("input")).toLowerCase();
            //EventEncoder.buildEventSignature("atomicMatch_(address[14],uint256[18],uint8[8],bytes,bytes,bytes,bytes,bytes,bytes,uint8[2],bytes32[5])").substring(0, 10)
            if (!input.startsWith("0xab834bab")) {
                return;
            }
            parameters = ContractUtils.decodeInputData(input, "atomicMatch_(address[14],uint256[18],uint8[8],bytes,bytes,bytes,bytes,bytes,bytes,uint8[2],bytes32[5])");
            List<String> addrs = TypeConvertUtils.convert2StringList(parameters.get(0));
            List<String> uints = TypeConvertUtils.convert2StringList(parameters.get(1));
            List<String> feeMethodsSidesKindsHowToCalls = TypeConvertUtils.convert2StringList(parameters.get(2));
            Order buyOrder = getBuyOder(addrs, uints, feeMethodsSidesKindsHowToCalls);
            Order sellOrder = getSellOder(addrs, uints, feeMethodsSidesKindsHowToCalls);
            Long now = ((Date)record.get("block_timestamp")).getTime();
            //sellOrder.feeRecipient != "0x"
            boolean useSellPrice = addrs.get(10).equalsIgnoreCase(Address.DEFAULT.toString());
            BigInteger totalPrice = calculateFinalPrice(buyOrder, sellOrder, useSellPrice, now);

            List<NftTransfer> nftTransfers = new ArrayList<>();
            //decode proxy.proxy(sell.target, sell.howToCall, sell.calldata)
            decodeTransCall(addrs.get(11), TypeConvertUtils.convert2String(parameters.get(4)), nftTransfers);
            // for batch
            BigInteger price = nftTransfers.size() != 0 ? totalPrice.divide(BigInteger.valueOf(nftTransfers.size())):BigInteger.ZERO;

            //String label = sdf.format((Date)record.get("block_timestamp"));
            for(NftTransfer nftTransfer: nftTransfers) {
                Record result = context.createOutputRecord(context.getInputTableInfo().getLabel());
                result.set("block_number", record.get("block_number"));
                result.set("block_timestamp", record.get("block_timestamp"));
                result.set("transaction_index", record.get("transaction_index"));
                result.set("transaction_hash", record.get("transaction_hash"));
                result.set("market_contract_address", record.get("to"));
                result.set("tx_from", record.get("from"));
                result.set("tx_to", record.get("to"));
                result.set("trade_category", buyOrder.maker.equals(record.get("from")) ? "Buy":"Sell");
                result.set("buyer", buyOrder.maker);
                result.set("nft_contract_address", nftTransfer.nftContractAddress);
                result.set("token_id", nftTransfer.tokenId);
                result.set("number_of_items", nftTransfer.amount);
                // paymentToken,address(0) for ETH
                result.set("currency_contract", addrs.get(6));
                result.set("amount_raw", price.toString());
                result.set("seller", sellOrder.maker);
                context.write(result, context.getInputTableInfo().getLabel());
            }
        }

        /**
         * Side:
         *      0:Buy
         *      1:Sell
         *
         * SaleKind
         *      0:FixedPrice
         *      1:DutchAuction
         *
         * @param buyOrder
         * @param sellOrder
         * @param useSellPrice  sellOrder.feeRecipient != "0x"
         * @return
         */
        private BigInteger calculateFinalPrice(Order buyOrder, Order sellOrder, boolean useSellPrice, Long now) {
            BigInteger sellPrice = calculateFinalPrice(sellOrder, now);
            BigInteger buyPrice = calculateFinalPrice(buyOrder, now);
            if (buyPrice.compareTo(sellPrice) < 0) {
                throw new RuntimeException(String.format("buyPrice:{} is less than sellPrice:{}.", buyPrice, sellPrice));
            }
            return useSellPrice ? sellPrice:buyPrice;
        }

        private BigInteger calculateFinalPrice(Order order, Long now) {
            // FixedPrice
            if ("0".equals(order.saleKind) ) {
                return new BigInteger(order.basePrice);
            }
            // DutchAuction
            else if ("1".equals(order.saleKind)) {
                //uint diff = SafeMath.div(SafeMath.mul(extra, SafeMath.sub(now, listingTime)), SafeMath.sub(expirationTime, listingTime));
                BigInteger diff = new BigInteger(order.extra).multiply(BigInteger.valueOf(now - Long.parseLong(order.listingTime))).divide(BigInteger.valueOf(Long.parseLong(order.expirationTime) - Long.parseLong(order.listingTime)));
                // sell
                if ("1".equals(order.side)) {
                    return new BigInteger(order.basePrice).subtract(diff);
                }
                // buy
                else {
                    return new BigInteger(order.basePrice).add(diff);
                }
            }
            throw new RuntimeException(String.format("order saleKind error: {%s}", order.side));
        }


        private Order getBuyOder(List<String> addrs, List<String> uints, List<String> feeMethodsSidesKindsHowToCalls) {
            Order order = new Order();
            order.setExchange(uints.get(0));
            order.setMaker(addrs.get(1));
            order.setTaker(addrs.get(2));
            order.setSide(feeMethodsSidesKindsHowToCalls.get(1));
            order.setSaleKind(feeMethodsSidesKindsHowToCalls.get(2));
            order.setBasePrice(uints.get(4));
            order.setExtra(uints.get(5));
            order.setListingTime(uints.get(6));
            order.setExpirationTime(uints.get(7));
            return order;
        }


        private Order getSellOder(List<String> addrs, List<String> uints, List<String> feeMethodsSidesKindsHowToCalls) {
            Order order = new Order();
            order.setExchange(uints.get(7));
            order.setMaker(addrs.get(8));
            order.setTaker(addrs.get(9));
            order.setSide(feeMethodsSidesKindsHowToCalls.get(5));
            order.setSaleKind(feeMethodsSidesKindsHowToCalls.get(6));
            order.setBasePrice(uints.get(13));
            order.setExtra(uints.get(14));
            order.setListingTime(uints.get(15));
            order.setExpirationTime(uints.get(16));
            return order;
        }

        /**
         * proxy.proxy(sell.target, sell.howToCall, sell.calldata)
         * single ERC721: transferFrom(address,address,uint256)
         * batch contract WyvernAtomicizer: 0xC99f70bFD82fb7c8f8191fdfbFB735606b15e5c5
         */
        private void decodeTransCall(String target, String callDatas, List<NftTransfer> nftTransfers) {
            if (wyvernAtomicizerContractAddress.equalsIgnoreCase(target)) {
                List<Type> parameters = ContractUtils.decodeInputData(callDatas,"atomicize(address[],uint256[],uint256[],bytes)");
                List<String> contractList = TypeConvertUtils.convert2StringList(parameters.get(0));
                List<String> callDataLengths = TypeConvertUtils.convert2StringList(parameters.get(2));
                for (int i = 0, current = 0; i < contractList.size(); i++) {
                    int callDataLength = Integer.parseInt(callDataLengths.get(i));
                    byte[] callData = new byte[callDataLength];
                    System.arraycopy(parameters.get(3).getValue(), current, callData, 0, callDataLength);
                    String inputData = Numeric.toHexString(callData);
                    decodeTransCall(contractList.get(i), inputData, nftTransfers);
                    current += callDataLength;
                }
            } else if (merkleValidatorContractAddress.equalsIgnoreCase(target)) {
                //EventEncoder.buildEventSignature("matchERC1155UsingCriteria(address,address,address,uint256,uint256,bytes32,bytes32[])").substring(0, 10)
                if (callDatas.startsWith("0x96809f90")) {
                    List<String> parameters = TypeConvertUtils.convert2String(ContractUtils.decodeInputData(callDatas, "matchERC1155UsingCriteria(address,address,address,uint256,uint256,bytes32,bytes32[])"));
                    NftTransfer nftTransfer = new NftTransfer(parameters.get(2), parameters.get(0), parameters.get(1), parameters.get(3), parameters.get(4));
                    nftTransfers.add(nftTransfer);
                }
                //EventEncoder.buildEventSignature("matchERC721UsingCriteria(address,address,address,uint256,bytes32,bytes32[])").substring(0, 10)
                else if (callDatas.startsWith("0xfb16a595")) {
                    List<String> parameters = TypeConvertUtils.convert2String(ContractUtils.decodeInputData(callDatas, "matchERC721UsingCriteria(address,address,address,uint256,bytes32,bytes32[])"));
                    NftTransfer nftTransfer = new NftTransfer(parameters.get(2), parameters.get(0), parameters.get(1), parameters.get(3));
                    nftTransfers.add(nftTransfer);
                }
                //EventEncoder.buildEventSignature("matchERC721WithSafeTransferUsingCriteria(address,address,address,uint256,bytes32,bytes32[])").substring(0, 10)
                else if (callDatas.startsWith("0xc5a0236e")) {
                    List<String> parameters = TypeConvertUtils.convert2String(ContractUtils.decodeInputData(callDatas, "matchERC721WithSafeTransferUsingCriteria(address,address,address,uint256,bytes32,bytes32[])"));
                    NftTransfer nftTransfer = new NftTransfer(parameters.get(2), parameters.get(0), parameters.get(1), parameters.get(3));
                    nftTransfers.add(nftTransfer);
                }

            }
            //EventEncoder.buildEventSignature("transferFrom(address,address,uint256)").substring(0, 10)
            else if (callDatas.startsWith("0x23b872dd")){
                List<String> parameters = TypeConvertUtils.convert2String(ContractUtils.decodeInputData(callDatas, "transferFrom(address,address,uint256)"));
                NftTransfer nftTransfer = new NftTransfer(target, parameters.get(0), parameters.get(1), parameters.get(2));
                nftTransfers.add(nftTransfer);
            }
            //safeTransferFrom(address,address,uint256,uint256,bytes)
            else if (callDatas.startsWith("0xf242432a")) {
                List<String> parameters = TypeConvertUtils.convert2String(ContractUtils.decodeInputData(callDatas, "safeTransferFrom(address,address,uint256,uint256,bytes)"));
                NftTransfer nftTransfer = new NftTransfer(target, parameters.get(0), parameters.get(1), parameters.get(2), parameters.get(3));
                nftTransfers.add(nftTransfer);
            } else {
                System.out.println(callDatas.substring(0, 10));
                throw new RuntimeException(String.format("unsupported proxy call, methodId:{%s}.", callDatas.substring(0, 10)));
            }

        }

        class Order {
            private String exchange;
            private String maker;
            private String taker;
            private String side;
            private String saleKind;
            private String basePrice;
            private String extra;
            private String listingTime;
            private String expirationTime;

            public String getExchange() {
                return exchange;
            }

            public void setExchange(String exchange) {
                this.exchange = exchange;
            }

            public String getMaker() {
                return maker;
            }

            public void setMaker(String maker) {
                this.maker = maker;
            }

            public String getTaker() {
                return taker;
            }

            public void setTaker(String taker) {
                this.taker = taker;
            }

            public String getSide() {
                return side;
            }

            public void setSide(String side) {
                this.side = side;
            }

            public String getSaleKind() {
                return saleKind;
            }

            public void setSaleKind(String saleKind) {
                this.saleKind = saleKind;
            }

            public String getBasePrice() {
                return basePrice;
            }

            public void setBasePrice(String basePrice) {
                this.basePrice = basePrice;
            }

            public String getExtra() {
                return extra;
            }

            public void setExtra(String extra) {
                this.extra = extra;
            }

            public String getListingTime() {
                return listingTime;
            }

            public void setListingTime(String listingTime) {
                this.listingTime = listingTime;
            }

            public String getExpirationTime() {
                return expirationTime;
            }

            public void setExpirationTime(String expirationTime) {
                this.expirationTime = expirationTime;
            }
        }

        class NftTransfer {
            private String nftContractAddress;
            private String from;
            private String to;
            private String tokenId;

            private String amount;

            public NftTransfer(String nftContractAddress, String from, String to, String tokenId) {
                this.nftContractAddress = nftContractAddress;
                this.from = from;
                this.to = to;
                this.tokenId = tokenId;
                this.amount = "1";
            }

            public NftTransfer(String nftContractAddress, String from, String to, String tokenId, String amount) {
                this.nftContractAddress = nftContractAddress;
                this.from = from;
                this.to = to;
                this.tokenId = tokenId;
                this.amount = amount;
            }
        }

    }

    public static List<String> getPtList(String startStr, String endStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        List<String> result = new ArrayList<>();
        LocalDate start = LocalDate.parse(startStr, formatter);
        LocalDate end = LocalDate.parse(endStr, formatter);
        while (!start.isAfter(end)) {
            result.add(start.format(formatter));
            start = start.plusDays(1);
        }
        return result;
    }


    /**
     * Wyven exchange v1 contract: 0x7be8076f4ea4a4ad08075c2508e481d6c946d12b
     * Wyven exchange v2 contract: 0x7f268357A8c2552623316e2562D90e642bB538E5
     * @param args
     * @throws OdpsException
     */
    public static void main(String[] args) throws OdpsException {
        if (args.length != 1 && args.length != 2  ) {
            System.err.println("Usage: OnlyMapper <pt_start> <pt_end> ");
            System.exit(2);
        }

        JobConf job = new JobConf();
        List<String> dateList = getPtList(args[0], args.length == 2 ? args[1]:args[0]);
        String[] columns = {"block_number","block_timestamp","transaction_index","transaction_hash","from","to","input","success"};
        dateList.forEach(date -> {
            LinkedHashMap<String, String> pt = new LinkedHashMap<>();
            pt.put("pt", date);
            InputUtils.addTable(TableInfo.builder().projectName("web3_maxCompute").tableName("tmp_ethereum_transactions_di")
                    .partSpec(pt).label(date).cols(columns).build(), job);
            OutputUtils.addTable(TableInfo.builder().tableName("opensea_trades_di").partSpec(pt).label(date).build(), job);
        });

        job.setMapperClass(openSeaMapper.class);
        job.setNumReduceTasks(0);
        JobClient.runJob(job);
    }
}

