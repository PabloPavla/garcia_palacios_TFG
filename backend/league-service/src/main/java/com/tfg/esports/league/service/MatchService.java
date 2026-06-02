package com.tfg.esports.league.service;

import com.tfg.esports.league.dto.MatchRequest;
import com.tfg.esports.league.dto.MatchResponse;
import com.tfg.esports.league.dto.MatchScoreRequest;
import com.tfg.esports.league.entity.League;
import com.tfg.esports.league.entity.Match;
import com.tfg.esports.league.entity.MatchStatus;
import com.tfg.esports.league.repository.LeagueClubRepository;
import com.tfg.esports.league.repository.LeagueRepository;
import com.tfg.esports.league.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de negocio para la gestión de partidos.
 *
 * @author Pablo García Palacios
 */
@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final LeagueRepository leagueRepository;
    private final LeagueClubRepository leagueClubRepository;

    /**
     * Obtiene todos los partidos de una liga paginados.
     *
     * @param leagueId ID de la liga
     * @param pageable paginación
     * @return página de partidos
     */
    @Transactional(readOnly = true)
    public Page<MatchResponse> getMatchesByLeague(Long leagueId, Pageable pageable) {
        return matchRepository.findByLeagueId(leagueId, pageable)
                .map(MatchResponse::fromEntity);
    }

    /**
     * Programa un nuevo partido en una liga.
     * Valida que ambos clubes estén inscritos en la liga y no jueguen contra sí mismos.
     *
     * @param request datos del partido
     * @return partido creado
     */
    @Transactional
    public MatchResponse scheduleMatch(MatchRequest request) {
        if (request.getHomeClubId().equals(request.getAwayClubId())) {
            throw new IllegalArgumentException("Un club no puede jugar contra sí mismo");
        }

        League league = leagueRepository.findById(request.getLeagueId())
                .orElseThrow(() -> new IllegalArgumentException("Liga no encontrada"));

        if (!leagueClubRepository.existsByIdLeagueIdAndIdClubId(league.getId(), request.getHomeClubId()) ||
            !leagueClubRepository.existsByIdLeagueIdAndIdClubId(league.getId(), request.getAwayClubId())) {
            throw new IllegalArgumentException("Ambos clubes deben estar inscritos en la liga");
        }

        if (matchRepository.existsMatchBetweenClubs(league.getId(), request.getHomeClubId(), request.getAwayClubId())) {
            throw new IllegalArgumentException("Ya existe un partido programado entre estos clubes");
        }

        Match match = Match.builder()
                .league(league)
                .homeClubId(request.getHomeClubId())
                .awayClubId(request.getAwayClubId())
                .matchDate(request.getMatchDate())
                .status(MatchStatus.SCHEDULED)
                .build();

        return MatchResponse.fromEntity(matchRepository.save(match));
    }

    /**
     * Registra el resultado de un partido y lo marca como completado.
     * El trigger en BD se encargará de actualizar la clasificación.
     *
     * @param id      ID del partido
     * @param request resultado
     * @return partido actualizado
     */
    @Transactional
    public MatchResponse recordResult(Long id, MatchScoreRequest request) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Partido no encontrado"));

        if (match.getStatus() == MatchStatus.COMPLETED) {
            throw new IllegalArgumentException("El partido ya está completado");
        }

        match.setHomeScore(request.getHomeScore());
        match.setAwayScore(request.getAwayScore());
        match.setStatus(MatchStatus.COMPLETED);

        // Al guardar, el trigger 'trg_update_standings_after_match' actualizará la clasificación
        return MatchResponse.fromEntity(matchRepository.save(match));
    }

    /**
     * Cancela un partido.
     *
     * @param id ID del partido
     */
    @Transactional
    public void cancelMatch(Long id) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Partido no encontrado"));

        if (match.getStatus() == MatchStatus.COMPLETED) {
            throw new IllegalArgumentException("No se puede cancelar un partido completado");
        }

        match.setStatus(MatchStatus.CANCELLED);
        matchRepository.save(match);
    }
}
