package com.simlect.constants;

// ============================================================================
// Constants（全局常量字典）——Redis 键前缀、业务阈值、MQ 命名、正则等"跨模块硬约定"的唯一出处
// 角色：纯常量容器（无方法、无状态），被 common 与各业务模块同时引用；改这里等于改全局契约。
// 解决什么问题：
//   1. Redis 键统一以 mall: 开头（REDIS_KEY_PREFIX），避免与同实例上的其他项目撞键——生产 Redis 常是多项目共用；
//   2. 键名把"业务域:子域:标识"拼成命名空间（如 mall:rushing:stock:），运维 SCAN 时能一眼看出归属；
//   3. 阈值集中（5MB 上传上限、限流窗口、缓存 TTL、锁超时），避免各处硬编码慢慢漂移。
//   4. 线程安全：全是编译期常量或不可变对象（String/包装类），天然线程安全
// 被谁使用：RedisComponent（缓存/签到 bitmap/分布式锁）、FileUtils 与 ImageCompressUtils（上传上限、缩略图后缀）、
//           各业务 Service（限流、缓存 TTL、幂等键）、RabbitMQConfig 与 MqConsumeReplayRouter（MQ 命名）。
// 注意：键前缀改动属于破坏性变更——旧键不会自动迁移，滚动发布期间会出现"读不到缓存"的抖动。
// ============================================================================
public class Constants {

    // 密码正则：必须包含数字+字母，允许~!@#$%^&*_，长度8~18位；只做最低门槛，不代表安全强度
    public static final String REGEX_PASSWORD = "^(?=.*\\d)(?=.*[a-zA-Z])[\\da-zA-Z~!@#$%^&*_]{8,18}$";

    // 字符串形式数字0，与前端/Redis 里的字符串比较时用它，避免 "0" 与数字 0 的类型混淆
    public static final String ZERO_STR = "0";

    // 数字常量
    // 长度常量 5：验证码等短随机串
    public static final Integer LENGTH_5 = 5;
    // 长度常量 10：短标识/盐值
    public static final Integer LENGTH_10 = 10;
    // 长度常量 15：时间戳后缀等中长串
    public static final Integer LENGTH_15 = 15;
    // 长度常量 30：文件名随机串（FileUtils 落盘用），30 位 62 进制足以避免同目录撞名
    public static final Integer LENGTH_30 = 30;

    // WebSocket 用户端推送主题：，用于消息发布订阅，订单状态、通知等用户维度消息走这里
    public static final String WS_MESSAGE_TOPIC = "message.topic";
    // WebSocket Agent 端主题：AI 对话流式输出单独一条通道，避免与业务通知互相阻塞
    public static final String WS_MESSAGE_TOPIC_AGENT = "message.topic.agent";

    // 消息类型标记 notify：前端据此把消息分发到"通知中心"而不是聊天窗口
    public static final String WS_MESSAGE_TYPE_NOTIFY = "notify";

    // 上传文件的一级子目录（file/）：实际落盘路径 = project.folder + file/ + yyyyMM/
    public static final String FILE_FOLDER_FILE = "file/";

    // 审核隔离区前缀（moderation/pending/）：待审图先落这里，通过后才迁到正式目录 —— 未过审的图不会出现在商品详情里
    public static final String MODERATION_PENDING_PREFIX = "moderation/pending/";

    // 单图上传上限 5MB：ImageCompressUtils 以它为压缩目标，FileUtils 以它为前提；改这里会同时改变"压到多大"和"允许多大"
    public static final long MAX_IMAGE_UPLOAD_BYTES = 5L * 1024 * 1024;

    // 前端 token 的 Cookie/Header 名（token）：网关与拦截器共用，改名等于全站登录态失效
    public static final String TOKEN_WEB = "token";

    // 用户签到-连续签到天数：签到 DTO / Redis Hash 的 key
    public static final String CONTINUOUS_DAYS = "continuousDays";
    // 用户签到-已使用次数：券/权益的使用计数
    public static final String USED_COUNT = "usedCount";
    // 用户签到-累计签到总天数：总天数，与连续天数分开维护
    public static final String TOTAL_SIGN_DAYS = "totalSignDays";
    // 用户签到-上次签到日期（yyyy-MM-dd）：与 bitmap 月历互为校验
    public static final String LAST_SIGN_DATE = "lastSignDate";

