package com.tfg.esports.transfer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "CLUB-SERVICE")
public interface ClubClient {

    @PostMapping("/players/{id}/sign")
    void signPlayer(@PathVariable("id") Long id, @RequestParam("clubId") Long clubId);

    @PutMapping("/clubs/{clubId}/rp/deduct")
    void deductRp(@PathVariable("clubId") Long clubId, @RequestParam("amount") Integer amount);

    @PutMapping("/clubs/{clubId}/rp/add")
    void addRp(@PathVariable("clubId") Long clubId, @RequestParam("amount") Integer amount);

    @GetMapping("/players/{id}/owner")
    Long getPlayerOwnerClubId(@PathVariable("id") Long id);

    @PutMapping("/players/{id}/transfer")
    void transferPlayer(@PathVariable("id") Long id, @RequestParam("toClubId") Long toClubId);

    @PutMapping("/players/swap")
    void swapPlayers(@RequestParam("player1Id") Long player1Id, @RequestParam("player2Id") Long player2Id);
}
