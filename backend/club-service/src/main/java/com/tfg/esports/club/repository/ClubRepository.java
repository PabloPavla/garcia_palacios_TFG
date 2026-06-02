package com.tfg.esports.club.repository;

import com.tfg.esports.club.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link Club}.
 *
 * @author Pablo García Palacios
 */
@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

    /**
     * Obtiene el club que pertenece a un propietario concreto.
     * Cada usuario solo puede tener un club.
     *
     * @param ownerId ID del usuario propietario
     * @return Optional con el club si existe
     */
    Optional<Club> findByOwnerId(Long ownerId);

    /**
     * Comprueba si ya existe un club con el nombre indicado.
     *
     * @param name nombre del club
     * @return true si el nombre ya está registrado
     */
    boolean existsByName(String name);

    /**
     * Lista todos los clubes de una división concreta.
     *
     * @param division nombre de la división (enum como String)
     * @return lista de clubes en esa división
     */
    List<Club> findByDivision(com.tfg.esports.club.entity.Division division);
}
