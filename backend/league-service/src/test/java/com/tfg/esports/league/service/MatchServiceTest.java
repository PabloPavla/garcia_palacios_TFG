package com.tfg.esports.league.service;

import com.tfg.esports.league.client.ClubClient;
import com.tfg.esports.league.dto.MatchRequest;
import com.tfg.esports.league.dto.MatchResponse;
import com.tfg.esports.league.dto.MatchScoreRequest;
import com.tfg.esports.league.entity.League;
import com.tfg.esports.league.entity.Match;
import com.tfg.esports.league.entity.MatchStatus;
import com.tfg.esports.league.repository.LeagueClubRepository;
import com.tfg.esports.league.repository.LeagueRepository;
import com.tfg.esports.league.repository.MatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private LeagueRepository leagueRepository;

    @Mock
    private LeagueClubRepository leagueClubRepository;

    @Mock
    private ClubClient clubClient;

    @InjectMocks
    private MatchService matchService;

    private League league;
    private Match match;

    @BeforeEach
    void setUp() {
        league = League.builder()
                .id(1L)
                .name("Superliga")
                .matchWagerRp(100)
                .build();

        match = Match.builder()
                .id(1L)
                .league(league)
                .homeClubId(10L)
                .awayClubId(20L)
                .matchDate(LocalDateTime.now().plusDays(1))
                .status(MatchStatus.SCHEDULED)
                .wagerRp(100)
                .build();
    }

    @Test
    void scheduleMatch_ShouldDeductWagerFromBothClubs() {
        // Arrange
        MatchRequest request = new MatchRequest();
        request.setLeagueId(1L);
        request.setHomeClubId(10L);
        request.setAwayClubId(20L);
        request.setMatchDate(LocalDateTime.now().plusDays(2));

        when(leagueRepository.findById(1L)).thenReturn(Optional.of(league));
        when(leagueClubRepository.existsByIdLeagueIdAndIdClubId(1L, 10L)).thenReturn(true);
        when(leagueClubRepository.existsByIdLeagueIdAndIdClubId(1L, 20L)).thenReturn(true);
        when(matchRepository.save(any(Match.class))).thenAnswer(i -> {
            Match m = i.getArgument(0);
            m.setId(2L);
            return m;
        });

        // Act
        MatchResponse response = matchService.scheduleMatch(request);

        // Assert
        assertNotNull(response);
        assertEquals(100, response.getWagerRp());
        verify(clubClient, times(1)).updateRiotPoints(10L, -100);
        verify(clubClient, times(1)).updateRiotPoints(20L, -100);
    }

    @Test
    void recordResult_WhenHomeWins_ShouldRewardHomeClub() {
        // Arrange
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchRepository.save(any(Match.class))).thenReturn(match);

        MatchScoreRequest scoreReq = new MatchScoreRequest();
        scoreReq.setHomeScore(3);
        scoreReq.setAwayScore(1);

        // Act
        MatchResponse response = matchService.recordResult(1L, scoreReq);

        // Assert
        assertEquals(MatchStatus.COMPLETED, response.getStatus());
        assertEquals(3, response.getHomeScore());
        assertEquals(1, response.getAwayScore());
        // Winner takes 200 (double the wager)
        verify(clubClient, times(1)).updateRiotPoints(10L, 200);
        verify(clubClient, never()).updateRiotPoints(20L, 200);
    }

    @Test
    void recordResult_WhenDraw_ShouldRefundBothClubs() {
        // Arrange
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchRepository.save(any(Match.class))).thenReturn(match);

        MatchScoreRequest scoreReq = new MatchScoreRequest();
        scoreReq.setHomeScore(2);
        scoreReq.setAwayScore(2);

        // Act
        MatchResponse response = matchService.recordResult(1L, scoreReq);

        // Assert
        assertEquals(MatchStatus.COMPLETED, response.getStatus());
        assertEquals(2, response.getHomeScore());
        assertEquals(2, response.getAwayScore());
        // Both get their 100 wager back
        verify(clubClient, times(1)).updateRiotPoints(10L, 100);
        verify(clubClient, times(1)).updateRiotPoints(20L, 100);
    }
}
