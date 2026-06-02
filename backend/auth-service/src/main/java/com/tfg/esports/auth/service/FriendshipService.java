package com.tfg.esports.auth.service;

import com.tfg.esports.auth.dto.FriendRequestResponse;
import com.tfg.esports.auth.dto.FriendResponse;
import com.tfg.esports.auth.entity.Friendship;
import com.tfg.esports.auth.entity.FriendshipStatus;
import com.tfg.esports.auth.entity.User;
import com.tfg.esports.auth.repository.FriendshipRepository;
import com.tfg.esports.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    @Transactional
    public void sendRequest(Long senderId, String receiverUsername) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario remitente no encontrado"));
        
        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new IllegalArgumentException("Usuario destino no encontrado"));

        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("No puedes enviarte una solicitud a ti mismo");
        }

        // Check if there's already a friendship (in any direction)
        if (friendshipRepository.existsAnyFriendship(senderId, receiver.getId())) {
            throw new IllegalArgumentException("Ya existe una relación de amistad o una solicitud pendiente");
        }

        Friendship friendship = Friendship.builder()
                .sender(sender)
                .receiver(receiver)
                .status(FriendshipStatus.PENDING)
                .build();

        friendshipRepository.save(friendship);
    }

    @Transactional
    public void acceptRequest(Long friendshipId, Long receiverId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));

        if (!friendship.getReceiver().getId().equals(receiverId)) {
            throw new IllegalArgumentException("No tienes permiso para aceptar esta solicitud");
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalArgumentException("La solicitud no está pendiente");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship);
    }

    @Transactional
    public void rejectRequest(Long friendshipId, Long receiverId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));

        if (!friendship.getReceiver().getId().equals(receiverId)) {
            throw new IllegalArgumentException("No tienes permiso para rechazar esta solicitud");
        }

        friendshipRepository.delete(friendship);
    }

    @Transactional
    public void removeFriend(Long friendshipId, Long userId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Amistad no encontrada"));

        if (!friendship.getSender().getId().equals(userId) && !friendship.getReceiver().getId().equals(userId)) {
            throw new IllegalArgumentException("No tienes permiso para eliminar esta amistad");
        }

        friendshipRepository.delete(friendship);
    }

    @Transactional(readOnly = true)
    public List<FriendRequestResponse> getPendingRequests(Long userId) {
        User receiver = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        return friendshipRepository.findByReceiverAndStatus(receiver, FriendshipStatus.PENDING)
                .stream()
                .map(f -> FriendRequestResponse.builder()
                        .id(f.getId())
                        .senderId(f.getSender().getId())
                        .senderUsername(f.getSender().getUsername())
                        .senderProfilePictureUrl(f.getSender().getProfilePictureUrl())
                        .createdAt(f.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FriendResponse> getFriends(Long userId) {
        return friendshipRepository.findAcceptedFriendships(userId)
                .stream()
                .map(f -> {
                    // Si el usuario actual es el sender, el amigo es el receiver, y viceversa
                    User friend = f.getSender().getId().equals(userId) ? f.getReceiver() : f.getSender();
                    return FriendResponse.builder()
                            .id(friend.getId())
                            .friendshipId(f.getId())
                            .username(friend.getUsername())
                            .profilePictureUrl(friend.getProfilePictureUrl())
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean areFriends(Long userId1, Long userId2) {
        return friendshipRepository.existsAcceptedFriendship(userId1, userId2);
    }

    @Transactional(readOnly = true)
    public List<FriendResponse> searchUsers(String query, Long currentUserId) {
        if (query == null || query.trim().length() < 3) {
            return List.of(); // Return empty if query is too short
        }

        return userRepository.findByUsernameContainingIgnoreCase(query)
                .stream()
                .filter(u -> !u.getId().equals(currentUserId)) // Exclude self
                .map(u -> FriendResponse.builder()
                        .id(u.getId())
                        .username(u.getUsername())
                        .profilePictureUrl(u.getProfilePictureUrl())
                        .build())
                .toList();
    }
}