    // Redis 过期时间的原子单位：60 秒，下面用乘法派生更大单位，避免各处写 60/3600 的魔法数
    public static final Long REDIS_KEY_EXPIRES_ONE_MIN = 60L;
    // Redis过期时间：1天，基于1分钟常量计算，统一时间基准：登录态/用户信息缓存的默认有效期
    public static final Long REDIS_KEY_EXPIRES_DAY = REDIS_KEY_EXPIRES_ONE_MIN * 60 * 24;

    // 商品状态：未上架（0）——后台可编辑，前台不可见
    public static final Integer NOT_ON_SALE = 0;
    // 商品状态：在售（1）
    public static final Integer ON_SALE = 1;
    // 商品状态：已删除（-1）——软删除，保留历史订单的商品快照关联
    public static final Integer DELETED = -1;

    // Redis key 全局键前缀 mall:：单实例多项目共用时靠它隔离（private，防止业务代码绕过前缀直接拼键）
    private static final String REDIS_KEY_PREFIX = "mall:";

    // 管理员 token 标识：与用户端 token 分开，避免权限混淆
    public static final String TOKEN_ADMIN = "adminToken";

    // 秒杀商品库存 redis key 前缀 mall:rushing:stock:Redis 是秒杀扣减的唯一权威，DB 只做最终一致
    public static final String REDIS_KEY_RUSHING_STOCK = REDIS_KEY_PREFIX + "rushing:stock:";
    // 秒杀库存 -1代表无限制库存：秒杀活动"不限购"的哨兵值，扣减前必须先比对它
    public static final int RUSHING_STOCK_UNLIMITED = -1;

    // 秒杀库存同步锁 key 前缀，用于库存 DB→Redis 同步时加分布式锁
    // 回同步标记 mall:rushing:stock:syncing:{skuId}：标记"该 SKU 正在把 Redis 库存刷回 DB"，防并发重复同步
    public static final String REDIS_KEY_RUSHING_STOCK_SYNCING = REDIS_KEY_PREFIX + "rushing:stock:syncing:";

    // 标记该秒杀商品库存已耗尽的key前缀
    // 售罄标记 mall:rushing:stock:depleted:{skuId}：置位后直接拒绝请求，省掉一次库存查询
    public static final String REDIS_KEY_RUSHING_STOCK_DEPLETED = REDIS_KEY_PREFIX + "rushing:stock:depleted:";

    // 库存同步分布式锁过期时间，单位秒
    // 同步锁 30 秒：一次 DB 同步的最长容忍时间，超时自动释放防死锁
    public static final long RUSHING_STOCK_SYNC_LOCK_SECONDS = 30L;

    // 秒杀优惠券 redis key 前缀 mall:rushing:coupon:{couponId}
    public static final String REDIS_KEY_RUSHING_COUPON = REDIS_KEY_PREFIX + "rushing:coupon:";

    // 秒杀用户参与记录key前缀：mall:rushing:userId:{...}：用于"每人限购 N 件"的判重
    public static final String REDIS_KEY_RUSHING_USERID = REDIS_KEY_PREFIX + "rushing:userId:";

    // 邮箱验证码 mall:email:code:{email}：注册/找回密码共用，有效期由发送方决定
    public static final String REDIS_KEY_EMAIL_CODE = REDIS_KEY_PREFIX + "email:code:";

    // 接口限流计数前缀 mall:rate:limit:{...}：配合固定窗口/滑动窗口使用
    public static final String REDIS_RATE_LIMIT = REDIS_KEY_PREFIX + "rate:limit:";

    // 百度 access_token 缓存键（无动态后缀）：全局一个，过期前复用，避免每次审核都换 token
    public static final String REDIS_BAIDU_ACCESS_TOKEN = REDIS_KEY_PREFIX + "baidu:access_token";

