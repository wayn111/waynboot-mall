package com.wayn.mobile.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.mobile.api.domain.Seckill;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 秒杀库存表 Mapper 接口
 * </p>
 *
 * @author wayn
 * @since 2020-08-04
 */
public interface SeckillMapper extends BaseMapper<Seckill> {

    boolean updateSec(@Param("id") Long id, @Param("newVersion") Integer newVersion, @Param("oldVersion") Integer oldVersion, @Param("newNum") Integer newNum);

    boolean updateSec1(@Param("id") Long id, @Param("newNum") Integer newNum, @Param("oldNum") Integer oldNum);

    boolean updateSec2(@Param("id") Long id);
}
