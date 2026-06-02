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

    private final com.tfg.esports.league.client.AuthServiceClient authServiceClient;

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
     * Crea una nueva liga con la configuración proporcionada.
     */
    @Transactional
    public League createLeague(com.tfg.esports.league.dto.LeagueRequest request, Long creatorUserId) {
        com.tfg.esports.league.entity.LeagueVisibility visibility;
        try {
            visibility = request.getVisibility() != null 
                    ? com.tfg.esports.league.entity.LeagueVisibility.valueOf(request.getVisibility().toUpperCase())
                    : com.tfg.esports.league.entity.LeagueVisibility.PUBLIC;
        } catch (IllegalArgumentException e) {
            visibility = com.tfg.esports.league.entity.LeagueVisibility.PUBLIC;
        }

        League league = League.builder()
                .name(request.getName())
                .season(request.getSeason())
                .startDate(request.getStartDate())
                .initialRp(request.getInitialRp())
                .maxClubs(request.getMaxClubs())
                .transferRules(request.getTransferRules())
                .matchWagerRp(request.getMatchWagerRp())
                .active(true)
                .visibility(visibility)
                .creatorUserId(creatorUserId)
                .build();
        return leagueRepository.save(league);
    }

    /**
     * Inscribe a un club en una liga.
     *
     * @param leagueId ID de la liga
     * @param clubId   ID del club (desde Club Service)
     * @param userId   ID del usuario que intenta inscribir el club
     * @throws IllegalArgumentException si la liga no existe o ya está inscrito
     */
    @Transactional
    public void enrollClub(Long leagueId, Long clubId, Long userId) {
        League league = findLeagueOrThrow(leagueId);

        // Validar visibilidad de la liga
        if (league.getVisibility() == com.tfg.esports.league.entity.LeagueVisibility.PRIVATE) {
            if (!userId.equals(league.getCreatorUserId())) {
                throw new IllegalArgumentException("Esta liga es privada. Solo el creador puede inscribir clubes.");
            }
        } else if (league.getVisibility() == com.tfg.esports.league.entity.LeagueVisibility.FRIENDS_ONLY) {
            if (!userId.equals(league.getCreatorUserId())) {
                // Check if they are friends
                java.util.Map<String, Boolean> response = authServiceClient.checkFriendship(league.getCreatorUserId(), userId);
                Boolean areFriends = response != null ? response.get("areFriends") : false;
                if (Boolean.FALSE.equals(areFriends)) {
                    throw new IllegalArgumentException("Esta liga es solo para amigos del creador.");
                }
            }
        }

        if (leagueClubRepository.existsByIdLeagueIdAndIdClubId(leagueId, clubId)) {
            throw new IllegalArgumentException("El club ya está inscrito en esta liga");
        }
        
        long currentClubs = leagueClubRepository.countByIdLeagueId(leagueId);
        if (currentClubs >= league.getMaxClubs()) {
            throw new IllegalArgumentException("La liga ya ha alcanzado el número máximo de clubes");
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

    /**
     * Obtiene las ligas en las que un club está inscrito.
     *
     * @param clubId ID del club
     * @return lista de ligas
     */
    @Transactional(readOnly = true)
    public List<League> getLeaguesByClubId(Long clubId) {
        return leagueClubRepository.findByIdClubId(clubId)
                .stream()
                .map(LeagueClub::getLeague)
                .toList();
    }

    private League findLeagueOrThrow(Long id) {
        return leagueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Liga no encontrada con ID: " + id));
    }
}
