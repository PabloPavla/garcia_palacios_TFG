import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Form, Button, Container, Row, Col, Alert, Spinner } from 'react-bootstrap';
import { useAuth } from '../context/AuthContext';
import './AuthPages.css';

const LoginPage = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);
    
    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);
        setLoading(true);

        try {
            await login(username, password);
            navigate('/dashboard');
        } catch (err) {
            setError(err.response?.data?.message || 'Error al iniciar sesión. Comprueba tus credenciales.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-page-wrapper">
            <Container>
                <Row className="justify-content-center align-items-center min-vh-100">
                    <Col md={8} lg={5}>
                        <div className="glass-card p-5 animate-fade-in text-center">
                            <div className="mb-4">
                                <i className="bi bi-hexagon-fill fs-1" style={{ color: 'var(--brand-primary)' }}></i>
                                <h2 className="mt-3 fw-bold">BIENVENIDO DE VUELTA</h2>
                                <p className="text-secondary">Inicia sesión para gestionar tu club</p>
                            </div>

                            {error && <Alert variant="danger" className="text-start">{error}</Alert>}

                            <Form onSubmit={handleSubmit} className="text-start">
                                <Form.Group className="mb-4" controlId="formUsername">
                                    <Form.Label className="fw-semibold text-secondary">Nombre de usuario</Form.Label>
                                    <div className="input-group">
                                        <span className="input-group-text bg-transparent border-end-0 border-secondary">
                                            <i className="bi bi-person text-secondary"></i>
                                        </span>
                                        <Form.Control
                                            type="text"
                                            placeholder="Ingresa tu usuario"
                                            value={username}
                                            onChange={(e) => setUsername(e.target.value)}
                                            required
                                            className="border-start-0"
                                            style={{ backgroundColor: 'rgba(0,0,0,0.2)' }}
                                        />
                                    </div>
                                </Form.Group>

                                <Form.Group className="mb-4" controlId="formPassword">
                                    <Form.Label className="fw-semibold text-secondary">Contraseña</Form.Label>
                                    <div className="input-group">
                                        <span className="input-group-text bg-transparent border-end-0 border-secondary">
                                            <i className="bi bi-lock text-secondary"></i>
                                        </span>
                                        <Form.Control
                                            type="password"
                                            placeholder="Ingresa tu contraseña"
                                            value={password}
                                            onChange={(e) => setPassword(e.target.value)}
                                            required
                                            className="border-start-0"
                                            style={{ backgroundColor: 'rgba(0,0,0,0.2)' }}
                                        />
                                    </div>
                                </Form.Group>

                                <div className="d-grid gap-2 mt-5">
                                    <Button variant="primary" type="submit" disabled={loading} size="lg">
                                        {loading ? (
                                            <><Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" /> Iniciando...</>
                                        ) : (
                                            'INICIAR SESIÓN'
                                        )}
                                    </Button>
                                </div>
                            </Form>
                            
                            <div className="mt-4">
                                <p className="text-secondary mb-0">
                                    ¿No tienes una cuenta? <Link to="/register" className="fw-bold text-white text-decoration-underline">Regístrate aquí</Link>
                                </p>
                            </div>
                        </div>
                    </Col>
                </Row>
            </Container>
        </div>
    );
};

export default LoginPage;
