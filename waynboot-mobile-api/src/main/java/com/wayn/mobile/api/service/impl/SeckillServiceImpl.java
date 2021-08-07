package com.wayn.mobile.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.util.R;
import com.wayn.mobile.api.domain.Seckill;
import com.wayn.mobile.api.mapper.SeckillMapper;
import com.wayn.mobile.api.service.ISeckillService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * 秒杀库存表 服务实现类
 * </p>
 *
 * @author wayn
 * @since 2020-08-04
 */
@Service
@AllArgsConstructor
public class SeckillServiceImpl extends ServiceImpl<SeckillMapper, Seckill> implements ISeckillService {

    private final AtomicInteger atomicInteger = new AtomicInteger(0);
    private SeckillMapper seckillMapper;

    /**
     * 使用
     *
     * @param id
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public R updateSec(Long id) {
        Seckill seckill = getById(id);
        if (seckill.getNumber() <= 0) {
            return R.error();
        }
        Integer newNum = seckill.getNumber() - 1;
        Integer oldVersion = seckill.getVersion();
        Integer newVersion = seckill.getVersion() + 1;
        System.out.println(Thread.currentThread().getName() + ":剩余库存：" + newNum);
        boolean b = seckillMapper.updateSec(id, newVersion, oldVersion, newNum);
        System.out.println("result:" + b);
        int i = atomicInteger.incrementAndGet();
        System.out.println("update count:" + i);
        return R.result(b);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public R updateSec1(Long id) {
        Seckill seckill = getById(id);
        if (seckill.getNumber() <= 0) {
            return R.error();
        }
        Integer oldNum = seckill.getNumber();
        Integer newNum = seckill.getNumber() - 1;
        System.out.println(Thread.currentThread().getName() + ":剩余库存：" + newNum);
        boolean b = seckillMapper.updateSec1(id, newNum, oldNum);
        System.out.println("result:" + b);
        int i = atomicInteger.incrementAndGet();
        System.out.println("update count:" + i);
        return R.result(b);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public R updateSec2(Long id) {
        boolean b = seckillMapper.updateSec2(id);
        System.out.println("result:" + b);
        return R.result(b);
    }
}
