package com.tfg.esports.club.service;

import com.tfg.esports.club.dto.ClubRequest;
import com.tfg.esports.club.dto.ClubResponse;
import com.tfg.esports.club.entity.Club;
import com.tfg.esports.club.entity.Division;
import com.tfg.esports.club.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de negocio para la gestión de clubes.
 *
 * <p>Proporciona operaciones CRUD sobre los clubes y valida
 * las reglas de negocio antes de persistir cambios en la BD.</p>
 *
 * @author Pablo García Palacios
 */
@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;

    /**
     * Obtiene todos los clubes registrados en el sistema.
     *
     * @return lista con todos los clubes como DTOs de respuesta
     */
    @Transactional(readOnly = true)
    public List<ClubResponse> getAllClubs() {
        return clubRepository.findAll()
                .stream()
                .map(ClubResponse::fromEntity)
                .toList();
    }

    /**
     * Busca un club por su ID.
     *
     * @param id identificador del club
     * @return DTO del club encontrado
     * @throws IllegalArgumentException si el club no existe
     */
    @Transactional(readOnly = true)
    public ClubResponse getClubById(Long id) {
        Club club = findClubOrThrow(id);
        return ClubResponse.fromEntity(club);
    }

    /**
     * Obtiene los clubes que pertenecen a un usuario concreto.
     *
     * @param ownerId ID del usuario propietario
     * @return Lista de DTOs de los clubes del propietario
     */
    @Transactional(readOnly = true)
    public List<ClubResponse> getClubsByOwner(Long ownerId) {
        return clubRepository.findByOwnerId(ownerId)
                .stream()
                .map(ClubResponse::fromEntity)
                .toList();
    }

    /**
     * Crea un nuevo club para el usuario propietario indicado.
     *
     * <p>Un usuario puede tener múltiples clubes, uno para cada liga.</p>
     *
     * @param request datos del nuevo club
     * @param ownerId ID del usuario propietario (extraído del JWT por el Gateway)
     * @return DTO del club creado
     * @throws IllegalArgumentException si el nombre está en uso
     */
    @Transactional
    public ClubResponse createClub(ClubRequest request, Long ownerId) {
        // Verificar unicidad del nombre
        if (clubRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Ya existe un club con ese nombre");
        }

        Club club = Club.builder()
                .name(request.getName())
                .acronym(request.getAcronym().toUpperCase())
                .logoUrl(request.getLogoUrl())
                .ownerId(ownerId)
                .division(request.getDivision() != null ? request.getDivision() : Division.BRONZE)
                .riotPoints(request.getInitialRp() != null ? request.getInitialRp() : 2000)
                .build();

        return ClubResponse.fromEntity(clubRepository.save(club));
    }

    /**
     * Actualiza los datos de un club existente.
     *
     * <p>Solo el propietario del club puede modificarlo.</p>
     *
     * @param id      ID del club a actualizar
     * @param request nuevos datos del club
     * @param ownerId ID del usuario que realiza la petición
     * @return DTO del club actualizado
     * @throws IllegalArgumentException si el club no existe o el usuario no es el propietario
     */
    @Transactional
    public ClubResponse updateClub(Long id, ClubRequest request, Long ownerId) {
        Club club = findClubOrThrow(id);
        verifyOwnership(club, ownerId);

        // Solo actualizar si el nombre cambia y el nuevo nombre no está en uso
        if (!club.getName().equals(request.getName())
                && clubRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Ya existe un club con ese nombre");
        }

        club.setName(request.getName());
        club.setAcronym(request.getAcronym().toUpperCase());
        if (request.getLogoUrl() != null) club.setLogoUrl(request.getLogoUrl());
        if (request.getDivision() != null) club.setDivision(request.getDivision());

        return ClubResponse.fromEntity(clubRepository.save(club));
    }

    /**
     * Elimina un club del sistema. Solo un administrador puede realizar esta acción.
     *
     * @param id ID del club a eliminar
     * @throws IllegalArgumentException si el club no existe
     */
    @Transactional
    public void deleteClub(Long id) {
        Club club = findClubOrThrow(id);
        clubRepository.delete(club);
    }

    /**
     * Suma o resta Riot Points al club. Utilizado para gestionar recompensas y apuestas.
     * 
     * @param id ID del club
     * @param amount cantidad de RP a sumar (o restar si es negativo)
     * @return DTO del club actualizado
     */
    @Transactional
    public ClubResponse updateRiotPoints(Long id, Integer amount) {
        Club club = findClubOrThrow(id);
        int newBalance = club.getRiotPoints() + amount;
        if (newBalance < 0) {
            throw new IllegalArgumentException("No hay suficientes Riot Points.");
        }
        club.setRiotPoints(newBalance);
        return ClubResponse.fromEntity(clubRepository.save(club));
    }

    /**
     * Establece los Riot Points del club a un valor absoluto.
     * Utilizado por League Service al inscribir un club para aplicar el initialRp de la liga.
     *
     * @param id     ID del club
     * @param amount valor absoluto de RP a asignar
     */
    @Transactional
    public void setRiotPoints(Long id, Integer amount) {
        Club club = findClubOrThrow(id);
        club.setRiotPoints(amount);
        clubRepository.save(club);
    }

    @Transactional
    public void deductRp(Long id, Integer amount) {
        updateRiotPoints(id, -amount);
    }

    @Transactional
    public void addRp(Long id, Integer amount) {
        updateRiotPoints(id, amount);
    }

    /**
     * Busca un club por ID o lanza excepción si no existe.
     *
     * @param id ID del club
     * @return la entidad del club
     * @throws IllegalArgumentException si el club no existe
     */
    private Club findClubOrThrow(Long id) {
        return clubRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Club no encontrado con ID: " + id));
    }

    /**
     * Verifica que el usuario es el propietario del club.
     *
     * @param club    entidad del club
     * @param ownerId ID del usuario que realiza la acción
     * @throws SecurityException si el usuario no es el propietario
     */
    private void verifyOwnership(Club club, Long ownerId) {
        if (!club.getOwnerId().equals(ownerId)) {
            throw new SecurityException("No tienes permiso para modificar este club");
        }
    }
}
