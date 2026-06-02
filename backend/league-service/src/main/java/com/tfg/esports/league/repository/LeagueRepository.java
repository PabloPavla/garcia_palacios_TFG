package com.tfg.esports.league.repository;

import com.tfg.esports.league.entity.League;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link League}.
 *
 * @author Pablo García Palacios
 */
@Repository
public interface LeagueRepository extends JpaRepository<League, Long> {

    /**
     * Busca una liga por su nombre y temporada.
     *
     * @param name   nombre de la liga
     * @param season temporada
     * @return liga encontrada
     */
    Optional<League> findByNameAndSeason(String name, String season);

    /**
     * Lista todas las ligas activas o inactivas.
     *
     * @param active estado a filtrar
     * @return lista de ligas
     */
    List<League> findByActive(Boolean active);
}
