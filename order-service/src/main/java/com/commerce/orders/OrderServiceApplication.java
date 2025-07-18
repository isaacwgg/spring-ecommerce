package com.commerce.orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients(basePackages = {"com.commerce.common.serviceinvocation", "com.commerce.orders"})
@ComponentScan({"com.commerce.common.config", "com.commerce.orders"})
@EntityScan("com.commerce.orders.models")
@EnableDiscoveryClient //makes this services connect to the services discovery
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}