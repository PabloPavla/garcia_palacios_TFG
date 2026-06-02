package com.tfg.esports.transfer.controller;

import com.tfg.esports.transfer.dto.TransferRequest;
import com.tfg.esports.transfer.dto.TransferResponse;
import com.tfg.esports.transfer.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de transferencias y fichajes.
 *
 * <p>Expone endpoints bajo la ruta base {@code /transfers}.
 * La identidad del usuario se obtiene de las cabeceras
 * {@code X-Auth-User-Id} y {@code X-Auth-Role} inyectadas por el API Gateway.</p>
 *
 * @author Pablo García Palacios
 */
@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    /**
     * Lista todas las transferencias del sistema de forma paginada.
     * Solo accesible por administradores.
     *
     * @param pageable paginación (por defecto: 20 por página, más recientes primero)
     * @param role     rol del usuario autenticado (cabecera del Gateway)
     * @return 200 con la página de transferencias
     */
    @GetMapping
    public ResponseEntity<Page<TransferResponse>> getAllTransfers(
            @PageableDefault(size = 20, sort = "offeredAt") Pageable pageable,
            @RequestHeader("X-Auth-Role") String role) {
        return ResponseEntity.ok(transferService.getAllTransfers(pageable));
    }

    /**
     * Obtiene el detalle de una transferencia específica.
     *
     * @param id ID de la transferencia
     * @return 200 con los datos de la transferencia
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransferResponse> getTransfer(@PathVariable Long id) {
        return ResponseEntity.ok(transferService.getTransferById(id));
    }

    /**
     * Obtiene el historial de fichajes realizados por un club (como comprador).
     *
     * @param clubId   ID del club
     * @param pageable paginación
     * @return 200 con la página de transferencias del club
     */
    @GetMapping("/club/{clubId}/buying")
    public ResponseEntity<Page<TransferResponse>> getClubBuyingHistory(
            @PathVariable Long clubId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(transferService.getTransfersByBuyingClub(clubId, pageable));
    }

    /**
     * Obtiene el historial de ventas de un club (como vendedor).
     *
     * @param clubId   ID del club
     * @param pageable paginación
     * @return 200 con la página de ventas del club
     */
    @GetMapping("/club/{clubId}/selling")
    public ResponseEntity<Page<TransferResponse>> getClubSellingHistory(
            @PathVariable Long clubId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(transferService.getTransfersBySellingClub(clubId, pageable));
    }

    /**
     * Obtiene el historial de transferencias de un jugador concreto.
     *
     * @param playerId ID del jugador
     * @return 200 con la lista de transferencias del jugador
     */
    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<TransferResponse>> getPlayerTransfers(
            @PathVariable Long playerId) {
        return ResponseEntity.ok(transferService.getTransfersByPlayer(playerId));
    }

    @PostMapping
    public ResponseEntity<TransferResponse> createTransfer(
            @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transferService.createTransfer(request, request.getToClubId()));
    }

    /**
     * Acepta una transferencia pendiente.
     */
    @PutMapping("/{id}/accept")
    public ResponseEntity<TransferResponse> acceptTransfer(
            @PathVariable Long id,
            @RequestParam Long clubId) {
        return ResponseEntity.ok(transferService.acceptTransfer(id, clubId));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<TransferResponse> rejectTransfer(
            @PathVariable Long id,
            @RequestParam Long clubId) {
        return ResponseEntity.ok(transferService.rejectTransfer(id, clubId));
    }

    /**
     * Contraoferta una transferencia.
     */
    @PutMapping("/{id}/counter")
    public ResponseEntity<TransferResponse> counterOffer(
            @PathVariable Long id,
            @Valid @RequestBody TransferRequest request,
            @RequestParam Long clubId) {
        return ResponseEntity.ok(transferService.counterOffer(id, request, clubId));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<TransferResponse> cancelTransfer(
            @PathVariable Long id,
            @RequestParam Long clubId) {
        return ResponseEntity.ok(transferService.cancelTransfer(id, clubId));
    }
}
