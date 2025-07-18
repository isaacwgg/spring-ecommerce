package com.commerce.common.serviceinvocation;

import com.commerce.common.dto.UserRequestDTO;
import com.commerce.common.dto.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service", url = "${auth.service.url:}")
public interface AuthClient {
    @GetMapping("/api/auth/validate")
    UserResponseDTO validate(@RequestHeader("Authorization") String bearerToken);

    //    @GetMapping("/api/by-username/{username}")
    @GetMapping("/by-username/{username}")
    UserRequestDTO getByUsername(@PathVariable String username, @RequestHeader("Authorization") String bearerToken);
}
