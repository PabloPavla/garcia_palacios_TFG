package com.tfg.esports.club.service;

import com.tfg.esports.club.dto.ClubRequest;
import com.tfg.esports.club.dto.ClubResponse;
import com.tfg.esports.club.entity.Club;
import com.tfg.esports.club.repository.ClubRepository;
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
public class ClubServiceTest {

    @Mock
    private ClubRepository clubRepository;

    @InjectMocks
    private ClubService clubService;

    private Club club;

    @BeforeEach
    void setUp() {
        club = Club.builder()
                .id(1L)
                .name("Test Club")
                .acronym("TC")
                .ownerId(10L)
                .riotPoints(5000)
                .build();
    }

    @Test
    void createClub_WhenOwnerHasNoClub_ShouldCreate() {
        // Arrange
        ClubRequest request = new ClubRequest();
        request.setName("New Club");
        request.setAcronym("NEW");

        when(clubRepository.findByOwnerId(10L)).thenReturn(java.util.Collections.emptyList());
        when(clubRepository.existsByName("New Club")).thenReturn(false);
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> {
            Club c = invocation.getArgument(0);
            c.setId(2L);
            return c;
        });

        // Act
        ClubResponse response = clubService.createClub(request, 10L);

        // Assert
        assertNotNull(response);
        assertEquals("New Club", response.getName());
        assertEquals(10000, response.getRiotPoints()); // starts with 10000
        assertEquals(10L, response.getOwnerId());
    }

    @Test
    void addRiotPoints_ShouldIncreasePoints() {
        // Arrange
        when(clubRepository.findById(1L)).thenReturn(Optional.of(club));
        when(clubRepository.save(any(Club.class))).thenReturn(club);

        // Act
        ClubResponse response = clubService.updateRiotPoints(1L, 1000);

        // Assert
        assertEquals(6000, response.getRiotPoints());
    }

    @Test
    void deductRiotPoints_WhenSufficientFunds_ShouldDecreasePoints() {
        // Arrange
        when(clubRepository.findById(1L)).thenReturn(Optional.of(club));
        when(clubRepository.save(any(Club.class))).thenReturn(club);

        // Act
        ClubResponse response = clubService.updateRiotPoints(1L, -2000);

        // Assert
        assertEquals(3000, response.getRiotPoints());
    }

    @Test
    void deductRiotPoints_WhenInsufficientFunds_ShouldThrowException() {
        // Arrange
        when(clubRepository.findById(1L)).thenReturn(Optional.of(club));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            clubService.updateRiotPoints(1L, -6000);
        });
        assertEquals("No hay suficientes Riot Points.", exception.getMessage());
        verify(clubRepository, never()).save(any(Club.class));
    }
}
