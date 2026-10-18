# Aurora-Mall 逐文件精读清单

> **全量清单，895 文件全覆盖**（723 Java + 107 Python + 65 XML 配置）。
> 按 `ZHIDAO.md` 第 5 章 10 步路线分章，每章挂模块，模块内分 4 层。
> ZHIDAO 是"导读捷径"（只标 52 必读），本清单是"全集"——每个文件都能打勾。

---

## 使用说明

**4 层分级**（每文件前标注）：

| 标记 | 层级 | 读法 | 单文件耗时 |
|---|---|---|---|
| ⭐⭐ | 必读·核心 | 逐行精读，配合 ZHIDAO 全景图 | 30–60 分钟 |
| ⭐ | 必读 | 逐行精读 | 15–30 分钟 |
| · | 选读 | 读类头注释 + 关键方法 | 5–15 分钟 |
| ○ | 薄文件 | 读类注释一句话即可（PO/DTO/VO/Enum/Query/Mapper接口/启动类） | 1–3 分钟 |
| T | 测试 | 验证理解用，可后读 | 3–10 分钟 |

**勾选规则**：`- [ ]` 未读 / `- [x]` 已读。按章节顺序推进，不要跳层。

**路径约定**：双模块服务（order/pay/coupon/stock/user/product/cart）路径前缀 `api/`（对外契约：FeignClient/DTO/VO/Enum/Fallback）或 `app/`（业务实现：Controller/Service/Mapper/state/task）。单模块服务（common/gateway/search/admin/agent）无此前缀。

**说明来源**：功能说明基于文件名 + 包路径 + ZHIDAO 信息推断，以源码类头注释为最终准。

---

## 时间估算总表

| 步 | 模块 | 文件数 | 必读⭐ | 估算耗时 |
|---|---|---|---|---|
| 0 | 全局认知（文档） | 4 | 4 | 半天 |
| 1 | common 公共底座 | 149 | 12 | 1.5 天 |
| 2-4-6 | order 订单编排（含状态机/主链路/死信队列/定时任务） | 95 | 13 | 3 天 |
| 5 | pay 支付回调 | 32 | 6 | 1 天 |
| 7 | stock + coupon 库存与优惠券 | 20 + 65 | 7 | 1.5 天 |
| 8 | user + gateway 用户鉴权与网关 | 156 + 20 | 7 | 1.5 天 |
| 9 | agent AI 客服（Python） | ~107 | ~15 | 2.5 天 |
| 10 | product + search + cart + admin | 77 + 41 + 21 + 47 | 5 | 1 天 |
| 配 | XML 配置（pom + Mapper SQL） | 65 | — | 半天 |
| | **总计** | **~895** | **~85** | **约 14 天**（每天 6h） |

> 全量精读（含薄文件）约 90–110 小时。纯必读⭐约 45 小时。

---

## 10 步路线骨架索引

| 步 | 目标 | 对应章节 |
|---|---|---|
| 0 | 建立全局认知 | 第 0 步 |
| 1 | 公共底座 | 第 1 步 common |
| 2 | 状态字典+状态机 | 第 2 步 order（2.1 状态机子节） |
| 3 | 下单主链路 | 第 2 步 order（2.1 主链路子节） |
| 4 | 死信队列消费者 | 第 2 步 order（2.1 死信队列子节） |
| 5 | 支付回调链路 | 第 5 步 pay |
| 6 | 分布式定时任务 | 第 2 步 order（2.1 定时任务子节） |
| 7 | 库存与优惠券 | 第 7 步 stock + coupon |
| 8 | 用户鉴权与网关 | 第 8 步 user + gateway |
| 9 | AI 智能客服 | 第 9 步 agent |
| 10 | 商品/搜索/购物车/后台 | 第 10 步 product + search + cart + admin |

---

# 第 0 步：全局认知（文档，4 个）

> 纯中文文档，零代码门槛。读完建立全局心智再碰源码。

- [ ] ⭐⭐ `README.md` — 项目门面：双主线（商城+AI客服）、架构图、技术栈表、各微服务职责表（30 分钟）
- [ ] ⭐⭐ `ZHIDAO.md` — 项目导读（10 章）：重点读第 1/2/4 章——项目是什么、8 核心概念、运行流程全景图（1 小时）
- [ ] ⭐ `Description.md` — 项目名片：含金量机制清单，每机制解决什么问题（5 分钟）
- [ ] ⭐ `deploy/GO_LIVE.md` — 部署清单：中间件列表、SQL 初始化顺序、启动顺序、冒烟测试（30 分钟）

---

# 第 1 步：common 公共底座（149 文件）⭐⭐

> 所有模块都依赖 common——先读懂它，后面读任何模块都不会卡。
> **模块路径**：`Simlect-backend/Simlect-common/`（单模块，路径前缀无 api/app）

## 1.1 必读 ⭐⭐ / ⭐（15 个，逐行精读）

- [ ] ⭐⭐ `src/main/java/com/simlect/component/RedisComponent.java` — **核心**：Token 存储/验证码/秒杀 Lua 预占/生命周期锁/支付单关单标记/退款幂等闸（1 小时）
- [ ] ⭐⭐ `src/main/java/com/simlect/constants/RabbitMQConfig.java` — 所有交换机/队列/死信/路由键定义——MQ 全景图（30 分钟）
- [ ] ⭐ `src/main/java/com/simlect/constants/ReliableMessageSender.java` — Outbox 模式核心：DB 与 MQ 一致性，同库同事务写消息表后异步投递（30 分钟）
- [ ] ⭐ `src/main/java/com/simlect/constants/TransactionalMqSender.java` — `sendAfterCommit` 挂事务提交回调，延迟关单/物流消息靠它发（20 分钟）
- [ ] ⭐ `src/main/java/com/simlect/component/MqListenerHelper.java` — 消费幂等表抢锁/nack 重试死信/释放幂等锁（30 分钟）
- [ ] ⭐ `src/main/java/com/simlect/component/RemoteCompensateRecorder.java` — Feign 补偿失败落库记录，等人工兜底（15 分钟）
- [ ] ⭐ `src/main/java/com/simlect/component/MqConsumerIdempotencyHelper.java` — MQ 消费幂等核心逻辑（20 分钟）
- [ ] ⭐ `src/main/java/com/simlect/component/MqIdempotencyGuard.java` — 幂等闸门守卫（15 分钟）
- [ ] ⭐ `src/main/java/com/simlect/component/MqPublisherConfirmHelper.java` — publisher confirm 机制，保 MQ 必达（15 分钟）
- [ ] ⭐ `src/main/java/com/simlect/support/PayOrderLifecycleLockHolder.java` — 支付单生命周期锁封装（15 分钟）
- [ ] ⭐ `src/main/java/com/simlect/support/MqIdempotencyKeys.java` — 幂等表操作封装（10 分钟）
- [ ] ⭐ `src/main/java/com/simlect/entity/vo/ResponseVO.java` — 统一响应格式（code/msg/data）（5 分钟）
- [ ] ⭐ `src/main/java/com/simlect/exception/BusinessException.java` — 统一业务异常，携带错误码和消息（10 分钟）
- [ ] ⭐ `src/main/java/com/simlect/entity/dto/TokenUserInfoDTO.java` — Redis 登录态载体（userId/email/nickName/token）（10 分钟）
- [ ] ⭐ `src/main/java/com/simlect/utils/StringTools.java` — 订单号生成/支付单号生成/日期格式化/随机数（30 分钟）

## 1.2 选读 ·（约 35 个）

### 1.2.1 组件 / Component

- [ ] · `src/main/java/com/simlect/component/MqCompensationStore.java` — MQ 补偿记录存储
- [ ] · `src/main/java/com/simlect/component/MqConsumeFailureRecorder.java` — 消费失败记录器
- [ ] · `src/main/java/com/simlect/component/CouponRushRateLimitService.java` — 秒杀限流服务
- [ ] · `src/main/java/com/simlect/component/UserLoginLockService.java` — IP 维度失败计数 + 锁定
- [ ] · `src/main/java/com/simlect/component/SensitiveWordFilter.java` — 敏感词过滤
- [ ] · `src/main/java/com/simlect/component/SpringContext.java` — Spring 上下文持有器

### 1.2.2 配置 / Config

- [ ] · `src/main/java/com/simlect/entity/config/AppConfig.java` — 应用配置属性绑定
- [ ] · `src/main/java/com/simlect/config/CaptchaRedisConfiguration.java` — 验证码 Redis 配置
- [ ] · `src/main/java/com/simlect/config/CouponCacheExecutorConfig.java` — 优惠券缓存线程池配置
- [ ] · `src/main/java/com/simlect/config/FeignSentinelRulesConfig.java` — Feign 接 Sentinel 规则
- [ ] · `src/main/java/com/simlect/config/MqAsyncConfiguration.java` — MQ 异步配置
- [ ] · `src/main/java/com/simlect/config/OkHttpConfig.java` — OkHttp 客户端配置
- [ ] · `src/main/java/com/simlect/config/ProductionSafetyValidator.java` — 生产就绪校验（SIMLECT_PRODUCTION_READY）
- [ ] · `src/main/java/com/simlect/config/SimlectLoadBalancerAutoConfiguration.java` — 负载均衡自动配置
- [ ] · `src/main/java/com/simlect/config/SimlectLoadBalancerClientConfiguration.java` — 负载均衡客户端配置

### 1.2.3 任务 / Service / Task

- [ ] · `src/main/java/com/simlect/task/OutboxDispatchTask.java` — Outbox 投递后台任务（轮询未投递消息重试）
- [ ] · `src/main/java/com/simlect/task/MqCompensationAutoReplayTask.java` — 补偿自动重放任务
- [ ] · `src/main/java/com/simlect/service/PasswordService.java` — bcrypt 加密/校验/老密码格式升级
- [ ] · `src/main/java/com/simlect/service/SlideCaptchaVerifier.java` — 滑动验证码校验
- [ ] · `src/main/java/com/simlect/service/SlideCaptchaTokenCleaner.java` — 滑动验证码 token 清理
- [ ] · `src/main/java/com/simlect/service/MqCompensationLogService.java` — 补偿日志服务接口
- [ ] · `src/main/java/com/simlect/service/OutboxMessageService.java` — Outbox 消息服务接口
- [ ] · `src/main/java/com/simlect/service/impl/MqCompensationLogServiceImpl.java` — 补偿日志实现
- [ ] · `src/main/java/com/simlect/service/impl/OutboxMessageServiceImpl.java` — Outbox 消息实现

### 1.2.4 控制器 / 异常 / 切面 / Web

- [ ] · `src/main/java/com/simlect/controller/ABaseController.java` — 基础控制器
- [ ] · `src/main/java/com/simlect/controller/admin/ABaseController.java` — 管理端基础控制器
- [ ] · `src/main/java/com/simlect/controller/AGlobalExceptionHandlerController.java` — 全局异常处理器（@RestControllerAdvice）
- [ ] · `src/main/java/com/simlect/exception/PayOrderLifecycleBusyException.java` — 支付单生命周期锁忙异常
- [ ] · `src/main/java/com/simlect/aspect/GlobalOperationAspect.java` — 全局操作切面
- [ ] · `src/main/java/com/simlect/aspect/RateLimitAspect.java` — 限流切面（@RateLimit）
- [ ] · `src/main/java/com/simlect/aspect/AdminSensitiveConfirmAspect.java` — 管理端敏感操作确认切面
- [ ] · `src/main/java/com/simlect/web/InternalApiAuthFilter.java` — 服务间内部 API 鉴权过滤器
- [ ] · `src/main/java/com/simlect/web/TraceIdFilter.java` — 链路追踪 ID 过滤器
- [ ] · `src/main/java/com/simlect/interceptor/WebAppConfigurer.java` — Web 拦截器注册
- [ ] · `src/main/java/com/simlect/interceptor/AppInterceptor.java` — 应用拦截器

### 1.2.5 Feign 基础设施 / 补偿 / 工具

- [ ] · `src/main/java/com/simlect/api/feign/FeignInternalAuthInterceptor.java` — Feign 服务间鉴权拦截器
- [ ] · `src/main/java/com/simlect/api/feign/FeignTraceInterceptor.java` — Feign 链路追踪拦截器
- [ ] · `src/main/java/com/simlect/api/feign/SimlectFeignConfiguration.java` — Feign 配置
- [ ] · `src/main/java/com/simlect/api/feign/SimlectFeignErrorDecoder.java` — Feign 错误解码器
- [ ] · `src/main/java/com/simlect/api/support/FeignFallbackResponses.java` — Fallback 响应工具
- [ ] · `src/main/java/com/simlect/api/support/FeignResponseSupport.java` — Feign 响应工具
- [ ] · `src/main/java/com/simlect/compensation/StockBatchCompensatePort.java` — 库存批量补偿端口
- [ ] · `src/main/java/com/simlect/compensation/UserCouponStatusCompensatePort.java` — 用户券状态补偿端口
- [ ] · `src/main/java/com/simlect/captcha/CaptchaCacheServiceRedisImpl.java` — 验证码 Redis 缓存实现
- [ ] · `src/main/java/com/simlect/utils/OrderPayAmountUtil.java` — 退款金额按实付比例分摊计算
- [ ] · `src/main/java/com/simlect/utils/AuthCookieHelper.java` — HttpOnly Cookie 工具
- [ ] · `src/main/java/com/simlect/utils/CheckCodeGenerator.java` — 验证码生成器
- [ ] · `src/main/java/com/simlect/utils/ImageCompressUtils.java` — 图片压缩
- [ ] · `src/main/java/com/simlect/utils/FileUtils.java` — 文件工具
- [ ] · `src/main/java/com/simlect/utils/JsonUtils.java` — JSON 工具
- [ ] · `src/main/java/com/simlect/utils/DateUtil.java` — 日期工具
- [ ] · `src/main/java/com/simlect/utils/IpUtils.java` — IP 工具
- [ ] · `src/main/java/com/simlect/utils/ProcessUtils.java` — 进程工具
- [ ] · `src/main/java/com/simlect/utils/CollectionCompare.java` — 集合比较工具
- [ ] · `src/main/java/com/simlect/support/MqConsumeReplayRouter.java` — 消费补偿重放路由
- [ ] · `src/main/java/com/simlect/constants/InternalApiHeaders.java` — 内部 API Header 常量
- [ ] · `src/main/java/com/simlect/constants/RabbitMqPersistenceConfig.java` — MQ 持久化配置
- [ ] · `src/main/java/com/simlect/constants/Constants.java` — 通用常量

