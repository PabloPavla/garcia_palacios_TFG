package com.tfg.esports.club.service;

import com.tfg.esports.club.dto.PlayerRequest;
import com.tfg.esports.club.dto.PlayerResponse;
import com.tfg.esports.club.entity.Club;
import com.tfg.esports.club.entity.LolRole;
import com.tfg.esports.club.entity.Player;
import com.tfg.esports.club.repository.ClubRepository;
import com.tfg.esports.club.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private ClubRepository clubRepository;

    @InjectMocks
    private PlayerService playerService;

    private Player freeAgentPlayer;
    private Player nonFreeAgentPlayer;
    private Club testClub;

    @BeforeEach
    void setUp() {
        testClub = Club.builder()
                .id(1L)
                .name("Test Club")
                .acronym("TC")
                .riotPoints(1000)
                .build();

        freeAgentPlayer = Player.builder()
                .id(1L)
                .summonerName("FreeAgent")
                .lolRole(LolRole.MID)
                .isFreeAgent(true)
                .leagueId(1L)
                .build();

        nonFreeAgentPlayer = Player.builder()
                .id(2L)
                .summonerName("SignedPlayer")
                .lolRole(LolRole.TOP)
                .isFreeAgent(false)
                .club(testClub)
                .leagueId(1L)
                .build();
    }

    @Test
    void createPlayer_ShouldSaveAsFreeAgent() {
        // Arrange
        PlayerRequest request = new PlayerRequest();
        request.setSummonerName("NewPlayer");
        request.setLolRole(LolRole.ADC);
        request.setLeagueId(1L);

        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> {
            Player p = invocation.getArgument(0);
            p.setId(3L);
            return p;
        });

        // Act
        PlayerResponse response = playerService.createPlayer(request);

        // Assert
        assertNotNull(response);
        assertEquals("NewPlayer", response.getSummonerName());
        assertTrue(response.getIsFreeAgent());
        assertNull(response.getClubId());
        verify(playerRepository, times(1)).save(any(Player.class));
    }

    @Test
    void signPlayer_WhenPlayerIsFreeAgentAndLimitNotReached_ShouldSign() {
        // Arrange
        when(playerRepository.findById(1L)).thenReturn(Optional.of(freeAgentPlayer));
        when(clubRepository.findById(1L)).thenReturn(Optional.of(testClub));
        when(playerRepository.countByClubIdAndLolRole(1L, LolRole.MID)).thenReturn(1L); // Not reached limit of 2

        when(playerRepository.save(any(Player.class))).thenReturn(freeAgentPlayer);

        // Act
        PlayerResponse response = playerService.signPlayer(1L, 1L);

        // Assert
        assertNotNull(response);
        assertFalse(response.getIsFreeAgent());
        assertEquals(1L, response.getClubId());
    }

    @Test
    void signPlayer_WhenPlayerNotFreeAgent_ShouldThrowException() {
        // Arrange
        when(playerRepository.findById(2L)).thenReturn(Optional.of(nonFreeAgentPlayer));
        when(clubRepository.findById(1L)).thenReturn(Optional.of(testClub));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            playerService.signPlayer(2L, 1L);
        });
        assertEquals("El jugador no es un agente libre", exception.getMessage());
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void signPlayer_WhenRoleLimitReached_ShouldThrowException() {
        // Arrange
        when(playerRepository.findById(1L)).thenReturn(Optional.of(freeAgentPlayer));
        when(clubRepository.findById(1L)).thenReturn(Optional.of(testClub));
        // Limit is 2
        when(playerRepository.countByClubIdAndLolRole(1L, LolRole.MID)).thenReturn(2L);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            playerService.signPlayer(1L, 1L);
        });
        assertTrue(exception.getMessage().contains("El club ya tiene el máximo de jugadores en el rol"));
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void releasePlayer_WhenPlayerIsNotFreeAgent_ShouldRelease() {
        // Arrange
        when(playerRepository.findById(2L)).thenReturn(Optional.of(nonFreeAgentPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(nonFreeAgentPlayer);

        // Act
        PlayerResponse response = playerService.releasePlayer(2L);

        // Assert
        assertTrue(response.getIsFreeAgent());
        assertNull(response.getClubId());
    }
}
