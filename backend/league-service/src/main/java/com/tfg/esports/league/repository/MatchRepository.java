package com.tfg.esports.league.repository;

import com.tfg.esports.league.entity.Match;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad {@link Match}.
 *
 * @author Pablo García Palacios
 */
@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    /**
     * Lista los partidos de una liga paginados y ordenados por fecha.
     *
     * @param leagueId ID de la liga
     * @param pageable paginación
     * @return página de partidos
     */
    Page<Match> findByLeagueId(Long leagueId, Pageable pageable);

    long countByLeagueId(Long leagueId);

    /**
     * Lista los partidos (como local o visitante) de un club.
     *
     * @param clubId1 ID del club (como local)
     * @param clubId2 ID del club (como visitante)
     * @param pageable paginación
     * @return página de partidos del club
     */
    Page<Match> findByHomeClubIdOrAwayClubId(Long clubId1, Long clubId2, Pageable pageable);

    /**
     * Comprueba si ya hay un partido programado entre dos clubes en una liga.
     *
     * @param leagueId ID de la liga
     * @param home     ID del local
     * @param away     ID del visitante
     * @return true si ya existe
     */
    @Query("""
        SELECT COUNT(m) > 0 FROM Match m
        WHERE m.league.id = :leagueId
          AND ((m.homeClubId = :home AND m.awayClubId = :away)
               OR (m.homeClubId = :away AND m.awayClubId = :home))
        """)
    boolean existsMatchBetweenClubs(Long leagueId, Long home, Long away);
}
