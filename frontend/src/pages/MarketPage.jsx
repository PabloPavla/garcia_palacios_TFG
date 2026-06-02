import { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Badge, Button, Spinner, Alert, Modal, Form } from 'react-bootstrap';
import { useParams } from 'react-router-dom';
import clubService from '../services/clubService';
import transferService from '../services/transferService';

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

    // Modal estado
    const [showModal, setShowModal] = useState(false);
    const [selectedPlayer, setSelectedPlayer] = useState(null);
    const [offerFee, setOfferFee] = useState(0);
    const [submitting, setSubmitting] = useState(false);

    const fetchFreeAgents = async (pageNumber = 0) => {
        setLoading(true);
        try {
            const response = await clubService.getFreeAgents(leagueId, pageNumber, 12);
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
        fetchFreeAgents(0);
    }, []);

    const handleOpenModal = (player) => {
        setSelectedPlayer(player);
        setOfferFee(player.priceRp || 0);
        setShowModal(true);
    };

    const handleSubmitOffer = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        setError(null);
        setSuccessMsg(null);
        
        try {
            await transferService.createTransferOffer(selectedPlayer.id, offerFee);
            setSuccessMsg(`¡Oferta enviada exitosamente por ${selectedPlayer.summonerName}!`);
            setShowModal(false);
        } catch (err) {
            setError(err.response?.data?.message || 'Error al enviar la oferta. Es posible que ya tengas una oferta pendiente por este jugador.');
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
                    <Row className="g-4">
                        {players.map(player => (
                            <Col lg={3} md={4} sm={6} key={player.id}>
                                <Card className="glass-card h-100 border-0 bg-dark text-white shadow-lg">
                                    <Card.Body className="d-flex flex-column">
                                        <div className="d-flex justify-content-between align-items-center mb-3">
                                            <Badge bg={getRoleColor(player.lolRole)} className="px-2 py-1">
                                                {player.lolRole}
                                            </Badge>
                                            <span className="fw-bold fs-5 text-warning">{player.overallRating} <i className="bi bi-star-fill small"></i></span>
                                        </div>
                                        <Card.Title className="fw-bold fs-4 text-truncate">{player.summonerName}</Card.Title>
                                        <Card.Text className="text-secondary small mb-3 flex-grow-1">
                                            {player.nationality || 'Desconocida'} • {player.age ? `${player.age} años` : ''}
                                        </Card.Text>
                                        
                                        <div className="bg-black bg-opacity-25 rounded p-2 mb-3 text-center">
                                            <span className="d-block small text-secondary">Valor Estimado</span>
                                            <span className="fw-bold text-info">
                                                {player.priceRp} RP
                                            </span>
                                        </div>
                                        
                                        <Button 
                                            variant="outline-primary" 
                                            className="w-100 fw-bold"
                                            onClick={() => handleOpenModal(player)}
                                        >
                                            OFERTAR
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
                                onClick={() => fetchFreeAgents(page - 1)}
                            >
                                <i className="bi bi-chevron-left"></i> Anterior
                            </Button>
                            <div className="d-flex align-items-center px-3 text-secondary">
                                Página {page + 1} de {totalPages}
                            </div>
                            <Button 
                                variant="outline-secondary" 
                                disabled={page >= totalPages - 1}
                                onClick={() => fetchFreeAgents(page + 1)}
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
                                    Valor recomendado: {selectedPlayer.priceRp} RP
                                </Form.Text>
                            </Form.Group>
                            
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
