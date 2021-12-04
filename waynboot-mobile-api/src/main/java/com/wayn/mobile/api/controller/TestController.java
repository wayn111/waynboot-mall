package com.wayn.mobile.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class TestController {

    @GetMapping("shutdown")
    public String gracefullyShutdown() throws InterruptedException {
        Thread.sleep(15000);
        return "gracefully shutdown!";
    }
}
