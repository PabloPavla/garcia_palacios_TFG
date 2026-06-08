package com.tfg.esports.league.controller;

import com.tfg.esports.league.dto.MatchRequest;
import com.tfg.esports.league.dto.MatchResponse;
import com.tfg.esports.league.dto.MatchScoreRequest;
import com.tfg.esports.league.service.MatchService;
import com.tfg.esports.league.service.TournamentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para la gestión de partidos.
 *
 * @author Pablo García Palacios
 */
@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final TournamentService tournamentService;

    @GetMapping("/league/{leagueId}")
    public ResponseEntity<Page<MatchResponse>> getLeagueMatches(
            @PathVariable Long leagueId,
            @PageableDefault(size = 20, sort = "matchDate") Pageable pageable) {
        return ResponseEntity.ok(matchService.getMatchesByLeague(leagueId, pageable));
    }

    @PostMapping
    public ResponseEntity<?> scheduleMatch(
            @Valid @RequestBody MatchRequest request,
            @RequestHeader("X-Auth-Role") String role) {
        if (!"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo administradores pueden programar partidos"));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(matchService.scheduleMatch(request));
    }

    @PutMapping("/{id}/result")
    public ResponseEntity<?> recordResult(
            @PathVariable Long id,
            @Valid @RequestBody MatchScoreRequest request,
            @RequestHeader("X-Auth-Role") String role) {
        if (!"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo administradores pueden registrar resultados"));
        }
        return ResponseEntity.ok(matchService.recordResult(id, request));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelMatch(
            @PathVariable Long id,
            @RequestHeader("X-Auth-Role") String role) {
        if (!"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo administradores pueden cancelar partidos"));
        }
        matchService.cancelMatch(id);
        return ResponseEntity.ok(Map.of("message", "Partido cancelado"));
    }

    @PostMapping("/league/{leagueId}/tournament")
    public ResponseEntity<?> generateTournament(
            @PathVariable Long leagueId,
            @RequestHeader("X-Auth-User-Id") Long userId,
            @RequestHeader(value = "X-Auth-Role", required = false) String role) {
        tournamentService.generateTournament(leagueId, userId, role);
        return ResponseEntity.ok(Map.of("message", "Torneo generado correctamente"));
    }

    @PostMapping("/{id}/wager/accept")
    public ResponseEntity<?> acceptWager(
            @PathVariable Long id,
            @RequestParam Long clubId,
            @RequestHeader("X-Auth-User-Id") Long userId) {
        tournamentService.acceptWager(id, clubId);
        return ResponseEntity.ok(Map.of("message", "Apuesta aceptada"));
    }
}
