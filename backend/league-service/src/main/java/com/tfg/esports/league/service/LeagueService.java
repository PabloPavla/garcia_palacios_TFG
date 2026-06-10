package com.tfg.esports.league.service;

import com.tfg.esports.league.dto.StandingResponse;
import com.tfg.esports.league.entity.League;
import com.tfg.esports.league.entity.LeagueClub;
import com.tfg.esports.league.entity.LeagueClubId;
import com.tfg.esports.league.entity.LeagueMember;
import com.tfg.esports.league.repository.LeagueClubRepository;
import com.tfg.esports.league.repository.LeagueMemberRepository;
import com.tfg.esports.league.entity.LeagueInvitation;
import com.tfg.esports.league.repository.LeagueInvitationRepository;
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
    private final LeagueMemberRepository leagueMemberRepository;
    private final com.tfg.esports.league.repository.MatchRepository matchRepository;

    private final com.tfg.esports.league.client.AuthServiceClient authServiceClient;
    private final LeagueInvitationRepository leagueInvitationRepository;

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
                .inviteToken(java.util.UUID.randomUUID().toString())
                .build();
        return leagueRepository.save(league);
    }

    /**
     * Inscribe a un club en una liga.
     *
     * @param leagueId ID de la liga
     * @param clubId   ID del club (desde Club Service)
     * @param userId   ID del usuario que intenta inscribir el club
     * @param role     Rol del usuario
     * @throws IllegalArgumentException si la liga no existe o ya está inscrito o excede límites
     */
    @Transactional
    public void enrollClub(Long leagueId, Long clubId, Long userId, String role) {
        League league = findLeagueOrThrow(leagueId);

        // Si no es administrador, el usuario debe estar primero inscrito/miembro de la liga
        if (!"ROLE_ADMIN".equals(role) && !leagueMemberRepository.existsByLeagueIdAndUserId(leagueId, userId)) {
            throw new IllegalArgumentException("Debes inscribirte primero en la liga antes de poder crear o inscribir un club.");
        }

        // Si no es administrador, no puede crear más de un club en la misma liga
        if (!"ROLE_ADMIN".equals(role)) {
            boolean alreadyHasClub = leagueClubRepository.existsByIdLeagueIdAndOwnerId(leagueId, userId);
            if (alreadyHasClub) {
                throw new IllegalArgumentException("Ya tienes un club creado en esta liga.");
            }
        }

        // Validar visibilidad de la liga
        boolean hasInvitation = leagueInvitationRepository.existsByLeagueIdAndUserIdAndStatus(leagueId, userId, "PENDING") ||
                               leagueInvitationRepository.existsByLeagueIdAndUserIdAndStatus(leagueId, userId, "ACCEPTED");
        if (!hasInvitation) {
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
        }

        if (matchRepository.countByLeagueId(leagueId) > 0) {
            throw new IllegalArgumentException("No puedes unirte. El torneo de esta liga ya ha comenzado.");
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
                .ownerId(userId)
                .build();

        leagueClubRepository.save(lc);
    }

    /**
     * Inscribe a un usuario en una liga.
     */
    @Transactional
    public void joinLeague(Long leagueId, Long userId) {
        League league = findLeagueOrThrow(leagueId);

        if (leagueMemberRepository.existsByLeagueIdAndUserId(leagueId, userId)) {
            throw new IllegalArgumentException("Ya estás inscrito en esta liga.");
        }

        // Validar visibilidad de la liga
        boolean hasInvitation = leagueInvitationRepository.existsByLeagueIdAndUserIdAndStatus(leagueId, userId, "PENDING") ||
                               leagueInvitationRepository.existsByLeagueIdAndUserIdAndStatus(leagueId, userId, "ACCEPTED");
        if (!hasInvitation) {
            if (league.getVisibility() == com.tfg.esports.league.entity.LeagueVisibility.PRIVATE) {
                if (!userId.equals(league.getCreatorUserId())) {
                    throw new IllegalArgumentException("Esta liga es privada. Solo el creador puede unirse.");
                }
            } else if (league.getVisibility() == com.tfg.esports.league.entity.LeagueVisibility.FRIENDS_ONLY) {
                if (!userId.equals(league.getCreatorUserId())) {
                    java.util.Map<String, Boolean> response = authServiceClient.checkFriendship(league.getCreatorUserId(), userId);
                    Boolean areFriends = response != null ? response.get("areFriends") : false;
                    if (Boolean.FALSE.equals(areFriends)) {
                        throw new IllegalArgumentException("Esta liga es solo para amigos del creador.");
                    }
                }
            }
        }

        LeagueMember member = LeagueMember.builder()
                .leagueId(leagueId)
                .userId(userId)
                .build();
        leagueMemberRepository.save(member);
    }

    /**
     * Obtiene todas las ligas en las que está inscrito un usuario.
     */
    @Transactional(readOnly = true)
    public List<League> getLeaguesByUserId(Long userId) {
        List<LeagueMember> memberships = leagueMemberRepository.findByUserId(userId);
        List<Long> leagueIds = memberships.stream()
                .map(LeagueMember::getLeagueId)
                .toList();
        return leagueRepository.findAllById(leagueIds);
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
        return leagueClubRepository.findByIdClubIdWithLeague(clubId)
                .stream()
                .map(LeagueClub::getLeague)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getWonLeaguesCount(Long userId) {
        return leagueRepository.countByWinnerUserId(userId);
    }

    @Transactional
    public void inviteUser(Long leagueId, String username, Long currentUserId) {
        League league = findLeagueOrThrow(leagueId);

        if (!currentUserId.equals(league.getCreatorUserId())) {
            throw new IllegalArgumentException("Solo el creador de la liga puede invitar usuarios.");
        }

        if (league.getVisibility() == com.tfg.esports.league.entity.LeagueVisibility.PUBLIC) {
            throw new IllegalArgumentException("No es necesario invitar a ligas públicas.");
        }

        // Buscar el usuario en auth-service
        java.util.Map<String, Object> userMap;
        try {
            userMap = authServiceClient.getUserByUsername(username);
        } catch (Exception e) {
            throw new IllegalArgumentException("Usuario no encontrado: " + username);
        }

        if (userMap == null || !userMap.containsKey("id")) {
            throw new IllegalArgumentException("Usuario no encontrado: " + username);
        }

        Long invitedUserId = Long.valueOf(userMap.get("id").toString());

        if (leagueMemberRepository.existsByLeagueIdAndUserId(leagueId, invitedUserId)) {
            throw new IllegalArgumentException("El usuario ya está inscrito en esta liga.");
        }

        if (leagueInvitationRepository.findByLeagueIdAndUserId(leagueId, invitedUserId).isPresent()) {
            throw new IllegalArgumentException("Ya existe una invitación pendiente o procesada para este usuario.");
        }

        LeagueInvitation invitation = LeagueInvitation.builder()
                .leagueId(leagueId)
                .userId(invitedUserId)
                .status("PENDING")
                .build();
        leagueInvitationRepository.save(invitation);
    }

    @Transactional
    public void joinByToken(String token, Long userId) {
        League league = leagueRepository.findByInviteToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Enlace de invitación inválido o caducado."));

        if (leagueMemberRepository.existsByLeagueIdAndUserId(league.getId(), userId)) {
            throw new IllegalArgumentException("Ya estás inscrito en esta liga.");
        }

        if (matchRepository.countByLeagueId(league.getId()) > 0) {
            throw new IllegalArgumentException("No puedes unirte. El torneo de esta liga ya ha comenzado.");
        }

        LeagueMember member = LeagueMember.builder()
                .leagueId(league.getId())
                .userId(userId)
                .build();
        leagueMemberRepository.save(member);
        
        // Si había una invitación previa directa, la marcamos como ACCEPTED
        leagueInvitationRepository.findByLeagueIdAndUserId(league.getId(), userId)
                .ifPresent(inv -> {
                    inv.setStatus("ACCEPTED");
                    leagueInvitationRepository.save(inv);
                });
    }

    @Transactional(readOnly = true)
    public List<java.util.Map<String, Object>> getPendingInvitations(Long userId) {
        List<LeagueInvitation> invitations = leagueInvitationRepository.findByUserIdAndStatus(userId, "PENDING");
        List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (LeagueInvitation inv : invitations) {
            java.util.Optional<League> leagueOpt = leagueRepository.findById(inv.getLeagueId());
            if (leagueOpt.isPresent()) {
                League league = leagueOpt.get();
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", inv.getId());
                map.put("leagueId", league.getId());
                map.put("leagueName", league.getName());
                map.put("season", league.getSeason());
                map.put("creatorUserId", league.getCreatorUserId());
                map.put("status", inv.getStatus());
                map.put("createdAt", inv.getCreatedAt());
                result.add(map);
            }
        }
        return result;
    }

    @Transactional
    public void acceptInvitation(Long invitationId, Long userId) {
        LeagueInvitation invitation = leagueInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitación no encontrada."));

        if (!userId.equals(invitation.getUserId())) {
            throw new IllegalArgumentException("Esta invitación no te pertenece.");
        }

        if (!"PENDING".equals(invitation.getStatus())) {
            throw new IllegalArgumentException("La invitación ya no está pendiente.");
        }

        // Unirse a la liga
        joinLeague(invitation.getLeagueId(), userId);

        invitation.setStatus("ACCEPTED");
        leagueInvitationRepository.save(invitation);
    }

    @Transactional
    public void rejectInvitation(Long invitationId, Long userId) {
        LeagueInvitation invitation = leagueInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitación no encontrada."));

        if (!userId.equals(invitation.getUserId())) {
            throw new IllegalArgumentException("Esta invitación no te pertenece.");
        }

        if (!"PENDING".equals(invitation.getStatus())) {
            throw new IllegalArgumentException("La invitación ya no está pendiente.");
        }

        invitation.setStatus("REJECTED");
        leagueInvitationRepository.save(invitation);
    }

    @Transactional
    public void deleteLeague(Long id) {
        League league = findLeagueOrThrow(id);
        leagueRepository.delete(league);
    }

    private League findLeagueOrThrow(Long id) {
        return leagueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Liga no encontrada con ID: " + id));
    }
}
