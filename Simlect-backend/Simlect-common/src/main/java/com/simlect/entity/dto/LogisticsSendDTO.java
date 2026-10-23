package com.simlect.entity.dto;

/*
 * LogisticsSendDTO —— 后台配置的"默认发货人信息"载体：寄件人姓名、电话、地址三件套。
 *
 * 在项目里扮演什么角色：
 *   发货单上的"寄件方"填写模板。这个信息不是每单手填的，而是运营在后台设置一次、
 *   存进 Redis（RedisComponent#saveLogistics，key 为 REDIS_KEY_SETTING_LOGISTICS），
 *   之后所有订单生成物流记录时都从这里取值回填。
 *
 * 被谁使用：
 *   Simlect-admin SettingController —— 后台保存设置（写）；
 *   Simlect-order OrderInfoServiceImpl —— 下单/发货时 getLogisticsInfo() 读出，逐字段拷进
 *   OrderLogisticsInfo 的 senderName / senderPhone / senderAddress（读）。
 *
 * 约定（改动前必读）：
 *   1. 这是"全局一份"的配置快照，不做订单级隔离；同一时间所有订单共用同一个寄件人；
 *   2. 新增字段要同步 OrderLogisticsInfo 的字段与前端设置页，否则存了也没人读；
 *   3. 按业务规范，DTO 的 getter/setter 属于样板代码，不逐行加注释。
 */

// LogisticsSendDTO——内部 DTO，服务间传递数据。
// [补充] 实为"默认寄件人配置"的缓存载体（Redis 单 key 覆盖写），不是每次调用临时组装的数据包。
public class LogisticsSendDTO {

    // 寄件人姓名：既是对外展示的发货方，也是历史上按名索引的查询维度（该查询已因 key 不一致失效）
    private String senderName;

    // 寄件人电话：物流单必填项，用户收货异常时由快递员联系
    private String senderPhone;

    // 寄件人详细地址：发货地，物流轨迹的起点；
    private String senderAddress;

    public String getSenderPhone() {
        return senderPhone;
    }

    public void setSenderPhone(String senderPhone) {
        this.senderPhone = senderPhone;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
}
