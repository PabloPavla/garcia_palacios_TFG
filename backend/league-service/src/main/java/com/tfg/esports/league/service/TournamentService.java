package com.tfg.esports.league.service;

import com.tfg.esports.league.client.ClubClient;
import com.tfg.esports.league.dto.MatchScoreRequest;
import com.tfg.esports.league.entity.League;
import com.tfg.esports.league.entity.LeagueClub;
import com.tfg.esports.league.entity.Match;
import com.tfg.esports.league.entity.MatchStatus;
import com.tfg.esports.league.repository.LeagueClubRepository;
import com.tfg.esports.league.repository.LeagueRepository;
import com.tfg.esports.league.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TournamentService {

    private final LeagueRepository leagueRepository;
    private final LeagueClubRepository leagueClubRepository;
    private final MatchRepository matchRepository;
    private final MatchService matchService;
    private final ClubClient clubClient;

    @Transactional
    public void generateTournament(Long leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("Liga no encontrada"));

        List<LeagueClub> clubs = leagueClubRepository.findStandingsByLeagueId(leagueId);
        if (clubs.size() < 4) {
            throw new IllegalArgumentException("Se necesitan al menos 4 clubes para generar el torneo.");
        }

        // Check if a tournament already exists
        long existingTournamentMatches = matchRepository.findByLeagueId(leagueId, org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .filter(m -> m.getTournamentRound() != null)
                .count();

        if (existingTournamentMatches > 0) {
            throw new IllegalArgumentException("El torneo ya ha sido generado para esta liga.");
        }

        // Shuffle and pick 4
        Collections.shuffle(clubs);
        List<LeagueClub> participants = clubs.subList(0, 4);

        // Create 2 Semifinal Matches
        createTournamentMatch(league, participants.get(0).getId().getClubId(), participants.get(1).getId().getClubId(), "SEMIFINAL");
        createTournamentMatch(league, participants.get(2).getId().getClubId(), participants.get(3).getId().getClubId(), "SEMIFINAL");
    }

    private void createTournamentMatch(League league, Long homeClubId, Long awayClubId, String round) {
        Match match = Match.builder()
                .league(league)
                .homeClubId(homeClubId)
                .awayClubId(awayClubId)
                .matchDate(LocalDateTime.now().plusDays(1))
                .wagerRp(league.getMatchWagerRp())
                .status(MatchStatus.SCHEDULED)
                .homeWagerAccepted(false)
                .awayWagerAccepted(false)
                .tournamentRound(round)
                .build();
        matchRepository.save(match);
    }

    @Transactional
    public void acceptWager(Long matchId, Long clubId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Partido no encontrado"));

        if (match.getStatus() == MatchStatus.COMPLETED) {
            throw new IllegalArgumentException("El partido ya ha finalizado.");
        }

        if (match.getHomeClubId().equals(clubId)) {
            match.setHomeWagerAccepted(true);
        } else if (match.getAwayClubId().equals(clubId)) {
            match.setAwayWagerAccepted(true);
        } else {
            throw new IllegalArgumentException("El club no participa en este partido.");
        }

        matchRepository.save(match);

        // If both accepted, simulate the match!
        if (Boolean.TRUE.equals(match.getHomeWagerAccepted()) && Boolean.TRUE.equals(match.getAwayWagerAccepted())) {
            simulateMatch(match);
        }
    }

    private void simulateMatch(Match match) {
        Integer homeRating = clubClient.getClubRating(match.getHomeClubId());
        Integer awayRating = clubClient.getClubRating(match.getAwayClubId());

        if (homeRating == null) homeRating = 70;
        if (awayRating == null) awayRating = 70;

        // Apply random factor +/- 20%
        double homeRandom = 0.8 + (Math.random() * 0.4);
        double awayRandom = 0.8 + (Math.random() * 0.4);

        double homePower = homeRating * homeRandom;
        double awayPower = awayRating * awayRandom;

        int homeGoals = 0;
        int awayGoals = 0;

        if (homePower > awayPower + 10) {
            homeGoals = 3; awayGoals = 0;
        } else if (homePower > awayPower) {
            homeGoals = 2; awayGoals = 1;
        } else if (awayPower > homePower + 10) {
            homeGoals = 0; awayGoals = 3;
        } else if (awayPower > homePower) {
            homeGoals = 1; awayGoals = 2;
        } else {
            // Draw? In tournament we need a winner, give it randomly
            if (Math.random() > 0.5) {
                homeGoals = 2; awayGoals = 1;
            } else {
                homeGoals = 1; awayGoals = 2;
            }
        }

        MatchScoreRequest scoreRequest = new MatchScoreRequest();
        scoreRequest.setHomeScore(homeGoals);
        scoreRequest.setAwayScore(awayGoals);

        // Deduct wagers first, because recordResult will reward them
        clubClient.updateRiotPoints(match.getHomeClubId(), -match.getWagerRp());
        clubClient.updateRiotPoints(match.getAwayClubId(), -match.getWagerRp());

        matchService.recordResult(match.getId(), scoreRequest);

        // If this was a semifinal, check if both are completed to create FINAL
        if ("SEMIFINAL".equals(match.getTournamentRound())) {
            checkAndCreateFinal(match.getLeague());
        }
    }

    private void checkAndCreateFinal(League league) {
        List<Match> semifinals = matchRepository.findByLeagueId(league.getId(), org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .filter(m -> "SEMIFINAL".equals(m.getTournamentRound()))
                .collect(Collectors.toList());

        if (semifinals.size() == 2) {
            Match s1 = semifinals.get(0);
            Match s2 = semifinals.get(1);

            if (s1.getStatus() == MatchStatus.COMPLETED && s2.getStatus() == MatchStatus.COMPLETED) {
                Long winner1 = s1.getHomeScore() > s1.getAwayScore() ? s1.getHomeClubId() : s1.getAwayClubId();
                Long winner2 = s2.getHomeScore() > s2.getAwayScore() ? s2.getHomeClubId() : s2.getAwayClubId();

                // Ensure final is not already created
                long finalCount = matchRepository.findByLeagueId(league.getId(), org.springframework.data.domain.Pageable.unpaged())
                        .stream()
                        .filter(m -> "FINAL".equals(m.getTournamentRound()))
                        .count();

                if (finalCount == 0) {
                    createTournamentMatch(league, winner1, winner2, "FINAL");
                }
            }
        }
    }
}
