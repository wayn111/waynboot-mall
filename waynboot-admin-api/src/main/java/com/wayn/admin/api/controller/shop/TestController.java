package com.wayn.admin.api.controller.shop;


import com.wayn.common.base.BaseElasticService;
import com.wayn.common.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class TestController {

    @Autowired
    private BaseElasticService baseElasticService;

    @GetMapping
    public R test() {
        baseElasticService.createIndex("2", "");
        return R.success();
    }
}
