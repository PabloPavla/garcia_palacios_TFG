package com.tfg.esports.league.controller;

import com.tfg.esports.league.dto.StandingResponse;
import com.tfg.esports.league.entity.League;
import com.tfg.esports.league.service.LeagueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión de ligas.
 *
 * @author Pablo García Palacios
 */
@RestController
@RequestMapping("/leagues")
@RequiredArgsConstructor
public class LeagueController {

    private final LeagueService leagueService;

    @GetMapping
    public ResponseEntity<List<League>> getAllLeagues() {
        return ResponseEntity.ok(leagueService.getAllLeagues());
    }

    @GetMapping("/{id}")
    public ResponseEntity<League> getLeague(@PathVariable Long id) {
        return ResponseEntity.ok(leagueService.getLeagueById(id));
    }

    @GetMapping("/{id}/standings")
    public ResponseEntity<List<StandingResponse>> getStandings(@PathVariable Long id) {
        return ResponseEntity.ok(leagueService.getStandings(id));
    }

    @PostMapping("/{id}/enroll")
    public ResponseEntity<Map<String, String>> enrollClub(
            @PathVariable Long id,
            @RequestParam Long clubId,
            @RequestHeader("X-Auth-User-Id") Long userId,
            @RequestHeader(value = "X-Auth-Role", required = false) String role) {

        leagueService.enrollClub(id, clubId, userId, role);
        return ResponseEntity.ok(Map.of("message", "Club inscrito correctamente"));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<Map<String, String>> joinLeague(
            @PathVariable Long id,
            @RequestHeader("X-Auth-User-Id") Long userId) {
        leagueService.joinLeague(id, userId);
        return ResponseEntity.ok(Map.of("message", "Inscripción en la liga completada con éxito"));
    }

    @GetMapping("/my-leagues")
    public ResponseEntity<List<League>> getMyLeagues(
            @RequestHeader("X-Auth-User-Id") Long userId) {
        return ResponseEntity.ok(leagueService.getLeaguesByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<League> createLeague(
            @RequestBody @jakarta.validation.Valid com.tfg.esports.league.dto.LeagueRequest request,
            @RequestHeader("X-Auth-User-Id") Long userId) {

        League league = leagueService.createLeague(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(league);
    }

    /**
     * Obtiene las ligas en las que un club está inscrito.
     */
    @GetMapping("/by-club")
    public ResponseEntity<List<League>> getLeaguesByClub(@RequestParam Long clubId) {
        return ResponseEntity.ok(leagueService.getLeaguesByClubId(clubId));
    }
}
