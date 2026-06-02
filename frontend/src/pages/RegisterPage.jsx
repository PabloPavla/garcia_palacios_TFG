import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Form, Button, Container, Row, Col, Alert, Spinner } from 'react-bootstrap';
import { useAuth } from '../context/AuthContext';
import './AuthPages.css';

const RegisterPage = () => {
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);
    
    const { register } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);

        if (password !== confirmPassword) {
            setError('Las contraseñas no coinciden');
            return;
        }

        setLoading(true);

        try {
            await register(username, email, password);
            navigate('/dashboard'); // El register en authService ya hace auto-login
        } catch (err) {
            setError(err.response?.data?.message || 'Error al registrar el usuario.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-page-wrapper">
            <Container>
                <Row className="justify-content-center align-items-center min-vh-100">
                    <Col md={8} lg={6}>
                        <div className="glass-card p-5 animate-fade-in text-center my-5">
                            <div className="mb-4">
                                <i className="bi bi-hexagon-fill fs-1" style={{ color: 'var(--brand-primary)' }}></i>
                                <h2 className="mt-3 fw-bold">CREA TU CLUB</h2>
                                <p className="text-secondary">Únete a la liga y conviértete en el mejor mánager</p>
                            </div>

                            {error && <Alert variant="danger" className="text-start">{error}</Alert>}

                            <Form onSubmit={handleSubmit} className="text-start">
                                <Row>
                                    <Col md={6}>
                                        <Form.Group className="mb-4" controlId="formUsername">
                                            <Form.Label className="fw-semibold text-secondary">Nombre de usuario</Form.Label>
                                            <div className="input-group">
                                                <span className="input-group-text bg-transparent border-end-0 border-secondary">
                                                    <i className="bi bi-person text-secondary"></i>
                                                </span>
                                                <Form.Control
                                                    type="text"
                                                    placeholder="Usuario"
                                                    value={username}
                                                    onChange={(e) => setUsername(e.target.value)}
                                                    required
                                                    className="border-start-0"
                                                />
                                            </div>
                                        </Form.Group>
                                    </Col>
                                    <Col md={6}>
                                        <Form.Group className="mb-4" controlId="formEmail">
                                            <Form.Label className="fw-semibold text-secondary">Correo Electrónico</Form.Label>
                                            <div className="input-group">
                                                <span className="input-group-text bg-transparent border-end-0 border-secondary">
                                                    <i className="bi bi-envelope text-secondary"></i>
                                                </span>
                                                <Form.Control
                                                    type="email"
                                                    placeholder="correo@ejemplo.com"
                                                    value={email}
                                                    onChange={(e) => setEmail(e.target.value)}
                                                    required
                                                    className="border-start-0"
                                                />
                                            </div>
                                        </Form.Group>
                                    </Col>
                                </Row>

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
                                            minLength={6}
                                        />
                                    </div>
                                </Form.Group>

                                <Form.Group className="mb-4" controlId="formConfirmPassword">
                                    <Form.Label className="fw-semibold text-secondary">Confirmar Contraseña</Form.Label>
                                    <div className="input-group">
                                        <span className="input-group-text bg-transparent border-end-0 border-secondary">
                                            <i className="bi bi-lock-fill text-secondary"></i>
                                        </span>
                                        <Form.Control
                                            type="password"
                                            placeholder="Repite tu contraseña"
                                            value={confirmPassword}
                                            onChange={(e) => setConfirmPassword(e.target.value)}
                                            required
                                            className="border-start-0"
                                            minLength={6}
                                        />
                                    </div>
                                </Form.Group>

                                <div className="d-grid gap-2 mt-5">
                                    <Button variant="primary" type="submit" disabled={loading} size="lg">
                                        {loading ? (
                                            <><Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" /> Registrando...</>
                                        ) : (
                                            'CREAR CUENTA'
                                        )}
                                    </Button>
                                </div>
                            </Form>
                            
                            <div className="mt-4">
                                <p className="text-secondary mb-0">
                                    ¿Ya tienes una cuenta? <Link to="/login" className="fw-bold text-white text-decoration-underline">Inicia sesión</Link>
                                </p>
                            </div>
                        </div>
                    </Col>
                </Row>
            </Container>
        </div>
    );
};

export default RegisterPage;
