package com.tfg.esports.club.repository;

import com.tfg.esports.club.entity.LolRole;
import com.tfg.esports.club.entity.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad {@link Player}.
 *
 * @author Pablo García Palacios
 */
@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    /**
     * Obtiene todos los jugadores de un club específico.
     * Usado para mostrar la plantilla completa de un equipo.
     *
     * @param clubId ID del club
     * @return lista de jugadores del club
     */
    List<Player> findByClubId(Long clubId);

    /**
     * Obtiene todos los jugadores de una liga específica (paginado).
     */
    Page<Player> findByLeagueId(Long leagueId, Pageable pageable);

    /**
     * Cuenta cuántos jugadores hay en una liga.
     *
     * @param leagueId ID de la liga
     * @return número de jugadores en la liga
     */
    long countByLeagueId(Long leagueId);

    /**
     * Obtiene todos los agentes libres (sin club) de forma paginada para una liga específica.
     * Usado para el mercado de fichajes de una liga.
     *
     * @param leagueId ID de la liga
     * @param pageable configuración de paginación y ordenación
     * @return página de jugadores libres
     */
    Page<Player> findByLeagueIdAndIsFreeAgentTrue(Long leagueId, Pageable pageable);

    /**
     * Filtra agentes libres por rol específico en una liga.
     * Útil para buscar jugadores de una posición concreta en el mercado.
     *
     * @param leagueId ID de la liga
     * @param role    rol de LoL a filtrar
     * @param pageable configuración de paginación
     * @return página de jugadores libres con ese rol
     */
    Page<Player> findByLeagueIdAndIsFreeAgentTrueAndLolRole(Long leagueId, LolRole role, Pageable pageable);

    /**
     * Cuenta cuántos jugadores de un rol concreto tiene un club.
     * Se usa para verificar el límite antes de realizar un fichaje
     * (aunque el trigger de BD también lo garantiza).
     *
     * @param clubId ID del club
     * @param role   rol de LoL
     * @return número de jugadores del club con ese rol
     */
    @Query("SELECT COUNT(p) FROM Player p WHERE p.club.id = :clubId AND p.lolRole = :role")
    long countByClubIdAndLolRole(Long clubId, LolRole role);

    /**
     * Busca jugadores libres cuyo nombre de invocador contenga el texto dado en una liga.
     * Usado para el buscador del mercado de fichajes.
     *
     * @param leagueId ID de la liga
     * @param name fragmento del nombre a buscar (case-insensitive)
     * @param pageable paginación
     * @return página de resultados
     */
    Page<Player> findByLeagueIdAndIsFreeAgentTrueAndSummonerNameContainingIgnoreCase(
            Long leagueId, String name, Pageable pageable);

    /**
     * Busca TODOS los jugadores de una liga por rol.
     */
    Page<Player> findByLeagueIdAndLolRole(Long leagueId, LolRole role, Pageable pageable);

    /**
     * Busca TODOS los jugadores de una liga por nombre.
     */
    Page<Player> findByLeagueIdAndSummonerNameContainingIgnoreCase(
            Long leagueId, String name, Pageable pageable);
}
