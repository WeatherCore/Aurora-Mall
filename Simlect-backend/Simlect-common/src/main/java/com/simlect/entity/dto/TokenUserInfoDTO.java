package com.simlect.entity.dto;

/*
 * TokenUserInfoDTO 用户登录信息DTO —— 登录会话的"身份证 + 通行证"：一次登录后，用户基本信息与 token 一起
 * 存进 Redis，之后每次请求凭 token 换回它，就不再查库，用于接口鉴权
 *
 * 在项目里扮演什么角色：
 *   登录态的唯一载体。RedisComponent 以两个方向各存一份（token → 本对象、userId → 本对象），
 *   TTL 1 天；正向索引服务鉴权（每次请求都读），反向索引服务"踢人/改资料后刷新会话"。
 *   这也是本项目"无状态 JWT"之外的另一种选择：会话在服务端，登出/顶号能立即生效。
 *
 * 被谁使用：
 *   AccountController 登录时组装（userId/email/nickName/avatar）→ RedisComponent#saveTokenUserInfo 落缓存；
 *   GlobalOperationAspect、CouponRushRateLimitAspect、各业务 Controller 通过 getTokenUserInfo 取当前用户；
 *   注意它不实现 Serializable，落 Redis 走 JSON 序列化而非 JDK 序列化。
 *
 * 约定（改动前必读）：
 *   1. token 字段是"写时输入、读时可空"的混合语义：登录时前端可能带着旧 token 进来（用于顶号清理），
 *      保存后会被覆盖成新生成的值，读取场景下一般不用它；
 *   2. 这份数据是登录瞬间的快照：用户改了昵称/头像后，要么刷新会话，要么接受最长 1 天的滞后；
 *   3. 不要在 DTO 里塞敏感字段（密码、手机号全量），它会被高频读出并可能进入日志。
 */

// [补充] 不是"服务间传参"，而是 Redis 会话缓存的值对象；字段即前端展示所需的最小用户画像。
public class TokenUserInfoDTO {

    // Redis 反向 key(REDIS_KEY_TOKEN_USERID_WEB + userId) 依赖此字段
    // 用户唯一 ID ，全链路身份主键，鉴权、限流、业务归属都靠它；空值视为未登录
    private String userId;

    // 登录邮箱：既是账号标识，也用于前端展示/找回密码提示
    private String email;

    // 昵称：页面展示用；改昵称后缓存不会自动刷新，需重新登录或显式刷新会话
    private String nickName;

    // 头像地址：页面展示用，通常是相对路径或对象存储 URL
    private String avatar;
    
    // 会话 token：登录时可为旧 token（用于清理旧会话），保存后由服务端重写为新生成的 32 位串
    private String token;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
