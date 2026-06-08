import { useState, useEffect } from 'react';
import { Modal, Spinner, Alert, Button, Badge, Table } from 'react-bootstrap';
import { useAuth } from '../context/AuthContext';
import clubService from '../services/clubService';
import friendService from '../services/friendService';

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

const ClubDetailsModal = ({ clubId, onHide }) => {
    const { user } = useAuth();
    const [club, setClub] = useState(null);
    const [players, setPlayers] = useState([]);
    const [friendship, setFriendship] = useState(null); // holds status response
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [actionLoading, setActionLoading] = useState(false);

    const loadClubDetails = async () => {
        if (!clubId) return;
        setLoading(true);
        setError(null);
        try {
            const [clubData, playersData] = await Promise.all([
                clubService.getClubById(clubId),
                clubService.getClubPlayers(clubId)
            ]);
            setClub(clubData);
            
            // Sort players standard: TOP, JUNGLE, MID, ADC, SUPPORT
            const roleOrder = { TOP: 1, JUNGLE: 2, MID: 3, ADC: 4, SUPPORT: 5 };
            const sortedPlayers = (playersData || []).sort((a, b) => {
                return (roleOrder[a.lolRole] || 99) - (roleOrder[b.lolRole] || 99);
            });
            setPlayers(sortedPlayers);

            // Fetch friendship status if owner is not the current user
            if (user && clubData.ownerId !== user.id) {
                const friendStatus = await friendService.getFriendshipStatus(clubData.ownerId);
                setFriendship(friendStatus);
            } else {
                setFriendship(null);
            }
        } catch (err) {
            console.error("Error loading club details:", err);
            setError("Error al cargar la información del club.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadClubDetails();
    }, [clubId]);

    const handleSendFriendRequest = async () => {
        if (!friendship || !friendship.username) return;
        setActionLoading(true);
        try {
            await friendService.sendRequest(friendship.username);
            // Refresh friendship status
            const friendStatus = await friendService.getFriendshipStatus(club.ownerId);
            setFriendship(friendStatus);
        } catch (err) {
            alert(err.response?.data?.message || err.response?.data?.error || "Error al enviar la solicitud.");
        } finally {
            setActionLoading(false);
        }
    };

    const handleAcceptFriendRequest = async () => {
        if (!friendship || !friendship.friendshipId) return;
        setActionLoading(true);
        try {
            await friendService.acceptRequest(friendship.friendshipId);
            // Refresh friendship status
            const friendStatus = await friendService.getFriendshipStatus(club.ownerId);
            setFriendship(friendStatus);
        } catch (err) {
            alert("Error al aceptar la solicitud.");
        } finally {
            setActionLoading(false);
        }
    };

    const handleRejectFriendRequest = async () => {
        if (!friendship || !friendship.friendshipId) return;
        setActionLoading(true);
        try {
            await friendService.rejectRequest(friendship.friendshipId);
            // Refresh friendship status
            const friendStatus = await friendService.getFriendshipStatus(club.ownerId);
            setFriendship(friendStatus);
        } catch (err) {
            alert("Error al rechazar la solicitud.");
        } finally {
            setActionLoading(false);
        }
    };

    const handleRemoveFriend = async () => {
        if (!friendship || !friendship.friendshipId) return;
        if (!window.confirm(`¿Seguro que deseas eliminar a ${friendship.username} de tus amigos?`)) return;
        setActionLoading(true);
        try {
            await friendService.removeFriend(friendship.friendshipId);
            // Refresh friendship status
            const friendStatus = await friendService.getFriendshipStatus(club.ownerId);
            setFriendship(friendStatus);
        } catch (err) {
            alert("Error al eliminar amigo.");
        } finally {
            setActionLoading(false);
        }
    };

    const renderFriendshipButton = () => {
        if (!user || !club) return null;
        if (club.ownerId === user.id) {
            return <Badge bg="success" className="p-2 fs-6">Tú (Propietario)</Badge>;
        }
        if (!friendship) return null;

        switch (friendship.friendshipStatus) {
            case 'ACCEPTED':
                return (
                    <div className="d-flex align-items-center gap-2">
                        <Badge bg="primary" className="p-2 fs-6"><i className="bi bi-people-fill me-1"></i>Amigos</Badge>
                        <Button variant="outline-danger" size="sm" disabled={actionLoading} onClick={handleRemoveFriend}>
                            {actionLoading ? <Spinner animation="border" size="sm" /> : "Eliminar"}
                        </Button>
                    </div>
                );
            case 'PENDING_SENT':
                return (
                    <Button variant="secondary" disabled className="d-flex align-items-center gap-1">
                        <i className="bi bi-clock"></i> Solicitud Enviada
                    </Button>
                );
            case 'PENDING_RECEIVED':
                return (
                    <div className="d-flex gap-2">
                        <Button variant="success" size="sm" disabled={actionLoading} onClick={handleAcceptFriendRequest}>
                            {actionLoading ? <Spinner animation="border" size="sm" /> : <><i className="bi bi-check-lg"></i> Aceptar</>}
                        </Button>
                        <Button variant="danger" size="sm" disabled={actionLoading} onClick={handleRejectFriendRequest}>
                            {actionLoading ? <Spinner animation="border" size="sm" /> : "Rechazar"}
                        </Button>
                    </div>
                );
            case 'NONE':
            default:
                return (
                    <Button variant="outline-warning" className="text-white border-warning" disabled={actionLoading} onClick={handleSendFriendRequest}>
                        {actionLoading ? <Spinner animation="border" size="sm" /> : <><i className="bi bi-person-plus-fill me-1"></i> Agregar Amigo</>}
                    </Button>
                );
        }
    };

    return (
        <Modal show={!!clubId} onHide={onHide} size="lg" centered contentClassName="bg-dark text-white border-secondary">
            <Modal.Header closeButton closeVariant="white" className="border-secondary">
                <Modal.Title className="d-flex align-items-center gap-2 text-gold" style={{ color: 'var(--brand-gold)' }}>
                    <i className="bi bi-shield-shaded fs-3"></i>
                    <span>DETALLES DEL CLUB</span>
                </Modal.Title>
            </Modal.Header>
            <Modal.Body className="p-4">
                {loading ? (
                    <div className="text-center py-5">
                        <Spinner animation="grow" variant="gold" style={{ color: 'var(--brand-gold)' }} />
                    </div>
                ) : error ? (
                    <Alert variant="danger">{error}</Alert>
                ) : club ? (
                    <>
                        {/* Club Header Info */}
                        <div className="d-flex justify-content-between align-items-center border-bottom border-secondary pb-4 mb-4 flex-wrap gap-3">
                            <div className="d-flex align-items-center gap-3">
                                {club.logoUrl ? (
                                    <img src={club.logoUrl} alt={club.name} style={{ width: '64px', height: '64px', objectFit: 'contain' }} />
                                ) : (
                                    <div className="rounded bg-secondary d-flex align-items-center justify-content-center text-white fw-bold" style={{ width: '64px', height: '64px', fontSize: '24px' }}>
                                        {club.acronym}
                                    </div>
                                )}
                                <div>
                                    <h2 className="fw-bold text-white mb-0">[{club.acronym}] {club.name}</h2>
                                    <div className="d-flex gap-2 align-items-center mt-1">
                                        <Badge bg="dark" className="border border-secondary">{club.division}</Badge>
                                        <span className="text-warning fw-bold">{club.riotPoints} RP</span>
                                    </div>
                                </div>
                            </div>
                            
                            {/* Manager Area */}
                            <div className="d-flex align-items-center gap-3 bg-black bg-opacity-25 p-3 rounded border border-secondary border-opacity-50">
                                <div className="text-end">
                                    <span className="d-block small text-secondary">MÁNAGER</span>
                                    <span className="fw-bold text-white">{friendship ? friendship.username : (club.ownerId === user?.id ? user?.username : 'Mánager')}</span>
                                </div>
                                {renderFriendshipButton()}
                            </div>
                        </div>

                        {/* Roster Table */}
                        <h4 className="fw-bold text-uppercase text-white mb-3">
                            <i className="bi bi-people-fill text-warning me-2"></i>Plantilla de Jugadores
                        </h4>
                        <div className="table-responsive glass-card">
                            <Table hover variant="dark" className="mb-0 align-middle text-center">
                                <thead style={{ backgroundColor: 'rgba(0,0,0,0.4)' }}>
                                    <tr>
                                        <th className="py-2 text-secondary">ROL</th>
                                        <th className="py-2 text-start text-secondary">JUGADOR</th>
                                        <th className="py-2 text-secondary">WINRATE</th>
                                        <th className="py-2 text-secondary">EDAD</th>
                                        <th className="py-2 text-secondary">NACIONALIDAD</th>
                                        <th className="py-2 text-secondary">VALOR</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {players.map(player => (
                                        <tr key={player.id}>
                                            <td className="py-2">
                                                <Badge bg={getRoleColor(player.lolRole)} className="px-2 py-1">
                                                    {player.lolRole}
                                                </Badge>
                                            </td>
                                            <td className="py-2 text-start fw-bold text-white">
                                                {player.summonerName}
                                                {player.realName && <span className="d-block text-secondary small font-monospace">{player.realName}</span>}
                                            </td>
                                            <td className="py-2 fw-bold text-warning">{player.overallRating}%</td>
                                            <td className="py-2">{player.age || '-'}</td>
                                            <td className="py-2">{player.nationality || '-'}</td>
                                            <td className="py-2 text-info fw-bold">{player.priceRp} RP</td>
                                        </tr>
                                    ))}
                                    {players.length === 0 && (
                                        <tr>
                                            <td colSpan="6" className="py-4 text-secondary">Este club no tiene jugadores contratados.</td>
                                        </tr>
                                    )}
                                </tbody>
                            </Table>
                        </div>
                    </>
                ) : null}
            </Modal.Body>
        </Modal>
    );
};

export default ClubDetailsModal;
