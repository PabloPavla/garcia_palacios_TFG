package com.tfg.esports.club.dto;

import com.tfg.esports.club.entity.LolRole;
import com.tfg.esports.club.entity.Player;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta con los datos completos de un jugador.
 *
 * @author Pablo García Palacios
 */
@Data
@Builder
public class PlayerResponse {

    private Long id;
    private String summonerName;
    private String realName;
    private String nationality;
    private Integer age;
    private LolRole lolRole;
    private Integer priceRp;
    private Long leagueId;
    private Long clubId;
    private String clubName;
    private Boolean isFreeAgent;
    private Integer overallRating;
    private LocalDateTime createdAt;

    /**
     * Convierte una entidad {@link Player} a este DTO de respuesta.
     *
     * @param player la entidad del jugador
     * @return el DTO con los datos del jugador
     */
    public static PlayerResponse fromEntity(Player player) {
        return PlayerResponse.builder()
                .id(player.getId())
                .summonerName(player.getSummonerName())
                .realName(player.getRealName())
                .nationality(player.getNationality())
                .age(player.getAge())
                .lolRole(player.getLolRole())
                .priceRp(player.getPriceRp())
                .leagueId(player.getLeagueId())
                .clubId(player.getClub() != null ? player.getClub().getId() : null)
                .clubName(player.getClub() != null ? player.getClub().getName() : null)
                .isFreeAgent(player.getIsFreeAgent())
                .overallRating(player.getOverallRating())
                .createdAt(player.getCreatedAt())
                .build();
    }
}
