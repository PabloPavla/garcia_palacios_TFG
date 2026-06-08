package com.tfg.esports.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FriendStatusResponse {
    private Long userId;
    private String username;
    private String profilePictureUrl;
    private String friendshipStatus; // NONE, PENDING_SENT, PENDING_RECEIVED, ACCEPTED
    private Long friendshipId;
}
