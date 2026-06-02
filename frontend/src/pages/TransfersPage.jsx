import { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Badge, Button, Spinner, Alert } from 'react-bootstrap';
import clubService from '../services/clubService';
import transferService from '../services/transferService';

const getStatusBadge = (status) => {
    switch (status) {
        case 'PENDING': return <Badge bg="warning" text="dark"><i className="bi bi-hourglass-split me-1"></i> PENDIENTE</Badge>;
        case 'ACCEPTED': return <Badge bg="success"><i className="bi bi-check-circle me-1"></i> ACEPTADA</Badge>;
        case 'REJECTED': return <Badge bg="danger"><i className="bi bi-x-circle me-1"></i> RECHAZADA</Badge>;
        case 'CANCELLED': return <Badge bg="secondary"><i className="bi bi-slash-circle me-1"></i> CANCELADA</Badge>;
        default: return <Badge bg="light" text="dark">{status}</Badge>;
    }
};

const TransfersPage = () => {
    const [club, setClub] = useState(null);
    const [transfers, setTransfers] = useState([]);
    const [playersCache, setPlayersCache] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const loadData = async () => {
        setLoading(true);
        try {
            const myClub = await clubService.getMyClub();
            setClub(myClub);

            const transfersData = await transferService.getClubBuyingHistory(myClub.id, 0);
            const content = transfersData.content || [];
            
            // Obtener nombres de jugadores en paralelo
            const pCache = { ...playersCache };
            for (let t of content) {
                if (!pCache[t.playerId]) {
                    try {
                        const playerInfo = await clubService.getPlayerById(t.playerId);
                        pCache[t.playerId] = playerInfo.summonerName;
                    } catch (e) {
                        pCache[t.playerId] = "Desconocido";
                    }
                }
            }
            setPlayersCache(pCache);
            setTransfers(content);
        } catch (err) {
            setError('Error al cargar el historial de transferencias. ¿Has creado ya tu club?');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadData();
        // eslint-disable-next-line
    }, []);

    const handleCancel = async (id) => {
        try {
            await transferService.cancelOffer(id);
            // Recargar datos
            loadData();
        } catch (err) {
            alert('No se pudo cancelar la oferta.');
        }
    };

    return (
        <Container className="mt-4 pt-4 animate-fade-in">
            <h1 className="fw-bold mb-1 text-white"><i className="bi bi-arrow-left-right text-primary me-2"></i> MIS TRANSFERENCIAS</h1>
            <p className="text-secondary mb-5">Historial de ofertas y negociaciones de tu club.</p>

            {error && <Alert variant="danger">{error}</Alert>}

            {loading ? (
                <div className="text-center py-5">
                    <Spinner animation="grow" variant="primary" />
                </div>
            ) : transfers.length === 0 ? (
                <div className="glass-card p-5 text-center mt-4">
                    <i className="bi bi-envelope-x display-1 text-secondary mb-3"></i>
                    <h3>Sin ofertas activas</h3>
                    <p className="text-secondary">Ve al Mercado para empezar a negociar por nuevos talentos.</p>
                </div>
            ) : (
                <Row className="g-4">
                    {transfers.map(transfer => (
                        <Col md={6} lg={4} key={transfer.id}>
                            <Card className="glass-card h-100 border-0">
                                <Card.Body>
                                    <div className="d-flex justify-content-between align-items-center mb-3">
                                        <span className="text-secondary small">ID: #{transfer.id}</span>
                                        {getStatusBadge(transfer.status)}
                                    </div>
                                    <Card.Title className="fw-bold fs-3 text-white mb-1">
                                        {playersCache[transfer.playerId] || 'Cargando...'}
                                    </Card.Title>
                                    <p className="text-info mb-4 fs-5 fw-bold">
                                        {new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' }).format(transfer.transferFee)}
                                    </p>
                                    
                                    <div className="text-secondary small border-top border-secondary pt-3 mt-3">
                                        <div><i className="bi bi-calendar-event me-2"></i> Ofertado: {new Date(transfer.offeredAt).toLocaleDateString()}</div>
                                        {transfer.resolvedAt && (
                                            <div><i className="bi bi-clock-history me-2"></i> Resuelto: {new Date(transfer.resolvedAt).toLocaleDateString()}</div>
                                        )}
                                    </div>

                                    {transfer.status === 'PENDING' && (
                                        <Button 
                                            variant="outline-danger" 
                                            className="w-100 mt-4"
                                            onClick={() => handleCancel(transfer.id)}
                                        >
                                            <i className="bi bi-x-circle me-1"></i> Cancelar Oferta
                                        </Button>
                                    )}
                                </Card.Body>
                            </Card>
                        </Col>
                    ))}
                </Row>
            )}
        </Container>
    );
};

export default TransfersPage;
