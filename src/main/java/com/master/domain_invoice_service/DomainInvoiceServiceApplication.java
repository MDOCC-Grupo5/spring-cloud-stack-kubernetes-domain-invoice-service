package com.master.domain_invoice_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class DomainInvoiceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DomainInvoiceServiceApplication.class, args);
    }

}
