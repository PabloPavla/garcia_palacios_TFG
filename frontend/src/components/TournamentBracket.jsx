import { useState, useEffect } from 'react';
import { Row, Col, Card, Button, Spinner, Alert, Badge } from 'react-bootstrap';
import leagueService from '../services/leagueService';
import clubService from '../services/clubService';

const TournamentBracket = ({ leagueId, activeClubId }) => {
    const [matches, setMatches] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [clubsCache, setClubsCache] = useState({});

    const loadTournamentData = async () => {
        try {
            setLoading(true);
            // We assume there's an endpoint to get league matches
            const response = await leagueService.getLeagueMatches(leagueId);
            const tournamentMatches = response.content.filter(m => m.tournamentRound);
            setMatches(tournamentMatches);

            // Fetch clubs for cache
            const cCache = {};
            for (let m of tournamentMatches) {
                if (!cCache[m.homeClubId]) {
                    const h = await clubService.getClubById(m.homeClubId);
                    cCache[m.homeClubId] = h.acronym;
                }
                if (!cCache[m.awayClubId]) {
                    const a = await clubService.getClubById(m.awayClubId);
                    cCache[m.awayClubId] = a.acronym;
                }
            }
            setClubsCache(cCache);
        } catch (err) {
            setError('Error al cargar el torneo');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadTournamentData();
    }, [leagueId]);

    const handleAcceptWager = async (matchId) => {
        try {
            await leagueService.acceptWager(matchId, activeClubId);
            alert('¡Apuesta aceptada!');
            loadTournamentData(); // Recargar para ver si ya se simuló
        } catch (err) {
            alert('Error al aceptar la apuesta: ' + (err.response?.data?.message || err.message));
        }
    };

    if (loading) return <div className="text-center py-5"><Spinner animation="border" variant="gold" style={{color: 'var(--brand-gold)'}}/></div>;
    if (error) return <Alert variant="warning">{error}</Alert>;

    const semifinals = matches.filter(m => m.tournamentRound === 'SEMIFINAL');
    const finals = matches.filter(m => m.tournamentRound === 'FINAL');

    if (matches.length === 0) {
        return <Alert variant="info" className="bg-dark text-white border-secondary">El torneo aún no ha sido generado.</Alert>;
    }

    const renderMatchBox = (match) => {
        if (!match) return <div className="match-box empty" style={{ border: '1px solid #333', padding: '10px', borderRadius: '5px', height: '100px', backgroundColor: 'rgba(0,0,0,0.5)' }}></div>;
        
        const isParticipant = match.homeClubId === activeClubId || match.awayClubId === activeClubId;
        const participantAccepted = (match.homeClubId === activeClubId && match.homeWagerAccepted) || 
                                    (match.awayClubId === activeClubId && match.awayWagerAccepted);

        return (
            <div className="match-box p-3 mb-3" style={{ border: '1px solid var(--brand-gold)', borderRadius: '8px', backgroundColor: 'rgba(20,20,20,0.8)' }}>
                <div className="d-flex justify-content-between align-items-center mb-2">
                    <span className={`fw-bold ${match.homeScore > match.awayScore ? 'text-warning' : 'text-white'}`}>{clubsCache[match.homeClubId] || 'Cargando...'}</span>
                    <span className="fw-bold text-white fs-5">{match.status === 'COMPLETED' ? match.homeScore : '-'}</span>
                </div>
                <div className="d-flex justify-content-between align-items-center">
                    <span className={`fw-bold ${match.awayScore > match.homeScore ? 'text-warning' : 'text-white'}`}>{clubsCache[match.awayClubId] || 'Cargando...'}</span>
                    <span className="fw-bold text-white fs-5">{match.status === 'COMPLETED' ? match.awayScore : '-'}</span>
                </div>
                
                {match.status === 'SCHEDULED' && (
                    <div className="mt-3 text-center border-top border-secondary pt-2">
                        {isParticipant ? (
                            participantAccepted ? (
                                <Badge bg="success">Esperando al rival...</Badge>
                            ) : (
                                <Button variant="outline-gold" size="sm" style={{color: 'var(--brand-gold)', borderColor: 'var(--brand-gold)'}} onClick={() => handleAcceptWager(match.id)}>
                                    Apostar y Jugar
                                </Button>
                            )
                        ) : (
                            <Badge bg="secondary">Pendiente</Badge>
                        )}
                    </div>
                )}
            </div>
        );
    };

    return (
        <div className="tournament-bracket mt-4 p-4 rounded" style={{ backgroundColor: 'rgba(0,0,0,0.4)', border: '1px solid #333' }}>
            <h3 className="text-center text-white mb-5 fw-bold"><i className="bi bi-diagram-2 text-warning me-2"></i> ELIMINATORIAS</h3>
            <Row className="align-items-center">
                <Col md={4}>
                    <h5 className="text-center text-secondary mb-4">SEMIFINALES</h5>
                    {renderMatchBox(semifinals[0])}
                    {renderMatchBox(semifinals[1])}
                </Col>
                <Col md={4} className="d-flex justify-content-center">
                    <i className="bi bi-arrow-right display-4 text-secondary opacity-50"></i>
                </Col>
                <Col md={4}>
                    <h5 className="text-center text-secondary mb-4">GRAN FINAL</h5>
                    {renderMatchBox(finals.length > 0 ? finals[0] : null)}
                </Col>
            </Row>
        </div>
    );
};

export default TournamentBracket;
