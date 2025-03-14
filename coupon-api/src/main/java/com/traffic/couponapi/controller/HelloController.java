package com.traffic.couponapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() throws InterruptedException {
        Thread.sleep(500);
        return "hello!";
    } // 초당 2건을 처리 * N ( 서버에서 동시에 처리할 수 있는 수) - 톰캣에서 Thread pool

}
