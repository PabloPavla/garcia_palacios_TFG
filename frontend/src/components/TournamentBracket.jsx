import { useState, useEffect } from 'react';
import { Row, Col, Card, Button, Spinner, Alert, Badge, Nav } from 'react-bootstrap';
import leagueService from '../services/leagueService';
import clubService from '../services/clubService';

const TournamentBracket = ({ leagueId, activeClubId }) => {
    const [matches, setMatches] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [clubsCache, setClubsCache] = useState({});
    const [activeRound, setActiveRound] = useState('');
    const [isRefreshing, setIsRefreshing] = useState(false);

    const getRoundNum = (roundStr) => {
        if (!roundStr) return 999;
        const numMatch = roundStr.match(/\d+/);
        return numMatch ? parseInt(numMatch[0], 10) : 999;
    };

    const loadTournamentData = async (silent = false) => {
        try {
            if (!silent) setLoading(true);
            const response = await leagueService.getLeagueMatches(leagueId);
            // Filtrar partidos que tengan ronda asignada
            const tournamentMatches = (response.content || []).filter(m => m.tournamentRound);
            setMatches(tournamentMatches);

            // Obtener clubes para cache
            const cCache = { ...clubsCache };
            let cacheUpdated = false;
            for (let m of tournamentMatches) {
                if (!cCache[m.homeClubId]) {
                    try {
                        const h = await clubService.getClubById(m.homeClubId);
                        cCache[m.homeClubId] = `[${h.acronym}] ${h.name}`;
                        cacheUpdated = true;
                    } catch (e) {
                        cCache[m.homeClubId] = `Club ${m.homeClubId}`;
                    }
                }
                if (!cCache[m.awayClubId]) {
                    try {
                        const a = await clubService.getClubById(m.awayClubId);
                        cCache[m.awayClubId] = `[${a.acronym}] ${a.name}`;
                        cacheUpdated = true;
                    } catch (e) {
                        cCache[m.awayClubId] = `Club ${m.awayClubId}`;
                    }
                }
            }
            if (cacheUpdated) {
                setClubsCache(cCache);
            }

            // Agrupar y obtener las rondas disponibles ordenadas numéricamente
            const uniqueRounds = Array.from(new Set(tournamentMatches.map(m => m.tournamentRound)))
                .sort((a, b) => getRoundNum(a) - getRoundNum(b));

            if (uniqueRounds.length > 0) {
                // Si la ronda activa no está inicializada o ya no existe en la lista de rondas
                setActiveRound(prev => {
                    if (prev && uniqueRounds.includes(prev)) {
                        return prev;
                    }
                    // Seleccionar la primera ronda que tenga partidos SCHEDULED pendientes
                    for (const r of uniqueRounds) {
                        const rMatches = tournamentMatches.filter(m => m.tournamentRound === r);
                        if (rMatches.some(m => m.status === 'SCHEDULED')) {
                            return r;
                        }
                    }
                    // Si todos están completados, seleccionar la primera ronda
                    return uniqueRounds[0];
                });
            }
        } catch (err) {
            setError('Error al cargar la información del torneo.');
        } finally {
            setLoading(false);
            setIsRefreshing(false);
        }
    };

    useEffect(() => {
        loadTournamentData();
    }, [leagueId]);

    // Intervalo de auto-refresco si hay partidos programados
    useEffect(() => {
        const hasScheduled = matches.some(m => m.status === 'SCHEDULED');
        if (!hasScheduled) return;

        const interval = setInterval(() => {
            loadTournamentData(true);
        }, 10000);

        return () => clearInterval(interval);
    }, [matches, leagueId]);

    const handleManualRefresh = () => {
        setIsRefreshing(true);
        loadTournamentData(true);
    };

    const formatMatchTime = (dateStr) => {
        if (!dateStr) return '';
        try {
            const date = new Date(dateStr);
            return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
        } catch (e) {
            return '';
        }
    };

    const getTimeRemainingStr = (matchDateStr) => {
        const matchTime = new Date(matchDateStr).getTime();
        const now = Date.now();
        const diffMs = matchTime - now;
        if (diffMs <= 0) {
            return "Simulando...";
        }
        const diffMin = Math.ceil(diffMs / 60000);
        return `En ${diffMin} min`;
    };

    if (loading) {
        return (
            <div className="text-center py-5">
                <Spinner animation="grow" variant="gold" style={{ color: 'var(--brand-gold)' }} />
            </div>
        );
    }

    if (error) {
        return <Alert variant="warning">{error}</Alert>;
    }

    if (matches.length === 0) {
        return (
            <Alert variant="info" className="bg-dark text-white border-secondary text-center">
                El torneo aún no ha comenzado. Espera a que el creador de la liga comience la competición.
            </Alert>
        );
    }

    const uniqueRounds = Array.from(new Set(matches.map(m => m.tournamentRound)))
        .sort((a, b) => getRoundNum(a) - getRoundNum(b));

    const activeRoundMatches = matches.filter(m => m.tournamentRound === activeRound);

    return (
        <div className="tournament-bracket mt-4 p-4 rounded animate-fade-in" style={{ backgroundColor: 'rgba(0,0,0,0.4)', border: '1px solid #333' }}>
            <div className="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
                <div>
                    <h3 className="text-white mb-0 fw-bold">
                        <i className="bi bi-calendar-event text-warning me-2"></i> CALENDARIO DEL TORNEO
                    </h3>
                    <p className="text-secondary mb-0">Partidos programados y resultados en tiempo real</p>
                </div>
                <Button 
                    variant="outline-secondary" 
                    size="sm" 
                    onClick={handleManualRefresh}
                    disabled={isRefreshing}
                    className="text-white d-flex align-items-center gap-2"
                >
                    {isRefreshing ? <Spinner animation="border" size="sm" /> : <i className="bi bi-arrow-clockwise"></i>}
                    Actualizar
                </Button>
            </div>

            {/* Selector de Rondas / Jornadas */}
            <div className="mb-4 overflow-auto pb-2">
                <Nav variant="pills" className="flex-nowrap gap-2" activeKey={activeRound}>
                    {uniqueRounds.map(r => {
                        const rMatches = matches.filter(m => m.tournamentRound === r);
                        const allCompleted = rMatches.every(m => m.status === 'COMPLETED');
                        return (
                            <Nav.Item key={r}>
                                <Nav.Link 
                                    eventKey={r} 
                                    onClick={() => setActiveRound(r)}
                                    className={`px-3 py-2 fw-bold rounded-pill text-uppercase ${
                                        activeRound === r 
                                            ? 'bg-warning text-dark' 
                                            : allCompleted 
                                                ? 'bg-dark text-success border border-success border-opacity-25' 
                                                : 'bg-dark text-secondary border border-secondary border-opacity-25'
                                    }`}
                                >
                                    {r} {allCompleted && <i className="bi bi-check-circle-fill ms-1"></i>}
                                </Nav.Link>
                            </Nav.Item>
                        );
                    })}
                </Nav>
            </div>

            {/* Grid de Partidos */}
            <Row className="g-4">
                {activeRoundMatches.map(match => {
                    const isCompleted = match.status === 'COMPLETED';
                    const homeWinner = isCompleted && match.homeScore > match.awayScore;
                    const awayWinner = isCompleted && match.awayScore > match.homeScore;

                    return (
                        <Col key={match.id} md={6} lg={4}>
                            <Card className="border-secondary h-100 shadow-sm" style={{ backgroundColor: 'rgba(20,20,20,0.85)', minHeight: '160px' }}>
                                <Card.Body className="d-flex flex-column justify-content-between p-3">
                                    <div className="d-flex justify-content-between align-items-center mb-3">
                                        <Badge bg="dark" className="text-secondary border border-secondary border-opacity-50">
                                            ID: {match.id}
                                        </Badge>
                                        {isCompleted ? (
                                            <Badge bg="success" className="bg-opacity-25 text-success">
                                                Finalizado
                                            </Badge>
                                        ) : (
                                            <Badge bg="warning" className="bg-opacity-25 text-warning d-flex align-items-center gap-1">
                                                <i className="bi bi-clock-fill"></i>
                                                {formatMatchTime(match.matchDate)} ({getTimeRemainingStr(match.matchDate)})
                                            </Badge>
                                        )}
                                    </div>

                                    {/* Marcador */}
                                    <div className="flex-grow-1 d-flex flex-column justify-content-center gap-2 py-2">
                                        {/* Home Team */}
                                        <div className="d-flex justify-content-between align-items-center">
                                            <span className={`fw-bold text-truncate ${homeWinner ? 'text-warning' : isCompleted ? 'text-secondary' : 'text-white'}`} style={{ maxWidth: '80%' }}>
                                                {homeWinner && <i className="bi bi-caret-right-fill text-warning me-1"></i>}
                                                {clubsCache[match.homeClubId] || `Club ${match.homeClubId}`}
                                            </span>
                                            <span className={`fw-bold fs-4 ${homeWinner ? 'text-warning' : isCompleted ? 'text-secondary' : 'text-white'}`}>
                                                {isCompleted ? match.homeScore : '-'}
                                            </span>
                                        </div>

                                        {/* Away Team */}
                                        <div className="d-flex justify-content-between align-items-center">
                                            <span className={`fw-bold text-truncate ${awayWinner ? 'text-warning' : isCompleted ? 'text-secondary' : 'text-white'}`} style={{ maxWidth: '80%' }}>
                                                {awayWinner && <i className="bi bi-caret-right-fill text-warning me-1"></i>}
                                                {clubsCache[match.awayClubId] || `Club ${match.awayClubId}`}
                                            </span>
                                            <span className={`fw-bold fs-4 ${awayWinner ? 'text-warning' : isCompleted ? 'text-secondary' : 'text-white'}`}>
                                                {isCompleted ? match.awayScore : '-'}
                                            </span>
                                        </div>
                                    </div>
                                </Card.Body>
                            </Card>
                        </Col>
                    );
                })}
            </Row>
        </div>
    );
};

export default TournamentBracket;