    // 用户临时封禁标记 mall:user:temp_ban:{userId}：与 MQ 延时解封消息配对使用
    public static final String REDIS_USER_TEMP_BAN = REDIS_KEY_PREFIX + "user:temp_ban:";

    // 秒杀按用户限流键 mall:rush:rate:user:{userId}：挡脚本单用户高频刷接口
    public static final String REDIS_KEY_RUSH_RATE_USER = REDIS_KEY_PREFIX + "rush:rate:user:";

    // 秒杀按券全局限流键 mall:rush:rate:coupon:{couponId}：挡全站对同一券的洪峰
    public static final String REDIS_KEY_RUSH_RATE_COUPON = REDIS_KEY_PREFIX + "rush:rate:coupon:";

    // 单用户每分钟最多 30 次秒杀请求：正常手速远低于此值，超过基本可判定为脚本
    public static final int RUSH_RATE_USER_MAX_PER_MINUTE = 30;

    // 用户限流窗口 60 秒：与上面的 30 次配套，构成固定窗口
    public static final long RUSH_RATE_USER_WINDOW_SECONDS = 60L;

    // 同一张优惠券，全站所有用户加起来，每秒最多放行 200 次抢购请求，超出即快速失败而不是排队
    // [BUG-222] 注意：本常量并非生产生效值——抢券切面（CouponRushRateLimitAspect L49-50）读的是
    //           @CouponRushRateLimit 注解的 default 30/200（独立硬编码副本）；本常量只被单参重载
    //           checkCouponLimit(couponId)/checkUserLimit(userId) 引用，而这两个重载目前仅测试在调。
    //           改这里不会影响线上限流阈值（详见 ISSUES.md BUG-222）
    public static final int RUSH_RATE_COUPON_MAX_PER_SECOND = 200;

    // 优惠券秒杀限流窗口：1 秒：配合 200 次/秒，粒度更细
    public static final long RUSH_RATE_COUPON_WINDOW_SECONDS = 1L;

    // 用户最近浏览商品记录 mall:browse:recent:{userId}：Redis List/ZSet，只保留最近 N 条
    public static final String REDIS_KEY_BROWSE_RECENT = REDIS_KEY_PREFIX + "browse:recent:";

    // 最近浏览商品最大缓存数量上限 200 条：再多用户也不会翻，反而白占内存
    public static final int BROWSE_REDIS_MAX_SIZE = 200;

    // 商品布隆过滤器键 mall:product:bloom：挡"查不存在的商品 ID"的穿透请求
    public static final String REDIS_KEY_PRODUCT_BLOOM = REDIS_KEY_PREFIX + "product:bloom";

    // 预期插入量 50 万：布隆位数组大小的计算依据，估小了误判率会飙升
    public static final long PRODUCT_BLOOM_EXPECTED_INSERTIONS = 500_000L;

    // 可接受误判率 0.1%：布隆只能产生"假阳性"，所以下游必须仍做真实查询兜底
    public static final double PRODUCT_BLOOM_FALSE_PROBABILITY = 0.001;

    // 秒杀券支付金额 "0.01"：象征性收 1 分钱（支付渠道不接受 0 元支付），不是真实售价
    public static final String RUSHING_COUPON_PAY_AMOUNT = "0.01";

    // 订单最小支付金额 "0.01"：低于它的订单走 0 元流程或直接拒绝，避免支付渠道报错
    public static final String MIN_ORDER_PAY_AMOUNT = "0.01";

    // 秒杀券订单标题前缀"优惠券："：支付渠道账单上展示，便于用户核对
    public static final String COUPON_RUSH_ORDER_SUBJECT_PREFIX = "优惠券：";

    // 支付发起标记 mall:pay:trade:initiated:{...}：用于支付状态校验，同一订单只允许发起一次支付，防重复下单
    public static final String REDIS_KEY_PAY_TRADE_INITIATED = REDIS_KEY_PREFIX + "pay:trade:initiated:";

