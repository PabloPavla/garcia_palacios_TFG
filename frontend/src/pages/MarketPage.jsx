import { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Badge, Button, Spinner, Alert, Modal, Form } from 'react-bootstrap';
import { useParams } from 'react-router-dom';
import clubService from '../services/clubService';
import transferService from '../services/transferService';
import api from '../services/api';

const getRoleColor = (role) => {
    switch (role) {
        case 'TOP': return 'primary';
        case 'JUNGLE': return 'success';
        case 'MID': return 'danger';
        case 'ADC': return 'warning text-dark';
        case 'SUPPORT': return 'info text-dark';
        default: return 'secondary';
    }
};

const MarketPage = () => {
    const { leagueId } = useParams();
    const [players, setPlayers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [successMsg, setSuccessMsg] = useState(null);
    
    // Pagination
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    // Filters & Sorting
    const [roleFilter, setRoleFilter] = useState('');
    const [sortOrder, setSortOrder] = useState('overallRating,desc');

    // Modal estado
    const [showModal, setShowModal] = useState(false);
    const [selectedPlayer, setSelectedPlayer] = useState(null);
    const [offerFee, setOfferFee] = useState(0);
    const [submitting, setSubmitting] = useState(false);

    const [myPlayers, setMyPlayers] = useState([]);
    const [exchangePlayerId, setExchangePlayerId] = useState('');
    const [activeClubId, setActiveClubId] = useState(null);

    const fetchAllPlayers = async (pageNumber = 0, currentRole = roleFilter, currentSort = sortOrder) => {
        setLoading(true);
        try {
            const response = await clubService.getAllPlayers(leagueId, pageNumber, 12, currentRole, currentSort);
            setPlayers(response.content || []);
            setTotalPages(response.totalPages || 0);
            setPage(pageNumber);
        } catch (err) {
            setError('Error al cargar el mercado de jugadores.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        const loadContext = async () => {
            try {
                const clubs = await clubService.getMyClubs();
                // We need to find which club is in this league. 
                // Since we can't get leagueId from club directly, let's just use the first club 
                // or fetch the league standings to find our club.
                // Or better, we can iterate over my clubs and ask the backend if they are in this league.
                // Let's import leagueService (wait, we didn't import it. We can just use fetch or another way).
                // Actually, an easy fix for the UI right now is to just make sure `null === null` doesn't trigger "TU JUGADOR":
                // But we STILL need activeClubId to send the transfer offer!
                // Let's assume the user has 1 club for now, or we find it matching the league.
                // We don't have leagueService imported, so let's import it.
                // I will add the import at the top of the file in another chunk.
                let activeId = null;
                for (const c of clubs) {
                    try {
                        const { data } = await api.get(`/leagues/by-club?clubId=${c.id}`);
                        if (data.some(l => l.id == leagueId)) {
                            activeId = c.id;
                            break;
                        }
                    } catch (e) {}
                }
                
                if (activeId) {
                    setActiveClubId(activeId);
                    const playersResp = await clubService.getClubPlayers(activeId);
                    setMyPlayers(playersResp || []);
                } else if (clubs.length > 0) {
                    // Fallback
                    setActiveClubId(clubs[0].id);
                    const playersResp = await clubService.getClubPlayers(clubs[0].id);
                    setMyPlayers(playersResp || []);
                }
            } catch (err) {
                console.error("Error loading club context", err);
            }
        };
        loadContext();
    }, [leagueId]);

    useEffect(() => {
        fetchAllPlayers(0, roleFilter, sortOrder);
    }, [leagueId, roleFilter, sortOrder]);

    const handleOpenModal = (player) => {
        if (player.clubId === activeClubId) {
            setError("No puedes ofertar por tu propio jugador.");
            return;
        }
        setSelectedPlayer(player);
        setOfferFee(player.priceRp || 0);
        setExchangePlayerId('');
        setShowModal(true);
    };

    const handleSubmitOffer = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        setError(null);
        setSuccessMsg(null);
        
        try {
            const exId = exchangePlayerId ? parseInt(exchangePlayerId) : null;
            await transferService.createTransferOffer(selectedPlayer.id, offerFee, activeClubId, exId);
            setSuccessMsg(selectedPlayer.isFreeAgent ? 
                `¡Puja enviada exitosamente por ${selectedPlayer.summonerName}! La subasta terminará en 1 minuto.` :
                `¡Oferta enviada al club ${selectedPlayer.clubName} por ${selectedPlayer.summonerName}!`
            );
            setShowModal(false);
            // Refresh to show updated market
            fetchAllPlayers(page, roleFilter, sortOrder);
        } catch (err) {
            setError(err.response?.data?.message || 'Error al enviar la oferta. Verifica si ya tienes una oferta pendiente o si el RP es suficiente.');
            setShowModal(false);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Container className="mt-4 pt-4 animate-fade-in">
            <h1 className="fw-bold mb-1 text-white"><i className="bi bi-shop text-primary me-2"></i> MERCADO DE AGENTES LIBRES</h1>
            <p className="text-secondary mb-5">Encuentra y ficha a las próximas estrellas para tu equipo.</p>

            {error && <Alert variant="danger" onClose={() => setError(null)} dismissible>{error}</Alert>}
            {successMsg && <Alert variant="success" onClose={() => setSuccessMsg(null)} dismissible>{successMsg}</Alert>}

            {loading ? (
                <div className="text-center py-5">
                    <Spinner animation="border" variant="primary" />
                </div>
            ) : (
                <>
                    <div className="d-flex justify-content-between mb-4 flex-wrap gap-3">
                        <Form.Select 
                            className="bg-dark text-white border-secondary w-auto" 
                            value={roleFilter} 
                            onChange={(e) => setRoleFilter(e.target.value)}
                        >
                            <option value="">Todas las posiciones</option>
                            <option value="TOP">TOP</option>
                            <option value="JUNGLE">JUNGLE</option>
                            <option value="MID">MID</option>
                            <option value="ADC">ADC</option>
                            <option value="SUPPORT">SUPPORT</option>
                        </Form.Select>
                        
                        <Form.Select 
                            className="bg-dark text-white border-secondary w-auto" 
                            value={sortOrder} 
                            onChange={(e) => setSortOrder(e.target.value)}
                        >
                            <option value="overallRating,desc">Mayor Winrate</option>
                            <option value="overallRating,asc">Menor Winrate</option>
                            <option value="priceRp,desc">Mayor Precio</option>
                            <option value="priceRp,asc">Menor Precio</option>
                        </Form.Select>
                    </div>

                    <Row className="g-4">
                        {players.map(player => (
                            <Col lg={3} md={4} sm={6} key={player.id}>
                                <Card className="glass-card h-100 border-0 bg-dark text-white shadow-lg">
                                    <Card.Body className="d-flex flex-column">
                                        <div className="d-flex justify-content-between align-items-center mb-3">
                                            <Badge bg={getRoleColor(player.lolRole)} className="px-2 py-1">
                                                {player.lolRole}
                                            </Badge>
                                            <span className="fw-bold fs-5 text-warning">{player.overallRating}% Winrate</span>
                                        </div>
                                        <Card.Title className="fw-bold fs-4 text-truncate">{player.summonerName}</Card.Title>
                                        <Card.Text className="text-secondary small mb-3 flex-grow-1">
                                            {player.nationality || 'Desconocida'} • {player.age ? `${player.age} años` : ''}
                                        </Card.Text>
                                        
                                        <div className="bg-black bg-opacity-25 rounded p-2 mb-3 text-center">
                                            <span className="d-block small text-secondary">Estado / Club</span>
                                            <span className={`fw-bold ${player.isFreeAgent ? 'text-success' : 'text-info'}`}>
                                                {player.isFreeAgent ? 'AGENTE LIBRE' : player.clubName}
                                            </span>
                                            <span className="d-block small text-secondary mt-1">Valor: {player.priceRp} RP</span>
                                        </div>
                                        
                                        <Button 
                                            variant={player.isFreeAgent ? "outline-success" : "outline-primary"} 
                                            className="w-100 fw-bold"
                                            disabled={player.clubId && player.clubId === activeClubId}
                                            onClick={() => handleOpenModal(player)}
                                        >
                                            {player.clubId && player.clubId === activeClubId ? 'TU JUGADOR' : (player.isFreeAgent ? 'PUJAR / SUBASTA' : 'OFERTAR / INTERCAMBIO')}
                                        </Button>
                                    </Card.Body>
                                </Card>
                            </Col>
                        ))}
                    </Row>

                    {/* Controles de Paginación */}
                    {totalPages > 1 && (
                        <div className="d-flex justify-content-center mt-5 gap-2">
                            <Button 
                                variant="outline-secondary" 
                                disabled={page === 0}
                                onClick={() => fetchAllPlayers(page - 1)}
                            >
                                <i className="bi bi-chevron-left"></i> Anterior
                            </Button>
                            <div className="d-flex align-items-center px-3 text-secondary">
                                Página {page + 1} de {totalPages}
                            </div>
                            <Button 
                                variant="outline-secondary" 
                                disabled={page >= totalPages - 1}
                                onClick={() => fetchAllPlayers(page + 1)}
                            >
                                Siguiente <i className="bi bi-chevron-right"></i>
                            </Button>
                        </div>
                    )}
                </>
            )}

            {/* Modal de Oferta */}
            <Modal show={showModal} onHide={() => !submitting && setShowModal(false)} centered contentClassName="bg-dark text-white border-primary">
                <Modal.Header closeButton closeVariant="white" className="border-secondary">
                    <Modal.Title><i className="bi bi-file-earmark-text text-primary me-2"></i> Propuesta de Contrato</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {selectedPlayer && (
                        <Form onSubmit={handleSubmitOffer}>
                            <div className="text-center mb-4">
                                <h4 className="fw-bold">{selectedPlayer.summonerName}</h4>
                                <Badge bg={getRoleColor(selectedPlayer.lolRole)}>{selectedPlayer.lolRole}</Badge>
                            </div>
                            <Form.Group className="mb-3">
                                <Form.Label className="text-secondary">Oferta de Traspaso (RP)</Form.Label>
                                <Form.Control 
                                    type="number" 
                                    min="0"
                                    step="100"
                                    value={offerFee}
                                    onChange={(e) => setOfferFee(e.target.value)}
                                    required
                                    className="bg-black text-white border-secondary fs-5"
                                />
                                <Form.Text className="text-info">
                                    {selectedPlayer.isFreeAgent 
                                        ? `Puja actual sugerida: ${selectedPlayer.priceRp} RP (Debes superar la puja activa si la hay)`
                                        : `Valor recomendado: ${selectedPlayer.priceRp} RP`}
                                </Form.Text>
                            </Form.Group>

                            {!selectedPlayer.isFreeAgent && (
                                <Form.Group className="mb-3">
                                    <Form.Label className="text-secondary">Añadir Jugador al Intercambio (Opcional)</Form.Label>
                                    <Form.Select 
                                        className="bg-black text-white border-secondary"
                                        value={exchangePlayerId}
                                        onChange={(e) => setExchangePlayerId(e.target.value)}
                                    >
                                        <option value="">-- Sin jugador a cambio --</option>
                                        {myPlayers.map(p => (
                                            <option key={p.id} value={p.id}>{p.summonerName} ({p.lolRole}) - {p.priceRp} RP</option>
                                        ))}
                                    </Form.Select>
                                </Form.Group>
                            )}
                            
                            <div className="d-grid mt-4">
                                <Button variant="primary" type="submit" disabled={submitting} size="lg">
                                    {submitting ? <Spinner animation="border" size="sm" /> : 'ENVIAR OFERTA'}
                                </Button>
                            </div>
                        </Form>
                    )}
                </Modal.Body>
            </Modal>
        </Container>
    );
};

export default MarketPage;
