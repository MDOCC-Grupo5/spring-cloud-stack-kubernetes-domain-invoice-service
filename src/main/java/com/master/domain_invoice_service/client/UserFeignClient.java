package com.master.domain_invoice_service.client;

import com.master.domain_invoice_service.dto.UserDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "domain-user-service")
public interface UserFeignClient {

    @GetMapping("/v1/{id}")
    @CircuitBreaker(name = "domainUserService", fallbackMethod = "findByIdFallback")
    UserDto findById(@PathVariable Long id);

    default UserDto findByIdFallback(Long id, Exception exception) {
        return UserDto.builder()
            .id(id)
            .firstName("n/a")
            .lastName("n/a")
            .email("n/a")
            .build();
    }

}