    // ==================== 管理端会话双键：token ↔ account 双向索引（与 C 端 token:web 组同构，见下） ====================
    // 这两个键是一对双向索引——管的是“管理员登录态”
    // 管理端 token 映射 mall:token:admin:{token}，门禁卡 → 持卡人。卡号是登录时发的 UUID token，值就是账号字符串。鉴权用它：请求带着卡来，查这张卡是谁的
    // 写入/读取：登录时 saveToken4Admin 写入（TTL 1 天）；网关 AuthGlobalFilter 只 hasKey 验存在、不解析内容（所以有 BUG-200 的伪造风险）；后台取账号走 getLoginInfo4Admin
    public static final String REDIS_KEY_TOKEN_ADMIN = REDIS_KEY_PREFIX + "token:admin:";
    // 管理端账号 → token 反查 mall:token:admin:account:{account}：持卡人 → 卡号。踢人用它：同一个账号再次登录时，得先找到“旧卡”才能把它作废
    // 流程：saveToken4Admin 先查它拿旧 token → 删旧正向键（旧会话立即失效，账号被盗时唯一的止血手段）→ 再发新卡；TTL 与正向一致，错位会留“幽灵会话”
    public static final String REDIS_KEY_TOKEN_ADMIN_ACCOUNT = REDIS_KEY_PREFIX + "token:admin:account:";
    // [注意] 网关 AuthGlobalFilter 硬编码了 "mall:token:admin:"（瘦网关 pom 刻意不依赖 common），改本前缀必须两头同步，否则管理员全员掉线

    /** 管理端敏感操作一次性确认票据：mall:admin:confirm:{token} */
    // 敏感操作二次确认票据：提交前先取票，票据一次性使用，防误点/防重放（键格式见上行 javadoc）
    public static final String REDIS_KEY_ADMIN_CONFIRM = REDIS_KEY_PREFIX + "admin:confirm:";

    /** 管理端敏感操作确认票据有效期（秒） */
    // 票据 300 秒有效：够用户看清确认弹窗，又不至于长期有效
    public static final int ADMIN_CONFIRM_TOKEN_TTL_SECONDS = 300;

    // 管理端登录失败计数 mall:admin:login:fail:{account}：达到阈值即锁定
    public static final String REDIS_KEY_ADMIN_LOGIN_FAIL = REDIS_KEY_PREFIX + "admin:login:fail:";

    // 管理端登录锁定标记 mall:admin:login:lock:{account}：存在即拒绝登录，与上面的计数配合
    public static final String REDIS_KEY_ADMIN_LOGIN_LOCK = REDIS_KEY_PREFIX + "admin:login:lock:";

    // 图形验证码 mall:checkcode:{...}：一次性，校验成功立即删除
    public static final String REDIS_KEY_CHECK_CODE = REDIS_KEY_PREFIX + "checkcode:";

    // ==================== C 端会话双键：token ↔ userId 双向索引（与管理端 token:admin 组同构，见上） ====================
    // 用户端 token 映射 mall:token:web:{token}，门禁卡 → 持卡人。与管理端的差别：value 存 TokenUserInfoDTO 完整对象（userId/email/nickName/token），不是单个字符串——下游业务要靠它知道“当前是哪个用户”
    // 写入/读取：登录时 saveTokenUserInfo 写入（TTL 1 天）；getTokenUserInfo 是全链路最热方法（每个请求都调），单次 GET 拿全量登录态、不回查用户库；网关 AuthGlobalFilter GET 解析 JSON 后把 userId 注入 X-User-Id 头传给下游——下单/订单归属全靠它（网关侧伪造风险见 BUG-203）
    public static final String REDIS_KEY_TOKEN_WEB = REDIS_KEY_PREFIX + "token:web:";
    // 用户 ID → token 反查 mall:token:web:userId:{userId}：用于单端登录/踢下线
    // 流程：顶号登录、改资料后刷新会话、登出（cleanTokenUserInfo 先查正向拿 userId，再两把钥匙一起收）；TTL 与正向一致，错位会留“幽灵会话”
    public static final String REDIS_KEY_TOKEN_USERID_WEB = REDIS_KEY_PREFIX + "token:web:userId:";
    // [注意] 网关 AuthGlobalFilter 硬编码了 "mall:token:web:"（瘦网关 pom 刻意不依赖 common），改本前缀必须两头同步，否则 C 端全员掉线

