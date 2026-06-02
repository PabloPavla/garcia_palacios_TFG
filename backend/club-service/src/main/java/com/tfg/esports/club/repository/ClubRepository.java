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
     * Obtiene los clubes que pertenecen a un propietario concreto.
     * Un usuario puede tener un club por cada liga en la que participe.
     *
     * @param ownerId ID del usuario propietario
     * @return Lista con los clubes del usuario
     */
    List<Club> findByOwnerId(Long ownerId);

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
