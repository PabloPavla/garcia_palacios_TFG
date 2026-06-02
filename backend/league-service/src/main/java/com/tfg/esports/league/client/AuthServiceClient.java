package com.tfg.esports.league.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "AUTH-SERVICE", path = "/auth/friends")
public interface AuthServiceClient {

    @GetMapping("/check")
    Map<String, Boolean> checkFriendship(
            @RequestParam("userId1") Long userId1,
            @RequestParam("userId2") Long userId2);
}