## 1.3 薄文件 ○（约 50 个）

### 1.3.1 注解 / Annotation

- [ ] ○ `src/main/java/com/simlect/annotation/GlobalInterceptor.java` — 全局拦截器注解
- [ ] ○ `src/main/java/com/simlect/annotation/AdminSensitiveConfirm.java` — 管理端敏感确认注解
- [ ] ○ `src/main/java/com/simlect/annotation/CouponRushRateLimit.java` — 秒杀限流注解
- [ ] ○ `src/main/java/com/simlect/annotation/RateLimit.java` — 限流注解
- [ ] ○ `src/main/java/com/simlect/valid/Create.java` — 校验组-创建
- [ ] ○ `src/main/java/com/simlect/valid/Update.java` — 校验组-更新

### 1.3.2 PO 实体

- [ ] ○ `src/main/java/com/simlect/entity/po/LocalMessageOutbox.java` — Outbox 本地消息表实体
- [ ] ○ `src/main/java/com/simlect/entity/po/MqCompensationLog.java` — MQ 补偿日志实体
- [ ] ○ `src/main/java/com/simlect/entity/po/ProductItem.java` — 商品项实体

### 1.3.3 DTO / VO

- [ ] ○ `src/main/java/com/simlect/entity/dto/LogisticsSendDTO.java` — 物流发货 DTO
- [ ] ○ `src/main/java/com/simlect/entity/dto/MessageSendDTO.java` — 消息发送 DTO
- [ ] ○ `src/main/java/com/simlect/entity/dto/MqCompensationRecord.java` — MQ 补偿记录 DTO
- [ ] ○ `src/main/java/com/simlect/entity/dto/RagDataDTO.java` — RAG 数据 DTO
- [ ] ○ `src/main/java/com/simlect/entity/dto/SensitiveWordCacheItem.java` — 敏感词缓存项
- [ ] ○ `src/main/java/com/simlect/entity/dto/SignRewardConfigDTO.java` — 签到奖励配置 DTO
- [ ] ○ `src/main/java/com/simlect/entity/dto/MemberLevelRewardConfigDTO.java` — 会员等级奖励配置 DTO
- [ ] ○ `src/main/java/com/simlect/entity/dto/UserLocationCoordsDTO.java` — 用户定位坐标 DTO
- [ ] ○ `src/main/java/com/simlect/entity/vo/CheckCodeVO.java` — 验证码 VO
- [ ] ○ `src/main/java/com/simlect/entity/vo/PaginationResultVO.java` — 分页结果 VO
- [ ] ○ `src/main/java/com/simlect/entity/vo/RagSyncFailureVO.java` — RAG 同步失败 VO
- [ ] ○ `src/main/java/com/simlect/api/vo/ServiceHealthVO.java` — 服务健康 VO

### 1.3.4 Enum / Query / Mapper 接口 / Redis 配置

- [ ] ○ `src/main/java/com/simlect/entity/enums/ResponseCodeEnum.java` — 响应码枚举
- [ ] ○ `src/main/java/com/simlect/entity/enums/DateTimePatternEnum.java` — 日期格式枚举
- [ ] ○ `src/main/java/com/simlect/entity/enums/MessageReliabilityLevelEnum.java` — 消息可靠性等级
- [ ] ○ `src/main/java/com/simlect/entity/enums/MqCompensationLogStatusEnum.java` — 补偿日志状态
- [ ] ○ `src/main/java/com/simlect/entity/enums/OutboxMessageStatusEnum.java` — Outbox 消息状态
- [ ] ○ `src/main/java/com/simlect/entity/enums/PageSize.java` — 分页大小枚举
- [ ] ○ `src/main/java/com/simlect/entity/enums/PromptTypeEnum.java` — Prompt 类型枚举
- [ ] ○ `src/main/java/com/simlect/entity/enums/RagDataTypeEnum.java` — RAG 数据类型枚举
- [ ] ○ `src/main/java/com/simlect/entity/enums/StatisticsDataTypeEnum.java` — 统计数据类型枚举
- [ ] ○ `src/main/java/com/simlect/entity/query/BaseParam.java` — 基础查询参数
- [ ] ○ `src/main/java/com/simlect/entity/query/SimplePage.java` — 简单分页
- [ ] ○ `src/main/java/com/simlect/entity/query/MqCompensationLogQuery.java` — 补偿日志查询
- [ ] ○ `src/main/java/com/simlect/mappers/BaseMapper.java` — 基础 Mapper
- [ ] ○ `src/main/java/com/simlect/mappers/BaseMapperTableSplit.java` — 分表 Mapper 基类
- [ ] ○ `src/main/java/com/simlect/mappers/LocalMessageOutboxMapper.java` — Outbox Mapper
- [ ] ○ `src/main/java/com/simlect/mappers/MqCompensationLogMapper.java` — 补偿日志 Mapper
- [ ] ○ `src/main/java/com/simlect/redis/RedisConfig.java` — Redis 序列化配置
- [ ] ○ `src/main/java/com/simlect/redis/RedisUtils.java` — Redis 工具
- [ ] ○ `src/main/java/com/simlect/redis/RedissionConfig.java` — Redisson 配置

### 1.3.5 Mapper XML（SQL）

- [ ] ○ `src/main/resources/com/simlect/mappers/LocalMessageOutboxMapper.xml` — Outbox SQL
- [ ] ○ `src/main/resources/com/simlect/mappers/MqCompensationLogMapper.xml` — 补偿日志 SQL

## 1.4 测试代码 T（35 个）

- [ ] T `src/test/java/com/simlect/component/RedisComponentTest.java`
- [ ] T `src/test/java/com/simlect/component/MqListenerHelperTest.java`
- [ ] T `src/test/java/com/simlect/component/MqConsumerIdempotencyHelperTest.java`
- [ ] T `src/test/java/com/simlect/component/MqIdempotencyGuardTest.java`
- [ ] T `src/test/java/com/simlect/component/MqPublisherConfirmHelperTest.java`
- [ ] T `src/test/java/com/simlect/component/MqConsumeFailureRecorderTest.java`
- [ ] T `src/test/java/com/simlect/component/MqCompensationStoreTest.java`
- [ ] T `src/test/java/com/simlect/component/CouponRushRateLimitServiceTest.java`
- [ ] T `src/test/java/com/simlect/component/RemoteCompensateRecorderTest.java`
- [ ] T `src/test/java/com/simlect/component/SensitiveWordFilterTest.java`
- [ ] T `src/test/java/com/simlect/component/PayOrderLifecycleLockIT.java` — 集成测试（IT）
- [ ] T `src/test/java/com/simlect/utils/StringToolsTest.java`
- [ ] T `src/test/java/com/simlect/utils/OrderPayAmountUtilTest.java`
- [ ] T `src/test/java/com/simlect/utils/DateUtilTest.java`
- [ ] T `src/test/java/com/simlect/utils/JsonUtilsTest.java`
- [ ] T `src/test/java/com/simlect/utils/IpUtilsTest.java`
- [ ] T `src/test/java/com/simlect/utils/ProcessUtilsTest.java`
- [ ] T `src/test/java/com/simlect/utils/CollectionCompareTest.java`
- [ ] T `src/test/java/com/simlect/utils/FileUtilsTest.java`
- [ ] T `src/test/java/com/simlect/utils/ImageCompressUtilsTest.java`
- [ ] T `src/test/java/com/simlect/utils/AuthCookieHelperTest.java`
- [ ] T `src/test/java/com/simlect/utils/CheckCodeGeneratorTest.java`
- [ ] T `src/test/java/com/simlect/entity/enums/ResponseCodeEnumTest.java`
- [ ] T `src/test/java/com/simlect/entity/enums/OutboxMessageStatusEnumTest.java`
- [ ] T `src/test/java/com/simlect/entity/enums/MqCompensationLogStatusEnumTest.java`
- [ ] T `src/test/java/com/simlect/entity/enums/MessageReliabilityLevelEnumTest.java`
- [ ] T `src/test/java/com/simlect/entity/enums/DateTimePatternEnumTest.java`
- [ ] T `src/test/java/com/simlect/entity/enums/PageSizeTest.java`
- [ ] T `src/test/java/com/simlect/entity/enums/PromptTypeEnumTest.java`
- [ ] T `src/test/java/com/simlect/entity/enums/RagDataTypeEnumTest.java`
- [ ] T `src/test/java/com/simlect/entity/enums/StatisticsDataTypeEnumTest.java`
- [ ] T `src/test/java/com/simlect/entity/query/BaseParamTest.java`
- [ ] T `src/test/java/com/simlect/support/MqConsumeReplayRouterTest.java`
- [ ] T `src/test/java/com/simlect/support/MqIdempotencyKeysTest.java`
- [ ] T `src/test/java/com/simlect/task/OutboxDispatchTaskTest.java`
- [ ] T `src/test/java/com/simlect/task/MqCompensationAutoReplayTaskTest.java`

## 第 1 步自检

- [ ] `RedisComponent` 里生命周期锁和退款幂等闸分别怎么用？
- [ ] Outbox 为什么能保"DB 提交即 MQ 必发"？
- [ ] `MqListenerHelper` 的消费幂等抢锁流程？
- [ ] `RemoteCompensateRecorder` 在三层容错里的位置？

---

# 第 2-4-6 步：order 订单编排中心（95 文件）⭐⭐⭐

> 整个项目的"主文"——交易编排中心，出度最高，一条线串起 stock/product/coupon/pay/MQ。
> **模块路径**：`Simlect-backend/Simlect-order/`（双模块：`api/` 对外契约 + `app/` 业务实现）
> 本模块跨 ZHIDAO 第 2/3/4/6 步：状态机 / 下单主链路 / 死信队列消费者 / 分布式定时任务。

## 2.1 必读 ⭐⭐ / ⭐（13 个，逐行精读）

> 按 ZHIDAO 子步骤分组。这是整个项目含金量最高的 13 个文件。

### 第 2 步：订单状态字典 + 状态机（3 个）

- [ ] ⭐ `app/src/main/java/com/simlect/state/OrderStateEvent.java` — 9 个触发事件枚举，驱动状态流转的"动作"（10 分钟）
- [ ] ⭐⭐ `app/src/main/java/com/simlect/state/OrderStateMachine.java` — **核心**：转换表 EnumMap + `transition` 带旧状态条件 UPDATE 防并发覆盖（30 分钟）
- [ ] ○ `api/src/main/java/com/simlect/api/enums/OrderStatusEnum.java` — 10 个订单状态枚举 + 中文描述，状态流转的"字典"（10 分钟）

### 第 3 步：下单主链路 OrderInfoServiceImpl（核心，1976 行）

- [ ] ⭐⭐⭐ `app/src/main/java/com/simlect/biz/impl/OrderInfoServiceImpl.java` — **整个项目主文**：`@GlobalTransactional` 下单 + Feign 调用链 + Outbox 投递 + catch 三层补偿兜底 + 秒杀预占 + 支付入账 + 关单 + 退款四重幂等闸 + 迟到支付退款（2 天）
- [ ] ⭐ `app/src/main/java/com/simlect/controller/OrderController.java` — 前台控制器：下单/取消/退款 HTTP 入口（15 分钟）
- [ ] ⭐ `app/src/main/java/com/simlect/controller/internal/OrderInternalController.java` — 内部 API：Feign 调用入口（15 分钟）
- [ ] ⭐ `app/src/main/java/com/simlect/controller/internal/OrderAgentInternalController.java` — Agent 内部 API：AI 客服查单入口（10 分钟）
- [ ] ⭐ `app/src/main/java/com/simlect/biz/impl/OrderLogisticsInfoServiceImpl.java` — 物流信息服务：链式延迟消息推进（20 分钟）

### 第 4 步：死信队列消费者（2 个）

- [ ] ⭐⭐ `app/src/main/java/com/simlect/component/RabbitMQPayOrderDeadListenerComponent.java` — **核心**：三类超时事件消费 + `registerAckSync` 事务后 ack + AOP 代理调事务方法（1 小时）
- [ ] ⭐ `app/src/main/java/com/simlect/component/RabbitMQOrderListenerComponent.java` — 秒杀订单异步建单 + 幂等校验（15 分钟）

### 第 6 步：分布式定时任务（1 个）

- [ ] ⭐ `app/src/main/java/com/simlect/task/PayOrderTask.java` — `@Scheduled` + SET NX EX + Lua 释放 + 时间窗 10 分钟 + 分页 3 页（30 分钟）

### 第 3 步配套：订单金额工具

- [ ] ⭐ `app/src/main/java/com/simlect/utils/OrderListPayAmountHelper.java` — 订单列表实付金额计算辅助（15 分钟）

## 2.2 选读 ·（约 14 个）

### 2.2.1 Service 接口与实现

