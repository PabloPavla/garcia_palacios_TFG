import { Link, useNavigate } from 'react-router-dom';
import { Navbar, Nav, Container, Button, Dropdown } from 'react-bootstrap';
import { useAuth } from '../context/AuthContext';
import './Navbar.css';

const Navigation = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <Navbar expand="lg" variant="dark" className="navbar-glass fixed-top" collapseOnSelect>
            <Container>
                <Navbar.Brand as={Link} to={user ? "/dashboard" : "/"} className="d-flex align-items-center gap-2 fw-bold text-uppercase fs-4" style={{ fontFamily: 'var(--font-heading)' }}>
                    <i className="bi bi-hexagon-fill" style={{ color: 'var(--brand-primary)' }}></i>
                    CLASH<span style={{ color: 'var(--brand-primary)' }}>MGR</span>
                </Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav className="me-auto">
                        {user && (
                            <>
                                <Nav.Link as={Link} to="/dashboard"><i className="bi bi-grid me-1"></i> Dashboard</Nav.Link>
                                <Nav.Link as={Link} to="/my-club"><i className="bi bi-shield-fill me-1"></i> Mi Club</Nav.Link>
                                <Nav.Link as={Link} to="/transfers"><i className="bi bi-arrow-left-right me-1"></i> Transferencias</Nav.Link>
                            </>
                        )}
                        <Nav.Link as={Link} to="/leagues"><i className="bi bi-trophy-fill me-1" style={{ color: 'var(--brand-gold)' }}></i> Ligas</Nav.Link>
                        
                        {user && user.role === 'ROLE_ADMIN' && (
                            <Nav.Link as={Link} to="/admin" className="text-warning"><i className="bi bi-gear-fill me-1"></i> Admin</Nav.Link>
                        )}
                    </Nav>
                    <Nav>
                        {user ? (
                            <Dropdown align="end">
                                <Dropdown.Toggle variant="link" className="text-decoration-none text-white d-flex align-items-center gap-2" id="dropdown-user">
                                    <div className="rounded-circle bg-primary d-flex align-items-center justify-content-center" style={{ width: '32px', height: '32px' }}>
                                        {user.username.charAt(0).toUpperCase()}
                                    </div>
                                    {user.username}
                                </Dropdown.Toggle>

                                <Dropdown.Menu variant="dark" className="glass-card mt-2">
                                    <Dropdown.Item as={Link} to="/profile">
                                        <i className="bi bi-person-circle me-2"></i> Mi Perfil
                                    </Dropdown.Item>
                                    <Dropdown.Divider />
                                    <Dropdown.Item onClick={handleLogout} className="text-danger">
                                        <i className="bi bi-box-arrow-right me-2"></i> Cerrar Sesión
                                    </Dropdown.Item>
                                </Dropdown.Menu>
                            </Dropdown>
                        ) : (
                            <div className="d-flex gap-2">
                                <Button as={Link} to="/login" variant="outline-primary">Ingresar</Button>
                                <Button as={Link} to="/register" variant="primary">Registrarse</Button>
                            </div>
                        )}
                    </Nav>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
};

export default Navigation;
