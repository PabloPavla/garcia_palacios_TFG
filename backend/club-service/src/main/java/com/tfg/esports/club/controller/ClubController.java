package com.tfg.esports.club.controller;

import com.tfg.esports.club.dto.ClubRequest;
import com.tfg.esports.club.dto.ClubResponse;
import com.tfg.esports.club.dto.PlayerResponse;
import com.tfg.esports.club.service.ClubService;
import com.tfg.esports.club.service.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión de clubes.
 *
 * <p>Expone endpoints bajo la ruta base {@code /clubs}.
 * El usuario autenticado se identifica a través de las cabeceras
 * {@code X-Auth-User-Id} y {@code X-Auth-Role} inyectadas por el API Gateway.</p>
 *
 * @author Pablo García Palacios
 */
@RestController
@RequestMapping("/clubs")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService   clubService;
    private final PlayerService playerService;

    /**
     * Lista todos los clubes registrados en el sistema.
     *
     * @return 200 con la lista de clubes
     */
    @GetMapping
    public ResponseEntity<List<ClubResponse>> getAllClubs() {
        return ResponseEntity.ok(clubService.getAllClubs());
    }

    /**
     * Obtiene el detalle de un club por su ID.
     *
     * @param id ID del club
     * @return 200 con los datos del club
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClubResponse> getClub(@PathVariable Long id) {
        return ResponseEntity.ok(clubService.getClubById(id));
    }

    /**
     * Obtiene los clubes que pertenecen al usuario autenticado.
     * Usa la cabecera X-Auth-User-Id inyectada por el Gateway.
     *
     * @param userId ID del usuario (cabecera del Gateway)
     * @return 200 con la lista de clubes del propietario
     */
    @GetMapping("/my-clubs")
    public ResponseEntity<List<ClubResponse>> getMyClubs(
            @RequestHeader("X-Auth-User-Id") Long userId) {
        return ResponseEntity.ok(clubService.getClubsByOwner(userId));
    }

    /**
     * Crea un nuevo club para el usuario autenticado.
     *
     * @param request datos del nuevo club
     * @param userId  ID del propietario (cabecera del Gateway)
     * @return 201 con el club creado
     */
    @PostMapping
    public ResponseEntity<ClubResponse> createClub(
            @Valid @RequestBody ClubRequest request,
            @RequestHeader("X-Auth-User-Id") Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clubService.createClub(request, userId));
    }

    /**
     * Actualiza los datos de un club existente.
     * Solo el propietario puede modificar su propio club.
     *
     * @param id      ID del club
     * @param request nuevos datos del club
     * @param userId  ID del propietario autenticado
     * @return 200 con el club actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClubResponse> updateClub(
            @PathVariable Long id,
            @Valid @RequestBody ClubRequest request,
            @RequestHeader("X-Auth-User-Id") Long userId) {
        return ResponseEntity.ok(clubService.updateClub(id, request, userId));
    }

    /**
     * Elimina un club del sistema. Solo para administradores.
     *
     * @param id   ID del club a eliminar
     * @param role rol del usuario autenticado (debe ser ROLE_ADMIN)
     * @return 200 con mensaje de confirmación
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteClub(
            @PathVariable Long id,
            @RequestHeader("X-Auth-Role") String role) {
        if (!"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo los administradores pueden eliminar clubes"));
        }
        clubService.deleteClub(id);
        return ResponseEntity.ok(Map.of("message", "Club eliminado correctamente"));
    }

    /**
     * Obtiene la plantilla de jugadores de un club concreto.
     *
     * @param id ID del club
     * @return 200 con la lista de jugadores del club
     */
    @GetMapping("/{id}/players")
    public ResponseEntity<List<PlayerResponse>> getClubPlayers(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.getPlayersByClub(id));
    }

    /**
     * Genera jugadores base para una liga recién creada.
     *
     * @param leagueId ID de la liga
     * @param role rol del usuario autenticado (debe ser admin o owner)
     * @return 200 con confirmación
     */
    @PostMapping("/generate-players")
    public ResponseEntity<Map<String, String>> generatePlayersForLeague(
            @RequestParam Long leagueId) {
        playerService.generatePlayersForLeague(leagueId);
        return ResponseEntity.ok(Map.of("message", "Jugadores generados correctamente para la liga " + leagueId));
    }

    /**
     * Obtiene la valoración global de un club.
     */
    @GetMapping("/{id}/rating")
    public ResponseEntity<Integer> getClubRating(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.getClubRating(id));
    }
}
