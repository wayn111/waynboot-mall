package com.wayn.domain.api.inventory.enums;

/**
 * 库存流水变更类型。
 * 用于把订单链路中的冻结、确认、释放和售后回补语义固定下来，避免库存流水里继续散落硬编码字符串。
 */
public enum InventoryChangeTypeEnum {

    /**
     * 下单冻结库存：可售库存减少，冻结库存增加。
     */
    FREEZE("FREEZE", "下单冻结库存"),

    /**
     * 支付确认库存：冻结库存减少，库存完成售卖确认。
     */
    CONFIRM("CONFIRM", "支付确认库存"),

    /**
     * 取消释放库存：冻结库存减少，可售库存增加。
     */
    RELEASE("RELEASE", "取消释放库存"),

    /**
     * 退款回补库存：已售库存重新回到可售库存。
     */
    REFUND_RETURN("REFUND_RETURN", "退款回补库存");

    private final String type;
    private final String description;

    /**
     * 构造库存变更类型。
     *
     * @param type 变更类型编码
     * @param description 变更类型描述
     */
    InventoryChangeTypeEnum(String type, String description) {
        this.type = type;
        this.description = description;
    }

    /**
     * 获取库存变更类型编码。
     *
     * @return 变更类型编码
     */
    public String getType() {
        return type;
    }

    /**
     * 获取库存变更类型描述。
     *
     * @return 变更类型描述
     */
    public String getDescription() {
        return description;
    }
}
