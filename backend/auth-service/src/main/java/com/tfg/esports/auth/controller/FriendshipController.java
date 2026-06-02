package com.tfg.esports.auth.controller;

import com.tfg.esports.auth.dto.FriendRequestResponse;
import com.tfg.esports.auth.dto.FriendResponse;
import com.tfg.esports.auth.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    @PostMapping("/request")
    public ResponseEntity<Map<String, String>> sendRequest(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @RequestBody Map<String, String> body) {
        
        String username = body.get("username");
        friendshipService.sendRequest(userId, username);
        return ResponseEntity.ok(Map.of("message", "Solicitud de amistad enviada"));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<Map<String, String>> acceptRequest(
            @PathVariable Long id,
            @RequestHeader("X-Auth-User-Id") Long userId) {
        
        friendshipService.acceptRequest(id, userId);
        return ResponseEntity.ok(Map.of("message", "Solicitud aceptada"));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Map<String, String>> rejectRequest(
            @PathVariable Long id,
            @RequestHeader("X-Auth-User-Id") Long userId) {
        
        friendshipService.rejectRequest(id, userId);
        return ResponseEntity.ok(Map.of("message", "Solicitud rechazada"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> removeFriend(
            @PathVariable Long id,
            @RequestHeader("X-Auth-User-Id") Long userId) {
        
        friendshipService.removeFriend(id, userId);
        return ResponseEntity.ok(Map.of("message", "Amigo eliminado"));
    }

    @GetMapping
    public ResponseEntity<List<FriendResponse>> getFriends(
            @RequestHeader("X-Auth-User-Id") Long userId) {
        
        return ResponseEntity.ok(friendshipService.getFriends(userId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<FriendRequestResponse>> getPendingRequests(
            @RequestHeader("X-Auth-User-Id") Long userId) {
        
        return ResponseEntity.ok(friendshipService.getPendingRequests(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<FriendResponse>> searchUsers(
            @RequestParam String q,
            @RequestHeader("X-Auth-User-Id") Long userId) {
        
        return ResponseEntity.ok(friendshipService.searchUsers(q, userId));
    }

    /**
     * Endpoint interno usado por otros microservicios (ej. league-service)
     * para verificar si dos usuarios son amigos.
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> checkFriendship(
            @RequestParam Long userId1,
            @RequestParam Long userId2) {
        
        boolean areFriends = friendshipService.areFriends(userId1, userId2);
        return ResponseEntity.ok(Map.of("areFriends", areFriends));
    }
}
