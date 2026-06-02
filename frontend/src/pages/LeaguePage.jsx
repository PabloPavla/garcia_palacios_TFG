import { useState, useEffect } from 'react';
import { Container, Table, Spinner, Alert, Badge } from 'react-bootstrap';
import leagueService from '../services/leagueService';
import clubService from '../services/clubService';

const LeaguePage = () => {
    const [league, setLeague] = useState(null);
    const [standings, setStandings] = useState([]);
    const [clubsCache, setClubsCache] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const loadLeagueData = async () => {
            try {
                // 1. Obtener la liga activa (por simplicidad tomamos la primera)
                const leagues = await leagueService.getAllLeagues();
                if (leagues.length === 0) {
                    setError('No hay ninguna liga activa en este momento.');
                    setLoading(false);
                    return;
                }
                
                const currentLeague = leagues[0];
                setLeague(currentLeague);

                // 2. Obtener clasificación
                const standingsData = await leagueService.getStandings(currentLeague.id);
                setStandings(standingsData);

                // 3. Obtener nombres de los clubes
                const cCache = {};
                for (let s of standingsData) {
                    try {
                        const c = await clubService.getClubById(s.clubId);
                        cCache[s.clubId] = `[${c.acronym}] ${c.name}`;
                    } catch (e) {
                        cCache[s.clubId] = `Club Desconocido (${s.clubId})`;
                    }
                }
                setClubsCache(cCache);

            } catch (err) {
                setError('Error al cargar la clasificación de la liga.');
            } finally {
                setLoading(false);
            }
        };

        loadLeagueData();
    }, []);

    if (loading) {
        return (
            <Container className="d-flex justify-content-center align-items-center min-vh-100">
                <Spinner animation="grow" variant="gold" style={{ color: 'var(--brand-gold)' }} />
            </Container>
        );
    }

    return (
        <Container className="mt-4 pt-4 animate-fade-in">
            <div className="text-center mb-5">
                <i className="bi bi-trophy display-1 mb-3" style={{ color: 'var(--brand-gold)' }}></i>
                <h1 className="fw-bold text-uppercase text-white">
                    {league ? `${league.name} - ${league.season}` : 'CLASIFICACIÓN'}
                </h1>
                <p className="text-secondary">Clasificación oficial de la temporada</p>
            </div>

            {error ? (
                <Alert variant="warning" className="text-center">{error}</Alert>
            ) : (
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
                </div>
            )}
        </Container>
    );
};

export default LeaguePage;
