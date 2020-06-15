package com.wayn.admin.api.controller.shop;

import com.wayn.admin.api.service.shop.IBannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("shop/banner")
public class BannerController {

    @Autowired
    private IBannerService iBannerService;
}
