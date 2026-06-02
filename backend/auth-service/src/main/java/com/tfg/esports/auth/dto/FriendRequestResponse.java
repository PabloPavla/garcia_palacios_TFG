package com.tfg.esports.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FriendRequestResponse {
    private Long id; // Friendship ID
    private Long senderId;
    private String senderUsername;
    private String senderProfilePictureUrl;
    private LocalDateTime createdAt;
}
