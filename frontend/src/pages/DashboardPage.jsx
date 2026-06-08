import { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Spinner, Alert, Badge } from 'react-bootstrap';
import { useAuth } from '../context/AuthContext';
import clubService from '../services/clubService';
import leagueService from '../services/leagueService';
import { Link } from 'react-router-dom';

const DashboardPage = () => {
    const { user } = useAuth();
    const [leaguesData, setLeaguesData] = useState([]);
    const [clubsCount, setClubsCount] = useState(0);
    const [totalRiotPoints, setTotalRiotPoints] = useState(0);
    const [wonLeaguesCount, setWonLeaguesCount] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchDashboardData = async () => {
            try {
                setLoading(true);
                setError(null);

                // 1. Obtener los clubes del usuario
                const myClubs = await clubService.getMyClubs();

                // 2. Obtener las ligas a las que el usuario está inscrito
                const myLeagues = await leagueService.getMyLeagues();

                const details = [];
                let rpSum = 0;

                for (const league of myLeagues) {
                    try {
                        const [standings, matchesResponse] = await Promise.all([
                            leagueService.getStandings(league.id),
                            leagueService.getMatches(league.id, 0)
                        ]);

                        // Encontrar si el usuario tiene un club en esta liga usando ownerId
                        const myStanding = (standings || []).find(s => s.ownerId === user?.id);
                        const clubId = myStanding?.clubId;
                        const club = clubId ? myClubs.find(c => c.id === clubId) : null;

                        if (club) {
                            rpSum += (club.riotPoints || 0);
                            const myPosition = (standings || []).findIndex(s => s.clubId === club.id) + 1;
                            
                            // Obtener partidos futuros que involucren a este club
                            const allMatches = matchesResponse?.content || matchesResponse || [];
                            const upcomingMatches = allMatches
                                .filter(m => m.status === 'SCHEDULED' && (m.homeClubId === club.id || m.awayClubId === club.id))
                                .slice(0, 2);

                            details.push({
                                league,
                                hasClub: true,
                                club,
                                standings: (standings || []).slice(0, 5),
                                myPosition,
                                myStanding,
                                upcomingMatches,
                                totalTeams: standings.length
                            });
                        } else {
                            details.push({
                                league,
                                hasClub: false,
                                club: null,
                                standings: [],
                                myPosition: 0,
                                myStanding: null,
                                upcomingMatches: [],
                                totalTeams: 0
                            });
                        }
                    } catch (e) {
                        details.push({
                            league,
                            hasClub: false,
                            club: null,
                            standings: [],
                            myPosition: 0,
                            myStanding: null,
                            upcomingMatches: [],
                            totalTeams: 0
                        });
                    }
                }

                try {
                    const wonData = await leagueService.getWonLeaguesCount();
                    setWonLeaguesCount(wonData.count || 0);
                } catch (e) {
                    console.error("Error al cargar el conteo de ligas ganadas:", e);
                    setWonLeaguesCount(0);
                }

                setLeaguesData(details);
                setClubsCount(myClubs.length);
                setTotalRiotPoints(rpSum || myClubs.reduce((acc, c) => acc + (c.riotPoints || 0), 0));
            } catch (err) {
                setError('Error al cargar la información del dashboard.');
            } finally {
                setLoading(false);
            }
        };

        fetchDashboardData();
    }, [user]);

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

    // Si el usuario no se ha inscrito a ninguna liga aún
    if (leaguesData.length === 0) {
        return (
            <Container className="mt-5 pt-4 animate-fade-in">
                <Row className="justify-content-center">
                    <Col md={8} lg={6}>
                        <div className="glass-card p-5 text-center">
                            <i className="bi bi-trophy display-1 mb-3" style={{ color: 'var(--brand-gold)' }}></i>
                            <h2 className="fw-bold mb-4">¡BIENVENIDO A CLASH MANAGER!</h2>
                            <p className="text-secondary mb-4">
                                Hola {user?.username}, aún no te has inscrito a ninguna liga.
                                Dirígete a la sección de Ligas para ver las ligas disponibles y unirte a una.
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

            {/* Tarjetas de Resumen */}
            <Row className="g-3 mb-4">
                <Col md={6} lg={3}>
                    <div className="glass-card p-4 h-100 border-start border-primary border-4">
                        <div className="d-flex justify-content-between">
                            <h6 className="text-secondary fw-bold mb-0">MIS EQUIPOS</h6>
                            <i className="bi bi-shield-fill fs-4 text-primary"></i>
                        </div>
                        <h2 className="mt-3 fw-bold text-white mb-0">{clubsCount}</h2>
                        <small className="text-secondary">Club{clubsCount !== 1 ? 'es' : ''} activo{clubsCount !== 1 ? 's' : ''}</small>
                    </div>
                </Col>
                <Col md={6} lg={3}>
                    <div className="glass-card p-4 h-100 border-start border-warning border-4">
                        <div className="d-flex justify-content-between">
                            <h6 className="text-secondary fw-bold mb-0">MIS LIGAS</h6>
                            <i className="bi bi-trophy-fill fs-4 text-warning"></i>
                        </div>
                        <h2 className="mt-3 fw-bold text-white mb-0">{leaguesData.length}</h2>
                        <small className="text-secondary">Ligas inscritas</small>
                    </div>
                </Col>
                <Col md={6} lg={3}>
                    <div className="glass-card p-4 h-100 border-start border-gold border-4" style={{ borderLeftColor: 'var(--brand-gold)' }}>
                        <div className="d-flex justify-content-between">
                            <h6 className="text-secondary fw-bold mb-0" style={{ color: 'var(--brand-gold)' }}>LIGAS GANADAS</h6>
                            <i className="bi bi-award-fill fs-4" style={{ color: 'var(--brand-gold)' }}></i>
                        </div>
                        <h2 className="mt-3 fw-bold text-white mb-0">{wonLeaguesCount}</h2>
                        <small className="text-secondary">Ligas ganadas</small>
                    </div>
                </Col>
                <Col md={6} lg={3}>
                    <div className="glass-card p-4 h-100 border-start border-info border-4">
                        <div className="d-flex justify-content-between">
                            <h6 className="text-secondary fw-bold mb-0">PRESUPUESTO TOTAL</h6>
                            <i className="bi bi-gem fs-4 text-info"></i>
                        </div>
                        <h2 className="mt-3 fw-bold text-white mb-0">{totalRiotPoints} RP</h2>
                        <small className="text-secondary">Riot Points de tus clubes</small>
                    </div>
                </Col>
            </Row>

            {/* Listado de Ligas con o sin Club */}
            {leaguesData.map(({ league, hasClub, club, standings, myPosition, myStanding, upcomingMatches, totalTeams }) => {
                if (!hasClub) {
                    return (
                        <Card key={`league-${league.id}`} className="glass-card mb-4 border-0 overflow-hidden border-start border-warning border-4">
                            <Card.Body className="p-4 d-flex justify-content-between align-items-center flex-wrap gap-3">
                                <div>
                                    <h5 className="fw-bold text-white mb-1">
                                        <i className="bi bi-trophy-fill me-2" style={{ color: 'var(--brand-gold)' }}></i>
                                        {league.name}
                                    </h5>
                                    <p className="text-secondary mb-0">Temporada {league.season} • Estás inscrito en esta liga, pero aún no has creado tu club.</p>
                                </div>
                                <Button as={Link} to="/leagues" variant="outline-primary" size="sm">
                                    <i className="bi bi-shield-plus me-1"></i>Crear Club
                                </Button>
                            </Card.Body>
                        </Card>
                    );
                }

                return (
                    <Card key={club.id} className="glass-card mb-4 border-0 overflow-hidden">
                        {/* Cabecera del Club */}
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
                                        <span className="mx-2">•</span>
                                        <i className="bi bi-trophy me-1 text-warning"></i>{league.name}
                                    </small>
                                </div>
                            </div>
                            <Button as={Link} to="/my-club" variant="outline-primary" size="sm">
                                <i className="bi bi-eye me-1"></i>Ver Plantilla
                            </Button>
                        </Card.Header>

                        <Card.Body className="p-4">
                            <Row className="g-3">
                                <Col md={6}>
                                    <div className="bg-dark bg-opacity-50 rounded p-3 h-100">
                                        <div className="d-flex justify-content-between align-items-start mb-3">
                                            <div>
                                                <h6 className="fw-bold text-white mb-0">Clasificación</h6>
                                                <small className="text-secondary">Temporada {league.season}</small>
                                            </div>
                                            {myPosition > 0 && (
                                                <Badge bg={myPosition <= 3 ? 'warning' : 'secondary'} className="fs-6 py-2 px-3">
                                                    #{myPosition}
                                                </Badge>
                                            )}
                                        </div>

                                        {myStanding && (
                                            <div className="d-flex gap-2 mb-3 flex-wrap">
                                                <span className="badge bg-success bg-opacity-25 text-success">{myStanding.wins}V</span>
                                                <span className="badge bg-secondary bg-opacity-25 text-secondary">{myStanding.draws}E</span>
                                                <span className="badge bg-danger bg-opacity-25 text-danger">{myStanding.losses}D</span>
                                                <span className="badge bg-primary bg-opacity-25 text-primary">{myStanding.points} PTS</span>
                                                <span className="badge bg-info bg-opacity-10 text-info">{myStanding.gamesPlayed} PJ</span>
                                            </div>
                                        )}

                                        {standings.length > 0 && (
                                            <div className="mb-3">
                                                <small className="text-secondary d-block mb-2 fw-bold">CLASIFICACIÓN (Top 5)</small>
                                                {standings.map((s, idx) => (
                                                    <div key={s.clubId} className={`d-flex justify-content-between align-items-center py-1 px-2 rounded mb-1 ${s.clubId === club.id ? 'bg-primary bg-opacity-10' : ''}`}>
                                                        <div className="d-flex align-items-center gap-2">
                                                            <span className={`fw-bold ${idx < 3 ? 'text-warning' : 'text-secondary'}`} style={{ minWidth: '20px' }}>{idx + 1}</span>
                                                            <small className={`${s.clubId === club.id ? 'text-primary fw-bold' : 'text-white'}`}>
                                                                {s.clubId === club.id ? `[${club.acronym}] ${club.name}` : `Club #${s.clubId}`}
                                                            </small>
                                                        </div>
                                                        <small className="fw-bold text-white">{s.points} pts</small>
                                                    </div>
                                                ))}
                                            </div>
                                        )}
                                    </div>
                                </Col>

                                <Col md={6}>
                                    <div className="bg-dark bg-opacity-50 rounded p-3 h-100 d-flex flex-column justify-content-between">
                                        <div>
                                            <small className="text-secondary d-block mb-2 fw-bold">PRÓXIMOS PARTIDOS</small>
                                            {upcomingMatches.length > 0 ? (
                                                upcomingMatches.map(match => (
                                                    <div key={match.id} className="d-flex justify-content-between align-items-center py-2 px-2 rounded bg-black bg-opacity-25 mb-2">
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
                                                ))
                                            ) : (
                                                <p className="text-secondary small">No hay partidos programados próximamente.</p>
                                            )}
                                        </div>

                                        <div className="text-end mt-3">
                                            <Link to="/leagues" className="text-primary text-decoration-none small">
                                                Ver liga completa <i className="bi bi-arrow-right"></i>
                                            </Link>
                                        </div>
                                    </div>
                                </Col>
                            </Row>
                        </Card.Body>
                    </Card>
                );
            })}

            {/* Acciones Rápidas */}
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
