import { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Badge, Spinner, Alert, Button } from 'react-bootstrap';
import leagueService from '../services/leagueService';
import clubService from '../services/clubService';

const MatchesPage = () => {
    const [league, setLeague] = useState(null);
    const [matches, setMatches] = useState([]);
    const [clubsCache, setClubsCache] = useState({});
    
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    
    // Pagination
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    const loadData = async (pageNumber = 0) => {
        setLoading(true);
        try {
            // 1. Obtener la liga activa
            const leagues = await leagueService.getAllLeagues();
            if (leagues.length === 0) {
                setError('No hay ninguna liga activa.');
                setLoading(false);
                return;
            }
            
            const currentLeague = leagues[0];
            setLeague(currentLeague);

            // 2. Obtener partidos
            const response = await leagueService.getMatches(currentLeague.id, pageNumber);
            const content = response.content || [];
            
            // 3. Cachear nombres de clubes
            const cCache = { ...clubsCache };
            for (let m of content) {
                if (!cCache[m.homeClubId]) {
                    try {
                        const c = await clubService.getClubById(m.homeClubId);
                        cCache[m.homeClubId] = c.acronym;
                    } catch(e) { cCache[m.homeClubId] = '???'; }
                }
                if (!cCache[m.awayClubId]) {
                    try {
                        const c = await clubService.getClubById(m.awayClubId);
                        cCache[m.awayClubId] = c.acronym;
                    } catch(e) { cCache[m.awayClubId] = '???'; }
                }
            }
            
            setClubsCache(cCache);
            setMatches(content);
            setTotalPages(response.totalPages || 0);
            setPage(pageNumber);
        } catch (err) {
            setError('Error al cargar los partidos.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadData(0);
        // eslint-disable-next-line
    }, []);

    const getStatusBadge = (status) => {
        switch (status) {
            case 'SCHEDULED': return <Badge bg="primary">PROGRAMADO</Badge>;
            case 'COMPLETED': return <Badge bg="success">FINALIZADO</Badge>;
            case 'CANCELLED': return <Badge bg="danger">CANCELADO</Badge>;
            default: return <Badge bg="secondary">{status}</Badge>;
        }
    };

    if (loading && matches.length === 0) {
        return (
            <Container className="d-flex justify-content-center align-items-center min-vh-100">
                <Spinner animation="grow" variant="primary" />
            </Container>
        );
    }

    return (
        <Container className="mt-4 pt-4 animate-fade-in">
            <h1 className="fw-bold mb-1 text-white"><i className="bi bi-calendar-event text-primary me-2"></i> JORNADAS Y RESULTADOS</h1>
            <p className="text-secondary mb-5">
                {league ? `Calendario oficial - ${league.name}` : 'Calendario oficial'}
            </p>

            {error && <Alert variant="warning">{error}</Alert>}

            {!error && matches.length === 0 ? (
                <div className="glass-card p-5 text-center mt-4">
                    <i className="bi bi-calendar-x display-1 text-secondary mb-3"></i>
                    <h3>No hay partidos programados</h3>
                    <p className="text-secondary">El administrador aún no ha generado el calendario.</p>
                </div>
            ) : (
                <>
                    <Row className="g-4">
                        {matches.map(match => (
                            <Col md={6} lg={4} key={match.id}>
                                <Card className="glass-card h-100 border-0 text-center">
                                    <Card.Body className="d-flex flex-column justify-content-center">
                                        <div className="mb-3">
                                            {getStatusBadge(match.status)}
                                            <div className="text-secondary small mt-2">
                                                <i className="bi bi-clock"></i> {new Date(match.matchDate).toLocaleString()}
                                            </div>
                                        </div>
                                        
                                        <div className="d-flex justify-content-around align-items-center mb-3">
                                            <div className="w-25 text-end">
                                                <h3 className="fw-bold text-white mb-0">{clubsCache[match.homeClubId] || '...'}</h3>
                                            </div>
                                            
                                            <div className="w-50 mx-2 bg-dark rounded-pill py-2 border border-secondary">
                                                <span className="fs-3 fw-bold text-primary mx-2">
                                                    {match.status === 'COMPLETED' ? match.homeScore : '-'}
                                                </span>
                                                <span className="text-secondary">:</span>
                                                <span className="fs-3 fw-bold text-primary mx-2">
                                                    {match.status === 'COMPLETED' ? match.awayScore : '-'}
                                                </span>
                                            </div>
                                            
                                            <div className="w-25 text-start">
                                                <h3 className="fw-bold text-white mb-0">{clubsCache[match.awayClubId] || '...'}</h3>
                                            </div>
                                        </div>
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
                                onClick={() => loadData(page - 1)}
                            >
                                <i className="bi bi-chevron-left"></i> Anterior
                            </Button>
                            <div className="d-flex align-items-center px-3 text-secondary">
                                Página {page + 1} de {totalPages}
                            </div>
                            <Button 
                                variant="outline-secondary" 
                                disabled={page >= totalPages - 1}
                                onClick={() => loadData(page + 1)}
                            >
                                Siguiente <i className="bi bi-chevron-right"></i>
                            </Button>
                        </div>
                    )}
                </>
            )}
        </Container>
    );
};

export default MatchesPage;