- [ ] · `app/src/main/java/com/simlect/biz/OrderInfoService.java` — 订单服务接口
- [ ] · `app/src/main/java/com/simlect/biz/OrderInternalService.java` — 内部订单服务接口
- [ ] · `app/src/main/java/com/simlect/biz/OrderItemService.java` — 订单项服务接口
- [ ] · `app/src/main/java/com/simlect/biz/OrderCommentService.java` — 订单评论服务接口
- [ ] · `app/src/main/java/com/simlect/biz/CommentReportService.java` — 评论举报服务接口
- [ ] · `app/src/main/java/com/simlect/biz/OrderLogisticsInfoService.java` — 物流信息服务接口
- [ ] · `app/src/main/java/com/simlect/biz/OrderLogisticsInfoRecordService.java` — 物流记录服务接口
- [ ] · `app/src/main/java/com/simlect/biz/impl/OrderItemServiceImpl.java` — 订单项实现
- [ ] · `app/src/main/java/com/simlect/biz/impl/OrderCommentServiceImpl.java` — 评论实现
- [ ] · `app/src/main/java/com/simlect/biz/impl/CommentReportServiceImpl.java` — 举报实现
- [ ] · `app/src/main/java/com/simlect/biz/impl/OrderLogisticsInfoRecordServiceImpl.java` — 物流记录实现

### 2.2.2 管理端 / 内部控制器

- [ ] · `app/src/main/java/com/simlect/controller/admin/OrderController.java` — 管理端订单控制器
- [ ] · `app/src/main/java/com/simlect/controller/admin/OrderInfoController.java` — 管理端订单信息控制器
- [ ] · `app/src/main/java/com/simlect/controller/admin/OrderItemController.java` — 管理端订单项控制器
- [ ] · `app/src/main/java/com/simlect/controller/admin/OrderLogisticsInfoController.java` — 管理端物流控制器
- [ ] · `app/src/main/java/com/simlect/controller/admin/OrderLogisticsInfoRecordController.java` — 管理端物流记录控制器
- [ ] · `app/src/main/java/com/simlect/controller/admin/OrderCommentController.java` — 管理端评论控制器
- [ ] · `app/src/main/java/com/simlect/controller/admin/CommentReportController.java` — 管理端举报控制器
- [ ] · `app/src/main/java/com/simlect/controller/OrderCommentController.java` — 前台评论控制器
- [ ] · `app/src/main/java/com/simlect/controller/CommentReportController.java` — 前台举报控制器
- [ ] · `app/src/main/java/com/simlect/cloud/OrderApplication.java` — 订单服务启动类

## 2.3 薄文件 ○（约 47 个）

### 2.3.1 api 模块契约（FeignClient + DTO + VO + Enum + Fallback）

- [ ] ○ `api/src/main/java/com/simlect/api/OrderFeignClient.java` — 订单 Feign 契约 + 方法注释
- [ ] ○ `api/src/main/java/com/simlect/api/fallback/OrderFeignFallbackFactory.java` — 降级工厂：下游挂了明确失败不 NPE
- [ ] ○ `api/src/main/java/com/simlect/api/support/OrderFeignSupport.java` — Feign 支持工具
- [ ] ○ `api/src/main/java/com/simlect/api/dto/PostOrderDTO.java` — 下单 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/OrderIdDTO.java` — 订单 ID DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/OrderStatusDTO.java` — 订单状态 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/OrderStatsRangeDTO.java` — 订单统计区间 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/PayOrderMessageDTO.java` — 支付订单消息 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/CouponRushPayRequestDTO.java` — 秒杀支付请求 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/CouponRushPrepareRequestDTO.java` — 秒杀预占请求 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/enums/CommentStatusEnum.java` — 评论状态枚举
- [ ] ○ `api/src/main/java/com/simlect/api/enums/LogisticsStatusEnum.java` — 物流状态枚举
- [ ] ○ `api/src/main/java/com/simlect/api/enums/OrderCommentStatusEnum.java` — 订单评论状态枚举
- [ ] ○ `api/src/main/java/com/simlect/api/enums/OrderFromTypeEnum.java` — 订单来源类型枚举
- [ ] ○ `api/src/main/java/com/simlect/api/enums/OrderItemStatusEnum.java` — 订单项状态枚举
- [ ] ○ `api/src/main/java/com/simlect/api/vo/OrderBriefVO.java` — 订单简要 VO
- [ ] ○ `api/src/main/java/com/simlect/api/vo/OrderCountVO.java` — 订单计数 VO
- [ ] ○ `api/src/main/java/com/simlect/api/vo/OrderDailyStatsVO.java` — 订单日统计 VO
- [ ] ○ `api/src/main/java/com/simlect/api/vo/OrderRangeStatsVO.java` — 订单区间统计 VO
- [ ] ○ `api/src/main/java/com/simlect/api/vo/ProductCommentStatsVO.java` — 商品评论统计 VO

### 2.3.2 app 模块 PO 实体 / Query / Mapper 接口

- [ ] ○ `app/src/main/java/com/simlect/entity/po/OrderInfo.java` — 订单主表实体
- [ ] ○ `app/src/main/java/com/simlect/entity/po/OrderItem.java` — 订单项实体
- [ ] ○ `app/src/main/java/com/simlect/entity/po/OrderCouponRel.java` — 订单优惠券关联实体
- [ ] ○ `app/src/main/java/com/simlect/entity/po/OrderLogisticsInfo.java` — 物流信息实体
- [ ] ○ `app/src/main/java/com/simlect/entity/po/OrderLogisticsInfoRecord.java` — 物流记录实体
- [ ] ○ `app/src/main/java/com/simlect/entity/po/OrderComment.java` — 订单评论实体
- [ ] ○ `app/src/main/java/com/simlect/entity/po/CommentReport.java` — 评论举报实体
- [ ] ○ `app/src/main/java/com/simlect/entity/query/OrderInfoQuery.java` — 订单查询
- [ ] ○ `app/src/main/java/com/simlect/entity/query/OrderItemQuery.java` — 订单项查询
- [ ] ○ `app/src/main/java/com/simlect/entity/query/OrderCouponRelQuery.java` — 订单券查询
- [ ] ○ `app/src/main/java/com/simlect/entity/query/OrderLogisticsInfoQuery.java` — 物流查询
- [ ] ○ `app/src/main/java/com/simlect/entity/query/OrderLogisticsInfoRecordQuery.java` — 物流记录查询
- [ ] ○ `app/src/main/java/com/simlect/entity/query/OrderCommentQuery.java` — 评论查询
- [ ] ○ `app/src/main/java/com/simlect/entity/query/CommentReportQuery.java` — 举报查询
- [ ] ○ `app/src/main/java/com/simlect/mappers/OrderInfoMapper.java` — 订单 Mapper
- [ ] ○ `app/src/main/java/com/simlect/mappers/OrderItemMapper.java` — 订单项 Mapper
- [ ] ○ `app/src/main/java/com/simlect/mappers/OrderCouponRelMapper.java` — 订单券 Mapper
- [ ] ○ `app/src/main/java/com/simlect/mappers/OrderLogisticsInfoMapper.java` — 物流 Mapper
- [ ] ○ `app/src/main/java/com/simlect/mappers/OrderLogisticsInfoRecordMapper.java` — 物流记录 Mapper
- [ ] ○ `app/src/main/java/com/simlect/mappers/OrderCommentMapper.java` — 评论 Mapper
- [ ] ○ `app/src/main/java/com/simlect/mappers/CommentReportMapper.java` — 举报 Mapper

### 2.3.3 Mapper XML（SQL）

- [ ] ○ `app/src/main/resources/com/simlect/mappers/OrderInfoMapper.xml`
- [ ] ○ `app/src/main/resources/com/simlect/mappers/OrderItemMapper.xml`
- [ ] ○ `app/src/main/resources/com/simlect/mappers/OrderCouponRelMapper.xml`
- [ ] ○ `app/src/main/resources/com/simlect/mappers/OrderLogisticsInfoMapper.xml`
- [ ] ○ `app/src/main/resources/com/simlect/mappers/OrderLogisticsInfoRecordMapper.xml`
- [ ] ○ `app/src/main/resources/com/simlect/mappers/OrderCommentMapper.xml`
- [ ] ○ `app/src/main/resources/com/simlect/mappers/CommentReportMapper.xml`

## 2.4 测试代码 T（22 个）

- [ ] T `app/src/test/java/com/simlect/OrderModuleSmokeTest.java`
- [ ] T `app/src/test/java/com/simlect/api/enums/OrderEnumsTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/OrderInternalServiceTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/CommentReportServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/OrderCommentServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/OrderInfoServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/OrderItemServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/OrderLogisticsInfoRecordServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/OrderLogisticsInfoServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/component/RabbitMQOrderListenerComponentTest.java`
- [ ] T `app/src/test/java/com/simlect/component/RabbitMQPayOrderDeadListenerComponentTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/CommentReportControllerTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/OrderCommentControllerTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/OrderControllerTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/admin/AdminCrudControllersTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/admin/AdminOrderControllerTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/internal/OrderAgentInternalControllerTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/internal/OrderInternalControllerTest.java`
- [ ] T `app/src/test/java/com/simlect/state/OrderStateMachineTest.java`
- [ ] T `app/src/test/java/com/simlect/task/PayOrderTaskTest.java`
- [ ] T `app/src/test/java/com/simlect/utils/OrderListPayAmountHelperTest.java`

## 第 2-4-6 步自检

- [ ] 状态机的 `transition` 方法为什么用 `WHERE status=期望态` 条件 UPDATE？不这样会怎样？
- [ ] `postOrder` 里 catch 块的三层补偿是什么？为什么不能只靠 Seata？
- [ ] `registerAckSync` 为什么把 ack 挂到事务后？不挂会怎样？
- [ ] 多实例部署时 PayOrderTask 怎么保证只有一个执行？锁过期了怎么办？

---

# 第 5 步：pay 支付回调（32 文件）⭐⭐

> 支付安全三件套——验签防伪造、金额校验防篡改、幂等闸防重复退款。
> **模块路径**：`Simlect-backend/Simlect-pay/`（双模块：`api/` + `app/`）

## 5.1 必读 ⭐⭐ / ⭐（6 个，逐行精读）

- [ ] ⭐⭐ `app/src/main/java/com/simlect/biz/impl/PayChannel4AliPay.java` — **核心**：支付宝渠道——验签（rsaCertCheckV2）/实付金额校验/退款幂等闸（out_request_no Redis SET NX EX 7 天）/查单/关单（1 小时）
- [ ] ⭐ `app/src/main/java/com/simlect/controller/PayNotifyController.java` — 回调入口：Redisson tryLock 看门狗防重复回调 + Feign 调 paySuccess（30 分钟）
- [ ] ⭐ `app/src/main/java/com/simlect/cloud/PayApplication.java` — 支付服务启动类（5 分钟）
- [ ] ⭐ `api/src/main/java/com/simlect/api/PayFeignClient.java` — 支付 Feign 契约 + 方法注释（10 分钟）
- [ ] ⭐ `api/src/main/java/com/simlect/api/fallback/PayFeignFallbackFactory.java` — 降级工厂：下游挂了明确失败不 NPE（15 分钟）
- [ ] ⭐ `app/src/main/java/com/simlect/biz/PayChannel.java` — 支付渠道接口（多渠道抽象）（10 分钟）

## 5.2 选读 ·（5 个）

- [ ] · `app/src/main/java/com/simlect/biz/PayInternalService.java` — 支付内部服务接口
- [ ] · `app/src/main/java/com/simlect/biz/PayTradeRecordService.java` — 交易记录服务接口
- [ ] · `app/src/main/java/com/simlect/biz/impl/PayTradeRecordServiceImpl.java` — 交易记录实现
- [ ] · `app/src/main/java/com/simlect/controller/PayTradeRecordController.java` — 交易记录控制器
- [ ] · `app/src/main/java/com/simlect/controller/internal/PayInternalController.java` — 内部支付 API

## 5.3 薄文件 ○（14 个）

- [ ] ○ `api/src/main/java/com/simlect/api/dto/PayInfoDTO.java` — 支付信息 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/PayUrlRequestDTO.java` — 支付链接请求 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/PayQueryDTO.java` — 支付查询 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/PayCloseDTO.java` — 支付关闭 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/PayRefundDTO.java` — 退款 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/PayOrderNotifyDTO.java` — 支付通知 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/PayTradeCreateDTO.java` — 交易创建 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/PayTradeMarkDTO.java` — 交易标记 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/PayTradeStatusDTO.java` — 交易状态 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/enums/PayChannelEnum.java` — 支付渠道枚举
- [ ] ○ `api/src/main/java/com/simlect/api/support/PayFeignSupport.java` — Feign 支持工具
- [ ] ○ `app/src/main/java/com/simlect/entity/po/PayTradeRecord.java` — 交易记录实体
- [ ] ○ `app/src/main/java/com/simlect/entity/query/PayTradeRecordQuery.java` — 交易记录查询
- [ ] ○ `app/src/main/java/com/simlect/mappers/PayTradeRecordMapper.java` — 交易记录 Mapper
- [ ] ○ `app/src/main/resources/com/simlect/mappers/PayTradeRecordMapper.xml` — 交易记录 SQL

## 5.4 测试代码 T（7 个）

- [ ] T `app/src/test/java/com/simlect/api/enums/PayChannelEnumTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/PayInternalServiceTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/PayChannel4AliPayTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/PayTradeRecordServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/PayNotifyControllerTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/PayTradeRecordControllerTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/internal/PayInternalControllerTest.java`

## 第 5 步自检

- [ ] 支付宝回调验签失败会怎样？退款幂等闸失败后怎么清？
- [ ] Redisson 看门狗锁的续期机制？

---

# 第 7 步：stock 库存（20）+ coupon 优惠券（65）⭐

> 被 order Feign 调的下游——理解热路径隔离和库存幂等扣补。
> **模块路径**：`Simlect-backend/Simlect-stock/` 和 `Simlect-backend/Simlect-coupon/`（均双模块）

## 7.1 stock 必读 ⭐（2 个）

- [ ] ⭐ `app/src/main/java/com/simlect/cloud/StockApplication.java` — 库存服务启动类（5 分钟）
- [ ] ⭐ `api/src/main/java/com/simlect/api/StockFeignClient.java` — 库存 Feign 契约（10 分钟）

