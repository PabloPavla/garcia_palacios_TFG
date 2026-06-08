import { useState, useEffect } from 'react';
import { Container, Table, Spinner, Alert, Button, Modal, Form, Row, Col } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import leagueService from '../services/leagueService';
import clubService from '../services/clubService';
import TournamentBracket from '../components/TournamentBracket';

const LeaguePage = () => {
    const { user } = useAuth();
    const [leagues, setLeagues] = useState([]);
    const [myLeagues, setMyLeagues] = useState([]);
    const [activeLeagueId, setActiveLeagueId] = useState(null);
    const [standings, setStandings] = useState([]);
    const [clubsCache, setClubsCache] = useState({});
    const [clubsDataCache, setClubsDataCache] = useState({});
    const [activeTab, setActiveTab] = useState('standings'); // 'standings' | 'tournament'
    
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    
    // Modal Crear Liga
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [newLeagueData, setNewLeagueData] = useState({ name: '', season: '2024', initialRp: 10000, maxClubs: 4, visibility: 'PUBLIC', matchWagerRp: 500 });
    
    // Modal Unirse a Liga
    const [showJoinModal, setShowJoinModal] = useState(false);
    const [newClubData, setNewClubData] = useState({ name: '', acronym: '' });

    const loadLeagues = async () => {
        try {
            setLoading(true);
            const [data, myLeaguesData] = await Promise.all([
                leagueService.getAllLeagues(),
                leagueService.getMyLeagues()
            ]);
            setLeagues(data);
            setMyLeagues(myLeaguesData || []);
            if (data.length > 0 && !activeLeagueId) {
                setActiveLeagueId(data[0].id);
            }
        } catch (err) {
            setError('Error al cargar las ligas.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadLeagues();
    }, []);

    const fetchAndSetStandings = async () => {
        if (!activeLeagueId) return;
        try {
            setLoading(true);
            const standingsData = await leagueService.getStandings(activeLeagueId);
            setStandings(standingsData);

            const cCache = {};
            const cDataCache = {};
            for (let s of standingsData) {
                try {
                    const c = await clubService.getClubById(s.clubId);
                    cCache[s.clubId] = `[${c.acronym}] ${c.name}`;
                    cDataCache[s.clubId] = c;
                } catch (e) {
                    cCache[s.clubId] = `Club Desconocido (${s.clubId})`;
                }
            }
            setClubsCache(cCache);
            setClubsDataCache(cDataCache);
        } catch (err) {
            setError('Error al cargar la clasificación.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchAndSetStandings();
    }, [activeLeagueId]);

    const handleCreateLeague = async (e) => {
        e.preventDefault();
        try {
            // Calculate tomorrow's date to bypass the @Future validation constraint
            const tomorrow = new Date();
            tomorrow.setDate(tomorrow.getDate() + 1);
            
            const newLeague = await leagueService.createLeague({
                ...newLeagueData,
                startDate: tomorrow.toISOString().split('T')[0]
            });
            
            // Generate players for the new league
            await clubService.generatePlayersForLeague(newLeague.id);

            setShowCreateModal(false);
            loadLeagues();
        } catch (err) {
            alert('Error al crear la liga');
        }
    };

    const handleJoinLeagueOnly = async () => {
        try {
            setLoading(true);
            await leagueService.joinLeague(activeLeagueId);
            alert('¡Te has inscrito en la liga con éxito! Ahora puedes crear tu club.');
            await loadLeagues();
            await fetchAndSetStandings();
        } catch (err) {
            alert('Error al inscribirse en la liga: ' + (err.response?.data?.error || err.response?.data?.message || err.message));
        } finally {
            setLoading(false);
        }
    };

    const handleJoinLeague = async (e) => {
        e.preventDefault();
        try {
            // 1. Create Club
            const newClub = await clubService.createClub({
                name: newClubData.name,
                acronym: newClubData.acronym,
                division: 'BRONZE'
            });
            
            // 2. Enroll in League
            try {
                await leagueService.enrollClub(activeLeagueId, newClub.id);
            } catch (err) {
                // Rollback club creation
                await clubService.deleteClub(newClub.id);
                throw err;
            }
            
            setShowJoinModal(false);
            // Reload standings and cache
            await fetchAndSetStandings();
            await loadLeagues();
            alert('¡Club creado e inscrito en la liga con éxito!');
        } catch (err) {
            alert('Error al unirse a la liga: ' + (err.response?.data?.error || err.response?.data?.message || err.message));
        }
    };

    const activeLeague = leagues.find(l => l.id === Number(activeLeagueId));
    const isCreator = user && activeLeague && activeLeague.creatorUserId === user.id;

    // Helper for visibility icons
    const getVisibilityIcon = (visibility) => {
        switch (visibility) {
            case 'PRIVATE': return <i className="bi bi-lock-fill text-danger me-1" title="Privada"></i>;
            case 'FRIENDS_ONLY': return <i className="bi bi-people-fill text-info me-1" title="Solo Amigos"></i>;
            default: return <i className="bi bi-globe-americas text-success me-1" title="Pública"></i>;
        }
    };

    return (
        <Container className="mt-4 pt-4 animate-fade-in">
            <div className="d-flex justify-content-between align-items-center mb-5 flex-wrap gap-3">
                <div className="d-flex align-items-center gap-3">
                    <i className="bi bi-trophy display-4" style={{ color: 'var(--brand-gold)' }}></i>
                    <div>
                        <h1 className="fw-bold text-uppercase text-white mb-0">LIGAS</h1>
                        <p className="text-secondary mb-0">Compite contra otros managers</p>
                    </div>
                </div>
                
                <div className="d-flex gap-2 align-items-center">
                    <select 
                        className="form-select bg-dark text-white border-secondary" 
                        value={activeLeagueId || ''} 
                        onChange={(e) => setActiveLeagueId(Number(e.target.value))}
                        style={{ minWidth: '200px' }}
                    >
                        {leagues.map(l => (
                            <option key={l.id} value={l.id}>
                                {l.visibility === 'PRIVATE' ? '🔒 ' : l.visibility === 'FRIENDS_ONLY' ? '👥 ' : ''}
                                {l.name} - {l.season}
                            </option>
                        ))}
                    </select>
                    
                    {user && (
                        <Button variant="outline-gold" onClick={() => setShowCreateModal(true)} style={{ color: 'var(--brand-gold)', borderColor: 'var(--brand-gold)' }}>
                            <i className="bi bi-plus-circle me-1"></i> Crear Liga
                        </Button>
                    )}
                </div>
            </div>

            {loading ? (
                <div className="text-center py-5">
                    <Spinner animation="grow" variant="gold" style={{ color: 'var(--brand-gold)' }} />
                </div>
            ) : error ? (
                <Alert variant="warning" className="text-center">{error}</Alert>
            ) : (
                <>
                    {activeLeague && (
                        <div className="d-flex justify-content-between mb-3 align-items-end">
                            <h3 className="text-white mb-0">
                                {getVisibilityIcon(activeLeague.visibility)}
                                {activeLeague.name} <span className="text-secondary fs-5">Temporada {activeLeague.season}</span>
                            </h3>
                            
                            <div className="d-flex gap-2 align-items-center">
                                {(() => {
                                    const isJoined = myLeagues.some(l => l.id === Number(activeLeagueId));
                                    const hasClub = standings.some(s => s.ownerId === user?.id || clubsDataCache[s.clubId]?.ownerId === user?.id);
                                    
                                    if (!isJoined) {
                                        return (
                                            <Button variant="success" onClick={handleJoinLeagueOnly}>
                                                <i className="bi bi-plus-circle-fill me-1"></i> Inscribirse a la Liga
                                            </Button>
                                        );
                                    } else if (!hasClub || (user && user.role === 'ROLE_ADMIN')) {
                                        return (
                                            <Button variant="primary" onClick={() => {
                                                setNewClubData({ name: '', acronym: '' });
                                                setShowJoinModal(true);
                                            }}>
                                                <i className="bi bi-shield-plus me-1"></i> Crear Club
                                            </Button>
                                        );
                                    } else {
                                        return (
                                            <span className="badge bg-success bg-opacity-25 text-success p-2 fs-6">
                                                <i className="bi bi-check-circle-fill me-1"></i> Inscrito con Club
                                            </span>
                                        );
                                    }
                                })()}

                                <Link to={`/market/${activeLeague.id}`} className="btn btn-outline-primary">
                                    <i className="bi bi-shop me-1"></i> Mercado
                                </Link>
                            </div>
                        </div>
                    )}

                    {activeLeague && (
                        <div className="d-flex mb-3 gap-2 border-bottom border-secondary pb-2 mt-4">
                            <Button 
                                variant={activeTab === 'standings' ? 'gold' : 'link'} 
                                className={`text-decoration-none fw-bold ${activeTab === 'standings' ? 'text-dark bg-warning' : 'text-secondary'}`}
                                onClick={() => setActiveTab('standings')}
                            >
                                <i className="bi bi-list-ol me-1"></i> Clasificación
                            </Button>
                            {standings.length >= 4 && (
                                <Button 
                                    variant={activeTab === 'tournament' ? 'gold' : 'link'} 
                                    className={`text-decoration-none fw-bold ${activeTab === 'tournament' ? 'text-dark bg-warning' : 'text-secondary'}`}
                                    onClick={() => setActiveTab('tournament')}
                                >
                                    <i className="bi bi-diagram-2 me-1"></i> Torneo
                                </Button>
                            )}
                        </div>
                    )}

                    {activeTab === 'standings' ? (
                        <div className="glass-card overflow-hidden">
                            <Table hover responsive variant="dark" className="mb-0 align-middle text-center" style={{ backgroundColor: 'transparent' }}>
                            <thead style={{ backgroundColor: 'rgba(0,0,0,0.4)' }}>
                                <tr>
                                    <th className="py-3 text-secondary">POS</th>
                                    <th className="py-3 text-start text-secondary">CLUB</th>
                                    <th className="py-3 text-secondary">PTS</th>
                                    <th className="py-3 text-secondary">PJ</th>
                                    <th className="py-3 text-secondary">V</th>
                                    <th className="py-3 text-secondary">E</th>
                                    <th className="py-3 text-secondary">D</th>
                                    <th className="py-3 text-secondary">GF</th>
                                    <th className="py-3 text-secondary">GC</th>
                                    <th className="py-3 text-secondary">DG</th>
                                </tr>
                            </thead>
                            <tbody>
                                {standings.map((team, index) => (
                                    <tr key={team.clubId} style={{ backgroundColor: index < 3 ? 'rgba(200, 155, 60, 0.1)' : 'transparent' }}>
                                        <td className="py-3 fw-bold">
                                            {index === 0 ? <i className="bi bi-1-circle-fill text-gold me-1" style={{ color: 'var(--brand-gold)' }}></i> : 
                                             index === 1 ? <i className="bi bi-2-circle-fill text-secondary me-1"></i> : 
                                             index === 2 ? <i className="bi bi-3-circle-fill text-warning me-1" style={{ color: '#cd7f32' }}></i> : 
                                             <span className="text-secondary">{index + 1}</span>}
                                        </td>
                                        <td className="py-3 text-start fw-bold text-white">
                                            {clubsCache[team.clubId] || 'Cargando...'}
                                        </td>
                                        <td className="py-3 fw-bold fs-5 text-primary">{team.points}</td>
                                        <td className="py-3">{team.gamesPlayed}</td>
                                        <td className="py-3 text-success">{team.wins}</td>
                                        <td className="py-3 text-secondary">{team.draws}</td>
                                        <td className="py-3 text-danger">{team.losses}</td>
                                        <td className="py-3">{team.goalsFor}</td>
                                        <td className="py-3">{team.goalsAgainst}</td>
                                        <td className="py-3 fw-bold">{team.goalDifference > 0 ? `+${team.goalDifference}` : team.goalDifference}</td>
                                    </tr>
                                ))}
                                {standings.length === 0 && (
                                    <tr>
                                        <td colSpan="10" className="py-5 text-secondary">Aún no hay equipos clasificados en esta liga.</td>
                                    </tr>
                                )}
                            </tbody>
                        </Table>
                        {isCreator && standings.length >= 4 && (
                            <div className="p-3 text-center border-top border-secondary" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
                                <Button variant="outline-warning" onClick={async () => {
                                    try {
                                        await leagueService.generateTournament(activeLeagueId);
                                        alert("Torneo generado con éxito");
                                        setActiveTab('tournament');
                                    } catch (e) {
                                        alert("No se pudo generar el torneo: " + (e.response?.data?.message || e.message));
                                    }
                                }}>
                                    <i className="bi bi-diagram-2 me-1"></i> Generar Torneo (Administrador)
                                </Button>
                            </div>
                        )}
                    </div>
                    ) : (
                        <TournamentBracket 
                            leagueId={activeLeagueId} 
                            activeClubId={standings.find(s => clubsDataCache[s.clubId]?.ownerId === user?.id)?.clubId} 
                        />
                    )}
                </>
            )}

            {/* Modal Crear Liga */}
            <Modal show={showCreateModal} onHide={() => setShowCreateModal(false)} contentClassName="bg-dark text-white border-secondary">
                <Modal.Header closeButton closeVariant="white" className="border-secondary">
                    <Modal.Title>Crear Nueva Liga</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Form onSubmit={handleCreateLeague}>
                        <Form.Group className="mb-3">
                            <Form.Label>Nombre de la Liga</Form.Label>
                            <Form.Control type="text" required className="bg-dark text-white border-secondary" value={newLeagueData.name} onChange={e => setNewLeagueData({...newLeagueData, name: e.target.value})} />
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>Temporada</Form.Label>
                            <Form.Control type="text" required className="bg-dark text-white border-secondary" value={newLeagueData.season} onChange={e => setNewLeagueData({...newLeagueData, season: e.target.value})} />
                        </Form.Group>
                        <Row>
                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label>RP Iniciales</Form.Label>
                                    <Form.Control type="number" required className="bg-dark text-white border-secondary" value={newLeagueData.initialRp} onChange={e => setNewLeagueData({...newLeagueData, initialRp: Number(e.target.value)})} />
                                </Form.Group>
                            </Col>
                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Visibilidad</Form.Label>
                                    <Form.Select className="bg-dark text-white border-secondary" value={newLeagueData.visibility} onChange={e => setNewLeagueData({...newLeagueData, visibility: e.target.value})}>
                                        <option value="PUBLIC">Pública</option>
                                        <option value="FRIENDS_ONLY">Solo Amigos</option>
                                        <option value="PRIVATE">Privada (Solo Creador)</option>
                                    </Form.Select>
                                </Form.Group>
                            </Col>
                        </Row>
                        <Button variant="primary" type="submit" className="w-100 mt-2">Crear Liga</Button>
                    </Form>
                </Modal.Body>
            </Modal>

            {/* Modal Unirse a Liga */}
            <Modal show={showJoinModal} onHide={() => setShowJoinModal(false)} contentClassName="bg-dark text-white border-secondary">
                <Modal.Header closeButton closeVariant="white" className="border-secondary">
                    <Modal.Title>Unirse a la Liga</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <p className="text-secondary">Para participar en esta liga necesitas crear un Club (equipo) específico para ella.</p>
                    <Form onSubmit={handleJoinLeague}>
                        <Form.Group className="mb-3">
                            <Form.Label>Nombre del Club</Form.Label>
                            <Form.Control type="text" required minLength={3} className="bg-dark text-white border-secondary" value={newClubData.name} onChange={e => setNewClubData({...newClubData, name: e.target.value})} />
                        </Form.Group>
                        <Form.Group className="mb-4">
                            <Form.Label>Acrónimo (máx 5 letras)</Form.Label>
                            <Form.Control type="text" required maxLength={5} className="bg-dark text-white border-secondary text-uppercase" value={newClubData.acronym} onChange={e => setNewClubData({...newClubData, acronym: e.target.value})} />
                        </Form.Group>
                        <Button variant="primary" type="submit" className="w-100">Crear Club y Unirse</Button>
                    </Form>
                </Modal.Body>
            </Modal>
        </Container>
    );
};

export default LeaguePage;
