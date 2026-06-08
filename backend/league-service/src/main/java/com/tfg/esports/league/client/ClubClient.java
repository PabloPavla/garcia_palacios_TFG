package com.tfg.esports.league.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "club-service")
public interface ClubClient {

    @PutMapping("/clubs/{id}/riot-points")
    void updateRiotPoints(@PathVariable("id") Long id, @RequestParam("amount") Integer amount);

    @org.springframework.web.bind.annotation.GetMapping("/clubs/{id}/rating")
    Integer getClubRating(@PathVariable("id") Long id);

    @org.springframework.web.bind.annotation.GetMapping("/clubs/{id}/players")
    java.util.List<java.util.Map<String, Object>> getClubPlayers(@org.springframework.web.bind.annotation.PathVariable("id") Long id);
}