## 7.2 stock 选读 ·（3 个）

- [ ] · `app/src/main/java/com/simlect/biz/SkuStockService.java` — SKU 库存服务（扣减/回补，幂等 operationId 防重复扣补）（30 分钟）
- [ ] · `app/src/main/java/com/simlect/controller/StockController.java` — 库存控制器
- [ ] · `app/src/main/java/com/simlect/controller/internal/StockInternalController.java` — 内部库存 API

## 7.3 stock 薄文件 ○（12 个）

- [ ] ○ `api/src/main/java/com/simlect/api/fallback/StockFeignFallbackFactory.java` — 降级工厂
- [ ] ○ `api/src/main/java/com/simlect/api/support/StockFeignSupport.java` — Feign 支持
- [ ] ○ `api/src/main/java/com/simlect/api/dto/SkuStockDTO.java` — SKU 库存 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/SkuStockChangeDTO.java` — 库存变更 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/SkuStockBatchChangeDTO.java` — 批量变更 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/SkuStockSetDTO.java` — 库存设置 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/SkuStockQueryDTO.java` — 库存查询 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/LessStockPageDTO.java` — 低库存分页 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/vo/ProductTotalStockVO.java` — 商品总库存 VO
- [ ] ○ `api/src/main/java/com/simlect/api/vo/StockChangeResultVO.java` — 库存变更结果 VO
- [ ] ○ `app/src/main/java/com/simlect/domain/SkuStock.java` — SKU 库存领域对象
- [ ] ○ `app/src/main/java/com/simlect/mappers/SkuStockMapper.java` — SKU 库存 Mapper
- [ ] ○ `app/src/main/resources/com/simlect/mappers/SkuStockMapper.xml` — SKU 库存 SQL

## 7.4 stock 测试 T（3 个）

- [ ] T `app/src/test/java/com/simlect/biz/SkuStockServiceTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/StockControllerTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/internal/StockInternalControllerTest.java`

## 7.5 coupon 必读 ⭐（5 个）

- [ ] ⭐⭐ `app/src/main/java/com/simlect/biz/impl/DiscountCouponServiceImpl.java` — **核心**：秒杀券 Redis Lua 预占 + DB 扣券库存管理（30 分钟）
- [ ] ⭐ `app/src/main/java/com/simlect/biz/impl/UserCouponServiceImpl.java` — 用户领券/用券/退券状态管理（20 分钟）
- [ ] ⭐ `app/src/main/java/com/simlect/aspect/CouponRushRateLimitAspect.java` — 秒杀限流切面：@Aspect IP 维度限流（15 分钟）
- [ ] ⭐ `app/src/main/java/com/simlect/component/RabbitMQOrderDeadListListenerComponent.java` — 秒杀超时未付释券死信消费（15 分钟）
- [ ] ⭐ `app/src/main/java/com/simlect/cloud/CouponApplication.java` — 优惠券服务启动类（5 分钟）

## 7.6 coupon 选读 ·（10 个）

- [ ] · `api/src/main/java/com/simlect/api/CouponFeignClient.java` — 优惠券 Feign 契约（10 分钟）
- [ ] · `app/src/main/java/com/simlect/biz/DiscountCouponService.java` — 优惠券服务接口
- [ ] · `app/src/main/java/com/simlect/biz/UserCouponService.java` — 用户券服务接口
- [ ] · `app/src/main/java/com/simlect/biz/CouponInternalService.java` — 内部券服务接口
- [ ] · `app/src/main/java/com/simlect/biz/CouponReminderService.java` — 券提醒服务接口
- [ ] · `app/src/main/java/com/simlect/biz/impl/CouponReminderServiceImpl.java` — 券提醒实现
- [ ] · `app/src/main/java/com/simlect/component/CouponRushStockService.java` — 秒杀券库存服务
- [ ] · `app/src/main/java/com/simlect/component/DiscountCouponCacheComponent.java` — 优惠券缓存组件
- [ ] · `app/src/main/java/com/simlect/component/SignStreakCouponListenerComponent.java` — 连签赠券监听
- [ ] · `app/src/main/java/com/simlect/task/CouponReminderTask.java` — 券提醒定时任务
- [ ] · `app/src/main/java/com/simlect/controller/DiscountCouponController.java` — 优惠券控制器
- [ ] · `app/src/main/java/com/simlect/controller/admin/DiscountCouponController.java` — 管理端券控制器
- [ ] · `app/src/main/java/com/simlect/controller/internal/CouponInternalController.java` — 内部券 API
- [ ] · `app/src/main/java/com/simlect/controller/internal/CouponAgentInternalController.java` — Agent 内部券 API

## 7.7 coupon 薄文件 ○（36 个）

