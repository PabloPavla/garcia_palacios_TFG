package com.tfg.esports.club.service;

import com.tfg.esports.club.dto.PlayerRequest;
import com.tfg.esports.club.dto.PlayerResponse;
import com.tfg.esports.club.entity.Club;
import com.tfg.esports.club.entity.LolRole;
import com.tfg.esports.club.entity.Player;
import com.tfg.esports.club.repository.ClubRepository;
import com.tfg.esports.club.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio de negocio para la gestión de jugadores.
 *
 * <p>Gestiona el mercado de fichajes (agentes libres) y la plantilla
 * de cada club. Valida las reglas de negocio antes de que el trigger
 * de BD actúe como segunda línea de defensa.</p>
 *
 * @author Pablo García Palacios
 */
@Service
@RequiredArgsConstructor
public class PlayerService {

    /** Máximo de jugadores por rol en un mismo club */
    private static final int MAX_PLAYERS_PER_ROLE = 2;

    private final PlayerRepository playerRepository;
    private final ClubRepository   clubRepository;

    /**
     * Obtiene todos los jugadores libres del mercado de forma paginada.
     *
     * @param pageable configuración de paginación y ordenación
     * @return página de jugadores agentes libres
     */
    @Transactional(readOnly = true)
    public Page<PlayerResponse> getFreeAgents(Pageable pageable) {
        return playerRepository.findByIsFreeAgentTrue(pageable)
                .map(PlayerResponse::fromEntity);
    }

    /**
     * Filtra agentes libres del mercado por rol.
     *
     * @param role    rol de LoL a filtrar
     * @param pageable paginación
     * @return página de jugadores libres con ese rol
     */
    @Transactional(readOnly = true)
    public Page<PlayerResponse> getFreeAgentsByRole(LolRole role, Pageable pageable) {
        return playerRepository.findByIsFreeAgentTrueAndLolRole(role, pageable)
                .map(PlayerResponse::fromEntity);
    }

    /**
     * Busca jugadores libres por nombre de invocador.
     *
     * @param name    fragmento del nombre a buscar
     * @param pageable paginación
     * @return página de resultados coincidentes
     */
    @Transactional(readOnly = true)
    public Page<PlayerResponse> searchFreeAgents(String name, Pageable pageable) {
        return playerRepository
                .findByIsFreeAgentTrueAndSummonerNameContainingIgnoreCase(name, pageable)
                .map(PlayerResponse::fromEntity);
    }

    /**
     * Obtiene la plantilla completa de un club.
     *
     * @param clubId ID del club
     * @return lista con todos los jugadores del club
     */
    @Transactional(readOnly = true)
    public List<PlayerResponse> getPlayersByClub(Long clubId) {
        return playerRepository.findByClubId(clubId)
                .stream()
                .map(PlayerResponse::fromEntity)
                .toList();
    }

    /**
     * Obtiene el detalle de un jugador por su ID.
     *
     * @param id ID del jugador
     * @return DTO del jugador
     * @throws IllegalArgumentException si el jugador no existe
     */
    @Transactional(readOnly = true)
    public PlayerResponse getPlayerById(Long id) {
        return PlayerResponse.fromEntity(findPlayerOrThrow(id));
    }

    /**
     * Crea un nuevo jugador en el sistema (solo ADMIN).
     * El jugador se crea como agente libre por defecto.
     *
     * @param request datos del jugador a crear
     * @return DTO del jugador creado
     */
    @Transactional
    public PlayerResponse createPlayer(PlayerRequest request) {
        Player player = Player.builder()
                .summonerName(request.getSummonerName())
                .realName(request.getRealName())
                .nationality(request.getNationality())
                .age(request.getAge())
                .lolRole(request.getLolRole())
                .marketValue(request.getMarketValue() != null
                        ? request.getMarketValue() : new BigDecimal("50000.00"))
                .overallRating(request.getOverallRating() != null
                        ? request.getOverallRating() : 70)
                .isFreeAgent(true)
                .build();

        return PlayerResponse.fromEntity(playerRepository.save(player));
    }

