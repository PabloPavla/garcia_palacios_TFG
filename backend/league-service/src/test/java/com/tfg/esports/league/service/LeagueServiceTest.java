package com.tfg.esports.league.service;

import com.tfg.esports.league.dto.LeagueRequest;
import com.tfg.esports.league.dto.StandingResponse;
import com.tfg.esports.league.entity.League;
import com.tfg.esports.league.entity.LeagueClub;
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
import com.tfg.esports.league.client.AuthServiceClient;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LeagueServiceTest {

    @Mock
    private LeagueRepository leagueRepository;

    @Mock
    private LeagueClubRepository leagueClubRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private LeagueService leagueService;

    private League league;

    @BeforeEach
    void setUp() {
        league = League.builder()
                .id(1L)
                .name("Superliga")
                .season("2026")
                .maxClubs(10)
                .build();
    }

    @Test
    void createLeague_ShouldSaveLeague() {
        // Arrange
        LeagueRequest request = new LeagueRequest();
        request.setName("Superliga");
        request.setSeason("2026");

        when(leagueRepository.save(any(League.class))).thenReturn(league);

        // Act
        League response = leagueService.createLeague(request, 1L);

        // Assert
        assertNotNull(response);
        assertEquals("Superliga", response.getName());
        verify(leagueRepository, times(1)).save(any(League.class));
    }

    @Test
    void enrollClub_WhenLimitNotReached_ShouldEnroll() {
        // Arrange
        when(leagueRepository.findById(1L)).thenReturn(Optional.of(league));
        when(leagueClubRepository.countByIdLeagueId(1L)).thenReturn(5L);
        when(leagueClubRepository.existsByIdLeagueIdAndIdClubId(1L, 10L)).thenReturn(false);

        // Act
        leagueService.enrollClub(1L, 10L, 1L);

        // Assert
        verify(leagueClubRepository, times(1)).save(any(LeagueClub.class));
    }

    @Test
    void enrollClub_WhenLimitReached_ShouldThrowException() {
        // Arrange
        when(leagueRepository.findById(1L)).thenReturn(Optional.of(league));
        when(leagueClubRepository.countByIdLeagueId(1L)).thenReturn(10L);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            leagueService.enrollClub(1L, 10L, 1L);
        });
        assertTrue(exception.getMessage().contains("máximo de clubes"));
        verify(leagueClubRepository, never()).save(any(LeagueClub.class));
    }

    @Test
    void getStandings_ShouldReturnStandings() {
        // Arrange
        when(leagueRepository.findById(1L)).thenReturn(Optional.of(league));
        
        LeagueClub lc1 = LeagueClub.builder().id(new com.tfg.esports.league.entity.LeagueClubId(1L, 10L)).league(league).points(3).wins(1).build();
        LeagueClub lc2 = LeagueClub.builder().id(new com.tfg.esports.league.entity.LeagueClubId(1L, 20L)).league(league).points(0).losses(1).build();
        
        when(leagueClubRepository.findStandingsByLeagueId(1L)).thenReturn(List.of(lc1, lc2));

        // Act
        List<StandingResponse> standings = leagueService.getStandings(1L);

        // Assert
        assertEquals(2, standings.size());
        assertEquals(10L, standings.get(0).getClubId());
        assertEquals(3, standings.get(0).getPoints());
    }
}
