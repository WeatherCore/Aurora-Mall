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

## 5. 逐文件代码导读

> 阅读顺序由 skeleton.json 的 `quick_start_files` 字段数据驱动（入口→中间→核心），非凭感觉。
> 注：本节聚焦后端，因为注释仅作用于后端。

### 5.1 快速上手 3 文件（骨架自动计算）

| 顺序 | 角色 | 文件 | 为什么先读它 |
|---|---|---|---|
| ① 入口 | 请求起点 | `Simlect-backend/Simlect-order/app/src/main/java/com/simlect/biz/impl/OrderInfoServiceImpl.java` | 入度0出度69，订单编排中心，一条线串起 stock/product/coupon/pay/MQ |
| ② 中间 | 连接入口与核心 | `Simlect-backend/Simlect-common/src/main/java/com/simlect/utils/StringTools.java` | 入口直接依赖里入度最高（108），全系统最被依赖的工具类 |
| ③ 补充 | 核心概念 | `Simlect-backend/Simlect-agent/app/models/response.py` | 入度90（agent 模块为 Python+Java 混合实现） |

### 5.2 OrderInfoServiceImpl 精读建议

```
行 1-68   : 类总纲注释（已有），讲清四条调用链——先读这块建立全局心智
行 69-115 : 依赖注入区（13 个 Feign/Mapper/组件）——看出"编排中心"调了谁
( postOrder  : ⭐ 下单主链路，配合 4.1 全景图逐行对照读，约 40 分钟 )
( paySuccess : ⭐ 支付入账，配合状态机 OrderStateMachine 读，约 30 分钟 )
( cancelOrder: ⭐ 超时关单/退款，配合 MQ 死信监听器读，约 30 分钟 )
```

### 5.3 配套必读文件

| 文件 | 作用 | 阅读耗时 |
|---|---|---|
| `OrderStatusEnum.java`（order） | 10 个订单状态枚举，状态机流转的"字典" | 5 分钟 |
| `OrderStateMachine.java`（order） | 状态机：校验转换合法性，非法转换拒绝 | 15 分钟 |
| `RabbitMQPayOrderDeadListenerComponent.java`（order） | 死信队列消费：支付超时关单/物流确认 | 20 分钟 |
| `PayNotifyController.java`（pay） | 支付宝回调入口：验签+Redisson锁+Feign入账 | 20 分钟 |
| `PayChannel4AliPay.java`（pay） | 支付宝渠道：验签/实付金额校验/退款幂等闸 | 30 分钟 |
| `PayOrderTask.java`（order） | 分布式定时任务锁：SET NX+Lua释放防误删 | 15 分钟 |

**要点**：先读 OrderInfoServiceImpl 类头注释 → 再读 OrderStatusEnum 状态字典 → 再逐方法精读三大入口。

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

## 9. 复刻建议与学习路线

> 654 个 Java 文件是多 session 工程。阶段 1 起点用 skeleton.json 的 `quick_start_files`。注释仅作用于后端，前端不加注。

| 阶段 | 目标 | 模块 | 耗时估计 |
|---|---|---|---|
| 1 | 理解交易主链路 | order（OrderInfoServiceImpl + 状态机 + MQ 监听器） | 1-2 周 |
| 2 | 理解支付回调安全 | pay（PayNotifyController + PayChannel4AliPay） | 3-5 天 |
| 3 | 理解库存与券热点 | stock + coupon（扣减回补 + Redis Lua 预占） | 3-5 天 |
| 4 | 理解鉴权与网关 | gateway + user | 3-5 天 |
| 5 | 理解 AI 客服 | agent（Spring AI + LangChain + RAG + MCP） | 1-2 周 |
| 6 | 前端与部署 | Simlect-front + deploy（Vue3 + Nginx + docker-compose） | 穿插 |

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