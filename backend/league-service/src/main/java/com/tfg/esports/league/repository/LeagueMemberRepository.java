package com.tfg.esports.league.repository;

import com.tfg.esports.league.entity.LeagueMember;
import com.tfg.esports.league.entity.LeagueMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeagueMemberRepository extends JpaRepository<LeagueMember, LeagueMemberId> {
    boolean existsByLeagueIdAndUserId(Long leagueId, Long userId);
    List<LeagueMember> findByUserId(Long userId);
}
