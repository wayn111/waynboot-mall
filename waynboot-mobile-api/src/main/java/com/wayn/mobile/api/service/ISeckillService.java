package com.wayn.mobile.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.util.R;
import com.wayn.mobile.api.domain.Seckill;

/**
 * <p>
 * 秒杀库存表 服务类
 * </p>
 *
 * @author wayn
 * @since 2020-08-04
 */
public interface ISeckillService extends IService<Seckill> {

    /**
     * 使用当前库存用作乐观锁
     * @param id ID
     * @return R
     */
    R updateSec(Long id);

    /**
     * 使用版本字段用作乐观锁
     * @param id ID
     * @return R
     */
    R updateSec1(Long id);

    /**
     * 使用number - 1 > 0作为乐观锁，在原子操作中自己查询一遍number的值，并将其扣减掉1
     * @param id ID
     * @return R
     */
    R updateSec2(Long id);
}
