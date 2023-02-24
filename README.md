# 数据说明

* 通过node、java项目实时解析geth节点的以太坊基础数据并同步到maxCompute
* 通过对已有的maxCompute表进行二次加工，产出有用分析中间表、结果表
        
# 数据建设
* 目前社区在不断的建设过程中，进度、质量都在不断向前，希望更多的社区成员参与进来
* 数据建设参与流程，可参考       
        

# 数据详情

## 原始数据

#### ethereum\_blocks

说明：块信息表，每个分区为从创世块到当日的全量块信息（优先使用最新分区）

maxCompute表：web3\_maxcompute.ethereum\_blocks

时效性：T+1

实时进度：起始~2022-09

|  **Column name**  |  **Data type**  |  **Description**  |
| --- | --- | --- |
|  block\_number  |  bigint  |  块高  |
|  block\_hash  |  string  |  当前块标  |
|  parent\_block\_hash  |  string  |  上一个块标  |
|  gas\_limit  |  bigint  |  gas上限  |
|  gas\_used  |  bigint  |  使用的gas  |
|  base\_fee\_per\_gas  |  bigint  |  块基础费用 (参考 [EIP1559](https://eips.ethereum.org/EIPS/eip-1559))  |
|  size  |  bigint  |  字节为单位的块大小 (被gas上限限制)  |
|  miner  |  string  |  矿工地址  |
|  nonce  |  bigint  |  挖坑难度证明  |
|  timestamp  |  datetime  |  块时间  |
|  transactions\_count  |  bigint  |  交易数  |
|  pt  |  string  |  日期分区（yyyymmdd）  |

#### ethereum\_transactions

说明：交易信息表，全量表

maxCompute表：web3\_maxcompute.ethereum\_transactions

时效性：T+1

实时进度：起始~2021-05

|  **Column name**  |  **Data type**  |  **Description**  |
| --- | --- | --- |
|  transaction\_hash  |  string  |  交易标识  |
|  transaction\_index  |  bigint  |  块内的交易序号  |
|  block\_number  |  bigint  |  块高  |
|  block\_hash  |  string  |  块标  |
|  block\_timestamp  |  datetime  |  块时间  |
|  from  |  string  |  发送地址  |
|  to  |  string  |  接受地址  |
|  value  |  decimal(38,18)  |  wei 单位的数量  |
|  input  |  string  |  附带信息  |
|  gas\_used  |  decimal(38,18)  |  使用的gas  |
|  gas\_price  |  decimal(38,18)  |  wei单位的gas价格  |
|  max\_fee\_per\_gas  |  decimal(38,18)  |  用户愿意支付的每个gas的最大费用  |
|  max\_priority\_fee\_per\_gas  |  decimal(38,18)  |  用户愿意支付给矿工的每个gas的最大费用  |
|  effective\_gas\_price  |  decimal(38,18)  |  实际gas价格  |
|  cumulative\_gas\_used  |  decimal(38,18)  |  块内到当前交易累计使用gas  |
|  success  |  bigint  |  是否成功  |
|  nonce  |  bigint  |  随机数  |
|  type  |  string  |  类型: Legacy, AccessList,  DynamicFee  |
|  access\_list  |  string  |  打算访问的地址列表（参考 [EIP2930](https://eips.ethereum.org/EIPS/eip-2930)）  |

#### ethereum\_logs

说明：合约事件日志表，全量表

maxCompute表：web3\_maxcompute.ethereum\_logs

时效性：T+1

实时进度：起始~2020-11

|  **Column name**  |  **Data type**  |  **Description**  |
| --- | --- | --- |
|  log\_id  |  bigint  |  主键  |
|  log\_index  |  bigint  |  块内日志序号  |
|  transaction\_hash  |  string  |  交易标识  |
|  transaction\_index  |  bigint  |  块内的交易序号  |
|  block\_number  |  bigint  |  块高  |
|  block\_hash  |  string  |  块标识  |
|  block\_timestamp  |  datetime  |  出块时间  |
|  contract\_address  |  string  |  合约地址  |
|  data  |  string  |  事件包含的非索引数据  |
|  topics\_count  |  bigint  |  索引数据数量  |
|  topic\_1  |  string  |  事件描述  |
|  topic\_2  |  string  |  第二个索引数据  |
|  topic\_3  |  string  |  第三个索引数据  |
|  topic\_4  |  string  |  第四个索引数据  |

#### ethereum\_traces

说明：合约内部交易表，全量表

maxCompute表：web3\_maxcompute.ethereum\_traces

时效性：T+1

实时进度：待更新

|  **Column name**  |  **Data type**  |  **Description**  |
| --- | --- | --- |
|  trace\_id  |  bigint  |  主键  |
|  trace\_address  |  string  |  树地址  |
|  trace\_children\_count  |  bigint  |  树子节点数量  |
|  trace\_success  |  bigint  |  是否成功  |
|  transaction\_hash  |  string  |  交易标识  |
|  transaction\_index  |  bigint  |  块内的交易序号  |
|  transaction\_success  |  bigint  |  交易是否成功  |
|  block\_number  |  bigint  |  块高  |
|  block\_hash  |  string  |  块标识  |
|  block\_timestamp  |  datetime  |  块时间  |
|  type  |  string  |  动作类型，reward, create, call or suicide  |
|  from  |  string  |  发送地址  |
|  to  |  string  |  接受地址  |
|  value  |  decimal(38,18)  |  wei 单位的数量  |
|  gas\_limit  |  decimal(38,18)  |  gas限制  |
|  gas\_used  |  decimal(38,18)  |  使用的gas  |
|  input  |  string  |  调用其他合约的输入值  |
|  output  |  string  |  调用其他合约的返回值  |
|  method\_id  |  string  |  调用其他合约方法id  |
|  error  |  string  |  报错信息  |

## 加工数据

#### token\_price

说明：token价格表（日维度）

maxCompute表：web3\_maxcompute.token\_price

时效性：T+1

polar-mysql表:blockchain.price\_1d

时效性：1h

实时进度：2020-04~至今

|  **Column name**  |  **Data type**  |  **Description**  |
| --- | --- | --- |
|  date  |  string  |  数据日期  |
|  source  |  string  |  数据来源  |
|  symbol  |  string  |  交易对  |
|  open\_time  |  datetime  |  k线开盘时间  |
|  close\_time  |  datetime  |  k线收盘时间  |
|  open  |  decimal(38,18)  |  开盘价  |
|  height  |  decimal(38,18)  |  最高价  |
|  low  |  decimal(38,18)  |  最低价  |
|  close  |  decimal(38,18)  |  收盘价(当前K线未结束的即为最新价)  |
|  volume  |  decimal(38,18)  |  成交量  |
|  turnover  |  decimal(38,18)  |  成交额  |
|  trading\_volume  |  decimal(38,18)  |  成交笔数  |
|  buying\_volume  |  decimal(38,18)  |  主动买入成交量  |
|  buying\_turnover  |  decimal(38,18)  |  主动买入成交额  |

#### ethereum\_balances

说明：有过交易记录的地址资产表（日维度）

maxCompute表：web3\_maxcompute.ethereum\_balances

时效性：T+1

实时进度：待更新

|  **Column name**  |  **Data type**  |  **Description**  |
| --- | --- | --- |
|  wallet\_address  |  string  |  钱包地址  |
|  amount  |  decimal(38,4)  |  eth 余额  |
|  amount\_raw  |  decimal(38,0)  |  wei 单位的余额  |
|  amount\_usd  |  decimal(38,6)  |  美元计价余额  |
|  pt  |  string  |  日期分区（yyyymmdd）  |

#### ethereum\_balance\_di

说明：当日有余额变动的地址资产表（日维度）

maxCompute表：web3\_maxcompute.ethereum\_balance\_di

时效性：T+1

实时进度：待更新

|  **Column name**  |  **Data type**  |  **Description**  |
| --- | --- | --- |
|  id  |  bigint  |  主键  |
|  address  |  string  |  地址  |
|  time  |  datetime  |  区块时间  |
|  amount  |  decimal(38,4)  |  余额  |
|  amount\_raw  |  decimal(38,0)  |  wei单位的余额  |
|  created  |  datetime  |  创建时间  |
|  pt  |  string  |  日期分区（yyyymmdd）  |

#### ethereum\_beacon\_data

说明：以太坊 POS 信标链指标表（日维度）

maxCompute表：web3\_maxcompute.ethereum\_beacon\_data

时效性：T+1（部分数据滞后一年）

实时进度：待更新

|  **Column name**  |  **Data type**  |  **Description**  |
| --- | --- | --- |
|  id             |  bigint  |  主键  |
|  timestamp  |  datetime  |  日期  |
|  active\_validators  |  int  |  活跃的验证者  |
|  deposits\_count  |  int  |  新的32ETH质押存款交易数量  |
|  avg\_effective\_balance  |  decimal  |  平均有效余额  |
|  effective\_balance\_sum  |  int  |  总有效余额  |
|  epoch\_height  |  int  |  纪元高度  |
|  est\_annual\_roi\_validator  |  decimal  |  验证者预估年度发行投资回报率  |
|  participation\_rate   |  decimal  |  参与率  |
|  total\_deposits\_count   |  int  |  ETH2存款合约的交易总数  |
|  total\_validators\_count  |  int  |  ETH2验证者总数  |
|  total\_volume\_sum   |  int  |  ETH2存款合约上的余额  |
|  new\_validators\_count  |  int  |  向ETH2存款合约存入32ETH的新地址数量  |
|  new\_volume\_sum   |  int  |  新存入ETH2存款合约的ETH数量  |
|  avg\_validator\_balance  |  decimal  |  验证者的平均总质押余额  |
|  voluntary\_exit\_count  |  int  |  自愿退出验证者池的验证者总数  |

### ERC20

#### ethereum\_erc20

说明：erc20的基本信息

maxCompute表：web3\_maxcompute.ethereum\_erc20

时效性：T+1

polar-mysql表:blockchain.ethereum\_erc20

时效性：1h

实时进度：实时

|  **Column name**  |  **Data type**  |  **Description**  |
| --- | --- | --- |
|  contract\_address  |  string  |  合约地址  |
|  name  |  string  |  名称  |
|  symbol  |  string  |  标识  |
|  decimals  |  bigint  |  精度  |
|  is\_stable  |  bigint  |  是否稳定币  |
|  deployer  |  string  |  部署地址  |
|  deploy\_time  |  datetime  |  部署时间  |
|  creation\_transaction\_hash  |  string  |  创建的交易哈希  |
|  description  |  string  |  描述  |
|  total\_supply  |  decimal(38,0)  |  总供给  |
|  circulating\_supply  |  decimal(38,0)  |  流通供给  |
|  market\_cap\_usd\_latest  |  decimal(30,8)  |  美元市值  |
|  volume\_usd\_24h  |  decimal(30,8)  |  近24小时流通量  |
|  last\_updated  |  datetime  |  最新更新时间  |

#### ethereum\_erc20\_balance

说明：erc20资产表（日维度）

maxCompute表：web3\_maxcompute.ethereum\_erc20\_balance

时效性：T+1

实时进度：起始  ~ 2020-11

|  **Column name**  |  **Data type**  |  **Description**  |
| --- | --- | --- |
|  contract\_address  |  string  |  合约地址  |
|  owner  |  string  |  账户地址  |
|  amount\_raw  |  decimal(38,0)  |  余额  |
|  amount\_usd  |  decimal(30,8)  |  美元单位余额  |

#### ethereum\_erc20\_event\_transfer

说明：erc20转交事件（日维度）

maxCompute表：web3\_maxcompute.ethereum\_erc20\_event\_transfer

时效性：T+1

实时进度：起始  ~ 2020-11

|  **Column name**  |  **Data type**  |  **Description**  |
| --- | --- | --- |
|  id  |  bigint  |  主键  |
|  contract\_address  |  string  |  合约地址  |
|  from  |  string  |  发送地址  |
|  to  |  string  |  接受地址  |
|  value  |  decimal(38,18)  |  wei 单位的数量  |
|  block\_number  |  bigint  |  块高  |
|  block\_timestamp  |  datetime  |  出块时间  |
|  transaction\_index  |  bigint  |  块内的交易序号  |
|  transaction\_hash  |  string  |  交易标识  |
|  log\_index  |  bigint  |  块内日志序号  |

#### ethereum\_erc20\_event\_approval

说明：erc20许可事件（日维度）

maxCompute表：web3\_maxcompute.ethereum\_erc20\_event\_approval

时效性：T+1

实时进度：起始  ~ 2020-11

|  **Column name**  |  **Data type**  |  **Description**  |
| --- | --- | --- |
|  id  |  bigint  |  主键  |
|  contract\_address  |  string  |  合约地址  |
|  owner  |  string  |  授权地址  |
|  spender  |  string  |  被授权地址  |
|  value  |  decimal(38,18)  |  wei 单位的数量  |
|  block\_number  |  bigint  |  块高  |
|  block\_timestamp  |  datetime  |  出块时间  |
|  transaction\_index  |  bigint  |  块内的交易序号  |
|  transaction\_hash  |  string  |  交易标识  |
|  log\_index  |  bigint  |  块内日志序号  |

## 平台数据

### TVL

说明：协议内的锁仓量

maxCompute表: 待更新

### Tags

## Q&A
