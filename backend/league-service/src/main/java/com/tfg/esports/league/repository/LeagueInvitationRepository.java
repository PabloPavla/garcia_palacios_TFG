package com.tfg.esports.league.repository;

import com.tfg.esports.league.entity.LeagueInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeagueInvitationRepository extends JpaRepository<LeagueInvitation, Long> {
    List<LeagueInvitation> findByUserIdAndStatus(Long userId, String status);
    boolean existsByLeagueIdAndUserIdAndStatus(Long leagueId, Long userId, String status);
    Optional<LeagueInvitation> findByLeagueIdAndUserId(Long leagueId, Long userId);
}
