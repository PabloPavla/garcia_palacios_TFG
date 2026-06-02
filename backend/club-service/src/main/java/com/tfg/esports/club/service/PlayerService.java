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
    public Page<PlayerResponse> getFreeAgents(Long leagueId, Pageable pageable) {
        return playerRepository.findByLeagueIdAndIsFreeAgentTrue(leagueId, pageable)
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
    public Page<PlayerResponse> getFreeAgentsByRole(Long leagueId, LolRole role, Pageable pageable) {
        return playerRepository.findByLeagueIdAndIsFreeAgentTrueAndLolRole(leagueId, role, pageable)
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
    public Page<PlayerResponse> searchFreeAgents(Long leagueId, String name, Pageable pageable) {
        return playerRepository
                .findByLeagueIdAndIsFreeAgentTrueAndSummonerNameContainingIgnoreCase(leagueId, name, pageable)
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
                .priceRp(request.getPriceRp() != null
                        ? request.getPriceRp() : 500)
                .leagueId(request.getLeagueId() != null ? request.getLeagueId() : 1L)
                .overallRating(request.getOverallRating())
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
        if (request.getPriceRp()     != null) player.setPriceRp(request.getPriceRp());
        if (request.getLeagueId()    != null) player.setLeagueId(request.getLeagueId());
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

    /**
     * Genera jugadores de ejemplo para una liga recién creada.
     * Crea 50 jugadores basados en pros reales de LoL como agentes libres.
     *
     * @param leagueId ID de la liga
     */
    @Transactional
    public void generatePlayersForLeague(Long leagueId) {
        // Verificar si ya existen jugadores para esta liga
        long existing = playerRepository.countByLeagueId(leagueId);
        if (existing > 0) {
            return; // Ya hay jugadores, no duplicar
        }

        String[][] playersData = {
            // TOP laners
            {"Zeus", "Choi Woo-je", "KR", "21", "TOP", "92", "3200"},
            {"Kiin", "Kim Gi-in", "KR", "24", "TOP", "89", "2800"},
            {"Bin", "Chen Ze-Bin", "CN", "22", "TOP", "88", "2700"},
            {"BrokenBlade", "Sergen Çelik", "DE", "24", "TOP", "87", "2500"},
            {"Odoamne", "Andrei Pascu", "RO", "28", "TOP", "83", "2000"},
            {"TheShy", "Kang Seung-lok", "KR", "24", "TOP", "90", "3000"},
            {"Wunder", "Martin Hansen", "DK", "25", "TOP", "84", "2100"},
            {"Impact", "Jung Eon-yeong", "KR", "28", "TOP", "82", "1900"},
            {"Breathe", "Chen Chen", "CN", "22", "TOP", "81", "1800"},
            {"Ssumday", "Kim Chan-ho", "KR", "27", "TOP", "80", "1700"},

            // JUNGLE
            {"Oner", "Moon Hyeon-jun", "KR", "21", "JUNGLE", "91", "3100"},
            {"Canyon", "Kim Geon-bu", "KR", "22", "JUNGLE", "93", "3400"},
            {"Jankos", "Marcin Jankowski", "PL", "29", "JUNGLE", "86", "2400"},
            {"Wei", "Yan Wei", "CN", "22", "JUNGLE", "87", "2500"},
            {"Elyoya", "Javier Prades", "ES", "23", "JUNGLE", "85", "2300"},
            {"Razork", "Iván Martín", "ES", "24", "JUNGLE", "84", "2100"},
            {"Inspired", "Kacper Słoma", "PL", "23", "JUNGLE", "85", "2200"},
            {"Peanut", "Han Wang-ho", "KR", "26", "JUNGLE", "86", "2400"},
            {"Tarzan", "Lee Seung-yong", "KR", "24", "JUNGLE", "88", "2600"},
            {"Kanavi", "Seo Jin-hyeok", "KR", "23", "JUNGLE", "89", "2800"},

            // MID
            {"Faker", "Lee Sang-hyeok", "KR", "28", "MID", "97", "5000"},
            {"Caps", "Rasmus Winther", "DK", "24", "MID", "91", "3100"},
            {"Chovy", "Jeong Ji-hoon", "KR", "23", "MID", "94", "3600"},
            {"ShowMaker", "Heo Su", "KR", "23", "MID", "93", "3500"},
            {"Knight", "Zhuo Ding", "CN", "23", "MID", "92", "3300"},
            {"Humanoid", "Marek Brázda", "CZ", "24", "MID", "86", "2400"},
            {"BDD", "Gwak Bo-seong", "KR", "25", "MID", "85", "2200"},
            {"Scout", "Lee Ye-chan", "KR", "25", "MID", "87", "2500"},
            {"Rookie", "Song Eui-jin", "KR", "27", "MID", "88", "2700"},
            {"Larssen", "Emil Larsson", "SE", "24", "MID", "84", "2100"},

            // ADC
            {"Gumayusi", "Lee Min-hyeong", "KR", "22", "ADC", "92", "3300"},
            {"Viper", "Park Do-hyeon", "KR", "23", "ADC", "91", "3100"},
            {"Ruler", "Park Jae-hyuk", "KR", "26", "ADC", "90", "3000"},
            {"Upset", "Elias Lipp", "DE", "24", "ADC", "87", "2500"},
            {"Hans Sama", "Steven Liv", "FR", "24", "ADC", "86", "2300"},
            {"GALA", "Chen Wei", "CN", "23", "ADC", "89", "2800"},
            {"Comp", "Markos Stamkopoulos", "GR", "23", "ADC", "84", "2100"},
            {"Deft", "Kim Hyuk-kyu", "KR", "27", "ADC", "88", "2600"},
            {"Flakked", "Víctor Tortosa", "ES", "23", "ADC", "82", "1900"},
            {"Peyz", "Kim Su-hwan", "KR", "19", "ADC", "85", "2200"},

            // SUPPORT
            {"Keria", "Ryu Min-seok", "KR", "21", "SUPPORT", "94", "3600"},
            {"Meiko", "Tian Ye", "CN", "26", "SUPPORT", "89", "2800"},
            {"Lehends", "Son Si-woo", "KR", "26", "SUPPORT", "87", "2500"},
            {"Hylissang", "Zdravets Galabov", "BG", "28", "SUPPORT", "85", "2300"},
            {"Mikyx", "Mihael Mehle", "SI", "25", "SUPPORT", "86", "2400"},
            {"CoreJJ", "Jo Yong-in", "KR", "29", "SUPPORT", "85", "2200"},
            {"Ming", "Shi Sen-Ming", "CN", "25", "SUPPORT", "88", "2600"},
            {"BeryL", "Cho Geon-hee", "KR", "25", "SUPPORT", "86", "2400"},
            {"Vulcan", "Philippe Laflamme", "CA", "24", "SUPPORT", "83", "2000"},
            {"Trymbi", "Adrian Trybus", "PL", "23", "SUPPORT", "82", "1900"}
        };

        for (String[] p : playersData) {
            Player player = Player.builder()
                    .summonerName(p[0] + "_L" + leagueId)  // Unique per league
                    .realName(p[1])
                    .nationality(p[2])
                    .age(Integer.parseInt(p[3]))
                    .lolRole(LolRole.valueOf(p[4]))
                    .overallRating(Integer.parseInt(p[5]))
                    .priceRp(Integer.parseInt(p[6]))
                    .leagueId(leagueId)
                    .isFreeAgent(true)
                    .build();
            playerRepository.save(player);
        }
    }
}

