package com.tfg.esports.auth.repository;

import com.tfg.esports.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link User}.
 *
 * <p>Spring Data JPA genera automáticamente la implementación de este interfaz.
 * Proporciona operaciones CRUD básicas heredadas de {@link JpaRepository}
 * más las consultas personalizadas definidas aquí.</p>
 *
 * @author Pablo García Palacios
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su nombre de usuario.
     * Utiliza el índice {@code idx_users_username} de la BD para eficiencia.
     *
     * @param username nombre de usuario a buscar
     * @return Optional con el usuario si existe, o vacío si no
     */
    Optional<User> findByUsername(String username);

    /**
     * Busca un usuario por su correo electrónico.
     * Utiliza el índice {@code idx_users_email} de la BD para eficiencia.
     *
     * @param email correo electrónico a buscar
     * @return Optional con el usuario si existe, o vacío si no
     */
    Optional<User> findByEmail(String email);

    /**
     * Comprueba si ya existe un usuario con el username indicado.
     *
     * @param username nombre de usuario a verificar
     * @return true si el username ya está registrado
     */
    boolean existsByUsername(String username);

    /**
     * Comprueba si ya existe un usuario con el email indicado.
     *
     * @param email correo electrónico a verificar
     * @return true si el email ya está registrado
     */
    boolean existsByEmail(String email);

    /**
     * Busca usuarios cuyo username contenga el texto dado.
     * Ignora mayúsculas y minúsculas.
     *
     * @param query texto a buscar
     * @return lista de usuarios coincidentes
     */
    java.util.List<User> findByUsernameContainingIgnoreCase(String query);
}
