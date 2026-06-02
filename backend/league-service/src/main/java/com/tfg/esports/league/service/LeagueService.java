package com.tfg.esports.league.service;

import com.tfg.esports.league.dto.StandingResponse;
import com.tfg.esports.league.entity.League;
import com.tfg.esports.league.entity.LeagueClub;
import com.tfg.esports.league.entity.LeagueClubId;
import com.tfg.esports.league.repository.LeagueClubRepository;
import com.tfg.esports.league.repository.LeagueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de negocio para la gestión de ligas y clasificaciones.
 *
 * @author Pablo García Palacios
 */
@Service
@RequiredArgsConstructor
public class LeagueService {

    private final LeagueRepository leagueRepository;
    private final LeagueClubRepository leagueClubRepository;

    /**
     * Obtiene la lista de todas las ligas.
     *
     * @return lista de ligas
     */
    @Transactional(readOnly = true)
    public List<League> getAllLeagues() {
        return leagueRepository.findAll();
    }

    /**
     * Obtiene una liga por ID.
     *
     * @param id ID de la liga
     * @return la liga
     */
    @Transactional(readOnly = true)
    public League getLeagueById(Long id) {
        return findLeagueOrThrow(id);
    }

    /**
     * Inscribe a un club en una liga.
     *
     * @param leagueId ID de la liga
     * @param clubId   ID del club (desde Club Service)
     * @throws IllegalArgumentException si la liga no existe o ya está inscrito
     */
    @Transactional
    public void enrollClub(Long leagueId, Long clubId) {
        League league = findLeagueOrThrow(leagueId);

        if (leagueClubRepository.existsByIdLeagueIdAndIdClubId(leagueId, clubId)) {
            throw new IllegalArgumentException("El club ya está inscrito en esta liga");
        }

        LeagueClubId id = new LeagueClubId(leagueId, clubId);
        LeagueClub lc = LeagueClub.builder()
                .id(id)
                .league(league)
                .build();

        leagueClubRepository.save(lc);
    }

    /**
     * Obtiene la clasificación (standings) de una liga ordenados por puntos y diferencia de goles.
     *
     * @param leagueId ID de la liga
     * @return lista de resultados ordenados
     */
    @Transactional(readOnly = true)
    public List<StandingResponse> getStandings(Long leagueId) {
        // Valida que la liga existe
        findLeagueOrThrow(leagueId);

        return leagueClubRepository.findStandingsByLeagueId(leagueId)
                .stream()
                .map(StandingResponse::fromEntity)
                .toList();
    }

    private League findLeagueOrThrow(Long id) {
        return leagueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Liga no encontrada con ID: " + id));
    }
}
