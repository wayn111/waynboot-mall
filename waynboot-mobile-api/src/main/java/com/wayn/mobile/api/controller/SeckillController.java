package com.wayn.mobile.api.controller;


import com.wayn.common.base.controller.BaseController;
import com.wayn.common.util.R;
import com.wayn.mobile.api.service.ISeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 秒杀库存表 前端控制器
 * </p>
 *
 * @author wayn
 * @since 2020-08-04
 */
@RestController
@RequestMapping("seckill")
public class SeckillController extends BaseController {

    @Autowired
    private ISeckillService iSeckillService;

    @GetMapping("update")
    public R update(Long id) {
        return iSeckillService.updateSec(id);
    }

    @GetMapping("update1")
    public R update1(Long id) {
        return iSeckillService.updateSec1(id);
    }

    @GetMapping("update2")
    public R update2(Long id) {
        return iSeckillService.updateSec2(id);
    }
}
