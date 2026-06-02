package com.tfg.esports.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FriendResponse {
    private Long id;
    private Long friendshipId;
    private String username;
    private String profilePictureUrl;
}