- [ ] ○ `api/src/main/java/com/simlect/api/fallback/CouponFeignFallbackFactory.java` — 降级工厂
- [ ] ○ `api/src/main/java/com/simlect/api/support/CouponFeignSupport.java` — Feign 支持
- [ ] ○ `api/src/main/java/com/simlect/api/dto/DiscountCouponDTO.java` — 优惠券 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/CouponIdDTO.java` — 券 ID DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/CouponLogicalCacheEntry.java` — 券逻辑缓存条目
- [ ] ○ `api/src/main/java/com/simlect/api/dto/CouponRushOpsDTO.java` — 秒杀操作 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/CouponRushPrepareDTO.java` — 秒杀预占 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/CouponRushStockReconcileDTO.java` — 秒杀库存对账 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/CouponValidateAndLockDTO.java` — 券校验锁定 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/RushingCouponMessageDTO.java` — 秒杀券消息 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/SignStreakCouponMessageDTO.java` — 连签赠券消息 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/UserCouponCreateDTO.java` — 用户券创建 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/UserCouponIdDTO.java` — 用户券 ID DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/UserCouponStatusChangeDTO.java` — 用户券状态变更 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/enums/CouponStatusEnum.java` — 券状态枚举
- [ ] ○ `api/src/main/java/com/simlect/api/enums/CouponTypeEnum.java` — 券类型枚举
- [ ] ○ `api/src/main/java/com/simlect/api/enums/RushingCouponStatusEnum.java` — 秒杀券状态枚举
- [ ] ○ `api/src/main/java/com/simlect/api/enums/RushingStatusEnum.java` — 秒杀状态枚举
- [ ] ○ `api/src/main/java/com/simlect/api/enums/UserCouponStatusEnum.java` — 用户券状态枚举
- [ ] ○ `api/src/main/java/com/simlect/api/vo/CouponBriefVO.java` — 券简要 VO
- [ ] ○ `api/src/main/java/com/simlect/api/vo/CouponLockResultVO.java` — 券锁定结果 VO
- [ ] ○ `api/src/main/java/com/simlect/api/vo/DiscountCouponVO.java` — 优惠券 VO
- [ ] ○ `api/src/main/java/com/simlect/api/vo/UserCouponVO.java` — 用户券 VO
- [ ] ○ `api/src/main/java/com/simlect/api/vo/UserCouponDetailVO.java` — 用户券详情 VO
- [ ] ○ `app/src/main/java/com/simlect/entity/po/DiscountCoupon.java` — 优惠券实体
- [ ] ○ `app/src/main/java/com/simlect/entity/po/UserCoupon.java` — 用户券实体
- [ ] ○ `app/src/main/java/com/simlect/entity/po/SignStreakCouponGrant.java` — 连签赠券实体
- [ ] ○ `app/src/main/java/com/simlect/entity/query/DiscountCouponQuery.java` — 优惠券查询
- [ ] ○ `app/src/main/java/com/simlect/entity/query/UserCouponQuery.java` — 用户券查询
- [ ] ○ `app/src/main/java/com/simlect/mappers/DiscountCouponMapper.java` — 优惠券 Mapper
- [ ] ○ `app/src/main/java/com/simlect/mappers/UserCouponMapper.java` — 用户券 Mapper
- [ ] ○ `app/src/main/java/com/simlect/mappers/SignStreakCouponGrantMapper.java` — 连签赠券 Mapper
- [ ] ○ `app/src/main/resources/com/simlect/mappers/DiscountCouponMapper.xml` — 优惠券 SQL
- [ ] ○ `app/src/main/resources/com/simlect/mappers/UserCouponMapper.xml` — 用户券 SQL
- [ ] ○ `app/src/main/resources/com/simlect/mappers/SignStreakCouponGrantMapper.xml` — 连签赠券 SQL

## 7.8 coupon 测试 T（15 个）

- [ ] T `app/src/test/java/com/simlect/aspect/CouponRushRateLimitAspectTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/CouponInternalServiceTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/CouponReminderServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/DiscountCouponServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/UserCouponServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/component/CouponRushStockServiceTest.java`
- [ ] T `app/src/test/java/com/simlect/component/DiscountCouponCacheComponentTest.java`
- [ ] T `app/src/test/java/com/simlect/component/RabbitMQOrderDeadListListenerComponentTest.java`
- [ ] T `app/src/test/java/com/simlect/component/SignStreakCouponListenerComponentTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/DiscountCouponControllerTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/admin/DiscountCouponControllerTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/internal/CouponAgentInternalControllerTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/internal/CouponInternalControllerTest.java`
- [ ] T `app/src/test/java/com/simlect/task/CouponReminderTaskTest.java`

## 第 7 步自检

- [ ] 秒杀券为什么先 Redis Lua 再 DB 扣减？Feign 重试时 rushCode==2 怎么处理？
- [ ] 库存扣减的 operationId 幂等机制？

---

# 第 8 步：user 用户鉴权（156）+ gateway 网关（20）⭐

> 登录安全链路——IP 锁定 + 验证码 + bcrypt + 老密码无感升级 + 封禁 + Token。
> **模块路径**：`Simlect-backend/Simlect-user/`（双模块）+ `Simlect-backend/Simlect-gateway/`（单模块）

## 8.1 user 必读 ⭐（6 个，逐行精读）

- [ ] ⭐⭐ `app/src/main/java/com/simlect/controller/AccountController.java` — **核心**：注册/登录/验证码/找回密码——登录安全链路（IP 锁定→验证码→bcrypt→老密码无感升级→封禁→Redis Token+HttpOnly Cookie）（1 小时）
- [ ] ⭐ `app/src/main/java/com/simlect/cloud/UserApplication.java` — 用户服务启动类（5 分钟）
- [ ] ⭐ `api/src/main/java/com/simlect/api/UserFeignClient.java` — 用户 Feign 契约（10 分钟）
- [ ] ⭐ `app/src/main/java/com/simlect/biz/impl/UserInfoServiceImpl.java` — 用户 CRUD + 注册逻辑（30 分钟）
- [ ] ⭐ `app/src/main/java/com/simlect/component/UserTempBanService.java` — 临时封禁 + 延时解禁（MQ 驱动）（20 分钟）
- [ ] ⭐ `app/src/main/java/com/simlect/component/AdminLoginLockService.java` — 管理端登录锁定

## 8.2 user 选读 ·（约 30 个）

### 8.2.1 鉴权 / 登录 / 文件 / 验证码

- [ ] · `api/src/main/java/com/simlect/api/AdminAuditFeignClient.java` — 管理审计 Feign 契约
- [ ] · `api/src/main/java/com/simlect/api/fallback/UserFeignFallbackFactory.java` — 用户降级工厂
- [ ] · `api/src/main/java/com/simlect/api/fallback/AdminAuditFeignFallbackFactory.java` — 审计降级工厂
- [ ] · `api/src/main/java/com/simlect/api/support/UserFeignSupport.java` — 用户 Feign 支持
- [ ] · `api/src/main/java/com/simlect/api/support/AdminAuditFeignSupport.java` — 审计 Feign 支持
- [ ] · `app/src/main/java/com/simlect/controller/FileController.java` — 文件上传控制器
- [ ] · `app/src/main/java/com/simlect/controller/SlideCaptchaController.java` — 滑动验证码控制器
- [ ] · `app/src/main/java/com/simlect/controller/LocationController.java` — 定位天气控制器
- [ ] · `app/src/main/java/com/simlect/controller/SignController.java` — 签到控制器
- [ ] · `app/src/main/java/com/simlect/controller/UserAddressController.java` — 收货地址控制器
- [ ] · `app/src/main/java/com/simlect/controller/UserBrowseController.java` — 浏览控制器
- [ ] · `app/src/main/java/com/simlect/controller/UserBrowseHistoryController.java` — 浏览历史控制器
- [ ] · `app/src/main/java/com/simlect/controller/UserFavoriteController.java` — 收藏控制器
- [ ] · `app/src/main/java/com/simlect/controller/UserMemberController.java` — 会员控制器
- [ ] · `app/src/main/java/com/simlect/controller/UserNotificationController.java` — 通知控制器
- [ ] · `app/src/main/java/com/simlect/controller/admin/AccountController.java` — 管理端账户控制器
- [ ] · `app/src/main/java/com/simlect/controller/admin/FileController.java` — 管理端文件控制器
- [ ] · `app/src/main/java/com/simlect/controller/admin/ImageModerationController.java` — 图片审核控制器
- [ ] · `app/src/main/java/com/simlect/controller/admin/MemberLevelRewardConfigController.java` — 会员奖励配置控制器
- [ ] · `app/src/main/java/com/simlect/controller/admin/SignRewardConfigController.java` — 签到奖励配置控制器
- [ ] · `app/src/main/java/com/simlect/controller/admin/UserAddressController.java` — 管理端地址控制器
- [ ] · `app/src/main/java/com/simlect/controller/admin/UserController.java` — 管理端用户控制器
- [ ] · `app/src/main/java/com/simlect/controller/admin/UserInfoController.java` — 管理端信息控制器
- [ ] · `app/src/main/java/com/simlect/controller/internal/UserAgentInternalController.java` — Agent 内部用户 API
- [ ] · `app/src/main/java/com/simlect/controller/internal/UserInternalController.java` — 内部用户 API

### 8.2.2 服务接口与实现

- [ ] · `app/src/main/java/com/simlect/biz/EmailService.java` — 邮件服务接口
- [ ] · `app/src/main/java/com/simlect/biz/impl/AliEmailServiceImpl.java` — 阿里邮件实现
- [ ] · `app/src/main/java/com/simlect/biz/ImageModerationService.java` — 图片审核服务接口
- [ ] · `app/src/main/java/com/simlect/biz/impl/ImageModerationServiceImpl.java` — 图片审核实现
- [ ] · `app/src/main/java/com/simlect/biz/LocationWeatherService.java` — 定位天气服务接口
- [ ] · `app/src/main/java/com/simlect/biz/impl/LocationWeatherServiceImpl.java` — 定位天气实现
- [ ] · `app/src/main/java/com/simlect/biz/MemberLevelRewardConfigService.java` — 会员奖励配置服务接口
- [ ] · `app/src/main/java/com/simlect/biz/impl/MemberLevelRewardConfigServiceImpl.java` — 会员奖励配置实现
- [ ] · `app/src/main/java/com/simlect/biz/SignRewardConfigService.java` — 签到奖励配置服务接口
- [ ] · `app/src/main/java/com/simlect/biz/impl/SignRewardConfigServiceImpl.java` — 签到奖励配置实现
- [ ] · `app/src/main/java/com/simlect/biz/SignService.java` — 签到服务接口
- [ ] · `app/src/main/java/com/simlect/biz/impl/SignServiceImpl.java` — 签到实现
- [ ] · `app/src/main/java/com/simlect/biz/SignStreakService.java` — 连签服务接口
- [ ] · `app/src/main/java/com/simlect/biz/impl/SignStreakServiceImpl.java` — 连签实现
- [ ] · `app/src/main/java/com/simlect/biz/SignSummaryService.java` — 签到汇总服务接口
- [ ] · `app/src/main/java/com/simlect/biz/impl/SignSummaryServiceImpl.java` — 签到汇总实现
- [ ] · `app/src/main/java/com/simlect/biz/impl/SignSummaryWriter.java` — 签到汇总写入器
- [ ] · `app/src/main/java/com/simlect/biz/UserAddressService.java` — 地址服务接口
- [ ] · `app/src/main/java/com/simlect/biz/impl/UserAddressServiceImpl.java` — 地址实现
- [ ] · `app/src/main/java/com/simlect/biz/UserBrowseHistoryService.java` — 浏览历史服务接口
- [ ] · `app/src/main/java/com/simlect/biz/impl/UserBrowseHistoryServiceImpl.java` — 浏览历史实现
- [ ] · `app/src/main/java/com/simlect/biz/UserInfoService.java` — 用户信息服务接口
- [ ] · `app/src/main/java/com/simlect/biz/UserInternalService.java` — 内部用户服务接口
- [ ] · `app/src/main/java/com/simlect/biz/UserMemberProfileService.java` — 会员档案服务接口
- [ ] · `app/src/main/java/com/simlect/biz/impl/UserMemberProfileServiceImpl.java` — 会员档案实现
- [ ] · `app/src/main/java/com/simlect/biz/UserNotificationService.java` — 通知服务接口
- [ ] · `app/src/main/java/com/simlect/biz/impl/UserNotificationServiceImpl.java` — 通知实现
- [ ] · `app/src/main/java/com/simlect/biz/UserProductFavoriteService.java` — 收藏服务接口
- [ ] · `app/src/main/java/com/simlect/biz/impl/UserProductFavoriteServiceImpl.java` — 收藏实现

### 8.2.3 组件 / 配置 / 任务

- [ ] · `app/src/main/java/com/simlect/component/BaiduImageCensorComponent.java` — 百度图片审核组件
- [ ] · `app/src/main/java/com/simlect/component/ImageCensorRateLimitService.java` — 图片审核限流
- [ ] · `app/src/main/java/com/simlect/component/NotifyPushPublisher.java` — 通知推送发布器
- [ ] · `app/src/main/java/com/simlect/component/RabbitMQBrowseListenerComponent.java` — 浏览异步监听
- [ ] · `app/src/main/java/com/simlect/component/RabbitMQNotifyDeadListenerComponent.java` — 通知死信监听
- [ ] · `app/src/main/java/com/simlect/component/RabbitMQNotifyListenerComponent.java` — 通知监听
- [ ] · `app/src/main/java/com/simlect/component/RabbitMQUserTempBanListenerComponent.java` — 封禁延时解禁监听
- [ ] · `app/src/main/java/com/simlect/config/BusinessTimeConfig.java` — 业务时间配置
- [ ] · `app/src/main/java/com/simlect/task/ImageModerationCleanupTask.java` — 图片审核清理任务

## 8.3 user 薄文件 ○（约 80 个）

### 8.3.1 api 模块契约（DTO/VO/Enum）

- [ ] ○ `api/src/main/java/com/simlect/api/dto/AdminAuditLogDTO.java` — 管理审计日志 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/BrowseHistoryMessageDTO.java` — 浏览历史消息 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/ImageUploadResultDTO.java` — 图片上传结果 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/NotificationMessageDTO.java` — 通知消息 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/SignRecordMessageDTO.java` — 签到记录消息 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/UserAddressQueryDTO.java` — 用户地址查询 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/UserGrowthAddDTO.java` — 用户成长值增加 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/UserIdDTO.java` — 用户 ID DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/UserIdsDTO.java` — 用户 ID 列表 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/UserJoinCountDTO.java` — 用户加入计数 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/UserNotifyDTO.java` — 用户通知 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/UserTempBanDTO.java` — 用户封禁 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/enums/ImageModerationSceneEnum.java` — 图片审核场景枚举
- [ ] ○ `api/src/main/java/com/simlect/api/enums/ImageModerationStatusEnum.java` — 图片审核状态枚举
- [ ] ○ `api/src/main/java/com/simlect/api/enums/UserSexEnum.java` — 用户性别枚举
- [ ] ○ `api/src/main/java/com/simlect/api/enums/UserStatusEnum.java` — 用户状态枚举
- [ ] ○ `api/src/main/java/com/simlect/api/vo/MemberLevelRewardVO.java` — 会员等级奖励 VO
- [ ] ○ `api/src/main/java/com/simlect/api/vo/SignDataVO.java` — 签到数据 VO
- [ ] ○ `api/src/main/java/com/simlect/api/vo/SignDateSyncResultVO.java` — 签到日期同步结果 VO
- [ ] ○ `api/src/main/java/com/simlect/api/vo/UserAddressVO.java` — 用户地址 VO
- [ ] ○ `api/src/main/java/com/simlect/api/vo/UserBriefVO.java` — 用户简要 VO
- [ ] ○ `api/src/main/java/com/simlect/api/vo/UserBrowseProductVO.java` — 用户浏览商品 VO
- [ ] ○ `api/src/main/java/com/simlect/api/vo/UserFavoriteProductVO.java` — 用户收藏商品 VO
- [ ] ○ `api/src/main/java/com/simlect/api/vo/UserVO.java` — 用户 VO

### 8.3.2 app 模块 PO / Query / Mapper 接口 / XML

- [ ] ○ `app/src/main/java/com/simlect/entity/dto/BaiduImageCensorResultDTO.java` — 百度审核结果 DTO
- [ ] ○ `app/src/main/java/com/simlect/entity/dto/SignSummarySnapshot.java` — 签到汇总快照
- [ ] ○ `app/src/main/java/com/simlect/entity/po/UserInfo.java` — 用户信息实体
- [ ] ○ `app/src/main/java/com/simlect/entity/po/UserAddress.java` — 用户地址实体
- [ ] ○ `app/src/main/java/com/simlect/entity/po/UserBrowseHistory.java` — 浏览历史实体
- [ ] ○ `app/src/main/java/com/simlect/entity/po/UserMemberProfile.java` — 会员档案实体
- [ ] ○ `app/src/main/java/com/simlect/entity/po/UserNotification.java` — 用户通知实体
- [ ] ○ `app/src/main/java/com/simlect/entity/po/UserProductFavorite.java` — 用户收藏实体
- [ ] ○ `app/src/main/java/com/simlect/entity/po/UserSignRecord.java` — 签到记录实体
- [ ] ○ `app/src/main/java/com/simlect/entity/po/UserSignRecordDetail.java` — 签到记录明细实体
- [ ] ○ `app/src/main/java/com/simlect/entity/po/UserSignSummary.java` — 签到汇总实体
- [ ] ○ `app/src/main/java/com/simlect/entity/po/SignBitmap.java` — 签到位图实体
- [ ] ○ `app/src/main/java/com/simlect/entity/po/ImageModerationRecord.java` — 图片审核记录实体
- [ ] ○ `app/src/main/java/com/simlect/entity/query/UserInfoQuery.java` — 用户信息查询
- [ ] ○ `app/src/main/java/com/simlect/entity/query/UserAddressQuery.java` — 地址查询
- [ ] ○ `app/src/main/java/com/simlect/entity/query/UserBrowseHistoryQuery.java` — 浏览历史查询
- [ ] ○ `app/src/main/java/com/simlect/entity/query/UserMemberProfileQuery.java` — 会员档案查询
- [ ] ○ `app/src/main/java/com/simlect/entity/query/UserNotificationQuery.java` — 通知查询
- [ ] ○ `app/src/main/java/com/simlect/entity/query/UserProductFavoriteQuery.java` — 收藏查询
- [ ] ○ `app/src/main/java/com/simlect/entity/query/UserSignRecordQuery.java` — 签到记录查询
- [ ] ○ `app/src/main/java/com/simlect/entity/query/UserSignRecordDetailQuery.java` — 签到明细查询
- [ ] ○ `app/src/main/java/com/simlect/entity/query/ImageModerationRecordQuery.java` — 图片审核查询
- [ ] ○ `app/src/main/java/com/simlect/entity/vo/LocationWeatherVO.java` — 定位天气 VO
- [ ] ○ `app/src/main/java/com/simlect/entity/vo/MemberCenterVO.java` — 会员中心 VO
- [ ] ○ `app/src/main/java/com/simlect/mappers/UserInfoMapper.java` — 用户信息 Mapper
- [ ] ○ `app/src/main/java/com/simlect/mappers/UserAddressMapper.java` — 地址 Mapper
- [ ] ○ `app/src/main/java/com/simlect/mappers/UserBrowseHistoryMapper.java` — 浏览历史 Mapper
- [ ] ○ `app/src/main/java/com/simlect/mappers/UserMemberProfileMapper.java` — 会员档案 Mapper
- [ ] ○ `app/src/main/java/com/simlect/mappers/UserNotificationMapper.java` — 通知 Mapper
- [ ] ○ `app/src/main/java/com/simlect/mappers/UserProductFavoriteMapper.java` — 收藏 Mapper
- [ ] ○ `app/src/main/java/com/simlect/mappers/UserSignRecordMapper.java` — 签到记录 Mapper
- [ ] ○ `app/src/main/java/com/simlect/mappers/UserSignRecordDetailMapper.java` — 签到明细 Mapper
- [ ] ○ `app/src/main/java/com/simlect/mappers/SignBitmapMapper.java` — 签到位图 Mapper
- [ ] ○ `app/src/main/java/com/simlect/mappers/SignGrowthRecordMapper.java` — 签到成长 Mapper
- [ ] ○ `app/src/main/java/com/simlect/mappers/SignSummaryMapper.java` — 签到汇总 Mapper
- [ ] ○ `app/src/main/java/com/simlect/mappers/SignSupplementUsedMapper.java` — 补签使用 Mapper
- [ ] ○ `app/src/main/java/com/simlect/mappers/ImageModerationRecordMapper.java` — 图片审核 Mapper

### 8.3.3 user Mapper XML（11 个）

- [ ] ○ `app/src/main/resources/com/simlect/mappers/UserInfoMapper.xml`
- [ ] ○ `app/src/main/resources/com/simlect/mappers/UserAddressMapper.xml`
- [ ] ○ `app/src/main/resources/com/simlect/mappers/UserBrowseHistoryMapper.xml`
- [ ] ○ `app/src/main/resources/com/simlect/mappers/UserMemberProfileMapper.xml`
- [ ] ○ `app/src/main/resources/com/simlect/mappers/UserNotificationMapper.xml`
- [ ] ○ `app/src/main/resources/com/simlect/mappers/UserProductFavoriteMapper.xml`
- [ ] ○ `app/src/main/resources/com/simlect/mappers/UserSignRecordMapper.xml`
- [ ] ○ `app/src/main/resources/com/simlect/mappers/UserSignRecordDetailMapper.xml`
- [ ] ○ `app/src/main/resources/com/simlect/mappers/SignBitmapMapper.xml`
- [ ] ○ `app/src/main/resources/com/simlect/mappers/SignGrowthRecordMapper.xml`
- [ ] ○ `app/src/main/resources/com/simlect/mappers/SignSummaryMapper.xml`
- [ ] ○ `app/src/main/resources/com/simlect/mappers/SignSupplementUsedMapper.xml`
- [ ] ○ `app/src/main/resources/com/simlect/mappers/ImageModerationRecordMapper.xml`

## 8.4 user 测试 T（27 个）

- [ ] T `app/src/test/java/com/simlect/SmokeTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/AliEmailServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/ImageModerationServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/LocationWeatherServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/MemberLevelRewardConfigServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/SignRewardConfigServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/SignServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/SignStreakServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/SignSummaryServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/UserAddressServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/UserBrowseHistoryServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/UserInfoServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/UserMemberProfileServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/UserNotificationServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/UserProductFavoriteServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/component/AdminLoginLockServiceTest.java`
- [ ] T `app/src/test/java/com/simlect/component/ImageCensorRateLimitServiceTest.java`
- [ ] T `app/src/test/java/com/simlect/component/RabbitMQNotifyDeadListenerComponentTest.java`
- [ ] T `app/src/test/java/com/simlect/component/UserTempBanServiceTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/AccountControllerTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/SignControllerTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/UserAddressControllerTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/UserBrowseHistoryControllerTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/UserFavoriteControllerTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/UserMemberControllerTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/UserNotificationControllerTest.java`

## 8.5 gateway 必读 ⭐（1 个）

- [ ] ⭐ `src/main/java/com/simlect/cloud/gateway/GatewayApplication.java` — 网关启动：路由/鉴权/限流（5 分钟）

## 8.6 gateway 选读 ·（8 个）

- [ ] · `src/main/java/com/simlect/cloud/gateway/filter/AuthGlobalFilter.java` — 全局鉴权过滤器（核心）
- [ ] · `src/main/java/com/simlect/cloud/gateway/filter/InternalTokenGlobalFilter.java` — 内部 token 过滤器
- [ ] · `src/main/java/com/simlect/cloud/gateway/filter/ForwardedIpGlobalFilter.java` — IP 转发过滤器
- [ ] · `src/main/java/com/simlect/cloud/gateway/filter/OriginCheckGlobalFilter.java` — 来源校验过滤器
- [ ] · `src/main/java/com/simlect/cloud/gateway/support/GatewayTokenResolver.java` — Token 解析器
- [ ] · `src/main/java/com/simlect/cloud/gateway/config/SentinelGatewayConfig.java` — Sentinel 网关配置
- [ ] · `src/main/java/com/simlect/cloud/gateway/config/GatewayProductionSafetyValidator.java` — 生产就绪校验
- [ ] · `src/main/java/com/simlect/cloud/gateway/config/GatewayRedisConfig.java` — 网关 Redis 配置

## 8.7 gateway 薄文件 ○（6 个）

- [ ] ○ `src/main/java/com/simlect/cloud/gateway/config/GatewayAuthProperties.java` — 鉴权属性
- [ ] ○ `src/main/java/com/simlect/cloud/gateway/config/GatewayInternalProperties.java` — 内部属性
- [ ] ○ `src/main/java/com/simlect/cloud/gateway/config/GatewayLoadBalancerClientConfiguration.java` — 负载均衡客户端配置
- [ ] ○ `src/main/java/com/simlect/cloud/gateway/config/GatewayLoadBalancerConfiguration.java` — 负载均衡配置
- [ ] ○ `src/main/java/com/simlect/cloud/gateway/config/GatewayRateLimitProperties.java` — 限流属性

## 8.8 gateway 测试 T（6 个）

- [ ] T `src/test/java/com/simlect/SmokeTest.java`
- [ ] T `src/test/java/com/simlect/cloud/gateway/config/GatewayProductionSafetyValidatorTest.java`
- [ ] T `src/test/java/com/simlect/cloud/gateway/config/SentinelGatewayConfigTest.java`
- [ ] T `src/test/java/com/simlect/cloud/gateway/filter/AuthGlobalFilterTest.java`
- [ ] T `src/test/java/com/simlect/cloud/gateway/filter/InternalTokenGlobalFilterTest.java`
- [ ] T `src/test/java/com/simlect/cloud/gateway/support/GatewayTokenResolverTest.java`

## 第 8 步自检

- [ ] 登录时老密码格式（非 bcrypt）怎么处理？邮箱验证码怎么防轰炸？
- [ ] 网关的 AuthGlobalFilter 鉴权流程？

---

# 第 9 步：agent AI 智能客服（~107 Python 文件）⭐

> agent 模块是 Python(LangChain) + Java(Spring AI) 双实现——实际扫描显示此模块为纯 Python（Java 部分未实现或在他处）。
> **模块路径**：`Simlect-backend/Simlect-agent/`（单模块，Python 包结构）
> 你 Python 背景是优势，可从此切入建立信心。

## 9.1 必读 ⭐（约 15 个，逐行精读）

### 9.1.1 对话图编排（核心，对应 ZHIDAO 第 9 步第 1-2 项）

- [ ] ⭐⭐ `app/graph/builder.py` — **核心**：LangGraph 对话图构建，意图识别→RAG→工具调用→流式回答节点编排（2 小时）
- [ ] ⭐⭐ `app/graph/nodes.py` — **核心**：图节点实现，每个节点的处理逻辑（2 小时）
- [ ] ⭐ `app/graph/state.py` — LangGraph State 定义：对话状态字段（20 分钟）
- [ ] ⭐ `app/graph/runner.py` — 图运行器：执行编排好的图（30 分钟）
- [ ] ⭐ `app/graph/checkpoint/redis_saver.py` — Redis 检查点：对话状态持久化（30 分钟）

### 9.1.2 意图识别 + RAG + MCP 工具

- [ ] ⭐ `app/domain/intent/classifier.py` — 意图识别：LLM 驱动判断走 RAG/工具/闲聊（30 分钟）
- [ ] ⭐ `app/domain/intent/rules.py` — 意图规则
- [ ] ⭐ `app/rag/retriever.py` — **核心**：RAG 检索（FAQ/知识库检索，Embedding+ES+Rerank）（1 小时）
- [ ] ⭐ `app/rag/embedding.py` — Embedding 生成
- [ ] ⭐ `app/rag/rrf.py` — RRF 融合排序（Reciprocal Rank Fusion）
- [ ] ⭐ `app/mcp/tools.py` — MCP 工具调用：查订单/查物流（调真实接口）（30 分钟）

### 9.1.3 Agent 编排核心 + 服务

- [ ] ⭐ `app/services/agent_runtime.py` — **核心**：Agent 编排运行时，组装图+工具+记忆（1 小时）
- [ ] ⭐ `app/services/agent_engine.py` — Agent 引擎
- [ ] ⭐ `app/services/agent_service.py` — Agent 服务层入口
- [ ] ⭐ `app/main.py` — 应用入口：FastAPI 启动 + 路由注册
- [ ] ⭐ `app/mcp_server/server.py` — MCP Server（端口 7060）：对外暴露工具

## 9.2 选读 ·（约 40 个）

### 9.2.1 API / 鉴权 / 配置 / 数据库

- [ ] · `app/api/routes/agent.py` — Agent 路由：对话/SSE 流式入口
- [ ] · `app/api/websocket.py` — WebSocket 支持
- [ ] · `app/api/deps.py` — 依赖注入
- [ ] · `app/api/exception_handlers.py` — 异常处理
- [ ] · `app/api/rate_limit.py` — 限流
- [ ] · `app/auth/token_service.py` — Token 服务
- [ ] · `app/config/settings.py` — 配置项
- [ ] · `app/db/pool.py` — 数据库连接池
- [ ] · `app/constants.py` — 常量
- [ ] · `app/exceptions.py` — 自定义异常

### 9.2.2 意图 / 护栏 / 记忆 / 容错

- [ ] · `app/domain/intent/types.py` — 意图类型
- [ ] · `app/harness/guardrails/input_guard.py` — 输入护栏
- [ ] · `app/harness/guardrails/output_guard.py` — 输出护栏
- [ ] · `app/harness/guardrails/product_text_guard.py` — 商品文本护栏
- [ ] · `app/harness/guardrails/tool_guard.py` — 工具护栏
- [ ] · `app/harness/metrics/runtime_sensors.py` — 运行时指标
- [ ] · `app/memory/context_builder.py` — 上下文构建
- [ ] · `app/memory/post_turn.py` — 轮次后处理
- [ ] · `app/memory/session_memory_service.py` — 会话记忆服务
- [ ] · `app/memory/compress_service.py` — 记忆压缩
- [ ] · `app/memory/assistant_condense.py` — 助手压缩
- [ ] · `app/memory/models.py` — 记忆模型
- [ ] · `app/memory/token_estimator.py` — Token 估算
- [ ] · `app/resilience/circuit_breaker.py` — 熔断器

### 9.2.3 服务层（最多，约 25 个）

- [ ] · `app/services/action_execute_service.py` — 动作执行服务
- [ ] · `app/services/health_service.py` — 健康检查
- [ ] · `app/services/java_internal_client.py` — Java 内部客户端（调 Java 服务）
- [ ] · `app/services/java_internal_client_db.py` — Java 内部客户端 DB
- [ ] · `app/services/llm_factory.py` — LLM 工厂
- [ ] · `app/services/mcp_streamable_client.py` — MCP 流式客户端
- [ ] · `app/services/mcp_tool_router.py` — MCP 工具路由
- [ ] · `app/services/mcp_tools_service.py` — MCP 工具服务
- [ ] · `app/services/message_service.py` — 消息服务
- [ ] · `app/services/order_service.py` — 订单服务（查单）
- [ ] · `app/services/pending_action_service.py` — 待处理动作服务
- [ ] · `app/services/product_search_query.py` — 商品搜索查询
- [ ] · `app/services/product_service.py` — 商品服务
- [ ] · `app/services/product_snapshot_service.py` — 商品快照服务
- [ ] · `app/services/prompt_service.py` — Prompt 服务
- [ ] · `app/services/rate_limit_service.py` — 限流服务
- [ ] · `app/services/redis_service.py` — Redis 服务
- [ ] · `app/services/search_recommend_service.py` — 搜索推荐服务
- [ ] · `app/services/sensitive_word_service.py` — 敏感词服务
- [ ] · `app/services/stream_service.py` — 流式服务
- [ ] · `app/services/tool_invoke_result.py` — 工具调用结果

### 9.2.4 工具 / 模型 / MCP Server

- [ ] · `app/utils/biz_payload.py` — 业务 payload 工具
- [ ] · `app/utils/order_ids.py` — 订单 ID 工具
- [ ] · `app/utils/product_consult.py` — 商品咨询工具
- [ ] · `app/utils/prompt_boundary.py` — Prompt 边界工具
- [ ] · `app/utils/ws_token.py` — WebSocket Token
- [ ] · `app/models/message_send.py` — 消息发送模型
- [ ] · `app/models/response.py` — 响应模型
- [ ] · `app/mcp_server/__main__.py` — MCP Server 启动入口

## 9.3 测试代码 T（约 25 个）

### 9.3.1 单元测试

- [ ] T `tests/test_agent_graph.py`
- [ ] T `tests/test_agent_prompt.py`
- [ ] T `tests/test_agent_rate_limit_key.py`
- [ ] T `tests/test_auth.py`
- [ ] T `tests/test_biz_payload_act.py`
- [ ] T `tests/test_finalize_displayed_cards.py`
- [ ] T `tests/test_guardrails.py`
- [ ] T `tests/test_intent_classifier.py`
- [ ] T `tests/test_internal_client_backend.py`
- [ ] T `tests/test_internal_client_db.py`
- [ ] T `tests/test_llm_factory.py`
- [ ] T `tests/test_memory_context.py`
- [ ] T `tests/test_post_turn_search_cards.py`
- [ ] T `tests/test_product_consult.py`
- [ ] T `tests/test_product_search_backfill.py`
- [ ] T `tests/test_product_search_relevance.py`
- [ ] T `tests/test_product_text_guard.py`
- [ ] T `tests/test_prompt_boundary.py`
- [ ] T `tests/test_prompt_service_intent.py`
- [ ] T `tests/test_strip_product_id_array.py`

### 9.3.2 集成测试

- [ ] T `tests/integration/test_intent_rules.py`
- [ ] T `tests/integration/test_order_status.py`
- [ ] T `tests/integration/test_pending_confirm.py`
- [ ] T `tests/integration/test_product_snapshot.py`
- [ ] T `tests/integration/test_websocket.py`

## 9.4 辅助脚本（tools/，非业务代码，可选读）

- [ ] · `tools/add_zh_comments.py` — 加中文注释脚本
- [ ] · `tools/add_zh_comments_java.py` — Java 加注释脚本
- [ ] · `tools/add_zh_comments_vue.py` — Vue 加注释脚本
- [ ] · `tools/audit_comments.py` — 注释审计脚本
- [ ] · `tools/dedupe_comments.py` — 注释去重
- [ ] · `tools/enhance_zh_comments.py` — 注释增强
- [ ] · `tools/enhance_zh_comments2.py` — 注释增强 v2
- [ ] · `tools/ensure_full_comments.py` — 确保完整注释
- [ ] · `tools/fix_vue_comments.py` — 修复 Vue 注释
- [ ] · `tools/generate_all_modules.py` — 生成所有模块文档
- [ ] · `tools/generate_module_docs.py` — 生成模块文档

## 第 9 步自检

- [ ] AI 客服对话流程是什么？意图识别 + RAG + MCP 工具怎么协作？
- [ ] LangGraph 的 checkpoint 怎么持久化对话状态？
- [ ] MCP 工具调用怎么调真实 Java 接口？

---

# 第 10 步：product + search + cart + admin（约 186 文件）

> 补齐剩余模块——理解商品快照防改价、ES 搜索、购物车、后台管理。

## 10.1 product 商品（77 文件）⭐

> **模块路径**：`Simlect-backend/Simlect-product/`（双模块）

### product 必读 ⭐（3 个）

- [ ] ⭐ `app/src/main/java/com/simlect/biz/impl/ProductInfoServiceImpl.java` — 商品 CRUD + 快照（下单防改价）+ 销量累加（20 分钟）
- [ ] ⭐ `app/src/main/java/com/simlect/cloud/ProductApplication.java` — 商品服务启动类（5 分钟）
- [ ] ⭐ `api/src/main/java/com/simlect/api/ProductFeignClient.java` — 商品 Feign 契约（10 分钟）

### product 选读 ·（12 个）

- [ ] · `app/src/main/java/com/simlect/biz/ProductInfoService.java` — 商品信息服务接口
- [ ] · `app/src/main/java/com/simlect/biz/ProductInternalService.java` — 内部商品服务接口
- [ ] · `app/src/main/java/com/simlect/biz/ProductSkuService.java` — SKU 服务接口
- [ ] · `app/src/main/java/com/simlect/biz/ProductPropertyValueService.java` — 属性值服务接口
- [ ] · `app/src/main/java/com/simlect/biz/SysCategoryService.java` — 分类服务接口
- [ ] · `app/src/main/java/com/simlect/biz/SysProductPropertyService.java` — 属性服务接口
- [ ] · `app/src/main/java/com/simlect/biz/impl/ProductSkuServiceImpl.java` — SKU 实现
- [ ] · `app/src/main/java/com/simlect/biz/impl/ProductPropertyValueServiceImpl.java` — 属性值实现
- [ ] · `app/src/main/java/com/simlect/biz/impl/SysCategoryServiceImpl.java` — 分类实现
- [ ] · `app/src/main/java/com/simlect/biz/impl/SysProductPropertyServiceImpl.java` — 属性实现
- [ ] · `app/src/main/java/com/simlect/component/ProductBloomFilterComponent.java` — 商品布隆过滤器（防穿透）
- [ ] · `app/src/main/java/com/simlect/controller/ProductController.java` — 商品控制器
- [ ] · `app/src/main/java/com/simlect/controller/admin/ProductInfoController.java` — 管理端商品控制器
- [ ] · `app/src/main/java/com/simlect/controller/admin/ProductSkuController.java` — 管理端 SKU 控制器
- [ ] · `app/src/main/java/com/simlect/controller/admin/ProductPropertyValueController.java` — 管理端属性值控制器
- [ ] · `app/src/main/java/com/simlect/controller/admin/SysCategoryController.java` — 管理端分类控制器
- [ ] · `app/src/main/java/com/simlect/controller/admin/SysProductPropertyController.java` — 管理端属性控制器
- [ ] · `app/src/main/java/com/simlect/controller/internal/ProductInternalController.java` — 内部商品 API
- [ ] · `app/src/main/java/com/simlect/controller/internal/ProductAgentInternalController.java` — Agent 内部商品 API

### product 薄文件 ○（约 42 个）

- [ ] ○ `api/src/main/java/com/simlect/api/fallback/ProductFeignFallbackFactory.java` — 降级工厂
- [ ] ○ `api/src/main/java/com/simlect/api/support/ProductFeignSupport.java` — Feign 支持
- [ ] ○ `api/src/main/java/com/simlect/api/dto/ProductIdDTO.java` — 商品 ID DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/ProductIdListDTO.java` — 商品 ID 列表 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/ProductInfoDTO.java` — 商品信息 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/ProductSalesIncreaseDTO.java` — 销量增加 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/ProductSnapshotBatchVO.java` — 商品快照批量 VO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/LessStockPageDTO.java` — 低库存分页 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/enums/CommendTypeEnum.java` — 推荐类型枚举
- [ ] ○ `api/src/main/java/com/simlect/api/enums/ProductStatusEnum.java` — 商品状态枚举
- [ ] ○ `api/src/main/java/com/simlect/api/vo/ProductInfoSnapshotVO.java` — 商品快照 VO
- [ ] ○ `api/src/main/java/com/simlect/api/vo/ProductPropertyVO.java` — 商品属性 VO
- [ ] ○ `api/src/main/java/com/simlect/api/vo/ProductPropertyValueVO.java` — 属性值 VO
- [ ] ○ `api/src/main/java/com/simlect/api/vo/ProductPropertyValueSnapshotVO.java` — 属性值快照 VO
- [ ] ○ `api/src/main/java/com/simlect/api/vo/ProductRagIndexVO.java` — RAG 索引 VO
- [ ] ○ `api/src/main/java/com/simlect/api/vo/ProductRagPropertyVO.java` — RAG 属性 VO
- [ ] ○ `api/src/main/java/com/simlect/api/vo/ProductRagSkuVO.java` — RAG SKU VO
- [ ] ○ `api/src/main/java/com/simlect/api/vo/ProductSearchIndexVO.java` — 搜索索引 VO
- [ ] ○ `api/src/main/java/com/simlect/api/vo/ProductSkuListVO.java` — SKU 列表 VO
- [ ] ○ `api/src/main/java/com/simlect/api/vo/ProductSkuProperDataVO.java` — SKU 属性数据 VO
- [ ] ○ `api/src/main/java/com/simlect/api/vo/ProductSkuSnapshotVO.java` — SKU 快照 VO
- [ ] ○ `api/src/main/java/com/simlect/api/vo/PropertyData.java` — 属性数据
- [ ] ○ `app/src/main/java/com/simlect/entity/dto/ProductSaveDTO.java` — 商品保存 DTO
- [ ] ○ `app/src/main/java/com/simlect/entity/po/ProductInfo.java` — 商品信息实体
- [ ] ○ `app/src/main/java/com/simlect/entity/po/ProductSku.java` — SKU 实体
- [ ] ○ `app/src/main/java/com/simlect/entity/po/ProductPropertyValue.java` — 属性值实体
- [ ] ○ `app/src/main/java/com/simlect/entity/po/SysCategory.java` — 分类实体
- [ ] ○ `app/src/main/java/com/simlect/entity/po/SysProductProperty.java` — 属性实体
- [ ] ○ `app/src/main/java/com/simlect/entity/po/Product4Load.java` — 商品加载实体
- [ ] ○ `app/src/main/java/com/simlect/entity/query/ProductInfoQuery.java` — 商品查询
- [ ] ○ `app/src/main/java/com/simlect/entity/query/ProductSkuQuery.java` — SKU 查询
- [ ] ○ `app/src/main/java/com/simlect/entity/query/ProductPropertyValueQuery.java` — 属性值查询
- [ ] ○ `app/src/main/java/com/simlect/entity/query/SysCategoryQuery.java` — 分类查询
- [ ] ○ `app/src/main/java/com/simlect/entity/query/SysProductPropertyQuery.java` — 属性查询
- [ ] ○ `app/src/main/java/com/simlect/entity/vo/Product4VO.java` — 商品 VO
- [ ] ○ `app/src/main/java/com/simlect/mappers/ProductInfoMapper.java` — 商品 Mapper
- [ ] ○ `app/src/main/java/com/simlect/mappers/ProductSkuMapper.java` — SKU Mapper
- [ ] ○ `app/src/main/java/com/simlect/mappers/ProductPropertyValueMapper.java` — 属性值 Mapper
- [ ] ○ `app/src/main/java/com/simlect/mappers/SysCategoryMapper.java` — 分类 Mapper
- [ ] ○ `app/src/main/java/com/simlect/mappers/SysProductPropertyMapper.java` — 属性 Mapper
- [ ] ○ `app/src/main/resources/com/simlect/mappers/ProductInfoMapper.xml` — 商品 SQL
- [ ] ○ `app/src/main/resources/com/simlect/mappers/ProductSkuMapper.xml` — SKU SQL
- [ ] ○ `app/src/main/resources/com/simlect/mappers/ProductPropertyValueMapper.xml` — 属性值 SQL
- [ ] ○ `app/src/main/resources/com/simlect/mappers/SysCategoryMapper.xml` — 分类 SQL
- [ ] ○ `app/src/main/resources/com/simlect/mappers/SysProductPropertyMapper.xml` — 属性 SQL

### product 测试 T（15 个）

- [ ] T `app/src/test/java/com/simlect/SmokeTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/ProductInfoServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/ProductPropertyValueServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/ProductSkuServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/SysCategoryServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/SysProductPropertyServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/component/ProductBloomFilterComponentTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/ProductControllerTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/admin/ProductInfoControllerTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/admin/ProductPropertyValueControllerTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/admin/ProductSkuControllerTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/admin/SysCategoryControllerTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/admin/SysProductPropertyControllerTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/internal/ProductAgentInternalControllerTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/internal/ProductInternalControllerTest.java`

## 10.2 search 搜索（41 文件）

> **模块路径**：`Simlect-backend/Simlect-search/`（单模块，含 ES 检索 + RAG 知识库）

### search 必读 ⭐（1 个）

- [ ] ⭐ `src/main/java/com/simlect/cloud/SearchApplication.java` — ES 搜索服务启动类 + analysis-ik 中文分词（10 分钟）

### search 选读 ·（约 12 个）

- [ ] · `src/main/java/com/simlect/component/EsSearchComponent.java` — ES 搜索组件（核心检索逻辑）
- [ ] · `src/main/java/com/simlect/component/RabbitMQRagListenerComponent.java` — RAG 同步监听
- [ ] · `src/main/java/com/simlect/component/RabbitMQRagDeadListenerComponent.java` — RAG 死信监听
- [ ] · `src/main/java/com/simlect/controller/SearchController.java` — 搜索控制器
- [ ] · `src/main/java/com/simlect/controller/admin/RagController.java` — RAG 管理控制器
- [ ] · `src/main/java/com/simlect/controller/admin/RagQuestionController.java` — RAG 问题控制器
- [ ] · `src/main/java/com/simlect/controller/admin/RagSyncFailureController.java` — RAG 同步失败控制器
- [ ] · `src/main/java/com/simlect/controller/admin/SearchHotKeywordController.java` — 热搜词控制器
- [ ] · `src/main/java/com/simlect/controller/internal/SearchToolInternalController.java` — 内部搜索工具 API
- [ ] · `src/main/java/com/simlect/biz/impl/SearchRecommendServiceImpl.java` — 搜索推荐实现
- [ ] · `src/main/java/com/simlect/biz/impl/RagQuestionServiceImpl.java` — RAG 问题实现
- [ ] · `src/main/java/com/simlect/biz/impl/RagSyncFailureServiceImpl.java` — RAG 同步失败实现
- [ ] · `src/main/java/com/simlect/biz/impl/SearchHotKeywordServiceImpl.java` — 热搜词实现
- [ ] · `src/main/java/com/simlect/biz/impl/SearchKeywordServiceImpl.java` — 搜索词实现

### search 薄文件 ○（约 14 个）

- [ ] ○ `src/main/java/com/simlect/biz/RagQuestionService.java` — RAG 问题服务接口
- [ ] ○ `src/main/java/com/simlect/biz/RagSyncFailureService.java` — RAG 同步失败服务接口
- [ ] ○ `src/main/java/com/simlect/biz/SearchHotKeywordService.java` — 热搜词服务接口
- [ ] ○ `src/main/java/com/simlect/biz/SearchKeywordService.java` — 搜索词服务接口
- [ ] ○ `src/main/java/com/simlect/biz/SearchRecommendService.java` — 搜索推荐服务接口
- [ ] ○ `src/main/java/com/simlect/entity/po/RagQuestion.java` — RAG 问题实体
- [ ] ○ `src/main/java/com/simlect/entity/po/SearchHotKeyword.java` — 热搜词实体
- [ ] ○ `src/main/java/com/simlect/entity/po/UserSearchKeyword.java` — 用户搜索词实体
- [ ] ○ `src/main/java/com/simlect/entity/query/RagQuestionQuery.java` — RAG 问题查询
- [ ] ○ `src/main/java/com/simlect/entity/query/RagSyncFailureQuery.java` — RAG 同步失败查询
- [ ] ○ `src/main/java/com/simlect/entity/query/SearchHotKeywordQuery.java` — 热搜词查询
- [ ] ○ `src/main/java/com/simlect/entity/query/UserSearchKeywordQuery.java` — 用户搜索词查询
- [ ] ○ `src/main/java/com/simlect/mappers/RagQuestionMapper.java` — RAG 问题 Mapper
- [ ] ○ `src/main/java/com/simlect/mappers/SearchHotKeywordMapper.java` — 热搜词 Mapper
- [ ] ○ `src/main/java/com/simlect/mappers/UserSearchKeywordMapper.java` — 用户搜索词 Mapper
- [ ] ○ `src/main/resources/com/simlect/mappers/RagQuestionMapper.xml` — RAG 问题 SQL
- [ ] ○ `src/main/resources/com/simlect/mappers/SearchHotKeywordMapper.xml` — 热搜词 SQL
- [ ] ○ `src/main/resources/com/simlect/mappers/UserSearchKeywordMapper.xml` — 用户搜索词 SQL

### search 测试 T（11 个）

- [ ] T `src/test/java/com/simlect/SmokeTest.java`
- [ ] T `src/test/java/com/simlect/biz/impl/RagQuestionServiceImplTest.java`
- [ ] T `src/test/java/com/simlect/biz/impl/RagSyncFailureServiceImplTest.java`
- [ ] T `src/test/java/com/simlect/biz/impl/SearchHotKeywordServiceImplTest.java`
- [ ] T `src/test/java/com/simlect/biz/impl/SearchKeywordServiceImplTest.java`
- [ ] T `src/test/java/com/simlect/biz/impl/SearchRecommendServiceImplTest.java`
- [ ] T `src/test/java/com/simlect/component/EsSearchComponentTest.java`
- [ ] T `src/test/java/com/simlect/controller/SearchControllerTest.java`
- [ ] T `src/test/java/com/simlect/controller/admin/RagQuestionControllerTest.java`
- [ ] T `src/test/java/com/simlect/controller/admin/SearchHotKeywordControllerTest.java`
- [ ] T `src/test/java/com/simlect/controller/internal/SearchToolInternalControllerTest.java`

## 10.3 cart 购物车（21 文件）

> **模块路径**：`Simlect-backend/Simlect-cart/`（双模块，下单成功后被 order 清）

### cart 必读 ⭐（2 个）

- [ ] ⭐ `app/src/main/java/com/simlect/cloud/CartApplication.java` — 购物车服务启动类（5 分钟）
- [ ] ⭐ `api/src/main/java/com/simlect/api/CartFeignClient.java` — 购物车 Feign 契约（10 分钟）

### cart 选读 ·（5 个）

- [ ] · `app/src/main/java/com/simlect/biz/ProductCartService.java` — 购物车服务接口
- [ ] · `app/src/main/java/com/simlect/biz/impl/ProductCartServiceImpl.java` — 购物车实现
- [ ] · `app/src/main/java/com/simlect/biz/CartInternalService.java` — 内部购物车服务接口
- [ ] · `app/src/main/java/com/simlect/controller/ProductCartController.java` — 购物车控制器
- [ ] · `app/src/main/java/com/simlect/controller/internal/CartInternalController.java` — 内部购物车 API
- [ ] · `app/src/main/java/com/simlect/controller/admin/ProductCartController.java` — 管理端购物车控制器

### cart 薄文件 ○（8 个）

- [ ] ○ `api/src/main/java/com/simlect/api/fallback/CartFeignFallbackFactory.java` — 降级工厂
- [ ] ○ `api/src/main/java/com/simlect/api/support/CartFeignSupport.java` — Feign 支持
- [ ] ○ `api/src/main/java/com/simlect/api/dto/CartDeleteBatchDTO.java` — 批量删除 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/dto/CartDeleteItemDTO.java` — 删除项 DTO
- [ ] ○ `api/src/main/java/com/simlect/api/vo/ProductCartVO.java` — 购物车 VO
- [ ] ○ `app/src/main/java/com/simlect/entity/po/ProductCart.java` — 购物车实体
- [ ] ○ `app/src/main/java/com/simlect/entity/query/ProductCartQuery.java` — 购物车查询
- [ ] ○ `app/src/main/java/com/simlect/mappers/ProductCartMapper.java` — 购物车 Mapper
- [ ] ○ `app/src/main/resources/com/simlect/mappers/ProductCartMapper.xml` — 购物车 SQL

