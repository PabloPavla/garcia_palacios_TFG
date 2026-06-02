import { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Form, Button, Spinner, Alert } from 'react-bootstrap';
import { useAuth } from '../context/AuthContext';
import clubService from '../services/clubService';
import { Link } from 'react-router-dom';

const DashboardPage = () => {
    const { auth } = useAuth();
    const [club, setClub] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    
    // Estado para el formulario de crear club
    const [clubName, setClubName] = useState('');
    const [clubAcronym, setClubAcronym] = useState('');

    const fetchMyClub = async () => {
        try {
            const data = await clubService.getMyClub();
            setClub(data);
        } catch (err) {
            // Si es 400 o no se encuentra, significa que aún no ha creado un club
            if (err.response?.status !== 400 && err.response?.status !== 404) {
                setError('Error al cargar la información del club.');
            }
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchMyClub();
    }, []);

    const handleCreateClub = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
        try {
            await clubService.createClub({
                name: clubName,
                acronym: clubAcronym
            });
            await fetchMyClub();
        } catch (err) {
            setError(err.response?.data?.message || 'Error al crear el club.');
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <Container className="d-flex justify-content-center align-items-center min-vh-100">
                <Spinner animation="border" variant="info" />
            </Container>
        );
    }

    // VISTA 1: Usuario aún NO ha creado su club
    if (!club) {
        return (
            <Container className="mt-5 pt-4 animate-fade-in">
                <Row className="justify-content-center">
                    <Col md={8} lg={6}>
                        <div className="glass-card p-5 text-center">
                            <i className="bi bi-shield-plus display-1 text-primary mb-3"></i>
                            <h2 className="fw-bold mb-4">FUNDA TU CLUB EN CLASH MANAGER</h2>
                            <p className="text-secondary mb-4">
                                ¡Bienvenido Mánager {auth?.username}! Parece que aún no has registrado tu equipo en la liga. 
                                Elige un nombre épico y comienza tu camino a la gloria.
                            </p>

                            {error && <Alert variant="danger">{error}</Alert>}

                            <Form onSubmit={handleCreateClub} className="text-start">
                                <Form.Group className="mb-4">
                                    <Form.Label className="text-secondary fw-semibold">Nombre del Club</Form.Label>
                                    <Form.Control 
                                        type="text" 
                                        placeholder="Ej: T1, Fnatic, G2" 
                                        value={clubName}
                                        onChange={(e) => setClubName(e.target.value)}
                                        required 
                                        maxLength={100}
                                        style={{ backgroundColor: 'rgba(0,0,0,0.3)', color: 'white', borderColor: 'var(--border-color)' }}
                                    />
                                </Form.Group>

                                <Form.Group className="mb-4">
                                    <Form.Label className="text-secondary fw-semibold">Acrónimo (Tag)</Form.Label>
                                    <Form.Control 
                                        type="text" 
                                        placeholder="Ej: T1, FNC, G2" 
                                        value={clubAcronym}
                                        onChange={(e) => setClubAcronym(e.target.value)}
                                        required 
                                        minLength={2}
                                        maxLength={5}
                                        style={{ backgroundColor: 'rgba(0,0,0,0.3)', color: 'white', borderColor: 'var(--border-color)', textTransform: 'uppercase' }}
                                    />
                                    <Form.Text className="text-muted">Entre 2 y 5 caracteres.</Form.Text>
                                </Form.Group>

                                <div className="d-grid mt-4">
                                    <Button variant="primary" type="submit" size="lg" className="fw-bold">
                                        CREAR EQUIPO <i className="bi bi-arrow-right-circle ms-2"></i>
                                    </Button>
                                </div>
                            </Form>
                        </div>
                    </Col>
                </Row>
            </Container>
        );
    }

    // VISTA 2: Dashboard principal (ya tiene club)
    return (
        <Container className="mt-4 pt-3 animate-fade-in">
            <div className="d-flex justify-content-between align-items-center mb-5">
                <div>
                    <h1 className="fw-bold mb-0">Dashboard</h1>
                    <p className="text-secondary">Bienvenido de nuevo, Mánager {auth?.username}</p>
                </div>
                <div className="text-end">
                    <h4 className="fw-bold text-primary mb-0">[{club.acronym}] {club.name}</h4>
                    <span className="badge bg-secondary">DIVISIÓN: {club.division}</span>
                </div>
            </div>

            <Row className="g-4">
                {/* Tarjeta Presupuesto */}
                <Col md={4}>
                    <div className="glass-card p-4 h-100 border-start border-primary border-4">
                        <div className="d-flex justify-content-between">
                            <h5 className="text-secondary fw-bold">RIOT POINTS</h5>
                            <i className="bi bi-gem fs-4 text-primary"></i>
                        </div>
                        <h2 className="mt-3 fw-bold text-white mb-0">
                            {club.riotPoints} RP
                        </h2>
                    </div>
                </Col>
                
                {/* Tarjeta Plantilla */}
                <Col md={4}>
                    <div className="glass-card p-4 h-100 border-start border-info border-4">
                        <div className="d-flex justify-content-between">
                            <h5 className="text-secondary fw-bold">PLANTILLA</h5>
                            <i className="bi bi-people-fill fs-4 text-info"></i>
                        </div>
                        <h2 className="mt-3 fw-bold text-white mb-0">
                            {club.playerCount} <span className="fs-6 text-secondary fw-normal">/ 10 Jugadores</span>
                        </h2>
                    </div>
                </Col>
                
                {/* Tarjeta Próximo Partido (Placeholder visual) */}
                <Col md={4}>
                    <div className="glass-card p-4 h-100 border-start border-gold border-4" style={{ borderColor: 'var(--brand-gold)' }}>
                        <div className="d-flex justify-content-between">
                            <h5 className="text-secondary fw-bold">LIGA</h5>
                            <i className="bi bi-trophy-fill fs-4" style={{ color: 'var(--brand-gold)' }}></i>
                        </div>
                        <div className="mt-3">
                            <Button as={Link} to="/league" variant="outline-primary" className="w-100">
                                Ver Clasificación
                            </Button>
                        </div>
                    </div>
                </Col>
            </Row>

            <Row className="mt-5 g-4">
                <Col md={6}>
                    <div className="glass-card p-4 text-center action-card hover-glow">
                        <i className="bi bi-shield-fill display-4 text-primary mb-3"></i>
                        <h3>Gestionar Plantilla</h3>
                        <p className="text-secondary">Visualiza a tus jugadores, revisa estadísticas y ajusta roles.</p>
                        <Button as={Link} to="/my-club" variant="primary" className="mt-2">Ir a Mi Club</Button>
                    </div>
                </Col>
                <Col md={6}>
                    <div className="glass-card p-4 text-center action-card hover-glow">
                        <i className="bi bi-shop display-4 text-info mb-3"></i>
                        <h3>Mercado de Fichajes</h3>
                        <p className="text-secondary">Busca agentes libres y refuerza tu equipo para competir en la liga.</p>
                        <Button as={Link} to="/leagues" variant="outline-primary" className="mt-2">Ver Ligas</Button>
                    </div>
                </Col>
            </Row>
        </Container>
    );
};

export default DashboardPage;
