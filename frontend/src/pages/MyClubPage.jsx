import { useState, useEffect } from 'react';
import { Container, Row, Col, Badge, Spinner, Alert, Card } from 'react-bootstrap';
import clubService from '../services/clubService';

const getRoleIcon = (role) => {
    // Retorna iconos representativos para las posiciones de LoL
    switch (role) {
        case 'TOP': return 'bi-shield-shaded';
        case 'JUNGLE': return 'bi-tree-fill';
        case 'MID': return 'bi-lightning-fill';
        case 'ADC': return 'bi-bullseye';
        case 'SUPPORT': return 'bi-heart-pulse-fill';
        default: return 'bi-person-fill';
    }
};

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

const MyClubPage = () => {
    const [club, setClub] = useState(null);
    const [players, setPlayers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchClubData = async () => {
            try {
                const myClub = await clubService.getMyClub();
                setClub(myClub);
                const clubPlayers = await clubService.getClubPlayers(myClub.id);
                setPlayers(clubPlayers);
            } catch (err) {
                setError('No se pudo cargar la información de la plantilla.');
            } finally {
                setLoading(false);
            }
        };
        fetchClubData();
    }, []);

    if (loading) {
        return (
            <Container className="d-flex justify-content-center align-items-center min-vh-100">
                <Spinner animation="grow" variant="primary" />
            </Container>
        );
    }

    if (error) {
        return <Container className="mt-5 pt-5"><Alert variant="danger">{error}</Alert></Container>;
    }

    if (!club) {
        return (
            <Container className="mt-5 pt-5 text-center">
                <h3 className="text-secondary">Primero debes crear un club en el Dashboard.</h3>
            </Container>
        );
    }

    return (
        <Container className="mt-4 pt-4 animate-fade-in">
            <div className="d-flex justify-content-between align-items-end mb-4 border-bottom border-secondary pb-3">
                <div>
                    <h1 className="fw-bold text-uppercase mb-0 text-white">[{club.acronym}] PLANTILLA</h1>
                    <p className="text-secondary mb-0">Gestiona tus jugadores profesionales</p>
                </div>
                <div className="text-end">
                    <h5 className="text-secondary mb-1">Riot Points Disponibles</h5>
                    <h3 className="text-primary fw-bold mb-0">
                        {club.riotPoints} RP
                    </h3>
                </div>
            </div>

            {players.length === 0 ? (
                <div className="glass-card p-5 text-center mt-4">
                    <i className="bi bi-people display-1 text-secondary mb-3"></i>
                    <h3>Tu plantilla está vacía</h3>
                    <p className="text-secondary">Dirígete al Mercado de Fichajes para contratar a tus primeros jugadores.</p>
                </div>
            ) : (
                <Row className="g-4">
                    {players.map((player) => (
                        <Col lg={4} md={6} key={player.id}>
                            <Card className="glass-card h-100 border-0 overflow-hidden">
                                <div className={`bg-${getRoleColor(player.lolRole)} p-1`} style={{ height: '4px' }}></div>
                                <Card.Body className="p-4">
                                    <div className="d-flex justify-content-between align-items-start mb-3">
                                        <Badge bg={getRoleColor(player.lolRole)} className="fs-6 py-2 px-3 rounded-pill">
                                            <i className={`bi ${getRoleIcon(player.lolRole)} me-2`}></i>
                                            {player.lolRole}
                                        </Badge>
                                        <div className="text-center bg-dark rounded-circle border border-secondary d-flex flex-column justify-content-center align-items-center" style={{ width: '55px', height: '55px' }}>
                                            <span className="fs-5 fw-bold text-white">{player.overallRating}</span>
                                        </div>
                                    </div>
                                    
                                    <Card.Title className="fs-3 fw-bold mb-1 text-white text-truncate">
                                        {player.summonerName}
                                    </Card.Title>
                                    <Card.Subtitle className="text-secondary mb-3">
                                        {player.realName || 'Nombre desconocido'}
                                    </Card.Subtitle>

                                    <hr className="border-secondary opacity-25" />

                                    <Row className="text-center">
                                        <Col>
                                            <small className="text-secondary d-block">Edad</small>
                                            <strong className="text-white">{player.age || '--'}</strong>
                                        </Col>
                                        <Col className="border-start border-secondary">
                                            <small className="text-secondary d-block">Nacionalidad</small>
                                            <strong className="text-white">{player.nationality || '--'}</strong>
                                        </Col>
                                    </Row>
                                    
                                    <div className="mt-4 pt-3 border-top border-secondary opacity-75 d-flex justify-content-between align-items-center">
                                        <span className="text-secondary small">Precio de mercado</span>
                                        <span className="fw-bold text-gold" style={{ color: 'var(--brand-gold)' }}>
                                            {player.priceRp} RP
                                        </span>
                                    </div>
                                </Card.Body>
                            </Card>
                        </Col>
                    ))}
                </Row>
            )}
        </Container>
    );
};

export default MyClubPage;
