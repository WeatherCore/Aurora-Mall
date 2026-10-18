# 📖 Aurora-Mall · 极光商城 项目导读指南

> 本文件是 `Aurora-Mall`（极光商城）全栈项目的中文导读，帮助你从零开始理解这个 **Spring Cloud Alibaba 微服务商城后端 + Vue 3 前端 + Spring AI 智能客服** 全栈系统的架构、代码和运行方式。
> 注释范围说明：代码逐行注释仅对后端 `Simlect-backend/`（654 个 Java 文件）添加，前端 `Simlect-front/` 不加注释；本导读文档覆盖整个项目。

---

## 目录

1. [这个项目是干什么的？](#1-这个项目是干什么的)
2. [核心概念速览](#2-核心概念速览)
3. [项目目录结构详解](#3-项目目录结构详解)
4. [运行流程全景图](#4-运行流程全景图)
5. [逐文件代码导读](#5-逐文件代码导读)
6. [关键设计模式解析](#6-关键设计模式解析)
7. [配置系统详解](#7-配置系统详解)
8. [如何运行和测试](#8-如何运行和测试)
9. [复刻建议与学习路线](#9-复刻建议与学习路线)
10. [常见问题](#10-常见问题)
- [附录：关键术语对照表](#附录关键术语对照表)

---

## 1. 这个项目是干什么的？

**一句话**：Aurora-Mall 是一个全栈电商系统，后端用 Spring Cloud Alibaba 构建 13 个微服务，前端 Vue 3 + PWA，配套 Spring AI 智能客服，覆盖"浏览→下单→支付→发货→退款→客服"全交易闭环。

**它不是**一个单体 CRUD demo，**而是**一个后端一域一库、跨服务强一致用 Seata、最终一致用 Outbox+MQ、支付回调用状态机+分布式锁防并发的工程级系统。简历意义上的"能打"就在这里——每个高并发难题都有对应的、源码可追溯的解法。

```
                ┌──────────────  用户 / 浏览器  ──────────────┐
                │     │                         │                 │
                ▼     ▼                         ▼                 ▼
          ┌──────────┐                ┌────────────┐        ┌──────────┐
          │ 前端 Vue │                │ AI 客服对话  │        │ 管理后台  │
          └────┬─────┘                └──────┬─────┘        └────┬─────┘
               │                             │                    │
               ▼                             ▼                    ▼
        ┌────────────────────────────────────────────────────────────┐
        │            Simlect-gateway（网关 / 鉴权 / 路由）              │
        └────────────────────────────┬───────────────────────────────┘
                                     │
   ┌────────┬────────┬────────┬──────┴──────┬────────┬────────┬────────┬────────┐
   ▼        ▼        ▼        ▼             ▼        ▼        ▼        ▼        ▼
 user    product   stock    cart         order      pay    coupon   search   admin   agent
 (鉴权)  (商品)   (库存)   (购物车)     (订单/编    (支付/  (优惠券)  (ES搜索) (后台)  (AI客服
                                                   排中心)     回调)                     RAG/MCP)
```

**类比**：网关 = 大厦前台总机；每个微服务 = 独立档口（一域一库，不串对方仓库）；Seata = 总账房，一笔跨档口交易要么全记账要么全退；RabbitMQ = 定时闹钟+回执箱，超时未付自动关单退库存退券。

---

## 2. 根心概念速览

读后端代码前先懂这 8 个概念，否则每个文件都会卡住：

| 概念 | 是什么 | 把它理解为… | 伪代码 |
|---|---|---|---|
| **一域一库** | 每个微服务独占一个 MySQL 库（simlect_order / simlect_pay…），互不直连对方表 | 各档口有自己的仓库，不串门 | `order` 服务只写 `simlect_order.*` |
| **Seata AT 模式** | 跨服务的强一致事务，`@GlobalTransactional` 开启全局事务，TC 协调各分支回滚/提交 | 总账房盯着所有档口，一人出错全部退货 | `@GlobalTransactional void postOrder(){...}` |
| **Outbox + MQ** | 业务表与消息表同库同事务写入，后台任务异步投递 MQ，解决"DB 提交了 MQ 没发"的不可靠 | 货物出库单和发货单钉在一起，专人取单发货 | `insert order + insert outbox` 原子 |
| **订单状态机** | 用枚举+条件更新约束订单状态流转，非法转换直接拒绝 | 订单的"工序卡"，不能从已发货跳回待付款 | `WAIT_PAYMENT→PAID→SHIPPED` |
| **生命周期锁** | 支付成功与超时关单并发时，用 Redis 锁保证只成功一次 | 两人同时抢关同一单，门锁决定谁进 | `tryLock(orderId)` 抢到才处理 |
| **消费幂等表** | MQ 消费前先查 `mq_idempotency_keys` 表，已消费则跳过 | 取货码核销簿，验过的不重复发 | `if(exist key) return;` |
| **Sentinel** | 阿里流控熔断组件，给 Feign 调用加限流和降级 | 档口门前保安，人多限流 | `feign.sentinel.enabled=true` |
| **Spring AI / MCP** | Spring 官方 AI 编排框架 + Model Context Protocol 工具协议 | 给 AI 客服装上"手"去调真实接口 | `ChatClient + ToolCallback` |

**要点**：前 6 个是交易主链路命根子；后 2 个是 AI 客服底座。面试硬核全在这 8 概念里。

---

## 3. 项目目录结构详解

```
Aurora-Mall/                          # 全栈项目根
├── README.md                     # 📃 项目门面（本仓库首页）
├── Description.md                # 📋 项目名片（GitHub About 用）
├── ZHIDAO.md                     # 📖 本导读文件
├── LICENSE                       # Apache 协议
├── sql/                          # 🗄️ 各服务建库脚本（一域一库）
├── deploy/                       # 🚀 部署：docker-compose 中间件 + Nginx + 环境变量示例
│   ├── docker-compose.middleware.yml   # 一键起 MySQL/Redis/RabbitMQ/Nacos/Seata/ES
│   ├── start-middleware.ps1            # Windows 启动脚本
│   └── nginx.simlect.conf.example       # Nginx 反代配置
├── docs/                         # 📚 文档与截图
├── Simlect-front/                # 🖥️ 前端（不加注释）
│   ├── Simlect-web/              #   用户端 Vue3 + Vite + Pinia + Element Plus + PWA
│   └── Simlect-admin/            #   管理后台
└── Simlect-backend/  ⭐ 加注释   # 🛒 后端（654 Java 文件）
    ├── pom.xml                   #   父 POM：Spring Boot 3.5.4 / Cloud 2025 / AI 1.1.2 / Java17
    ├── Simlect-common/  📦 公共底座  # ResponseVO/异常/工具/Redis组件/Feign基类
    ├── Simlect-gateway/  🚪 网关     # 路由/鉴权/限流入口
    ├── Simlect-user/     👤 用户    # 注册/登录/Token/地址
    ├── Simlect-product/  🛒 商品    # SPU/SKU/规格
    ├── Simlect-stock/    📦 库存    # 扣减/回补
    ├── Simlect-cart/     🛒 购物车
    ├── Simlect-order/    ⭐ 订单编排  # 交易主链路核心（出度69）
    ├── Simlect-pay/      💰 支付    # 支付宝/回调验签/退款幂等
    ├── Simlect-coupon/  🎟️ 优惠券   # 秒杀券 Redis Lua 预占
    ├── Simlect-search/   🔍 搜索    # ES 检索
    ├── Simlect-admin/    🛠️ 后台
    └── Simlect-agent/    🤖 AI客服   # Python(LangChain)+Java(Spring AI) 双实现
```

**模块依赖方向**：

```
gateway ──► 所有业务服务（路由转发）
order ──► stock / product / user / coupon / cart / pay（Feign 下单编排）
pay ──► order（回调入账 Feign）
agent ──► product / order / search（MCP 工具调用）
所有服务 ──► common（公共类）
所有服务 ──► Nacos / MySQL / Redis / RabbitMQ / Seata
```

**要点**：`order` 出度最高是编排中心，`common` 入度最高是被依赖底座；链路追踪从 `order` 起步最合理。

<details>
<summary><b>📁 各服务二级结构（api + app 双模块，点击展开）</b></summary>

每个业务服务采用 `api` + `app` 双模块结构：
- **api 模块**：对外契约——FeignClient 接口 / DTO / VO / 枚举 / Fallback 降级工厂，被其他服务依赖
- **app 模块**：业务实现——Controller / Service / Mapper / state / task / component / config

```
Simlect-{service}/
├── api/   ← 契约：@FeignClient + DTO/VO/枚举 + FallbackFactory
└── app/   ← 实现：cloud/XxxApplication + controller + biz/impl + state + task + component
```

</details>

---

## 4. 运行流程全景图

### 4.1 普通下单链路（核心交易主流程）

```
┌──────────┐  ①POST /order     ┌──────────┐
│ 前端 Vue  │ ─────────────────▶│ gateway  │  鉴权→改写→转发
└──────────┘                    └────┬─────┘
                                     │ ②Feign
                                     ▼
                              ┌────────────┐
                              │ OrderCtrl  │  前台总服务台：收单转交
                              └────┬───────┘
                                   │ ③
                                   ▼
                     ┌──────────────────────────────┐
                     │ OrderInfoServiceImpl         │  📋 编排中心
                     │ @GlobalTransactional          │  Seata 全局事务开启
                     │ ┌──────────────────────────┐ │
                     │ │ ④ Feign: user 校验地址    │ │  任一失败→全局回滚
                     │ │ ⑤ Feign: product 快照SKU │ │  （库存/券/地址都退）
                     │ │ ⑥ Feign: stock 锁库存     │ │
                     │ │ ⑦ Feign: coupon 锁券      │ │
                     │ │ ⑧ 写 order/order_item    │ │  本服务落库
                     │ │ ⑨ Feign: pay 拉支付单   │ │
                     │ │ ⑩ Outbox 发 MQ：15min超时  │ │  同库同事务写消息表
                     │ └──────────────────────────┘ │
                     └──────────────────────────────┘
                                   │ ⑪ 返回支付参数
                                   ▼
                  ┌────────────────┴─────────────────┐
                  ▼                                   ▼
        ⑫ 支付宝异步回调                      ⑬ 15分钟未付 → 死信队列
        ┌─────────────────┐                 ┌──────────────────┐
        │ PayNotifyCtrl   │  验签+金额校验   │ CancelUnpaid     │  回补库存/释券
        │ Redisson tryLock│  防重复回调      │ OrderListener    │  消费幂等表防重
        │ Feign→paySuccess │  状态机流转     │                  │  MQ 重复投递也只做一次
        │ WAIR_PAY→PAID   │                 └──────────────────┘
        └─────────────────┘
                  └──► 并发时：生命周期锁 + 状态机条件更新 → 只成功一次
```

### 4.2 秒杀券链路（热点路径隔离）

```
coupon → prepareCouponRush
  ① Redis Lua 原子预占券（热路径挡在 Redis，不进长事务）
  ② DB 扣券库存  ③ order 建待支付单
  作用：秒杀瞬时流量挡在内存层，避免打进 DB 长事务拖垮系统
```

### 4.3 AI 客服链路

```
用户提问 → agent(SSE)
  ① 意图识别（意图树）② RAG 检索（ES+Embedding+Rerank）
  ③ MCP 工具调用（查订单/物流）④ LLM 编排 → SSE 流式返回
```

**要点**：交易主链路是"强一致 + 最终一致"双保险——Seata 保跨库原子，Outbox+MQ 保超时关单最终一致；支付与关单并发靠"锁+状态机"只成功一次。迟到支付（订单已关）走原路退款不复活。模拟物流用链式延迟消息：事务提交后投递下一站消息，每站 1 小时，走完触发自动确认收货。

---

## 5. 新人通读路线（10 步从零到精通）

> 这是注释 770 个文件后总结出的最佳阅读顺序——按"被依赖者先读、依赖者后读"的拓扑排序设计。
> 每步精确到文件级，标注读什么、前置知识、学完能掌握什么、预计耗时。
> 跟着走完 10 步，你能彻底理解这个项目的每一个核心机制。

### 📋 路线总览

```
第0步 建立全局认知 → 第1步 公共底座 → 第2步 状态字典+状态机
→ 第3步 下单主链路（核心2天）→ 第4步 死信队列消费者
→ 第5步 支付回调链路 → 第6步 分布式定时任务
→ 第7步 库存与优惠券 → 第8步 用户鉴权与网关
→ 第9步 AI 智能客服 → 第10步 商品/搜索/购物车/后台/部署
```

### 📊 模块文件全景分级（别被文件数量吓到）

> 后端共 **770 个文件**，但只有 **~52 个必读**——其余是方法名自解释的薄文件，读类注释一句话即可。
>
> 三级分类标准：
> - **必读 ⭐**：含核心业务逻辑/设计模式的文件，面试会问到——第 5 章每步列出的就是这些
> - **选读**：含一定业务逻辑但非核心（Controller CRUD / Service 实现 / Component），想深入时读
> - **跳过**：薄文件（Mapper 接口 / PO 实体 / Query / DTO / VO / Enum），方法名自解释

| 模块 | 总文件 | 必读 ⭐ | 选读 | 跳过 | 主要分类分布 |
|---|---|---|---|---|---|
| **order** | 74 | 13 | ~14 | ~47 | controller:12, DTO:7, Service:7, PO:7, Query:7, Mapper:7 |
| **user** | 130 | 6 | ~26 | ~98 | controller:21, Service:15, DTO:14, Mapper:13, PO:11 |
| **common** | 113 | 12 | ~22 | ~79 | Component:12, Utils:11, Config:9, DTO:9, Enum:9 |
| **product** | 62 | 3 | ~12 | ~47 | VO:13, controller:8, DTO:7, Service:6, PO:6 |
| **coupon** | 51 | 5 | ~10 | ~36 | DTO:12, Enum:5, VO:5, Service:4, Component:4 |
| **search** | 30 | 1 | ~6 | ~23 | controller:6, Service:5, Query:4, Component:3 |
| **admin** | 38 | 1 | ~7 | ~30 | controller:8, Service:4, PO:4, Query:4 |
| **pay** | 25 | 6 | ~5 | ~14 | DTO:9, Service:3, controller:3, API:2 |
| **stock** | 17 | 2 | ~3 | ~12 | DTO:6, API:2, VO:2, controller:2 |
| **cart** | 16 | 2 | ~3 | ~11 | controller:3, DTO:2, Service:2 |
| **gateway** | 14 | 1 | ~2 | ~11 | Config:8, 启动类:6 |
| **agent** | ~195 | ~15 | ~40 | ~140 | Python:111, Java:84（Graph/RAG/MCP/Services/Harness） |
| **合计** | **~770** | **~52** | **~116** | **~602** | |

**结论**：770 个文件里，真正需要逐行精读的只有 **~52 个**（7%），其余 93% 读类注释一句话就够。别被目录里的文件数量吓到——跟着第 5 章的 10 步走，每步只读标注 ⭐ 的必读文件。

### 第 0 步：建立全局认知（半天）

| 文件 | 读什么 | 耗时 |
|---|---|---|
| `README.md` | 项目门面：双主线（商城+AI客服）、架构图、技术栈表、各微服务职责表 | 30 分钟 |
| `ZHIDAO.md`（本文件） | 重点读第 1/2/4 章——项目是什么、核心概念、运行流程全景图 | 1 小时 |
| `Description.md` | 项目名片：含金量机制清单，每个机制解决了什么问题 | 5 分钟 |
| `deploy/GO_LIVE.md` | 部署清单：中间件列表、SQL 初始化顺序、启动顺序、冒烟测试 | 30 分钟 |

**前置知识**：无。**学完掌握**：知道项目是什么、有什么、怎么跑起来、核心机制有哪些。

---

### 第 1 步：公共底座 common（1 天）

> 所有模块都依赖 common——先读懂它，后面读任何模块都不会卡。

| 顺序 | 文件 | 读什么 | 耗时 |
|---|---|---|---|
| 1 | `ResponseVO.java` | 统一响应格式（code/msg/data），所有 API 返回都靠它 | 5 分钟 |
| 2 | `BusinessException.java` | 统一业务异常，携带错误码和消息 | 10 分钟 |
| 3 | `TokenUserInfoDTO.java` | Redis 存储的登录态载体（userId/email/nickName/token） | 10 分钟 |
| 4 | `StringTools.java` | 订单号生成/支付单号生成/日期格式化/随机数 | 30 分钟 |
| 5 | `RedisComponent.java` ⭐ | **核心**：Token 存储/验证码/秒杀 Lua 预占/生命周期锁/支付单关单标记/退款幂等闸 | 1 小时 |
| 6 | `MqListenerHelper.java` ⭐ | 消费幂等表抢锁/nack 重试死信/释放幂等锁 | 30 分钟 |
| 7 | `ReliableMessageSender.java` ⭐ | Outbox 模式核心——DB 与 MQ 一致性 | 30 分钟 |
| 8 | `TransactionalMqSender.java` ⭐ | sendAfterCommit 挂事务提交回调，延迟关单/物流消息靠它发 | 20 分钟 |
| 9 | `RemoteCompensateRecorder.java` ⭐ | Feign 补偿失败落库记录，等人工兜底 | 15 分钟 |
| 10 | `RabbitMQConfig.java` ⭐ | 所有交换机/队列/死信/路由键定义——MQ 全景图 | 30 分钟 |

**前置知识**：无。**学完掌握**：响应格式、异常体系、Redis 封装（锁/Lua/Token）、MQ 封装（Outbox/幂等/补偿）、所有模块共用的基础设施。

---

### 第 2 步：订单状态字典 + 状态机（半天）⭐

> 交易核心的"语法规则"——先懂状态有哪些、怎么流转，再读下单主链路才不会迷路。

| 顺序 | 文件 | 读什么 | 耗时 |
|---|---|---|---|
| 1 | `OrderStatusEnum.java` | 10 个订单状态枚举 + 中文描述，状态流转的"字典" | 10 分钟 |
| 2 | `OrderStateEvent.java` | 9 个触发事件枚举，驱动状态流转的"动作" | 10 分钟 |
| 3 | `OrderStateMachine.java` ⭐⭐ | **核心**：转换表 EnumMap + `transition` 带旧状态条件 UPDATE 防并发覆盖 | 30 分钟 |

**重点读 `OrderStateMachine`**：
- `buildTransitions()`——所有合法转换规则，看懂"哪个态能到哪个态"
- `transition(orderId, expectedStates, event, patch)`——带 `WHERE order_status=期望态` 条件 UPDATE，并发只一行命中
- `transitionPayOrder`——按 payOrderId 流转（支付回调场景）
- `resolveCommonTarget`——多期望态必须流向同一目标态校验

**前置知识**：第 1 步（RedisComponent 的锁概念）。**学完掌握**：订单状态流转规则 + "带旧状态条件 UPDATE 防并发覆盖"机制。

---

### 第 3 步：下单主链路 OrderInfoServiceImpl（2 天）⭐⭐⭐

> 这是整个项目的"主文"——1976 行，交易编排中心，一条线串起 stock/product/coupon/pay/MQ。
> 先读类头注释建立全局心智，再按"下单→支付→关单→退款"四条链路逐方法精读。

**文件**：`Simlect-order/app/.../biz/impl/OrderInfoServiceImpl.java`（1976 行）

| 顺序 | 读什么 | 关键点 | 耗时 |
|---|---|---|---|
| 1 | 行 1-68 类总纲注释 | 四条调用链（普通下单/秒杀/支付回调/超时关单）——先读建立全局心智 | 10 分钟 |
| 2 | 行 69-115 依赖注入区 | 13 个 Feign/Mapper/组件——看出"编排中心"调了谁 | 10 分钟 |
| 3 | `postOrder` ⭐ | **下单主链路**：`@GlobalTransactional` + Feign 调用链 + Outbox 投递 + catch 补偿兜底。配合第 4 章全景图逐行对照 | 40 分钟 |
| 4 | `prepareCouponRush` ⭐ | **秒杀券预占**：Redis Lua → DB 扣券 → 建单三层闸门 + rushCode==2 复用 | 30 分钟 |
| 5 | `paySuccess` + `paySuccessInLock` ⭐ | **支付入账**：生命周期锁 + 状态机条件 UPDATE + 物流信息落库 + Outbox 投递物流消息 | 40 分钟 |
| 6 | `cancelOrder` + `cancelOrderInLock` ⭐ | **关单**：与 paySuccess 对称，生命周期锁内执行 | 30 分钟 |
| 7 | `closeUnpaidPayOrderForTimeout` | 批量超时关单：按 payOrderId 条件 UPDATE + 幂等标记 | 20 分钟 |
| 8 | `refund` + `refundInLock` + `doRefund` ⭐ | **退款**：四重幂等闸（Redis + 事务回滚清标记 + pay 侧 + 锚点）+ 金额按实付比例防超退 | 40 分钟 |
| 9 | `handlePaySuccessConflict` + `refundLatePaymentAfterClose` ⭐ | **迟到支付退款**：订单已关但回调迟到 → 原路退款不复活 | 20 分钟 |
| 10 | `cancelUnpaidOrderForPayTimeout` | MQ 消费入口：被死信队列调用 | 15 分钟 |

**配套读**：
- `OrderController.java`（前台控制器，15 分钟）——下单/取消/退款 HTTP 入口
- `OrderInternalController.java`（内部 API，15 分钟）——Feign 调用入口
- `PayOrderTask.java`（定时任务，见第 6 步）——轮询补齐回调

**前置知识**：第 1 步（common 基础设施）+ 第 2 步（状态机）。**学完掌握**：交易主链路完整流程——下单→支付→关单→退款，以及 Seata/Outbox/状态机/锁/幂等五大机制如何协作。

---

### 第 4 步：死信队列消费者（半天）⭐⭐

> 最终一致性的闭环——超时关单/自动收货/物流推进，以及"事务后 ack"机制。

| 顺序 | 文件 | 读什么 | 耗时 |
|---|---|---|---|
| 1 | `RabbitMQPayOrderDeadListenerComponent.java` ⭐⭐ | 三类超时事件消费 + `registerAckSync` 事务后 ack | 1 小时 |
| 2 | `RabbitMQOrderListenerComponent.java` | 秒杀订单异步建单 + 幂等校验 | 15 分钟 |

**重点读 `RabbitMQPayOrderDeadListenerComponent`**：
- `handlePayTimeoutOrder`——支付超时关单：消费幂等抢锁 + cancelUnpaidOrderForPayTimeout + nack 重试
- `handleConfirmOrder` + `processOrderConfirm`——自动确认收货：AOP 代理调事务方法
- `handleLogisticsOrder` + `processOrderLogistics`——模拟物流链式推进：step 幂等 + 状态流转 + afterCommit 投递下一站
- `registerAckSync` ⭐——**事务后 ack**：DB 提交才 ack，回滚则 nack，不用 XA 保 DB 与 MQ 一致

**前置知识**：第 1 步（MqListenerHelper）+ 第 3 步（OrderInfoServiceImpl）。**学完掌握**：最终一致性闭环 + 事务后 ack 机制 + AOP 代理调事务方法。

---

### 第 5 步：支付回调链路（1 天）⭐⭐

> 支付安全三件套——验签防伪造、金额校验防篡改、幂等闸防重复退款。

| 顺序 | 文件 | 读什么 | 耗时 |
|---|---|---|---|
| 1 | `PayNotifyController.java` ⭐ | 回调入口：Redisson tryLock 防重复回调 + Feign 调 paySuccess | 30 分钟 |
| 2 | `PayChannel4AliPay.java` ⭐⭐ | 支付宝渠道实现：验签/金额校验/退款幂等闸/查单/关单 | 1 小时 |
| 3 | `PayApplication.java` | 启动类 | 5 分钟 |
| 4 | `PayFeignClient.java` | Feign 契约 + 方法注释 | 10 分钟 |
| 5 | `PayFeignFallbackFactory.java` | 降级工厂：下游挂了明确失败不 NPE | 15 分钟 |

**重点读 `PayChannel4AliPay`**：
- `payNotify` ⭐——验签（rsaCertCheckV2）+ 实付金额校验（防篡改）
- `refund` ⭐——退款幂等闸：out_request_no 做 Redis SET NX EX 7 天，失败清闸允许重试
- `getPayUrl`——拉支付链接：PC/WAP 双渠道 + 本地 mock 模式
- `queryOrder`——查单：PayOrderTask 轮询调它补齐回调
- `closeOrder`——关单：关单时通知支付宝侧也关

**前置知识**：第 3 步（paySuccess 调用链）。**学完掌握**：支付安全三件套 + Redisson 看门狗锁 + 退款幂等闸。

---

### 第 6 步：分布式定时任务（半天）⭐

> 轮询补齐漏掉的支付回调——分布式锁 + 时间窗 + 分页防全表扫描。

| 文件 | 读什么 | 耗时 |
|---|---|---|
| `PayOrderTask.java` ⭐ | `@Scheduled(fixedDelay=5000)` + SET NX EX + Lua 释放 + 时间窗 10 分钟 + 分页 3 页 | 30 分钟 |

**重点读**：
- `pollPayOrders`——分布式锁抢锁 + deleteIfOwned（Lua 比对删除防误删）
- `pollPendingOrders`——时间窗 + 真实分页 + 单条异常不中断
- `handleOrder`——查支付宝 + 触发 paySuccess 补齐

**前置知识**：第 3 步（paySuccess）+ 第 5 步（PayChannel4AliPay.queryOrder）。**学完掌握**：分布式定时任务并发安全——多实例只一个执行、Lua 防误删锁、时间窗分页防全表扫描。

---

### 第 7 步：库存与优惠券（1 天）⭐

> 被 order Feign 调的下游——理解热路径隔离和库存幂等扣补。

| 顺序 | 文件 | 读什么 | 耗时 |
|---|---|---|---|
| 1 | `StockApplication.java` + `StockFeignClient.java` | 库存契约 + 启动类 | 10 分钟 |
| 2 | `StockServiceImpl.java` | 库存扣减/回补，幂等 operationId 防重复扣补 | 30 分钟 |
| 3 | `CouponApplication.java` + `CouponFeignClient.java` | 优惠券契约 + 启动类 | 10 分钟 |
| 4 | `DiscountCouponServiceImpl.java` ⭐ | 秒杀券 Redis Lua 预占 + DB 扣券库存管理 | 30 分钟 |
| 5 | `UserCouponServiceImpl.java` | 用户领券/用券/退券状态管理 | 20 分钟 |
| 6 | `CouponRushRateLimitAspect.java` | 秒杀限流切面：@Aspect IP 维度限流 | 15 分钟 |
| 7 | `RabbitMQOrderDeadListListenerComponent.java`（coupon 侧） | 秒杀超时未付释券死信消费 | 15 分钟 |

**前置知识**：第 3 步（postOrder 的 Feign 调用链 + prepareCouponRush）。**学完掌握**：热路径隔离（秒杀挡 Redis 不进 DB）+ 库存幂等扣补 + 优惠券状态流转。

---

### 第 8 步：用户鉴权与网关（1 天）⭐

> 登录安全链路——IP 锁定 + 验证码 + bcrypt + 老密码无感升级 + 封禁 + Token。

| 顺序 | 文件 | 读什么 | 耗时 |
|---|---|---|---|
| 1 | `GatewayApplication.java` | 网关启动：路由/鉴权/限流 | 5 分钟 |
| 2 | `AccountController.java` ⭐⭐ | **核心**：注册/登录/验证码/找回密码 | 1 小时 |
| 3 | `UserApplication.java` + `UserFeignClient.java` | 用户契约 + 启动类 | 10 分钟 |
| 4 | `UserInfoServiceImpl.java` | 用户 CRUD + 注册逻辑 | 30 分钟 |
| 5 | `UserTempBanService.java` | 临时封禁 + 延时解禁（MQ 驱动） | 20 分钟 |
| 6 | `UserLoginLockService.java` | IP 维度失败计数 + 锁定 | 15 分钟 |
| 7 | `PasswordService.java` | bcrypt 加密/校验/老密码格式升级 | 15 分钟 |

**重点读 `AccountController`**：
- `login` ⭐——登录安全链路：IP 锁定 → 验证码 → bcrypt 校验 → 老密码无感升级 → 封禁检查 → Redis Token + HttpOnly Cookie
- `getEmailCode` ⭐——双层限流：IP @RateLimit + 邮箱 Redis 计数 + 滑动验证码前置
- `autoLogin`——Token 有效性校验 + 刷新
- `logout`——清 Redis Token + 清 Cookie

**前置知识**：第 1 步（RedisComponent 的 Token 存储）。**学完掌握**：登录安全链路 + 会话管理（Token/Cookie/限流/封禁）+ 网关路由鉴权。

---

### 第 9 步：AI 智能客服（2 天）⭐

> agent 模块是 Python(LangChain) + Java(Spring AI) 双实现——AI 客服编排。

| 顺序 | 文件 | 读什么 | 耗时 |
|---|---|---|---|
| 1 | `app/graph/builder.py` + `app/graph/nodes.py` ⭐ | 对话图构建与节点：意图识别→RAG→工具调用→流式回答 | 2 小时 |
| 2 | `app/graph/state.py` | LangGraph State 定义 | 20 分钟 |
| 3 | `app/domain/intent/classifier.py` | 意图识别：LLM 驱动判断走 RAG/工具/闲聊 | 30 分钟 |
| 4 | `app/rag/retriever.py` | RAG 检索：FAQ/知识库检索（Embedding+ES+Rerank） | 1 小时 |
| 5 | `app/mcp/tools.py` | MCP 工具调用：查订单/查物流（调真实接口） | 30 分钟 |
| 6 | `app/services/agent_runtime.py` | Agent 编排核心：组装图+工具+记忆 | 1 小时 |
| 7 | `app/graph/checkpoint/redis_saver.py` | Redis 检查点：对话状态持久化 | 30 分钟 |
| 8 | Java 侧 `AgentApplication` 等 | Spring AI 实现（与 Python 功能对齐） | 穿插 |

**前置知识**：第 8 步（用户鉴权）。**学完掌握**：AI 客服编排——意图识别→RAG 检索→MCP 工具调用→SSE 流式回答 + 对话状态持久化。

---

### 第 10 步：商品/搜索/购物车/后台/部署（半天）

> 补齐剩余模块——理解商品快照防改价、ES 搜索、全栈部署。

| 文件 | 读什么 | 耗时 |
|---|---|---|
| `ProductApplication.java` + `ProductFeignClient.java` | 商品契约 | 10 分钟 |
| `ProductInfoServiceImpl.java` | 商品 CRUD + 快照（下单防改价）+ 销量累加 | 20 分钟 |
| `SearchApplication.java` | ES 商品检索 + analysis-ik 中文分词 | 10 分钟 |
| `CartApplication.java` | 购物车（下单成功后被 order 清） | 5 分钟 |
| `AdminApplication.java` | 后台管理/审计/敏感词/统计 | 10 分钟 |
| `deploy/docker-compose.middleware.yml` | 一键起 7 个中间件 | 15 分钟 |
| `deploy/nginx.simlect.conf.example` | Nginx 反代配置 | 10 分钟 |

**前置知识**：第 3 步（商品快照在 postOrder 中的角色）。**学完掌握**：剩余业务模块 + 全栈部署。

---

### 📊 通读路线速查

| 步 | 目标 | 核心文件 | 耗时 | 前置 |
|---|---|---|---|---|
| 0 | 建立全局认知 | README + ZHIDAO + Description + GO_LIVE | 半天 | 无 |
| 1 | 公共底座 | RedisComponent + MqListenerHelper + ReliableMessageSender + RabbitMQConfig | 1 天 | 无 |
| 2 | 状态字典+状态机 | OrderStatusEnum + OrderStateEvent + **OrderStateMachine** | 半天 | 1 |
| 3 | 下单主链路 | **OrderInfoServiceImpl**（1976行，10方法）+ OrderController | 2 天 | 1+2 |
| 4 | 死信队列消费者 | **RabbitMQPayOrderDeadListenerComponent**（registerAckSync） | 半天 | 1+3 |
| 5 | 支付回调链路 | **PayNotifyController** + **PayChannel4AliPay** | 1 天 | 3 |
| 6 | 分布式定时任务 | **PayOrderTask**（SET NX + Lua） | 半天 | 3+5 |
| 7 | 库存与优惠券 | StockServiceImpl + **DiscountCouponServiceImpl** | 1 天 | 3 |
| 8 | 用户鉴权与网关 | **AccountController**（登录安全链路） | 1 天 | 1 |
| 9 | AI 智能客服 | graph/builder + nodes + intent + rag + mcp | 2 天 | 8 |
| 10 | 商品/搜索/后台/部署 | ProductInfoServiceImpl + docker-compose | 半天 | 3 |
| | **总计** | | **约 10 天** | |

---

## 6. 关键设计模式解析

| 模式 | 意图 | 落点文件 |
|---|---|---|
| **Saga / 全局事务 + 三层容错** | 跨服务跨库强一致，任一失败全局回滚；Seata 不可达时 Feign 补偿回补，补偿也失败落 RemoteCompensateRecorder 等人工兜底 | `OrderInfoServiceImpl.@GlobalTransactional`（import io.seata）+ catch 块补偿 |
| **Outbox Pattern** | DB 与 MQ 一致性，同库同事务写消息表后异步投递 | `ReliableMessageSender` / `TransactionalMqSender` + `mq.outbox` 配置 |
| **事务后 ack 本地事务消息** | MQ 消费者把 ack/nack 挂事务提交/回滚回调——DB 提交才 ack，回滚则 nack 重试，不用 XA 也能保 DB 与 MQ 一致 | `RabbitMQPayOrderDeadListenerComponent#registerAckSync` |
| **状态机 + 条件 UPDATE** | 约束订单状态合法流转 + 带旧状态条件 UPDATE 防并发覆盖（WHERE status=期望态，并发只一行命中） | `OrderStateMachine` + `OrderStateEvent` + `OrderStatusEnum` |
| **分布式锁** | 回调/定时任务并发互斥 | `PayNotifyController` Redisson RLock（看门狗续期）；`PayOrderTask` SET NX EX + Lua 比对删除 |
| **幂等挡板（四重）** | 退款四重防重：Redis tryMarkOrderRefundOnce + 事务回滚清标记 + pay 侧 refundOrderId + refundOrderId 锚点复用 | `refundInLock` + `doRefund` + `PayChannel4AliPay.refund` |
| **迟到支付退款** | 订单已关但支付回调迟到 → 原路退款不复活订单，防"关了又付"资金事故 | `handlePaySuccessConflict` → `refundLatePaymentAfterClose` |
| **退款金额防超退** | 用券订单按券后实付比例分摊退款，防按原价超退 | `doRefund` → `calcRefundAmount` |
| **降级 Fallback** | 下游挂了返回明确失败不 NPE | 各 `*FeignFallbackFactory` |
| **热路径隔离** | 秒杀流量挡在 Redis 不进 DB 长事务；Feign 重试复用已建订单不卡死 | coupon `prepareCouponRush` Redis Lua 预占（rushCode==2 复用分支） |
| **链式延迟消息** | 事务提交后投递下一条延迟消息，模拟物流每站 1 小时链式推进，走完触发自动确认收货 | `processOrderLogistics` afterCommit 投递下一站 |
| **AOP 代理调事务方法** | exposeProxy=true + AopContext.currentProxy()，防同类内 this.xxx() 绕过 AOP 导致 @Transactional 失效 | `RabbitMQPayOrderDeadListenerComponent` |
| **Feign + Sentinel** | 服务间调用 + 限流熔断 | `feign.sentinel.enabled=true` |

---

## 7. 配置系统详解

| 配置项 | 默认值 | 说明 |
|---|---|---|
| `server.port` | 8093（order） | 各服务端口独立 |
| `spring.cloud.nacos.discovery.server-addr` | 127.0.0.1:8848 | Nacos 注册中心 |
| `spring.cloud.sentinel.transport.dashboard` | 127.0.0.1:8858 | Sentinel 控制台 |
| `seata.tx-service-group` | simlect_tx_group | Seata 事务分组 |
| `seata.data-source-proxy-mode` | AT | 自动建 undo_log 回滚 |
| `spring.cloud.openfeign.sentinel.enabled` | true | Feign 接 Sentinel |
| `spring.rabbitmq.listener.simple.acknowledge-mode` | manual | 手动 ack，prefetch 1 |
| `simlect.internal.token` | 环境变量 | 服务间内部调用鉴权 |
| `mq.outbox.dispatch-enabled` | true | Outbox 投递开关 |
| `mq.compensation.auto-replay-enabled` | true | 补偿自动重放 |

**加载优先级**：`simlect-common.yml`（公共）← 各服务 `application.yml` ← 环境变量覆盖。

---

## 8. 如何运行和测试

### 8.1 环境要求

| 组件 | 版本 | 默认端口 |
|---|---|---|
| JDK | 17 | — |
| Node.js | 18+ | — |
| MySQL | 8.0+ | 3306 |
| Redis | 6+ | 6379 |
| RabbitMQ | 3.10+ | 5672 |
| Nacos | 2.x | 8848 |
| Seata Server | 2.x | 8091 |
| Elasticsearch | 9.2.1 | 9200 |

### 8.2 后端启动

```bash
# 中间件一键起（docker-compose）
cd deploy && docker-compose -f docker-compose.middleware.yml up -d

# 建库（一域一库）
mysql -uroot -proot < sql/simlect_user.sql
mysql -uroot -proot < sql/simlect_product.sql
mysql -uroot -proot < sql/simlect_order.sql

# 编译后端
cd Simlect-backend && mvn clean install -DskipTests

# 按顺序启动服务（gateway → user → product → ... → agent）
cd Simlect-order/app && mvn spring-boot:run
```

### 8.3 前端启动

```bash
cd Simlect-front/Simlect-web
npm install
npm run dev      # 开发
npm run build    # 生产构建（自动生成 PWA 资源）
```

### 8.4 测试

```bash
cd Simlect-backend
mvn test          # 单元测试 *Test.java
mvn verify -Pit   # 集成测试 *IT.java（Testcontainers）
```

---

## 9. 学习里程碑与自检

> 第 5 章给了 10 步通读路线，这一章帮你自检每步学到位了没有。

### 🎯 每步学完应该能回答的问题

| 步 | 学完后自检题 | 如果答不出 |
|---|---|---|
| 1 | `RedisComponent` 里生命周期锁和退款幂等闸分别怎么用？Outbox 为什么能保"DB 提交即 MQ 必发"？ | 重读 RedisComponent + ReliableMessageSender |
| 2 | 状态机的 `transition` 方法为什么用 `WHERE status=期望态` 条件 UPDATE？不这样会怎样？ | 重读 OrderStateMachine#transition |
| 3 | `postOrder` 里 catch 块的三层补偿是什么？为什么不能只靠 Seata？ | 重读 postOrder catch 块 |
| 4 | `registerAckSync` 为什么把 ack 挂到事务后？不挂会怎样？ | 重读 RabbitMQPayOrderDeadListenerComponent |
| 5 | 支付宝回调验签失败会怎样？退款幂等闸失败后怎么清？ | 重读 PayChannel4AliPay#payNotify + refund |
| 6 | 多实例部署时 PayOrderTask 怎么保证只有一个执行？锁过期了怎么办？ | 重读 PayOrderTask pollPayOrders |
| 7 | 秒杀券为什么先 Redis Lua 再 DB 扣减？Feign 重试时 rushCode==2 怎么处理？ | 重读 prepareCouponRush |
| 8 | 登录时老密码格式（非 bcrypt）怎么处理？邮箱验证码怎么防轰炸？ | 重读 AccountController login + getEmailCode |
| 9 | AI 客服对话流程是什么？意图识别 + RAG + MCP 工具怎么协作？ | 重读 graph/builder + nodes |
| 10 | 商品快照在 postOrder 里起什么作用？前端怎么部署？ | 重读 postOrder 快照段 + deploy |

### 📅 建议时间分配

| 路径 | 耗时 | 适合 |
|---|---|---|
| **速览路径**（第 0→2→3→5 步） | 3-4 天 | 面试前快速掌握核心机制 |
| **交易主线**（第 0→1→2→3→4→5→6 步） | 6-7 天 | 彻底理解交易一致性 |
| **全量通读**（第 0→1→2→…→10 步） | 约 10 天 | 彻底学习整个项目 |

---

## 10. 常见问题

**Q：为什么 order 要加 `@GlobalTransactional`？普通 `@Transactional` 不行吗？**
A：下单跨 4 个库（锁 stock、锁 coupon、写 order、调 pay）。普通事务只管本服务数据源，跨库回滚不了。Seata AT 用 undo_log 记反向 SQL，全局回滚按 undo_log 逆向执行，实现跨库一致。

**Q：支付回调与超时关单并发了怎么办？**
A：两层兜底——① pay 服务 Redisson tryLock（看门狗续期）抢订单锁，抢不到说明正在处理/已关单，直接忽略；② order 服务 paySuccess 用状态机条件更新（`WHERE status = WAIT_PAYMENT`），状态已变则更新 0 行，自然幂等。锁 + 状态机双保险只成功一次。

**Q：Outbox 发 MQ 失败了怎么办？**
A：Outbox 消息表与业务表同库同事务写，DB 提交即消息必落库。后台 `dispatch` 任务轮询未投递消息重试；`compensation.auto-replay` 自动重放。投递成功才标记，失败无限重试——保证"DB 提交即 MQ 最终必发"。

**Q：为什么秒杀券要先用 Redis Lua 预占？**
A：秒杀瞬时高并发。直接进 DB 扣券 + 长事务，连接池瞬间打满系统崩。Redis Lua 把预占挡在内存层原子完成，只有成功才进 DB 扣减，把 DB 压力降到正常量级。

**Q：支付回调迟到了（订单已关）怎么办？**
A：不复活订单——`handlePaySuccessConflict` 检测到订单已关/已取消，走 `refundLatePaymentAfterClose` 把钱原路退回。幂等闸 `tryMarkLatePaymentRefundOnce` 防重复退款，退款失败清闸允许重试。这是"支付/关单竞态"的完整解法——不只"只成功一次"，还处理"晚了怎么办"。

**Q：用券订单退款怎么防止按原价超退？**
A：`calcRefundAmount` 按券后实付金额比例分摊退款——优惠券抵扣的部分不退，只退用户实际付的钱。整单退款退实付全额，部分退款按明细占比算。防"用 100 减 80 券的订单退 100"的套利。

**Q：Seata 全局事务回滚失败了怎么办？**
A：三层容错——① Seata AT 正常全局回滚（undo_log 逆向）；② Seata 不可达时 catch 块里 Feign 补偿回补库存/释券（幂等 operationId 防重复）；③ 补偿也失败时落 `RemoteCompensateRecorder` 等人工兜底。不依赖单一机制。

**Q：MQ 消费者怎么保证 DB 提交了才 ack？**
A：`registerAckSync` 把 ack/nack 挂到 Spring 事务同步回调——`afterCommit` 里执行 ack，`afterCompletion(ROLLED_BACK)` 里执行 nack。这样 DB 提交了才让消息从队列消失，回滚则消息重回队列重试。不用 XA 也能保 DB 与 MQ 一致。

**Q：同类内调 @Transactional 方法为什么事务不生效？**
A：Spring 事务基于 AOP 代理，`this.xxx()` 绕过代理直接调目标对象，注解失效。项目用 `@EnableAspectJAutoProxy(exposeProxy=true)` + `AopContext.currentProxy()` 取代理对象调，保证事务生效。死信队列监听器里 `processOrderConfirm`/`processOrderLogistics` 就这么调的。

---

## 附录：关键术语对照表

| 英文 | 中文 | 说明 |
|---|---|---|
| Seata AT | Seata 自动事务 | 自动建 undo_log 回滚 |
| Outbox | 发件箱模式 | 业务表与消息表同库同事务，异步投递 MQ |
| Sentinel | 阿里哨兵 | 流控熔断组件 |
| RLock | Redisson 锁 | 可重入分布式锁，看门狗自动续期 |
| MCP | 模型上下文协议 | AI 工具调用标准协议 |
| RAG | 检索增强生成 | 检索知识库拼入 LLM 上下文 |
| Feign | 伪声明式 HTTP 客户端 | Spring Cloud 服务间调用 |
| Nacos | 阿里注册/配置中心 | 服务发现 + 配置管理 |
| Saga | 长事务模式 | 跨服务事务，逐段提交+补偿 |
| State Machine | 状态机 | 约束状态合法流转 |