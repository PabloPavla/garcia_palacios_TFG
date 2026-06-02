package com.tfg.esports.club.controller;

import com.tfg.esports.club.dto.PlayerRequest;
import com.tfg.esports.club.dto.PlayerResponse;
import com.tfg.esports.club.entity.LolRole;
import com.tfg.esports.club.service.PlayerService;
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
 * Controlador REST para la gestión del mercado de jugadores.
 *
 * <p>Expone endpoints bajo la ruta base {@code /players}.
 * Permite buscar agentes libres, ver detalle de jugadores y
 * realizar operaciones de fichaje/liberación.</p>
 *
 * @author Pablo García Palacios
 */
@RestController
@RequestMapping("/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    /**
     * Obtiene el mercado de jugadores libres de forma paginada para una liga.
     * Permite filtrar por rol y buscar por nombre de invocador.
     *
     * @param leagueId ID de la liga (requerido)
     * @param role   rol de LoL a filtrar (opcional)
     * @param search fragmento del nombre a buscar (opcional)
     * @param pageable configuración de paginación (por defecto: 20 por página, orden por rating desc)
     * @return 200 con la página de jugadores libres
     */
    @GetMapping("/league/{leagueId}")
    public ResponseEntity<Page<PlayerResponse>> getFreeAgents(
            @PathVariable Long leagueId,
            @RequestParam(required = false) LolRole role,
            @RequestParam(required = false) String  search,
            @PageableDefault(size = 20, sort = "overallRating") Pageable pageable) {

        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(playerService.searchFreeAgents(leagueId, search, pageable));
        }
        if (role != null) {
            return ResponseEntity.ok(playerService.getFreeAgentsByRole(leagueId, role, pageable));
        }
        return ResponseEntity.ok(playerService.getFreeAgents(leagueId, pageable));
    }

    /**
     * Obtiene el mercado de TODOS los jugadores (libres y con club) para una liga.
     */
    @GetMapping("/league/{leagueId}/all")
    public ResponseEntity<Page<PlayerResponse>> getAllPlayersInLeague(
            @PathVariable Long leagueId,
            @RequestParam(required = false) LolRole role,
            @RequestParam(required = false) String  search,
            @PageableDefault(size = 20, sort = "overallRating") Pageable pageable) {
            
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(playerService.searchAllPlayers(leagueId, search, pageable));
        }
        if (role != null) {
            return ResponseEntity.ok(playerService.getAllPlayersByRole(leagueId, role, pageable));
        }
        return ResponseEntity.ok(playerService.getAllPlayers(leagueId, pageable));
    }

    @GetMapping("/{id}/owner")
    public ResponseEntity<Long> getPlayerOwnerClubId(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.getPlayerById(id).getClubId());
    }

    /**
     * Obtiene el detalle completo de un jugador.
     *
     * @param id ID del jugador
     * @return 200 con los datos del jugador
     */
    @GetMapping("/{id}")
    public ResponseEntity<PlayerResponse> getPlayer(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.getPlayerById(id));
    }

    /**
     * Crea un nuevo jugador en el sistema. Solo para administradores.
     *
     * @param request datos del jugador
     * @param role    rol del usuario autenticado (debe ser ROLE_ADMIN)
     * @return 201 con el jugador creado
     */
    @PostMapping
    public ResponseEntity<?> createPlayer(
            @Valid @RequestBody PlayerRequest request,
            @RequestHeader("X-Auth-Role") String role) {
        if (!"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo los administradores pueden crear jugadores"));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(playerService.createPlayer(request));
    }

    /**
     * Actualiza los datos de un jugador existente. Solo para administradores.
     *
     * @param id      ID del jugador
     * @param request nuevos datos del jugador
     * @param role    rol del usuario autenticado
     * @return 200 con el jugador actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePlayer(
            @PathVariable Long id,
            @Valid @RequestBody PlayerRequest request,
            @RequestHeader("X-Auth-Role") String role) {
        if (!"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo los administradores pueden actualizar jugadores"));
        }
        return ResponseEntity.ok(playerService.updatePlayer(id, request));
    }

    /**
     * Ficha a un jugador libre para el club del usuario autenticado.
     *
     * @param id     ID del jugador a fichar
     * @param clubId ID del club que realiza el fichaje
     * @param userId ID del usuario autenticado (del Gateway)
     * @return 200 con el jugador actualizado
     */
    @PostMapping("/{id}/sign")
    public ResponseEntity<PlayerResponse> signPlayer(
            @PathVariable Long id,
            @RequestParam Long clubId,
            @RequestHeader("X-Auth-User-Id") Long userId) {
        return ResponseEntity.ok(playerService.signPlayer(id, clubId));
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<PlayerResponse> releasePlayer(
            @PathVariable Long id,
            @RequestHeader("X-Auth-Role") String role) {
        return ResponseEntity.ok(playerService.releasePlayer(id));
    }

    /**
     * Transfiere un jugador con dueño a otro club.
     */
    @PutMapping("/{id}/transfer")
    public ResponseEntity<PlayerResponse> transferPlayer(
            @PathVariable Long id,
            @RequestParam Long toClubId) {
        return ResponseEntity.ok(playerService.transferPlayer(id, toClubId));
    }

    /**
     * Intercambia dos jugadores.
     */
    @PutMapping("/swap")
    public ResponseEntity<Void> swapPlayers(
            @RequestParam Long player1Id,
            @RequestParam Long player2Id) {
        playerService.swapPlayers(player1Id, player2Id);
        return ResponseEntity.ok().build();
    }
}
