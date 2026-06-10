package com.tfg.esports.league.repository;

import com.tfg.esports.league.entity.LeagueClub;
import com.tfg.esports.league.entity.LeagueClubId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad {@link LeagueClub}.
 *
 * @author Pablo García Palacios
 */
@Repository
public interface LeagueClubRepository extends JpaRepository<LeagueClub, LeagueClubId> {

    /**
     * Obtiene la clasificación de una liga ordenada por puntos y diferencia de goles.
     * Esta consulta es la equivalente JPQL al stored procedure {@code sp_get_standings}.
     *
     * @param leagueId ID de la liga
     * @return lista de clubes participantes ordenados por clasificación
     */
    @Query("""
        SELECT lc FROM LeagueClub lc
        WHERE lc.id.leagueId = :leagueId
        ORDER BY
            lc.points DESC,
            (lc.goalsFor - lc.goalsAgainst) DESC,
            lc.goalsFor DESC
        """)
    List<LeagueClub> findStandingsByLeagueId(Long leagueId);

    /**
     * Comprueba si un club ya está inscrito en una liga.
     *
     * @param leagueId ID de la liga
     * @param clubId   ID del club
     * @return true si ya está inscrito
     */
    boolean existsByIdLeagueIdAndIdClubId(Long leagueId, Long clubId);

    boolean existsByIdLeagueIdAndOwnerId(Long leagueId, Long ownerId);

    List<LeagueClub> findByIdLeagueIdAndOwnerId(Long leagueId, Long ownerId);

    long countByIdLeagueId(Long leagueId);

    /**
     * Obtiene todas las inscripciones de un club en distintas ligas.
     *
     * @param clubId ID del club
     * @return lista de inscripciones
     */
    List<LeagueClub> findByIdClubId(Long clubId);

    @Query("SELECT lc FROM LeagueClub lc JOIN FETCH lc.league WHERE lc.id.clubId = :clubId")
    List<LeagueClub> findByIdClubIdWithLeague(@org.springframework.data.repository.query.Param("clubId") Long clubId);
}