### cart 测试 T（5 个）

- [ ] T `app/src/test/java/com/simlect/biz/CartInternalServiceTest.java`
- [ ] T `app/src/test/java/com/simlect/biz/impl/ProductCartServiceImplTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/ProductCartControllerTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/admin/ProductCartControllerTest.java`
- [ ] T `app/src/test/java/com/simlect/controller/internal/CartInternalControllerTest.java`

## 10.4 admin 后台管理（47 文件）

> **模块路径**：`Simlect-backend/Simlect-admin/`（单模块，后台/审计/敏感词/统计）

### admin 必读 ⭐（1 个）

- [ ] ⭐ `src/main/java/com/simlect/cloud/AdminApplication.java` — 后台管理服务启动类（5 分钟）

### admin 选读 ·（约 12 个）

- [ ] · `src/main/java/com/simlect/biz/impl/AdminAuditLogServiceImpl.java` — 审计日志实现
- [ ] · `src/main/java/com/simlect/biz/impl/AgentMessageServiceImpl.java` — Agent 消息实现
- [ ] · `src/main/java/com/simlect/biz/impl/SensitiveWordServiceImpl.java` — 敏感词实现
- [ ] · `src/main/java/com/simlect/biz/impl/StatisticsInfoServiceImpl.java` — 统计信息实现
- [ ] · `src/main/java/com/simlect/component/SensitiveWordCacheComponent.java` — 敏感词缓存组件
- [ ] · `src/main/java/com/simlect/config/AdminRestClientConfig.java` — Rest 客户端配置
- [ ] · `src/main/java/com/simlect/controller/admin/AgentMessageController.java` — Agent 消息控制器
- [ ] · `src/main/java/com/simlect/controller/admin/HomeController.java` — 首页控制器
- [ ] · `src/main/java/com/simlect/controller/admin/MqCompensationLogController.java` — MQ 补偿日志控制器
- [ ] · `src/main/java/com/simlect/controller/admin/SensitiveWordController.java` — 敏感词控制器
- [ ] · `src/main/java/com/simlect/controller/admin/SettingController.java` — 设置控制器
- [ ] · `src/main/java/com/simlect/controller/admin/StatisticsInfoController.java` — 统计控制器
- [ ] · `src/main/java/com/simlect/controller/admin/ToolController.java` — 工具控制器
- [ ] · `src/main/java/com/simlect/controller/internal/AdminAuditInternalController.java` — 内部审计 API
- [ ] · `src/main/java/com/simlect/task/AutoDataTask.java` — 自动数据任务
- [ ] · `src/main/java/com/simlect/task/SensitiveWordRedisSyncTask.java` — 敏感词 Redis 同步任务
- [ ] · `src/main/java/com/simlect/api/SearchToolFeignClient.java` — 搜索工具 Feign 契约

