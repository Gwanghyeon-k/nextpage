package com.nextpage.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InstanceController {
    @GetMapping("/healthcheck")
    public String healthcheck() {
        return "OK";
    }
}
