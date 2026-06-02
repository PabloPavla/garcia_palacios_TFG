package com.tfg.esports.league.controller;

import com.tfg.esports.league.repository.MatchRepository;
import com.tfg.esports.league.repository.LeagueClubRepository;
import com.tfg.esports.league.entity.LeagueClub;
import com.tfg.esports.league.entity.Match;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import org.springframework.data.domain.Pageable;
import com.tfg.esports.league.entity.League;
import com.tfg.esports.league.repository.LeagueRepository;
import com.tfg.esports.league.entity.LeagueClubId;

import com.tfg.esports.league.service.LeagueService;

@RestController
@RequestMapping("/test/league")
@RequiredArgsConstructor
public class TestController {
    
    private final MatchRepository matchRepository;
    private final LeagueClubRepository leagueClubRepository;
    private final LeagueRepository leagueRepository;
    private final LeagueService leagueService;

    @PostMapping("/reset-tournament/{leagueId}")
    @Transactional
    public String resetTournament(@PathVariable Long leagueId) {
        // Delete all matches
        List<Match> matches = matchRepository.findByLeagueId(leagueId, Pageable.unpaged()).getContent();
        matchRepository.deleteAll(matches);

        // Delete all enrolled clubs
        List<LeagueClub> clubs = leagueClubRepository.findStandingsByLeagueId(leagueId);
        leagueClubRepository.deleteAll(clubs);

        return "League " + leagueId + " tournament reset. All matches and clubs removed.";
    }

    @PostMapping("/enroll-bots/{leagueId}")
    @Transactional
    public String enrollBots(@PathVariable Long leagueId) {
        League league = leagueRepository.findById(leagueId).orElseThrow();
        for (long botId = 991; botId <= 993; botId++) {
            if (!leagueClubRepository.existsByIdLeagueIdAndIdClubId(leagueId, botId)) {
                LeagueClub lc = LeagueClub.builder()
                    .id(new LeagueClubId(leagueId, botId))
                    .league(league)
                    .points(0)
                    .wins(0)
                    .losses(0)
                    .draws(0)
                    .goalsFor(0)
                    .goalsAgainst(0)
                    .build();
                leagueClubRepository.save(lc);
            }
        }
        return "Bots 991, 992, 993 enrolled in league " + leagueId;
    }
}
