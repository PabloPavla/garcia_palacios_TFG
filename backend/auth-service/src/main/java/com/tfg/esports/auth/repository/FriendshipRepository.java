package com.tfg.esports.auth.repository;

import com.tfg.esports.auth.entity.Friendship;
import com.tfg.esports.auth.entity.FriendshipStatus;
import com.tfg.esports.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    Optional<Friendship> findBySenderAndReceiver(User sender, User receiver);

    List<Friendship> findByReceiverAndStatus(User receiver, FriendshipStatus status);

    @Query("SELECT f FROM Friendship f WHERE (f.sender.id = :userId OR f.receiver.id = :userId) AND f.status = 'ACCEPTED'")
    List<Friendship> findAcceptedFriendships(Long userId);

    @Query("SELECT COUNT(f) > 0 FROM Friendship f WHERE " +
           "((f.sender.id = :userId1 AND f.receiver.id = :userId2) OR " +
           "(f.sender.id = :userId2 AND f.receiver.id = :userId1)) AND " +
           "f.status = 'ACCEPTED'")
    boolean existsAcceptedFriendship(Long userId1, Long userId2);
    
    @Query("SELECT COUNT(f) > 0 FROM Friendship f WHERE " +
           "((f.sender.id = :userId1 AND f.receiver.id = :userId2) OR " +
           "(f.sender.id = :userId2 AND f.receiver.id = :userId1))")
    boolean existsAnyFriendship(Long userId1, Long userId2);
}
