package com.ping.ojbackenduserservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.ping.ojbackenduserservice.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
// 注意需要添加全局扫包注册其他模块的 bean
@ComponentScan("com.ping")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.ping.ojbackendserviceclient.service"})
public class OjBackendUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OjBackendUserServiceApplication.class, args);

    }

}
