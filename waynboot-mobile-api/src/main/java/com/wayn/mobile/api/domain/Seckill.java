package com.wayn.mobile.api.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 秒杀库存表
 * </p>
 *
 * @author wayn
 * @since 2020-08-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Seckill implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品库存id
     */
    @TableId(value = "seckill_id", type = IdType.AUTO)
    private Long seckillId;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 库存数量
     */
    private Integer number;

    /**
     * 秒杀开始时间
     */
    private LocalDateTime startTime;

    /**
     * 秒杀结束时间
     */
    private LocalDateTime endTime;

    /**
     * 秒杀创建时间
     */
    private LocalDateTime createTime;

    private Integer version;
}