    // 用户签到主 key 前缀 sign:userId: + 用户id，存储用户签到相关Hash数据（连续签到、累计签到天数等）
    public static final String REDIS_KEY_SIGN = REDIS_KEY_PREFIX + "sign:userId:";
    //SETBIT sign:202605:uid:1001 0 1   # 5月1日签到
    // [BUG-12] 上面的示例键与真实拼接不一致：实际是 mall:sign:month:{yyyyMM}:userId:{userId}（RedisComponent L578），示例缺 mall: 前缀且字段顺序不同
    // 按月维度签到记录前缀 sign:month:，用来存放当月签到位图Bitmap：实际键 = mall:sign:month:{yyyyMM}:userId:{userId}，一天一个 bit（bit 位 = 日 - 1）
    public static final String REDIS_KEY_SIGN_MONTH = REDIS_KEY_PREFIX + "sign:month:";

    // 签到模块内部拼接字段前缀，不是完整redis外层key，用于hash内部field拼接
    public static final String REDIS_KEY_SIGN_USERID = "userId:";

    // 签到空缓存 mall:null:sign:{...}：防 "用户当天没签到" 被反复查库（缓存穿透）
    public static final String REDIS_KEY_SIGN_NULL = REDIS_KEY_PREFIX + "null:sign:";

    // 签到日历重建分布式锁 mall:lock:rebuild:sign:{...}，防止多实例并发重建签到日历重复执行，bitmap 损坏/丢失时只允许一个实例重建
    public static final String REDIS_KEY_SIGN_REBUILD_LOCK = REDIS_KEY_PREFIX + "lock:rebuild:sign:";

    // 签到空值占位符 @SIGN_NULL@，存入Redis标识该用户无签到记录，避免反复查数据库
    public static final String REDIS_SIGN_NULL_PLACEHOLDER = "@SIGN_NULL@";

    // 签到空缓存过期时间：5分钟，空缓存不能永久存在，：太短失去意义，太长会让"刚签到"的用户看到旧状态
    public static final long SIGN_NULL_CACHE_TTL_SECONDS = 5 * 60L;

    // 签到重建锁过期时间：5秒，锁超时自动释放，避免服务宕机死锁：重建是内存级操作，超时短一些，故障后能快速恢复
    public static final long SIGN_REBUILD_LOCK_SECONDS = 5L;

    // 签到日历一次性预生成天数：365天，一次性构建一整年签到日历，更早的历史没有展示场景
    public static final int SIGN_CALENDAR_REBUILD_DAYS = 365;

    // 每日签到初始化分布式锁，保证每日签到初始化任务只执行一次
    public static final String REDIS_KEY_SIGN_DAILY_INIT_LOCK = REDIS_KEY_PREFIX + "lock:sign:daily:init";

    // 签到对账定时任务分布式锁，小时级对账，多实例只有一台机器执行对账：签到 bitmap 与 DB 定期核对，单实例执行
    public static final String REDIS_KEY_SIGN_RECONCILE_LOCK = REDIS_KEY_PREFIX + "lock:sign:reconcile:hourly";

    // 商品分类列表缓存 mall:category:list:{...}：低频变更，适合长 TTL
    public static final String REDIS_KEY_CATEGORY_LIST = REDIS_KEY_PREFIX + "category:list:";

    // 优惠券广场缓存版本号，缓存版本机制，更新优惠券时变更版本号，实现缓存失效
    public static final String REDIS_KEY_COUPON_PLAZA_CACHE_VERSION = REDIS_KEY_PREFIX + "coupon:plaza:version";

    // 优惠券广场列表缓存前缀 mall:coupon:plaza:list:{version}:{...}：版本号在键里，天然实现批量失效，存放首页展示优惠券列表
    public static final String REDIS_KEY_COUPON_PLAZA_LIST = REDIS_KEY_PREFIX + "coupon:plaza:list:";

