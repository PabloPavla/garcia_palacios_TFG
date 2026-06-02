package com.tfg.esports.club.dto;

import com.tfg.esports.club.entity.Club;
import com.tfg.esports.club.entity.Division;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta con los datos de un club.
 * Incluye el resumen de la plantilla de jugadores.
 *
 * @author Pablo García Palacios
 */
@Data
@Builder
public class ClubResponse {

    private Long id;
    private String name;
    private String acronym;
    private String logoUrl;
    private BigDecimal budget;
    private Long ownerId;
    private Division division;
    private LocalDateTime createdAt;
    private int playerCount;

    /**
     * Convierte una entidad {@link Club} a este DTO de respuesta.
     *
     * @param club la entidad del club
     * @return el DTO con los datos del club
     */
    public static ClubResponse fromEntity(Club club) {
        return ClubResponse.builder()
                .id(club.getId())
                .name(club.getName())
                .acronym(club.getAcronym())
                .logoUrl(club.getLogoUrl())
                .budget(club.getBudget())
                .ownerId(club.getOwnerId())
                .division(club.getDivision())
                .createdAt(club.getCreatedAt())
                .playerCount(club.getPlayers() != null ? club.getPlayers().size() : 0)
                .build();
    }
}