### admin 薄文件 ○（约 18 个）

- [ ] ○ `src/main/java/com/simlect/api/fallback/SearchToolFeignFallbackFactory.java` — 降级工厂
- [ ] ○ `src/main/java/com/simlect/api/support/SearchToolFeignSupport.java` — Feign 支持
- [ ] ○ `src/main/java/com/simlect/biz/AdminAuditLogService.java` — 审计日志服务接口
- [ ] ○ `src/main/java/com/simlect/biz/AgentMessageService.java` — Agent 消息服务接口
- [ ] ○ `src/main/java/com/simlect/biz/SensitiveWordService.java` — 敏感词服务接口
- [ ] ○ `src/main/java/com/simlect/biz/StatisticsInfoService.java` — 统计信息服务接口
- [ ] ○ `src/main/java/com/simlect/entity/dto/AdminAuditLogDTO.java` — 审计日志 DTO
- [ ] ○ `src/main/java/com/simlect/entity/po/AdminAuditLog.java` — 审计日志实体
- [ ] ○ `src/main/java/com/simlect/entity/po/AgentMessage.java` — Agent 消息实体
- [ ] ○ `src/main/java/com/simlect/entity/po/SensitiveWord.java` — 敏感词实体
- [ ] ○ `src/main/java/com/simlect/entity/po/StatisticsInfo.java` — 统计信息实体
- [ ] ○ `src/main/java/com/simlect/entity/query/AdminAuditLogQuery.java` — 审计日志查询
- [ ] ○ `src/main/java/com/simlect/entity/query/AgentMessageQuery.java` — Agent 消息查询
- [ ] ○ `src/main/java/com/simlect/entity/query/SensitiveWordQuery.java` — 敏感词查询
- [ ] ○ `src/main/java/com/simlect/entity/query/StatisticsInfoQuery.java` — 统计信息查询
- [ ] ○ `src/main/java/com/simlect/entity/vo/StatisticsDataVO.java` — 统计数据 VO
- [ ] ○ `src/main/java/com/simlect/entity/vo/TodayDataVO.java` — 今日数据 VO
- [ ] ○ `src/main/java/com/simlect/mappers/AdminAuditLogMapper.java` — 审计日志 Mapper
- [ ] ○ `src/main/java/com/simlect/mappers/SensitiveWordMapper.java` — 敏感词 Mapper
- [ ] ○ `src/main/java/com/simlect/mappers/StatisticsInfoMapper.java` — 统计信息 Mapper
- [ ] ○ `src/main/resources/com/simlect/mappers/AdminAuditLogMapper.xml` — 审计日志 SQL
- [ ] ○ `src/main/resources/com/simlect/mappers/SensitiveWordMapper.xml` — 敏感词 SQL
- [ ] ○ `src/main/resources/com/simlect/mappers/StatisticsInfoMapper.xml` — 统计信息 SQL