    // 优惠券详情缓存前缀，缓存单条优惠券详情信息
    public static final String REDIS_KEY_COUPON_DETAIL = REDIS_KEY_PREFIX + "coupon:detail:";

    // 优惠券缓存重建分布式锁，防止多服务同时刷新优惠券缓存，并发重复查库
    public static final String REDIS_KEY_COUPON_REBUILD_LOCK = REDIS_KEY_PREFIX + "coupon:rebuild:lock:";

    // 优惠券空缓存占位符 @COUPON_NULL@：标识优惠券不存在，拦截缓存穿透。与签到空值占位分开，避免两个业务互相当成有效值
    public static final String REDIS_COUPON_NULL_PLACEHOLDER = "@COUPON_NULL@";

    // 优惠券缓存【逻辑过期时间】30分钟，逻辑过期：不删除key，后台异步更新缓存
    public static final long COUPON_CACHE_LOGICAL_TTL_SECONDS = 30 * 60L;

    // 优惠券缓存【物理过期时间】24小时，兜底策略，即使异步更新失败，key也会被redis删除
    public static final long COUPON_CACHE_PHYSICAL_TTL_SECONDS = 24 * 60 * 60L;

    // 优惠券空缓存过期时间：2分钟，挡住"查不存在券"的穿透，又不至于让新建券长时间不可见
    public static final long COUPON_CACHE_NULL_TTL_SECONDS = 2 * 60L;

    // 优惠券缓存重建锁过期时间：10秒，防止锁永久占用，够一次 DB 查询 + 序列化，超时后允许其他实例接手
    public static final long COUPON_CACHE_REBUILD_LOCK_SECONDS = 10L;

    // 购物车支付订单标题模板，使用 String 格式化填充商品件数，传给支付平台展示
    public static final String CART_PAY_NAME = "购物车支付-%d件商品";

    // 物流配置缓存前缀 mall:setting:logistics:{...}：后台可改，改完需主动失效
    public static final String REDIS_KEY_SETTING_LOGISTICS = REDIS_KEY_PREFIX + "setting:logistics:";

    // 签到奖励配置缓存（全局单键）：运营可调，读取方需容忍缓存为空的默认配置
    public static final String REDIS_KEY_SIGN_REWARD_CONFIG = REDIS_KEY_PREFIX + "sign:reward:config";

    // 会员等级奖励配置缓存（全局单键）
    public static final String REDIS_KEY_MEMBER_LEVEL_REWARD_CONFIG = REDIS_KEY_PREFIX + "member:level:reward:config";

    // 用户地理位置缓存 mall:user:location:{userId}：TTL 见 AppConfig#userLocationExpireDay
    public static final String REDIS_KEY_USER_LOCATION = REDIS_KEY_PREFIX + "user:location:";

    // 通知去重键 mall:notify:dedup:{...}：同一事件重复投递时只推一次，防止重复推送相同通知消息
    public static final String REDIS_KEY_NOTIFY_DEDUP = REDIS_KEY_PREFIX + "notify:dedup:";

    // 用户未读消息数量缓存 mall:user:unread:count:{userId}：读多写多，必须原子自增
    public static final String REDIS_KEY_USER_UNREAD_COUNT = REDIS_KEY_PREFIX + "user:unread:count:";

    // 用户待弹窗通知 mall:user:popup:notify:{userId}：用户下次上线时一次性弹出
    public static final String REDIS_KEY_USER_POPUP_NOTIFY = REDIS_KEY_PREFIX + "user:popup:notify:";

    // 待发送离线通知待发表（全局 List），暂存等待投递的通知，用户不在线时先存着，上线后补推
    public static final String REDIS_KEY_NOTIFY_PENDING_LIST = REDIS_KEY_PREFIX + "notify:pending:list";

    // 支付回调分布式锁 mall:pay:notify:lock:{...}：渠道会重复回调，同单只允许一个线程处理，防止支付通知重复回调，多次执行业务
    public static final String REDIS_KEY_PAY_NOTIFY_LOCK = REDIS_KEY_PREFIX + "pay:notify:lock:";

