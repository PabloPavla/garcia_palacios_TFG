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
    public void generateTournament(Long leagueId, Long userId, String role) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("Liga no encontrada"));

        // Validar que solo el creador (admin de la liga) o el administrador del sistema pueda iniciar el torneo
        if (!"ROLE_ADMIN".equals(role) && !userId.equals(league.getCreatorUserId())) {
            throw new IllegalArgumentException("Solo el creador de la liga (administrador) puede comenzar el torneo.");
        }

        List<LeagueClub> clubs = leagueClubRepository.findStandingsByLeagueId(leagueId);
        if (clubs.size() < 2) {
            throw new IllegalArgumentException("Se necesitan al menos 2 clubes para comenzar el torneo.");
        }

        // Validar que el número de clubes sea par
        if (clubs.size() % 2 != 0) {
            throw new IllegalArgumentException("El torneo solo puede comenzar con un número par de clubes (2, 4, 6, 8, etc.).");
        }

        // Validar que todos los clubes tengan al menos un jugador en cada una de las 5 posiciones
        for (LeagueClub club : clubs) {
            Long clubId = club.getId().getClubId();
            List<java.util.Map<String, Object>> players;
            try {
                players = clubClient.getClubPlayers(clubId);
            } catch (Exception e) {
                throw new IllegalArgumentException("No se pudieron verificar los jugadores del club con ID " + clubId + ": " + e.getMessage());
            }

            if (players == null || players.isEmpty()) {
                throw new IllegalArgumentException("El club " + clubId + " no tiene jugadores registrados. Cada equipo debe tener al menos un jugador por rol (TOP, JUNGLE, MID, ADC, SUPPORT).");
            }

            java.util.Set<String> roles = players.stream()
                    .map(p -> (String) p.get("lolRole"))
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet());

            List<String> missingRoles = new java.util.ArrayList<>();
            for (String roleName : List.of("TOP", "JUNGLE", "MID", "ADC", "SUPPORT")) {
                if (!roles.contains(roleName)) {
                    missingRoles.add(roleName);
                }
            }

            if (!missingRoles.isEmpty()) {
                throw new IllegalArgumentException("El club con ID " + clubId + " tiene posiciones vacías en su plantilla: " + String.join(", ", missingRoles) + ". Debe fichar al menos un jugador por rol.");
            }
        }

        // Validar si ya hay partidos en la liga
        long existingMatches = matchRepository.findByLeagueId(leagueId, org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .count();

        if (existingMatches > 0) {
            throw new IllegalArgumentException("Ya existen partidos en esta liga. El torneo ya ha comenzado.");
        }

        // Generar calendario Round-Robin (Berger)
        int numTeams = clubs.size();
        for (int round = 0; round < numTeams - 1; round++) {
            int day = round / 5;
            int minuteOffset = (round % 5) * 10;
            LocalDateTime roundTime = LocalDateTime.now().plusDays(day).plusMinutes(minuteOffset);
            for (int i = 0; i < numTeams / 2; i++) {
                int homeIdx = (round + i) % (numTeams - 1);
                int awayIdx = (numTeams - 1 - i + round) % (numTeams - 1);
                if (i == 0) {
                    awayIdx = numTeams - 1;
                }
                Long homeClubId = clubs.get(homeIdx).getId().getClubId();
                Long awayClubId = clubs.get(awayIdx).getId().getClubId();

                createTournamentMatch(league, homeClubId, awayClubId, roundTime, "Jornada " + (round + 1));
            }
        }

        // Simular la Jornada 1 de forma inmediata
        List<Match> round1Matches = matchRepository.findByLeagueId(leagueId, org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .filter(m -> "Jornada 1".equals(m.getTournamentRound()))
                .toList();

        for (Match match : round1Matches) {
            simulateMatch(match);
        }

        // Resolver la liga en caso de que termine inmediatamente (por ejemplo, con 2 equipos)
        checkAndResolveLeague(leagueId);
    }

    private void createTournamentMatch(League league, Long homeClubId, Long awayClubId, LocalDateTime matchDate, String roundName) {
        Match match = Match.builder()
                .league(league)
                .homeClubId(homeClubId)
                .awayClubId(awayClubId)
                .matchDate(matchDate)
                .wagerRp(0)  // Sin apuestas monetarias en partidos de torneo
                .status(MatchStatus.SCHEDULED)
                .homeWagerAccepted(false)
                .awayWagerAccepted(false)
                .tournamentRound(roundName)
                .build();
        
        matchRepository.save(match);
    }

    /**
     * Programador automático para simular partidos planificados.
     * Se ejecuta cada 10 segundos buscando partidos programados pendientes cuya fecha sea menor o igual a ahora.
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 10000)
    @Transactional
    public void simulateScheduledMatches() {
        List<Match> pendingMatches = matchRepository.findByStatusAndMatchDateBefore(
                MatchStatus.SCHEDULED, LocalDateTime.now());
        java.util.Set<Long> checkLeagues = new java.util.HashSet<>();
        
        for (Match match : pendingMatches) {
            try {
                simulateMatch(match);
                checkLeagues.add(match.getLeague().getId());
            } catch (Exception e) {
                System.err.println("Error al simular partido programado " + match.getId() + ": " + e.getMessage());
            }
        }

        // Comprobar si las ligas simuladas han finalizado
        for (Long leagueId : checkLeagues) {
            checkAndResolveLeague(leagueId);
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
            // Draw - can happen in league matches
            homeGoals = 1; awayGoals = 1;
        }

        MatchScoreRequest scoreRequest = new MatchScoreRequest();
        scoreRequest.setHomeScore(homeGoals);
        scoreRequest.setAwayScore(awayGoals);

        // Record result WITHOUT wagers (no RP deduction or rewards)
        matchService.recordResultForTournament(match.getId(), scoreRequest);

        // Bonificación del 10% al club ganador
        if (homeGoals > awayGoals) {
            applyWinBonus(match.getHomeClubId());
        } else if (awayGoals > homeGoals) {
            applyWinBonus(match.getAwayClubId());
        }
    }

    private void applyWinBonus(Long clubId) {
        try {
            java.util.Map<String, Object> club = clubClient.getClub(clubId);
            if (club != null && club.containsKey("riotPoints")) {
                Number rpVal = (Number) club.get("riotPoints");
                int currentRp = rpVal != null ? rpVal.intValue() : 0;
                int bonus = (int) Math.ceil(currentRp * 0.10);
                if (bonus < 1) bonus = 1; // Garantizar al menos 1 RP de incremento
                clubClient.updateRiotPoints(clubId, bonus);
                System.out.println("Aplicado 10% de bonificación al club " + clubId + ": +" + bonus + " RP");
            }
        } catch (Exception e) {
            System.err.println("Error al aplicar la bonificación de victoria al club " + clubId + ": " + e.getMessage());
        }
    }

    private void checkAndResolveLeague(Long leagueId) {
        try {
            // Obtener todos los partidos de torneo de esta liga
            List<Match> allMatches = matchRepository.findByLeagueId(leagueId, org.springframework.data.domain.Pageable.unpaged())
                    .getContent()
                    .stream()
                    .filter(m -> m.getTournamentRound() != null)
                    .toList();
            
            if (allMatches.isEmpty()) return;

            // Verificar si todos están completados
            boolean allCompleted = allMatches.stream().allMatch(m -> m.getStatus() == MatchStatus.COMPLETED);
            if (allCompleted) {
                // Obtener standings
                List<LeagueClub> standings = leagueClubRepository.findStandingsByLeagueId(leagueId);
                if (standings != null && !standings.isEmpty()) {
                    LeagueClub winner = standings.get(0);
                    League league = leagueRepository.findById(leagueId).orElse(null);
                    if (league != null && Boolean.TRUE.equals(league.getActive())) {
                        league.setActive(false);
                        league.setEndDate(java.time.LocalDate.now());
                        league.setWinnerClubId(winner.getId().getClubId());
                        league.setWinnerUserId(winner.getOwnerId());
                        leagueRepository.save(league);
                        System.out.println("Liga " + leagueId + " finalizada. Ganador: Club " + winner.getId().getClubId() + ", Manager " + winner.getOwnerId());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al resolver la liga " + leagueId + ": " + e.getMessage());
        }
    }

    @Transactional
    public void acceptWager(Long matchId, Long clubId) {
        // This method is kept for backwards compatibility but does nothing
        // since league matches are simulated immediately
    }
}
