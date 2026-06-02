import { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Badge, Button, Spinner, Alert, Tabs, Tab, Modal, Form } from 'react-bootstrap';
import clubService from '../services/clubService';
import transferService from '../services/transferService';

const getStatusBadge = (status) => {
    switch (status) {
        case 'PENDING': return <Badge bg="warning" text="dark"><i className="bi bi-hourglass-split me-1"></i> PENDIENTE</Badge>;
        case 'ACCEPTED': return <Badge bg="success"><i className="bi bi-check-circle me-1"></i> ACEPTADA</Badge>;
        case 'REJECTED': return <Badge bg="danger"><i className="bi bi-x-circle me-1"></i> RECHAZADA</Badge>;
        case 'CANCELLED': return <Badge bg="secondary"><i className="bi bi-slash-circle me-1"></i> CANCELADA</Badge>;
        case 'COUNTER_OFFERED': return <Badge bg="info" text="dark"><i className="bi bi-arrow-repeat me-1"></i> CONTRAOFERTA</Badge>;
        case 'AUCTION': return <Badge bg="primary"><i className="bi bi-gavel me-1"></i> SUBASTA</Badge>;
        default: return <Badge bg="light" text="dark">{status}</Badge>;
    }
};

const TransfersPage = () => {
    const [club, setClub] = useState(null);
    const [buyingTransfers, setBuyingTransfers] = useState([]);
    const [sellingTransfers, setSellingTransfers] = useState([]);
    const [playersCache, setPlayersCache] = useState({});
    const [myPlayers, setMyPlayers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Modal para Contraoferta
    const [showCounterModal, setShowCounterModal] = useState(false);
    const [selectedTransfer, setSelectedTransfer] = useState(null);
    const [counterFee, setCounterFee] = useState(0);
    const [counterExchangeId, setCounterExchangeId] = useState('');
    const [submitting, setSubmitting] = useState(false);

    const loadData = async () => {
        setLoading(true);
        try {
            const myClubList = await clubService.getMyClubs();
            if (!myClubList || myClubList.length === 0) {
                setError('No tienes ningún club creado.');
                setLoading(false);
                return;
            }
            const myClub = myClubList[0];
            setClub(myClub);

            const [buyingData, sellingData, playersResp] = await Promise.all([
                transferService.getClubBuyingHistory(myClub.id, 0),
                transferService.getClubSellingHistory(myClub.id, 0),
                clubService.getClubPlayers(myClub.id)
            ]);

            setMyPlayers(playersResp || []);
            const buyingContent = buyingData.content || [];
            const sellingContent = sellingData.content || [];
            
            // Fetch player names
            const pCache = { ...playersCache };
            const fetchPlayerName = async (pId) => {
                if (!pCache[pId] && pId) {
                    try {
                        const p = await clubService.getPlayerById(pId);
                        pCache[pId] = p.summonerName;
                    } catch (e) {
                        pCache[pId] = "Desconocido";
                    }
                }
            };

            for (let t of [...buyingContent, ...sellingContent]) {
                await fetchPlayerName(t.playerId);
                await fetchPlayerName(t.exchangePlayerId);
                await fetchPlayerName(t.counterExchangePlayerId);
            }
            
            setPlayersCache(pCache);
            setBuyingTransfers(buyingContent);
            setSellingTransfers(sellingContent);
        } catch (err) {
            setError('Error al cargar el historial de transferencias.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadData();
    }, []);

    const handleCancel = async (id) => {
        try {
            await transferService.cancelOffer(id, club.id);
            loadData();
        } catch (err) {
            alert('No se pudo cancelar la oferta.');
        }
    };

    const handleAccept = async (id) => {
        try {
            await transferService.acceptOffer(id, club.id);
            loadData();
        } catch (err) {
            alert(err.response?.data?.message || 'No se pudo aceptar la oferta.');
        }
    };

    const handleReject = async (id) => {
        try {
            await transferService.rejectOffer(id, club.id);
            loadData();
        } catch (err) {
            alert('No se pudo rechazar la oferta.');
        }
    };

    const openCounterModal = (transfer) => {
        setSelectedTransfer(transfer);
        const baseFee = transfer.status === 'COUNTER_OFFERED' ? transfer.counterTransferFeeRp : transfer.transferFeeRp;
        setCounterFee(baseFee || 0);
        setCounterExchangeId('');
        setShowCounterModal(true);
    };

    const submitCounterOffer = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        try {
            const exId = counterExchangeId ? parseInt(counterExchangeId) : null;
            await transferService.counterOffer(selectedTransfer.id, counterFee, exId, club.id);
            setShowCounterModal(false);
            loadData();
        } catch (err) {
            alert(err.response?.data?.message || 'Error al enviar contraoferta');
        } finally {
            setSubmitting(false);
        }
    };

    const renderTransferCard = (transfer, type) => {
        const isMyTurn = (transfer.status === 'PENDING' || transfer.status === 'COUNTER_OFFERED') && transfer.lastNegotiatorClubId !== club?.id;
        const currentFee = transfer.status === 'COUNTER_OFFERED' ? transfer.counterTransferFeeRp : transfer.transferFeeRp;
        const currentExchange = transfer.status === 'COUNTER_OFFERED' ? transfer.counterExchangePlayerId : transfer.exchangePlayerId;

        return (
            <Col md={6} lg={4} key={transfer.id}>
                <Card className="glass-card h-100 border-0 bg-dark text-white shadow">
                    <Card.Body>
                        <div className="d-flex justify-content-between align-items-center mb-3">
                            <span className="text-secondary small">ID: #{transfer.id}</span>
                            {getStatusBadge(transfer.status)}
                        </div>
                        <Card.Title className="fw-bold fs-4 mb-2">
                            {playersCache[transfer.playerId] || 'Cargando...'}
                        </Card.Title>
                        
                        <div className="bg-black bg-opacity-25 rounded p-3 mb-3">
                            <div className="d-flex justify-content-between mb-1">
                                <span className="text-secondary">RP Ofertados:</span>
                                <span className="fw-bold text-info">{currentFee || 0} RP</span>
                            </div>
                            {currentExchange && (
                                <div className="d-flex justify-content-between">
                                    <span className="text-secondary">A cambio:</span>
                                    <span className="fw-bold text-warning">{playersCache[currentExchange] || 'Cargando...'}</span>
                                </div>
                            )}
                        </div>
                        
                        <div className="text-secondary small border-top border-secondary pt-3 mt-3">
                            <div><i className="bi bi-calendar-event me-2"></i> Ofertado: {new Date(transfer.offeredAt).toLocaleDateString()}</div>
                        </div>

                        {/* Controles para Ofertas Enviadas */}
                        {type === 'sent' && transfer.status === 'PENDING' && (
                            <Button variant="outline-danger" className="w-100 mt-3" onClick={() => handleCancel(transfer.id)}>
                                Cancelar Oferta
                            </Button>
                        )}
                        
                        {/* Controles cuando es mi turno de responder */}
                        {isMyTurn && (
                            <div className="d-flex gap-2 mt-3">
                                <Button variant="success" className="flex-grow-1" onClick={() => handleAccept(transfer.id)}>Aceptar</Button>
                                <Button variant="warning" className="flex-grow-1" onClick={() => openCounterModal(transfer)}>Negociar</Button>
                                <Button variant="danger" className="flex-grow-1" onClick={() => handleReject(transfer.id)}>Rechazar</Button>
                            </div>
                        )}
                        
                        {(transfer.status === 'PENDING' || transfer.status === 'COUNTER_OFFERED') && !isMyTurn && type === 'received' && (
                            <div className="text-warning text-center mt-3 small">Esperando respuesta del otro club...</div>
                        )}
                    </Card.Body>
                </Card>
            </Col>
        );
    };

    return (
        <Container className="mt-4 pt-4 animate-fade-in">
            <h1 className="fw-bold mb-1 text-white"><i className="bi bi-arrow-left-right text-primary me-2"></i> TRANSFERENCIAS</h1>
            <p className="text-secondary mb-5">Gestiona tus ofertas enviadas y recibidas.</p>

            {error && <Alert variant="danger">{error}</Alert>}

            {loading ? (
                <div className="text-center py-5"><Spinner animation="border" variant="primary" /></div>
            ) : (
                <Tabs defaultActiveKey="received" className="mb-4 bg-dark rounded custom-tabs">
                    <Tab eventKey="received" title={`Recibidas (${sellingTransfers.length})`}>
                        {sellingTransfers.length === 0 ? (
                            <div className="text-center p-5"><p className="text-secondary">No has recibido ofertas.</p></div>
                        ) : (
                            <Row className="g-4">{sellingTransfers.map(t => renderTransferCard(t, 'received'))}</Row>
                        )}
                    </Tab>
                    <Tab eventKey="sent" title={`Enviadas (${buyingTransfers.length})`}>
                        {buyingTransfers.length === 0 ? (
                            <div className="text-center p-5"><p className="text-secondary">No has enviado ofertas.</p></div>
                        ) : (
                            <Row className="g-4">{buyingTransfers.map(t => renderTransferCard(t, 'sent'))}</Row>
                        )}
                    </Tab>
                </Tabs>
            )}

            {/* Modal de Contraoferta */}
            <Modal show={showCounterModal} onHide={() => setShowCounterModal(false)} centered contentClassName="bg-dark text-white border-primary">
                <Modal.Header closeButton closeVariant="white" className="border-secondary">
                    <Modal.Title>Enviar Contraoferta</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Form onSubmit={submitCounterOffer}>
                        <Form.Group className="mb-3">
                            <Form.Label>Oferta Económica (RP)</Form.Label>
                            <Form.Control type="number" min="0" step="100" className="bg-black text-white" value={counterFee} onChange={e => setCounterFee(e.target.value)} required />
                        </Form.Group>
                        <Form.Group className="mb-4">
                            <Form.Label>Jugador a Intercambiar (Opcional)</Form.Label>
                            <Form.Select className="bg-black text-white" value={counterExchangeId} onChange={e => setCounterExchangeId(e.target.value)}>
                                <option value="">-- Sin jugador a cambio --</option>
                                {myPlayers.map(p => (
                                    <option key={p.id} value={p.id}>{p.summonerName} - {p.priceRp} RP</option>
                                ))}
                            </Form.Select>
                        </Form.Group>
                        <Button variant="primary" type="submit" className="w-100" disabled={submitting}>
                            {submitting ? <Spinner size="sm" /> : 'ENVIAR CONTRAOFERTA'}
                        </Button>
                    </Form>
                </Modal.Body>
            </Modal>
        </Container>
    );
};

export default TransfersPage;
