import { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Form, Button, Alert, Spinner, Tab, Nav, Badge, ListGroup, InputGroup } from 'react-bootstrap';
import { useAuth } from '../context/AuthContext';
import friendService from '../services/friendService';
import './AuthPages.css';

const ProfilePage = () => {
    const { user, token, logout, updateUser } = useAuth();
    
    // Perfil Form State
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        profilePictureUrl: ''
    });
    const [profileError, setProfileError] = useState('');
    const [profileSuccess, setProfileSuccess] = useState('');
    const [profileLoading, setProfileLoading] = useState(false);

    // Amigos State
    const [friends, setFriends] = useState([]);
    const [pendingRequests, setPendingRequests] = useState([]);
    const [searchQuery, setSearchQuery] = useState('');
    const [searchResults, setSearchResults] = useState([]);
    const [friendsLoading, setFriendsLoading] = useState(true);
    const [friendsError, setFriendsError] = useState('');
    const [friendsSuccess, setFriendsSuccess] = useState('');

    useEffect(() => {
        if (user) {
            setFormData({
                username: user.username || '',
                email: user.email || '',
                password: '',
                profilePictureUrl: user.profilePictureUrl || ''
            });
        }
    }, [user]);

    // Load Friends Data
    useEffect(() => {
        loadFriendsData();
    }, []);

    const loadFriendsData = async () => {
        try {
            setFriendsLoading(true);
            const [friendsData, requestsData] = await Promise.all([
                friendService.getFriends(),
                friendService.getPendingRequests()
            ]);
            setFriends(friendsData);
            setPendingRequests(requestsData);
        } catch (err) {
            setFriendsError('Error al cargar la información de amigos.');
        } finally {
            setFriendsLoading(false);
        }
    };

    const handleProfileChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleProfileSubmit = async (e) => {
        e.preventDefault();
        setProfileError('');
        setProfileSuccess('');
        setProfileLoading(true);

        try {
            const response = await fetch('http://localhost:8080/auth/profile', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    username: formData.username,
                    email: formData.email,
                    password: formData.password || undefined,
                    profilePictureUrl: formData.profilePictureUrl
                })
            });

            if (!response.ok) {
                const errData = await response.json();
                throw new Error(errData.error || 'Error al actualizar el perfil');
            }

            const data = await response.json();
            
            localStorage.setItem('accessToken', data.accessToken);
            localStorage.setItem('refreshToken', data.refreshToken);
            
            const updatedUser = {
                id: data.userId,
                username: data.username,
                email: data.email,
                role: data.role,
                profilePictureUrl: data.profilePictureUrl
            };
            updateUser(updatedUser);
            
            setProfileSuccess('¡Perfil actualizado correctamente!');
            
        } catch (err) {
            setProfileError(err.message);
        } finally {
            setProfileLoading(false);
        }
    };

    // Friends Handlers
    const handleSearch = async (e) => {
        e.preventDefault();
        if (searchQuery.trim().length < 3) return;
        
        try {
            const results = await friendService.searchUsers(searchQuery);
            setSearchResults(results);
        } catch (err) {
            setFriendsError('Error al buscar usuarios.');
        }
    };

    const handleSendRequest = async (username) => {
        try {
            await friendService.sendRequest(username);
            setFriendsSuccess(`Solicitud enviada a ${username}`);
            setSearchResults(prev => prev.filter(u => u.username !== username));
            setTimeout(() => setFriendsSuccess(''), 3000);
        } catch (err) {
            setFriendsError(err.response?.data?.error || 'Error al enviar la solicitud');
            setTimeout(() => setFriendsError(''), 3000);
        }
    };

    const handleAcceptRequest = async (id) => {
        try {
            await friendService.acceptRequest(id);
            setFriendsSuccess('Solicitud aceptada');
            loadFriendsData();
            setTimeout(() => setFriendsSuccess(''), 3000);
        } catch (err) {
            setFriendsError('Error al aceptar la solicitud');
        }
    };

    const handleRejectRequest = async (id) => {
        try {
            await friendService.rejectRequest(id);
            loadFriendsData();
        } catch (err) {
            setFriendsError('Error al rechazar la solicitud');
        }
    };

    const handleRemoveFriend = async (id) => {
        if (window.confirm('¿Estás seguro de que quieres eliminar a este amigo?')) {
            try {
                await friendService.removeFriend(id);
                loadFriendsData();
            } catch (err) {
                setFriendsError('Error al eliminar amigo');
            }
        }
    };

    return (
        <Container className="mt-5 pt-5 auth-container">
            <Row className="justify-content-center">
                <Col md={10} lg={8}>
                    <div className="glass-card p-0 overflow-hidden">
                        
                        {/* Header con foto de perfil */}
                        <div className="bg-dark bg-opacity-75 p-4 text-center border-bottom border-primary border-2">
                            <div className="mb-3 position-relative d-inline-block" style={{ cursor: 'pointer' }} onClick={() => {
                                const newUrl = window.prompt("Introduce la URL de tu nueva foto de perfil:", formData.profilePictureUrl);
                                if (newUrl !== null) {
                                    setFormData(prev => ({ ...prev, profilePictureUrl: newUrl }));
                                }
                            }} title="Haz clic para cambiar tu foto">
                                {formData.profilePictureUrl || user?.profilePictureUrl ? (
                                    <img 
                                        src={formData.profilePictureUrl || user?.profilePictureUrl} 
                                        alt="Profile" 
                                        className="rounded-circle border border-primary border-3"
                                        style={{ width: '120px', height: '120px', objectFit: 'cover' }}
                                        onError={(e) => { e.target.style.display = 'none'; e.target.nextSibling.style.display = 'flex'; }}
                                    />
                                ) : (
                                    <div className="rounded-circle bg-primary text-white d-flex align-items-center justify-content-center mx-auto border border-white border-2" style={{ width: '120px', height: '120px', fontSize: '3rem' }}>
                                        {user?.username?.charAt(0).toUpperCase()}
                                    </div>
                                )}
                            </div>
                            <h3 className="fw-bold text-white mb-1">{user?.username}</h3>
                            <p className="text-secondary mb-0">{user?.email}</p>
                            <Badge bg="primary" className="mt-2">{user?.role === 'ROLE_ADMIN' ? 'Administrador' : 'Mánager'}</Badge>
                        </div>

                        {/* Tabs content */}
                        <Tab.Container id="profile-tabs" defaultActiveKey="profile">
                            <Nav variant="tabs" className="bg-dark bg-opacity-50 px-3 pt-3 border-bottom-0 custom-tabs">
                                <Nav.Item>
                                    <Nav.Link eventKey="profile" className="text-white fw-bold px-4 py-3">
                                        <i className="bi bi-person-gear me-2"></i> Mi Perfil
                                    </Nav.Link>
                                </Nav.Item>
                                <Nav.Item>
                                    <Nav.Link eventKey="friends" className="text-white fw-bold px-4 py-3">
                                        <i className="bi bi-people-fill me-2"></i> Amigos
                                        {pendingRequests.length > 0 && (
                                            <Badge bg="danger" className="ms-2 rounded-pill">{pendingRequests.length}</Badge>
                                        )}
                                    </Nav.Link>
                                </Nav.Item>
                            </Nav>
                            
                            <Tab.Content className="p-4 bg-dark bg-opacity-25">
                                {/* PESTAÑA PERFIL */}
                                <Tab.Pane eventKey="profile">
                                    {profileError && <Alert variant="danger">{profileError}</Alert>}
                                    {profileSuccess && <Alert variant="success">{profileSuccess}</Alert>}

                                    <Form onSubmit={handleProfileSubmit}>
                                        <Row>
                                            <Col md={6}>
                                                <Form.Group className="mb-3" controlId="formUsername">
                                                    <Form.Label className="text-light">Nombre de Usuario</Form.Label>
                                                    <Form.Control
                                                        type="text"
                                                        name="username"
                                                        value={formData.username}
                                                        onChange={handleProfileChange}
                                                        className="bg-dark text-light border-secondary"
                                                    />
                                                </Form.Group>
                                            </Col>
                                            <Col md={6}>
                                                <Form.Group className="mb-3" controlId="formEmail">
                                                    <Form.Label className="text-light">Correo Electrónico</Form.Label>
                                                    <Form.Control
                                                        type="email"
                                                        name="email"
                                                        value={formData.email}
                                                        onChange={handleProfileChange}
                                                        className="bg-dark text-light border-secondary"
                                                    />
                                                </Form.Group>
                                            </Col>
                                        </Row>

                                        <Form.Group className="mb-3" controlId="formPassword">
                                            <Form.Label className="text-light">Nueva Contraseña <small className="text-secondary">(Opcional)</small></Form.Label>
                                            <Form.Control
                                                type="password"
                                                name="password"
                                                value={formData.password}
                                                onChange={handleProfileChange}
                                                placeholder="Dejar en blanco para no cambiar"
                                                className="bg-dark text-light border-secondary"
                                            />
                                        </Form.Group>

                                        <Form.Group className="mb-4" controlId="formProfilePictureUrl" style={{ display: 'none' }}>
                                            <Form.Label className="text-light">URL Foto de Perfil</Form.Label>
                                            <Form.Control
                                                type="url"
                                                name="profilePictureUrl"
                                                value={formData.profilePictureUrl}
                                                onChange={handleProfileChange}
                                                placeholder="https://ejemplo.com/mifoto.jpg"
                                                className="bg-dark text-light border-secondary"
                                            />
                                        </Form.Group>

                                        <div className="d-flex justify-content-between">
                                            <Button variant="outline-danger" onClick={logout} type="button">
                                                <i className="bi bi-box-arrow-right me-2"></i>Cerrar Sesión
                                            </Button>
                                            
                                            <Button variant="primary" type="submit" disabled={profileLoading} className="fw-bold px-4">
                                                {profileLoading ? (
                                                    <><Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" className="me-2" /> Guardando...</>
                                                ) : (
                                                    <><i className="bi bi-save me-2"></i>Guardar Cambios</>
                                                )}
                                            </Button>
                                        </div>
                                    </Form>
                                </Tab.Pane>

                                {/* PESTAÑA AMIGOS */}
                                <Tab.Pane eventKey="friends">
                                    {friendsError && <Alert variant="danger">{friendsError}</Alert>}
                                    {friendsSuccess && <Alert variant="success">{friendsSuccess}</Alert>}

                                    <Row className="g-4">
                                        <Col md={6}>
                                            <h5 className="text-white mb-3 border-bottom pb-2">
                                                <i className="bi bi-search me-2 text-primary"></i>Añadir Amigos
                                            </h5>
                                            
                                            <Form onSubmit={handleSearch} className="mb-3">
                                                <InputGroup>
                                                    <Form.Control
                                                        placeholder="Buscar por usuario..."
                                                        value={searchQuery}
                                                        onChange={(e) => setSearchQuery(e.target.value)}
                                                        className="bg-dark text-light border-secondary"
                                                    />
                                                    <Button variant="primary" type="submit">
                                                        <i className="bi bi-search"></i>
                                                    </Button>
                                                </InputGroup>
                                            </Form>

                                            {searchResults.length > 0 && (
                                                <ListGroup variant="flush" className="bg-transparent border border-secondary rounded overflow-hidden">
                                                    {searchResults.map(result => (
                                                        <ListGroup.Item key={result.id} className="bg-dark text-white d-flex justify-content-between align-items-center py-2 border-secondary">
                                                            <div className="d-flex align-items-center">
                                                                <div className="rounded-circle bg-primary me-2 d-flex align-items-center justify-content-center" style={{ width: '30px', height: '30px' }}>
                                                                    {result.username.charAt(0).toUpperCase()}
                                                                </div>
                                                                <span>{result.username}</span>
                                                            </div>
                                                            <Button variant="outline-success" size="sm" onClick={() => handleSendRequest(result.username)}>
                                                                <i className="bi bi-person-plus-fill"></i>
                                                            </Button>
                                                        </ListGroup.Item>
                                                    ))}
                                                </ListGroup>
                                            )}

                                            {pendingRequests.length > 0 && (
                                                <div className="mt-4">
                                                    <h6 className="text-warning mb-2">Solicitudes Recibidas</h6>
                                                    <ListGroup variant="flush" className="bg-transparent border border-secondary rounded overflow-hidden">
                                                        {pendingRequests.map(req => (
                                                            <ListGroup.Item key={req.id} className="bg-dark text-white d-flex justify-content-between align-items-center py-2 border-secondary">
                                                                <div>
                                                                    <strong>{req.senderUsername}</strong> quiere ser tu amigo
                                                                </div>
                                                                <div>
                                                                    <Button variant="success" size="sm" className="me-2" onClick={() => handleAcceptRequest(req.id)}>
                                                                        <i className="bi bi-check-lg"></i>
                                                                    </Button>
                                                                    <Button variant="danger" size="sm" onClick={() => handleRejectRequest(req.id)}>
                                                                        <i className="bi bi-x-lg"></i>
                                                                    </Button>
                                                                </div>
                                                            </ListGroup.Item>
                                                        ))}
                                                    </ListGroup>
                                                </div>
                                            )}
                                        </Col>

                                        <Col md={6}>
                                            <h5 className="text-white mb-3 border-bottom pb-2">
                                                <i className="bi bi-people-fill me-2" style={{ color: 'var(--brand-gold)' }}></i>Mis Amigos
                                                <Badge bg="secondary" className="ms-2">{friends.length}</Badge>
                                            </h5>

                                            {friendsLoading ? (
                                                <div className="text-center py-4">
                                                    <Spinner animation="border" variant="primary" />
                                                </div>
                                            ) : friends.length === 0 ? (
                                                <div className="text-center text-secondary py-4 bg-dark rounded border border-secondary">
                                                    <i className="bi bi-emoji-frown fs-1 d-block mb-2"></i>
                                                    Aún no tienes amigos añadidos
                                                </div>
                                            ) : (
                                                <ListGroup variant="flush" className="bg-transparent border border-secondary rounded overflow-hidden">
                                                    {friends.map(friend => (
                                                        <ListGroup.Item key={friend.id} className="bg-dark text-white d-flex justify-content-between align-items-center py-3 border-secondary">
                                                            <div className="d-flex align-items-center">
                                                                {friend.profilePictureUrl ? (
                                                                    <img src={friend.profilePictureUrl} alt={friend.username} className="rounded-circle me-3" style={{ width: '40px', height: '40px', objectFit: 'cover' }} />
                                                                ) : (
                                                                    <div className="rounded-circle bg-primary me-3 d-flex align-items-center justify-content-center text-white fw-bold" style={{ width: '40px', height: '40px' }}>
                                                                        {friend.username.charAt(0).toUpperCase()}
                                                                    </div>
                                                                )}
                                                                <span className="fw-bold fs-5">{friend.username}</span>
                                                            </div>
                                                            <Button variant="outline-danger" size="sm" onClick={() => handleRemoveFriend(friend.friendshipId)}>
                                                                <i className="bi bi-person-x-fill"></i>
                                                            </Button>
                                                        </ListGroup.Item>
                                                    ))}
                                                </ListGroup>
                                            )}
                                        </Col>
                                    </Row>
                                </Tab.Pane>
                            </Tab.Content>
                        </Tab.Container>
                    </div>
                </Col>
            </Row>
        </Container>
    );
};

export default ProfilePage;
