package com.example.medical.service;

import org.springframework.stereotype.Service;

/** 医疗系统初始化阶段的服务连通性示例。 */
@Service
public class HelloService {

    public String hello() {
        return "Hello World! Medical System";
    }
}
