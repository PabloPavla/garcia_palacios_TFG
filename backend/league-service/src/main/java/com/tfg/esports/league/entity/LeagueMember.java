package com.tfg.esports.league.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "league_members")
@IdClass(LeagueMemberId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeagueMember {

    @Id
    @Column(name = "league_id")
    private Long leagueId;

    @Id
    @Column(name = "user_id")
    private Long userId;
}