    // 订单关闭完成标记 mall:pay:close:done:{...}：关单幂等，避免重复关闭订单
    public static final String REDIS_KEY_PAY_ORDER_CLOSE_DONE = REDIS_KEY_PREFIX + "pay:close:done:";

    // 订单生命周期分布式锁 mall:pay:lifecycle:lock:{...}：支付/关单/退款三类操作互斥，防"刚支付就被关单"
    public static final String REDIS_KEY_PAY_ORDER_LIFECYCLE_LOCK = REDIS_KEY_PREFIX + "pay:lifecycle:lock:";

    // 迟到退款完成标记 mall:pay:late:refund:done:{...}：关单后到账的支付需要退款，此键保证只退一次，防止重复退款
    public static final String REDIS_KEY_PAY_LATE_REFUND_DONE = REDIS_KEY_PREFIX + "pay:late:refund:done:";

    // 用户退款幂等 mall:pay:refund:done:{orderItemId}：按订单行退款，重复点击不会重复退，避免重复执行退款逻辑
    public static final String REDIS_KEY_ORDER_REFUND_DONE = REDIS_KEY_PREFIX + "pay:refund:done:";

    // ==================== 支付单生命周期锁（Redisson 看门狗锁）的两个时间参数 ====================
    // 这把锁串行化同一 payOrderId 上的「支付回调入账」与「超时关单」：两条链路并发抢同一单时，
    // 先拿到锁的执行，另一个等满 15 秒还拿不到就抛 PayOrderLifecycleBusyException 快速失败，
    // 与订单状态机的"带旧状态条件 UPDATE"组成锁+状态机双保险——支付/关单只成功一次
    /** 支付/关单互斥锁看门狗超时（毫秒）：业务执行多久续期多久，进程崩溃后此时间自动释放 */
    // 看门狗 30 秒：加锁走 tryLock(等 15s, leaseTime=-1)，-1 = 启用看门狗——持有线程每 10s（租期的 1/3）自动续期，
    //   业务跑多久锁就活多久；它的真正语义是「进程崩溃后锁最迟 30s 自动释放」的防死锁兜底，不是"单次业务耗时上限"
    // [BUG-223] 本常量当前无人引用，线上生效值是 RedissionConfig 里硬编码的 setLockWatchdogTimeout(30_000)，
    //   两处靠注释人肉保持一致——改这里不影响线上（与 BUG-222 同族，详见 ISSUES.md）
    public static final long PAY_ORDER_LIFECYCLE_LOCK_WATCHDOG_MS = 30_000L;
    // 等锁 15 秒：拿不到就快速失败，避免请求线程被长时间占住把线程池拖垮
    // 谁在抢这把锁：OrderInfoServiceImpl 的 3 处生命周期入口（支付回调入账 paySuccess / 关单 cancelOrder / 超时关单），
    //   都经 runWithPayOrderLifecycleLock 竞争；15 秒量级依据 = 单次入账/关单事务耗时远小于它，并发时第二个请求大概率能等到第一个做完再进
    public static final long PAY_ORDER_LIFECYCLE_LOCK_WAIT_MS = 15_000L;

    //向量数据库死信队列（异常消息）
    // RAG 检索失败记录 mall:rag:fail:record:{...}：向量库写入/问答失败留痕，供人工重放
    public static final String REDIS_RAG_FAIL_RECORD = REDIS_KEY_PREFIX + "rag:fail:record:";

    // Agent 消息取消标记 mall:cancel:agent:message:userId:{userId}：前端点"停止生成"时置位，流式循环轮询它
    public static final String REDIS_KEY_CANCEL_AGENT_MESSAGE = REDIS_KEY_PREFIX + "cancel:agent:message:userId:";

    // 提示词
    // AI 提示词缓存 mall:prompt:{...}：后台可在线调整提示词，不必重启
    public static final String REDIS_KEY_PROMPT = REDIS_KEY_PREFIX + "prompt:";

    // 咨询上下文 mall:agent:consult:product:userId:{userId}：记住用户正在问哪个商品
    public static final String REDIS_KEY_AGENT_CONSULT_PRODUCT = REDIS_KEY_PREFIX + "agent:consult:product:userId:";

