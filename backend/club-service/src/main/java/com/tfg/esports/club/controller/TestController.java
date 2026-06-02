package com.tfg.esports.club.controller;

import com.tfg.esports.club.repository.PlayerRepository;
import com.tfg.esports.club.entity.Player;
import com.tfg.esports.club.entity.Club;
import com.tfg.esports.club.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RestController
@RequestMapping("/test/club")
@RequiredArgsConstructor
public class TestController {
    
    private final PlayerRepository playerRepository;
    private final ClubRepository clubRepository;

    @PostMapping("/reset-players/{leagueId}")
    @Transactional
    public String resetPlayers(@PathVariable Long leagueId) {
        List<Player> players = playerRepository.findByLeagueId(leagueId, org.springframework.data.domain.Pageable.unpaged()).getContent();
        for (Player p : players) {
            p.setClub(null);
            p.setIsFreeAgent(true);
            playerRepository.save(p);
        }
        return "Players in league " + leagueId + " reset to free agents.";
    }

    @PostMapping("/create-bots")
    @Transactional
    public String createBots() {
        for (long botId = 991; botId <= 993; botId++) {
            if (!clubRepository.existsById(botId)) {
                Club bot = Club.builder()
                    .id(botId)
                    .name("Bot Club " + botId)
                    .acronym("BOT" + (botId % 10))
                    .ownerId(999L) // Dummy user
                    .division(com.tfg.esports.club.entity.Division.BRONZE)
                    .riotPoints(10000)
                    .build();
                clubRepository.save(bot);
            }
        }
        return "Bots created in club_db.";
    }
}