    /**
     * Actualiza los datos de un jugador existente (solo ADMIN).
     *
     * @param id      ID del jugador a actualizar
     * @param request nuevos datos del jugador
     * @return DTO del jugador actualizado
     * @throws IllegalArgumentException si el jugador no existe
     */
    @Transactional
    public PlayerResponse updatePlayer(Long id, PlayerRequest request) {
        Player player = findPlayerOrThrow(id);

        player.setSummonerName(request.getSummonerName());
        if (request.getRealName()    != null) player.setRealName(request.getRealName());
        if (request.getNationality() != null) player.setNationality(request.getNationality());
        if (request.getAge()         != null) player.setAge(request.getAge());
        if (request.getLolRole()     != null) player.setLolRole(request.getLolRole());
        if (request.getMarketValue() != null) player.setMarketValue(request.getMarketValue());
        if (request.getOverallRating() != null) player.setOverallRating(request.getOverallRating());

        return PlayerResponse.fromEntity(playerRepository.save(player));
    }

    /**
     * Ficha a un jugador libre para un club concreto.
     *
     * <p>Valida que:
     * <ul>
     *   <li>El jugador sea agente libre</li>
     *   <li>El club exista</li>
     *   <li>El club no supere el límite de 2 jugadores por rol</li>
     * </ul>
     * El trigger de BD actúa como segunda barrera de seguridad.</p>
     *
     * @param playerId ID del jugador a fichar
     * @param clubId   ID del club que ficha al jugador
     * @return DTO del jugador actualizado con su nuevo club
     * @throws IllegalArgumentException si no se cumplen las condiciones
     */
    @Transactional
    public PlayerResponse signPlayer(Long playerId, Long clubId) {
        Player player = findPlayerOrThrow(playerId);
        Club   club   = findClubOrThrow(clubId);

        // El jugador debe ser agente libre
        if (!player.getIsFreeAgent()) {
            throw new IllegalArgumentException(
                    "El jugador no es un agente libre");
        }
        // Verificar límite de rol en el club (primera línea de defensa)
        long roleCount = playerRepository.countByClubIdAndLolRole(
                clubId, player.getLolRole());
        if (roleCount >= MAX_PLAYERS_PER_ROLE) {
            throw new IllegalArgumentException(
                    "El club ya tiene el máximo de jugadores en el rol: " + player.getLolRole());
        }

        player.setClub(club);
        player.setIsFreeAgent(false);

        return PlayerResponse.fromEntity(playerRepository.save(player));
    }

    /**
     * Libera a un jugador de su club, convirtiéndolo en agente libre.
     *
     * @param playerId ID del jugador a liberar
     * @return DTO del jugador actualizado
     * @throws IllegalArgumentException si el jugador no existe o ya es agente libre
     */
    @Transactional
    public PlayerResponse releasePlayer(Long playerId) {
        Player player = findPlayerOrThrow(playerId);

        if (player.getIsFreeAgent()) {
            throw new IllegalArgumentException("El jugador ya es un agente libre");
        }

        player.setClub(null);
        player.setIsFreeAgent(true);

        return PlayerResponse.fromEntity(playerRepository.save(player));
    }

    /**
     * Busca un jugador por ID o lanza excepción.
     *
     * @param id ID del jugador
     * @return entidad del jugador
     * @throws IllegalArgumentException si el jugador no existe
     */
    private Player findPlayerOrThrow(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Jugador no encontrado con ID: " + id));
    }

    /**
     * Busca un club por ID o lanza excepción.
     *
     * @param id ID del club
     * @return entidad del club
     * @throws IllegalArgumentException si el club no existe
     */
    private Club findClubOrThrow(Long id) {
        return clubRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Club no encontrado con ID: " + id));
    }
}