### admin 测试 T（9 个）

- [ ] T `src/test/java/com/simlect/SmokeTest.java`
- [ ] T `src/test/java/com/simlect/biz/impl/AdminAuditLogServiceImplTest.java`
- [ ] T `src/test/java/com/simlect/biz/impl/AgentMessageServiceImplTest.java`
- [ ] T `src/test/java/com/simlect/biz/impl/SensitiveWordServiceImplTest.java`
- [ ] T `src/test/java/com/simlect/biz/impl/StatisticsInfoServiceImplTest.java`
- [ ] T `src/test/java/com/simlect/component/SensitiveWordCacheComponentTest.java`
- [ ] T `src/test/java/com/simlect/controller/admin/StatisticsInfoControllerTest.java`
- [ ] T `src/test/java/com/simlect/controller/admin/ToolControllerTest.java`
- [ ] T `src/test/java/com/simlect/task/AutoDataTaskTest.java`

## 第 10 步自检

- [ ] 商品快照在 postOrder 里起什么作用？
- [ ] ES 搜索的 analysis-ik 中文分词怎么配置？
- [ ] 布隆过滤器怎么防缓存穿透？

---

# 附录：配置文件（XML，65 个）

> Maven POM（依赖管理）+ MyBatis Mapper XML（SQL 语句）。POM 读父 POM 即可，子 POM 跳过；Mapper XML 已在各模块薄文件层列出，此处汇总。

## A.1 父 POM（必读 1 个）

- [ ] ⭐ `Simlect-backend/pom.xml` — 父 POM：Java17 + Spring Boot 3.5.4 + Spring Cloud 2025 + Spring AI 1.1.2 依赖管理（10 分钟）

## A.2 模块 POM（13 个，跳过）

- [ ] ○ `Simlect-backend/Simlect-common/pom.xml`
- [ ] ○ `Simlect-backend/Simlect-gateway/pom.xml`
- [ ] ○ `Simlect-backend/Simlect-user/api/pom.xml`（若有）
- [ ] ○ `Simlect-backend/Simlect-user/app/pom.xml`（若有）
- [ ] ○ `Simlect-backend/Simlect-product/pom.xml`（含 api + app 子模块）
- [ ] ○ `Simlect-backend/Simlect-stock/pom.xml`
- [ ] ○ `Simlect-backend/Simlect-cart/pom.xml`
- [ ] ○ `Simlect-backend/Simlect-order/pom.xml`
- [ ] ○ `Simlect-backend/Simlect-pay/pom.xml`
- [ ] ○ `Simlect-backend/Simlect-coupon/pom.xml`
- [ ] ○ `Simlect-backend/Simlect-search/pom.xml`
- [ ] ○ `Simlect-backend/Simlect-admin/pom.xml`

## A.3 Mapper XML（已在各模块薄文件层列出，此处不重复）

> 各模块 Mapper XML 已在对应模块的"薄文件 ○"层列出并打勾，此处不再重复，避免双重计数。

---

# 部署配置（deploy/，非 Java 文件）

> **目录路径**：`deploy/`

- [ ] ⭐ `deploy/docker-compose.middleware.yml` — 一键起 7 个中间件（MySQL/Redis/RabbitMQ/Nacos/Seata/ES/Sentinel）（15 分钟）
- [ ] · `deploy/start-middleware.ps1` — Windows 启动中间件脚本
- [ ] · `deploy/init-mysql-meta.ps1` — 初始化中间件元数据库脚本
- [ ] · `deploy/nginx.simlect.conf.example` — Nginx 反代配置示例
- [ ] · `deploy/env.production.example` — 生产环境变量示例
- [ ] · `deploy/GO_LIVE.md` — 上线清单
- [ ] · `deploy/MIDDLEWARE_DOCKER.md` — 中间件 Docker 部署文档
- [ ] · `deploy/FULL_STACK.md` — 全栈部署文档
- [ ] · `deploy/seata/` — Seata 自建镜像配置
- [ ] · `deploy/elasticsearch/` — ES 自建镜像配置

---

# SQL 建库脚本（sql/，26 个）

> **目录路径**：`sql/`（一域一库，16 个业务库 + 中间件元数据库）

- [ ] ⭐ `sql/00_create_databases.sql` — 建库脚本（一域一库）
- [ ] ⭐ `sql/00b_nacos_seata_databases.sql` — Nacos/Seata 元数据库
- [ ] · `sql/01_user.sql` — 用户库建表
- [ ] · `sql/02_product.sql` — 商品库建表
- [ ] · `sql/03_stock.sql` — 库存库建表
- [ ] · `sql/04_cart.sql` — 购物车库建表
- [ ] · `sql/05_order.sql` — 订单库建表
- [ ] · `sql/06_pay.sql` — 支付库建表
- [ ] · `sql/07_coupon.sql` — 优惠券库建表
- [ ] · `sql/08_search.sql` — 搜索库建表
- [ ] · `sql/09_agent.sql` — Agent 库建表
- [ ] · `sql/10_admin.sql` — 后台库建表
- [ ] · `sql/13_mq_infra_per_service.sql` — Outbox + 补偿日志表
- [ ] · `sql/14_nacos.sql` — Nacos 元数据
- [ ] · `sql/15_seata.sql` — Seata 元数据
- [ ] · `sql/16_seata_undo_log.sql` — Seata 回滚表（AT 模式核心）

---

# 全量清单完结

## 总览统计

| 类别 | 文件数 | 占比 |
|---|---|---|
| Java 源码（main） | ~480 | 54% |
| Java 测试（test） | ~245 | 27% |
| Python（agent） | ~107 | 12% |
| XML 配置（POM + Mapper） | 65 | 7% |
| **总计** | **~895** | 100% |

## 三条精读路径（按目标选）

| 路径 | 范围 | 耗时 | 适合 |
|---|---|---|---|
| **速览路径** | 第 0→2→3→5 步必读⭐ | 3-4 天 | 面试前快速掌握核心机制 |
| **交易主线** | 第 0→1→2→3→4→5→6→7 步 | 7-8 天 | 彻底理解交易一致性 |
| **全量精读** | 全部 10 步 + 配置 + SQL | 约 14 天 | 彻底吃透整个项目 |

## 使用建议

1. **按章节顺序推进**，不要跳层——每层依赖前一层
2. **必读⭐ 先读**，选读· 跟进，薄文件○ 快速过，测试T 验证理解
3. **每步读完做自检题**，答不出重读对应⭐ 文件
4. **配合 ZHIDAO.md 全景图**读源码，建立心智地图
5. **agent 模块用 Python 优势切入**，建立信心后回头啃 Java 主链路
6. **简历主打选 2 个机制**：推荐 Seata 全局事务 + Outbox 模式（最经典、最好讲）

---

> 清单到此完结。895 文件全覆盖，按 ZHIDAO 10 步路线 + 模块 + 4 层分级组织。
> 每文件一行 checkbox，逐个打勾推进。
> 祝你阅读顺利——这个项目的含金量，值得逐文件啃下来。