    // 咨询会话活跃标记：判断"当前是否有进行中的 AI 咨询"，防重复进入
    public static final String REDIS_KEY_AGENT_CONSULT_ACTIVE = REDIS_KEY_PREFIX + "agent:consult:active:";

    // 待用户确认的动作 mall:agent:pending:action:{...}：AI 提议的操作（如下单/退款）等用户点确认
    public static final String REDIS_KEY_AGENT_PENDING_ACTION = REDIS_KEY_PREFIX + "agent:pending:action:";

    // MQ 发送幂等 key mall:mq:send:idempotent:{...}，保证消息只发送一次，防止重复发消息
    public static final String REDIS_KEY_MQ_SEND_IDEMPOTENT = REDIS_KEY_PREFIX + "mq:send:idempotent:";

    // MQ 消费完成标记 mall:mq:consume:done:{...}：消费者先抢占再处理，重复消息直接 ack
    public static final String REDIS_KEY_MQ_CONSUME_IDEMPOTENT = REDIS_KEY_PREFIX + "mq:consume:done:";

    // MQ 补偿任务记录 mall:mq:compensate:{...}：投递失败/消费失败的事件登记在此等重放
    public static final String REDIS_KEY_MQ_COMPENSATE = REDIS_KEY_PREFIX + "mq:compensate:";

    // 待执行 MQ 补偿任务列表（全局）：补偿任务扫描的入口
    public static final String REDIS_KEY_MQ_COMPENSATE_PENDING = REDIS_KEY_PREFIX + "mq:compensate:pending";

    // MQ 消费重试计数 mall:mq:consume:retry:{...}：超过上限转人工/死信，防毒消息无限重投
    public static final String REDIS_KEY_MQ_CONSUME_RETRY = REDIS_KEY_PREFIX + "mq:consume:retry:";

    // MQ 自动补偿回放分布式锁：多实例只允许一个执行补偿扫描
    public static final String REDIS_KEY_MQ_COMPENSATE_AUTO_REPLAY_LOCK = REDIS_KEY_PREFIX + "mq:compensate:auto:replay:lock";

    // MQ 消费失败上报交换机名 mq.consume：与 RabbitMQConfig 的拓扑约定一致，改名必须两边同时改
    public static final String MQ_CONSUME_FAILURE_EXCHANGE = "mq.consume";

    // Agent 待处理消息上下文 mall:agent:pending:msg:userId:{userId}：多轮对话中暂存未完成的消息
    public static final String REDIS_KEY_AGENT_PENDING_MSG_CTX = REDIS_KEY_PREFIX + "agent:pending:msg:userId:";

    // 敏感词库内容缓存 mall:sensitive:word:payload：整库放一个键，读时全量加载进 Redis，接口快速校验内容
    public static final String REDIS_KEY_SENSITIVE_WORD_PAYLOAD = REDIS_KEY_PREFIX + "sensitive:word:payload";

    // 敏感词库版本号，版本变更触发重新加载词库，实现热更新，各实例据此判断是否需要重载快照
    public static final String REDIS_KEY_SENSITIVE_WORD_VERSION = REDIS_KEY_PREFIX + "sensitive:word:version";

    // 敏感词 DB 同步锁：DB → Redis 的同步任务单实例执行
    public static final String REDIS_KEY_SENSITIVE_WORD_DB_SYNC_LOCK = REDIS_KEY_PREFIX + "sensitive:word:db:sync:lock";

    // 缩略图文件名后缀 _thumbnail：FileUtils 生成/删除缩略图都靠它配对，改名会让旧缩略图变成孤儿文件
    public static final String IMAGE_THUMBNAIL_SUFFIX = "_thumbnail";

    //最近订单天数
    // "最近订单"回溯天数 15：后台首页/用户中心的订单概览窗口
    public static final Integer LATEST_ORDER_DAYS = 15;

    // 单 SKU 单笔最多买 99 件：防黄牛批量扫货，也是前端数量选择器的上限
    public static final int ORDER_MAX_BUY_COUNT_PER_SKU = 99;

}