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
     * Obtiene todos los agentes libres (sin club) de forma paginada.
     * Usado para el mercado de fichajes.
     *
     * @param pageable configuración de paginación y ordenación
     * @return página de jugadores libres
     */
    Page<Player> findByIsFreeAgentTrue(Pageable pageable);

    /**
     * Filtra agentes libres por rol específico.
     * Útil para buscar jugadores de una posición concreta en el mercado.
     *
     * @param role    rol de LoL a filtrar
     * @param pageable configuración de paginación
     * @return página de jugadores libres con ese rol
     */
    Page<Player> findByIsFreeAgentTrueAndLolRole(LolRole role, Pageable pageable);

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
     * Busca jugadores libres cuyo nombre de invocador contenga el texto dado.
     * Usado para el buscador del mercado de fichajes.
     *
     * @param name fragmento del nombre a buscar (case-insensitive)
     * @param pageable paginación
     * @return página de resultados
     */
    Page<Player> findByIsFreeAgentTrueAndSummonerNameContainingIgnoreCase(
            String name, Pageable pageable);
}
