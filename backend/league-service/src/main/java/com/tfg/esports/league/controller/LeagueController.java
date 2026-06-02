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
            @RequestHeader("X-Auth-Role") String role) {

        if (!"ROLE_ADMIN".equals(role) && !"ROLE_OWNER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No tienes permisos para inscribir clubes"));
        }

        leagueService.enrollClub(id, clubId);
        return ResponseEntity.ok(Map.of("message", "Club inscrito correctamente"));
    }

    @PostMapping
    public ResponseEntity<League> createLeague(
            @RequestBody @jakarta.validation.Valid com.tfg.esports.league.dto.LeagueRequest request,
            @RequestHeader("X-Auth-Role") String role) {
        
        if (!"ROLE_ADMIN".equals(role) && !"ROLE_OWNER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        League league = leagueService.createLeague(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(league);
    }
}
