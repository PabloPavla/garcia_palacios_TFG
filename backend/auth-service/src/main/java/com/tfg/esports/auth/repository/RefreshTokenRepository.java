package com.tfg.esports.auth.repository;

import com.tfg.esports.auth.entity.RefreshToken;
import com.tfg.esports.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link RefreshToken}.
 *
 * @author Pablo García Palacios
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Busca un refresh token por su valor único.
     * Utiliza el índice {@code idx_refresh_token} para eficiencia.
     *
     * @param token valor del refresh token
     * @return Optional con el RefreshToken si existe
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Elimina todos los refresh tokens de un usuario específico.
     * Se usa al hacer logout para invalidar todas las sesiones activas.
     *
     * @param user el usuario cuyos tokens se eliminarán
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
    void deleteAllByUser(User user);
}
