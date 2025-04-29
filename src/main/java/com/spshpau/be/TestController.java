package com.spshpau.be;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/demo")
public class TestController {
    @GetMapping
    public String hello() {
        return "Hello from user view.";
    }

    @GetMapping("/hello-admin")
    public String hello_admin() {
        // ToDo
        return "Hello from admin view.";
    }
}
