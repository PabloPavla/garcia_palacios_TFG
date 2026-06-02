import { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Spinner, Alert, Badge } from 'react-bootstrap';
import { useAuth } from '../context/AuthContext';
import clubService from '../services/clubService';
import leagueService from '../services/leagueService';
import { Link } from 'react-router-dom';

const DashboardPage = () => {
    const { user } = useAuth();
    const [clubs, setClubs] = useState([]);
    const [clubLeagues, setClubLeagues] = useState({}); // { clubId: [{ league, standings, matches }] }
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchDashboardData = async () => {
            try {
                setLoading(true);

                // 1. Get all user's clubs
                const myClubs = await clubService.getMyClubs();
                setClubs(myClubs);

                // 2. For each club, get leagues, standings, and matches
                const leagueData = {};
                for (const club of myClubs) {
                    try {
                        const leagues = await leagueService.getLeaguesByClub(club.id);
                        const leagueDetails = [];

                        for (const league of leagues) {
                            try {
                                const [standings, matchesResponse] = await Promise.all([
                                    leagueService.getStandings(league.id),
                                    leagueService.getMatches(league.id, 0)
                                ]);

                                // Find this club's position in standings
                                const myPosition = (standings || []).findIndex(s => s.clubId === club.id) + 1;
                                const myStanding = (standings || []).find(s => s.clubId === club.id);

                                // Get upcoming matches (SCHEDULED status) that involve this club
                                const allMatches = matchesResponse?.content || matchesResponse || [];
                                const upcomingMatches = allMatches
                                    .filter(m => m.status === 'SCHEDULED' && (m.homeClubId === club.id || m.awayClubId === club.id))
                                    .slice(0, 2);

                                leagueDetails.push({
                                    league,
                                    standings: (standings || []).slice(0, 5), // Top 5 only
                                    myPosition,
                                    myStanding,
                                    upcomingMatches,
                                    totalTeams: standings.length
                                });
                            } catch {
                                // If standings fail for a league, still show the league
                                leagueDetails.push({ league, standings: [], myPosition: 0, myStanding: null, upcomingMatches: [], totalTeams: 0 });
                            }
                        }

                        leagueData[club.id] = leagueDetails;
                    } catch {
                        leagueData[club.id] = [];
                    }
                }

                setClubLeagues(leagueData);
            } catch (err) {
                setError('Error al cargar la información del dashboard.');
            } finally {
                setLoading(false);
            }
        };

        fetchDashboardData();
    }, []);

    if (loading) {
        return (
            <Container className="d-flex justify-content-center align-items-center min-vh-100">
                <div className="text-center">
                    <Spinner animation="grow" variant="primary" />
                    <p className="text-secondary mt-3">Cargando tu dashboard...</p>
                </div>
            </Container>
        );
    }

    // No clubs yet - prompt to join a league
    if (clubs.length === 0) {
        return (
            <Container className="mt-5 pt-4 animate-fade-in">
                <Row className="justify-content-center">
                    <Col md={8} lg={6}>
                        <div className="glass-card p-5 text-center">
                            <i className="bi bi-trophy display-1 mb-3" style={{ color: 'var(--brand-gold)' }}></i>
                            <h2 className="fw-bold mb-4">¡BIENVENIDO A CLASH MANAGER!</h2>
                            <p className="text-secondary mb-4">
                                Hola {user?.username}, aún no te has unido a ninguna liga.
                                Dirígete a la sección de Ligas para crear o unirte a una.
                            </p>
                            <Button as={Link} to="/leagues" variant="primary" size="lg" className="fw-bold px-5">
                                <i className="bi bi-trophy-fill me-2"></i>VER LIGAS
                            </Button>
                        </div>
                    </Col>
                </Row>
            </Container>
        );
    }

    return (
        <Container className="mt-4 pt-3 animate-fade-in">
            {/* Header */}
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h1 className="fw-bold mb-0 text-white">Dashboard</h1>
                    <p className="text-secondary mb-0">Bienvenido de nuevo, Mánager <strong>{user?.username}</strong></p>
                </div>
            </div>

            {error && <Alert variant="danger">{error}</Alert>}

            {/* Summary Cards */}
            <Row className="g-3 mb-4">
                <Col md={4}>
                    <div className="glass-card p-4 h-100 border-start border-primary border-4">
                        <div className="d-flex justify-content-between">
                            <h6 className="text-secondary fw-bold mb-0">MIS EQUIPOS</h6>
                            <i className="bi bi-shield-fill fs-4 text-primary"></i>
                        </div>
                        <h2 className="mt-3 fw-bold text-white mb-0">{clubs.length}</h2>
                        <small className="text-secondary">Club{clubs.length !== 1 ? 'es' : ''} activo{clubs.length !== 1 ? 's' : ''}</small>
                    </div>
                </Col>
                <Col md={4}>
                    <div className="glass-card p-4 h-100 border-start border-4" style={{ borderColor: 'var(--brand-gold)' }}>
                        <div className="d-flex justify-content-between">
                            <h6 className="text-secondary fw-bold mb-0">LIGAS ACTIVAS</h6>
                            <i className="bi bi-trophy-fill fs-4" style={{ color: 'var(--brand-gold)' }}></i>
                        </div>
                        <h2 className="mt-3 fw-bold text-white mb-0">
                            {Object.values(clubLeagues).reduce((acc, leagues) => acc + leagues.length, 0)}
                        </h2>
                        <small className="text-secondary">Competiciones en curso</small>
                    </div>
                </Col>
                <Col md={4}>
                    <div className="glass-card p-4 h-100 border-start border-info border-4">
                        <div className="d-flex justify-content-between">
                            <h6 className="text-secondary fw-bold mb-0">PRESUPUESTO TOTAL</h6>
                            <i className="bi bi-gem fs-4 text-info"></i>
                        </div>
                        <h2 className="mt-3 fw-bold text-white mb-0">
                            {clubs.reduce((acc, c) => acc + (c.riotPoints || 0), 0)} RP
                        </h2>
                        <small className="text-secondary">Riot Points combinados</small>
                    </div>
                </Col>
            </Row>

            {/* Per-Club Sections */}
            {clubs.map(club => {
                const leagues = clubLeagues[club.id] || [];
                return (
                    <Card key={club.id} className="glass-card mb-4 border-0 overflow-hidden">
                        {/* Club Header */}
                        <Card.Header className="d-flex justify-content-between align-items-center py-3 px-4" style={{ backgroundColor: 'rgba(0,0,0,0.4)' }}>
                            <div className="d-flex align-items-center gap-3">
                                <div className="rounded-circle bg-primary d-flex align-items-center justify-content-center" style={{ width: '40px', height: '40px', fontSize: '1rem' }}>
                                    {club.acronym?.charAt(0) || 'C'}
                                </div>
                                <div>
                                    <h5 className="fw-bold text-white mb-0">[{club.acronym}] {club.name}</h5>
                                    <small className="text-secondary">
                                        <i className="bi bi-gem me-1 text-info"></i>{club.riotPoints} RP
                                        <span className="mx-2">•</span>
                                        <i className="bi bi-people me-1"></i>{club.playerCount || 0} jugadores
                                    </small>
                                </div>
                            </div>
                            <Button as={Link} to="/my-club" variant="outline-primary" size="sm">
                                <i className="bi bi-eye me-1"></i>Ver Plantilla
                            </Button>
                        </Card.Header>

                        <Card.Body className="p-4">
                            {leagues.length === 0 ? (
                                <div className="text-center py-3">
                                    <p className="text-secondary mb-2">Este club no está inscrito en ninguna liga aún.</p>
                                    <Button as={Link} to="/leagues" variant="outline-primary" size="sm">
                                        <i className="bi bi-plus-circle me-1"></i>Unirse a una Liga
                                    </Button>
                                </div>
                            ) : (
                                <Row className="g-3">
                                    {leagues.map(({ league, standings, myPosition, myStanding, upcomingMatches, totalTeams }) => (
                                        <Col md={6} key={league.id}>
                                            <div className="bg-dark bg-opacity-50 rounded p-3 h-100">
                                                {/* League Title */}
                                                <div className="d-flex justify-content-between align-items-start mb-3">
                                                    <div>
                                                        <h6 className="fw-bold text-white mb-0">
                                                            <i className="bi bi-trophy-fill me-2" style={{ color: 'var(--brand-gold)' }}></i>
                                                            {league.name}
                                                        </h6>
                                                        <small className="text-secondary">Temporada {league.season}</small>
                                                    </div>
                                                    {myPosition > 0 && (
                                                        <Badge bg={myPosition <= 3 ? 'warning' : 'secondary'} className="fs-6 py-2 px-3">
                                                            #{myPosition}
                                                        </Badge>
                                                    )}
                                                </div>

                                                {/* My Stats */}
                                                {myStanding && (
                                                    <div className="d-flex gap-2 mb-3 flex-wrap">
                                                        <span className="badge bg-success bg-opacity-25 text-success">{myStanding.wins}V</span>
                                                        <span className="badge bg-secondary bg-opacity-25 text-secondary">{myStanding.draws}E</span>
                                                        <span className="badge bg-danger bg-opacity-25 text-danger">{myStanding.losses}D</span>
                                                        <span className="badge bg-primary bg-opacity-25 text-primary">{myStanding.points} PTS</span>
                                                        <span className="badge bg-info bg-opacity-10 text-info">{myStanding.gamesPlayed || (myStanding.wins + myStanding.draws + myStanding.losses)} PJ</span>
                                                    </div>
                                                )}

                                                {/* Mini Standings */}
                                                {standings.length > 0 && (
                                                    <div className="mb-3">
                                                        <small className="text-secondary d-block mb-2 fw-bold">CLASIFICACIÓN (Top 5)</small>
                                                        {standings.map((s, idx) => (
                                                            <div key={s.clubId} className={`d-flex justify-content-between align-items-center py-1 px-2 rounded mb-1 ${s.clubId === club.id ? 'bg-primary bg-opacity-10' : ''}`}>
                                                                <div className="d-flex align-items-center gap-2">
                                                                    <span className={`fw-bold ${idx < 3 ? 'text-warning' : 'text-secondary'}`} style={{ minWidth: '20px' }}>{idx + 1}</span>
                                                                    <small className={`${s.clubId === club.id ? 'text-primary fw-bold' : 'text-white'}`}>
                                                                        Club #{s.clubId}
                                                                    </small>
                                                                </div>
                                                                <small className="fw-bold text-white">{s.points} pts</small>
                                                            </div>
                                                        ))}
                                                    </div>
                                                )}

                                                {/* Upcoming Matches */}
                                                {upcomingMatches.length > 0 && (
                                                    <div className="mb-2">
                                                        <small className="text-secondary d-block mb-2 fw-bold">PRÓXIMOS PARTIDOS</small>
                                                        {upcomingMatches.map(match => (
                                                            <div key={match.id} className="d-flex justify-content-between align-items-center py-2 px-2 rounded bg-black bg-opacity-25 mb-1">
                                                                <div>
                                                                    <small className="text-white">
                                                                        {match.homeClubId === club.id ? (
                                                                            <><strong className="text-primary">TÚ</strong> vs Club #{match.awayClubId}</>
                                                                        ) : (
                                                                            <>Club #{match.homeClubId} vs <strong className="text-primary">TÚ</strong></>
                                                                        )}
                                                                    </small>
                                                                </div>
                                                                <small className="text-secondary">
                                                                    <i className="bi bi-calendar-event me-1"></i>
                                                                    {new Date(match.matchDate).toLocaleDateString('es-ES', { day: '2-digit', month: 'short' })}
                                                                </small>
                                                            </div>
                                                        ))}
                                                    </div>
                                                )}

                                                {/* League Link */}
                                                <div className="text-end mt-2">
                                                    <Link to="/leagues" className="text-primary text-decoration-none small">
                                                        Ver liga completa <i className="bi bi-arrow-right"></i>
                                                    </Link>
                                                </div>
                                            </div>
                                        </Col>
                                    ))}
                                </Row>
                            )}
                        </Card.Body>
                    </Card>
                );
            })}

            {/* Quick Actions */}
            <Row className="mt-3 g-3 mb-5">
                <Col md={4}>
                    <div className="glass-card p-4 text-center action-card hover-glow h-100">
                        <i className="bi bi-trophy-fill display-5 mb-3" style={{ color: 'var(--brand-gold)' }}></i>
                        <h5 className="fw-bold">Ligas</h5>
                        <p className="text-secondary small">Crea o únete a nuevas competiciones</p>
                        <Button as={Link} to="/leagues" variant="outline-primary" size="sm">Ir a Ligas</Button>
                    </div>
                </Col>
                <Col md={4}>
                    <div className="glass-card p-4 text-center action-card hover-glow h-100">
                        <i className="bi bi-shield-fill display-5 text-primary mb-3"></i>
                        <h5 className="fw-bold">Mi Club</h5>
                        <p className="text-secondary small">Gestiona tu plantilla y jugadores</p>
                        <Button as={Link} to="/my-club" variant="outline-primary" size="sm">Ver Plantilla</Button>
                    </div>
                </Col>
                <Col md={4}>
                    <div className="glass-card p-4 text-center action-card hover-glow h-100">
                        <i className="bi bi-shop display-5 text-info mb-3"></i>
                        <h5 className="fw-bold">Mercado</h5>
                        <p className="text-secondary small">Ficha nuevos jugadores para tu equipo</p>
                        <Button as={Link} to="/leagues" variant="outline-primary" size="sm">Ver Ligas</Button>
                    </div>
                </Col>
            </Row>
        </Container>
    );
};

export default DashboardPage;
