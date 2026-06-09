import { useState, useEffect } from 'react';
import { Container, Table, Button, Modal, Form, Alert, Spinner, Badge, Card, Row, Col } from 'react-bootstrap';
import { useAuth } from '../context/AuthContext';
import authService from '../services/authService';
import leagueService from '../services/leagueService';

function AdminPage() {
  const { user: currentUser } = useAuth();
  const [activeTab, setActiveTab] = useState('users'); // 'users' o 'leagues'
  const [users, setUsers] = useState([]);
  const [leagues, setLeagues] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  // Estados de Modales
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [userToDelete, setUserToDelete] = useState(null);
  const [leagueToDelete, setLeagueToDelete] = useState(null);

  // Formulario Crear Usuario
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    role: 'ROLE_OWNER'
  });
  const [formError, setFormError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  // Cargar datos al montar la página o al cambiar de pestaña
  useEffect(() => {
    fetchData();
  }, [activeTab]);

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      if (activeTab === 'users') {
        const usersData = await authService.getAllUsers();
        setUsers(usersData);
      } else {
        const leaguesData = await leagueService.getAllLeagues();
        setLeagues(leaguesData);
      }
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || 'Error al cargar los datos. Inténtalo de nuevo.');
    } finally {
      setLoading(false);
    }
  };

  // Crear usuario
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleCreateUser = async (e) => {
    e.preventDefault();
    setFormError(null);
    setSubmitting(true);
    
    try {
      await authService.createUserByAdmin(formData);
      setSuccess(`Usuario "${formData.username}" creado con éxito.`);
      setShowCreateModal(false);
      // Limpiar formulario
      setFormData({
        username: '',
        email: '',
        password: '',
        role: 'ROLE_OWNER'
      });
      // Recargar usuarios
      fetchData();
    } catch (err) {
      console.error(err);
      setFormError(err.response?.data?.message || 'Error al crear el usuario. Por favor verifica los datos.');
    } finally {
      setSubmitting(false);
    }
  };

  // Eliminar usuario
  const handleDeleteUser = async () => {
    if (!userToDelete) return;
    setError(null);
    setSuccess(null);
    try {
      await authService.deleteUser(userToDelete.id);
      setSuccess(`Usuario "${userToDelete.username}" eliminado correctamente.`);
      setUserToDelete(null);
      fetchData();
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || `Error al eliminar el usuario "${userToDelete.username}".`);
      setUserToDelete(null);
    }
  };

  // Eliminar liga
  const handleDeleteLeague = async () => {
    if (!leagueToDelete) return;
    setError(null);
    setSuccess(null);
    try {
      await leagueService.deleteLeague(leagueToDelete.id);
      setSuccess(`Liga "${leagueToDelete.name}" eliminada correctamente.`);
      setLeagueToDelete(null);
      fetchData();
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || `Error al eliminar la liga "${leagueToDelete.name}".`);
      setLeagueToDelete(null);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('es-ES', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (e) {
      return dateString;
    }
  };

  return (
    <Container className="py-4 animate-fade-in">
      {/* Cabecera del Panel */}
      <div className="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
        <div>
          <h1 className="text-white mb-1">Consola de Administración</h1>
          <p className="text-muted mb-0">Gestión global de la plataforma, usuarios y competiciones activas.</p>
        </div>
        <div className="d-flex gap-2">
          <Button
            variant={activeTab === 'users' ? 'primary' : 'outline-primary'}
            onClick={() => { setActiveTab('users'); setSuccess(null); setError(null); }}
            className="px-4 py-2"
          >
            Usuarios
          </Button>
          <Button
            variant={activeTab === 'leagues' ? 'primary' : 'outline-primary'}
            onClick={() => { setActiveTab('leagues'); setSuccess(null); setError(null); }}
            className="px-4 py-2"
          >
            Ligas
          </Button>
        </div>
      </div>

      {/* Alertas */}
      {success && (
        <Alert variant="success" onClose={() => setSuccess(null)} dismissible className="border-0 shadow-sm mb-4">
          {success}
        </Alert>
      )}
      {error && (
        <Alert variant="danger" onClose={() => setError(null)} dismissible className="border-0 shadow-sm mb-4">
          {error}
        </Alert>
      )}

      {/* Tarjeta Contenedora Principal */}
      <Card className="glass-card border-0 mb-4">
        <Card.Body className="p-4">
          {loading ? (
            <div className="d-flex flex-column justify-content-center align-items-center py-5">
              <Spinner animation="border" variant="info" className="mb-3" />
              <p className="text-muted">Cargando información...</p>
            </div>
          ) : (
            <>
              {/* VISTA DE USUARIOS */}
              {activeTab === 'users' && (
                <div>
                  <div className="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
                    <h3 className="text-white mb-0">Listado de Usuarios ({users.length})</h3>
                    <Button variant="primary" onClick={() => setShowCreateModal(true)}>
                      + Crear Usuario
                    </Button>
                  </div>

                  <div className="table-responsive">
                    <Table hover variant="dark" className="align-middle mb-0">
                      <thead>
                        <tr>
                          <th>ID</th>
                          <th>Nombre de Usuario</th>
                          <th>Email</th>
                          <th>Rol</th>
                          <th>Estado</th>
                          <th>Fecha Registro</th>
                          <th className="text-end">Acciones</th>
                        </tr>
                      </thead>
                      <tbody>
                        {users.map((u) => {
                          const isSelf = currentUser?.id === u.id || currentUser?.username === u.username;
                          return (
                            <tr key={u.id} className={isSelf ? 'border-primary' : ''}>
                              <td><span className="text-muted">#{u.id}</span></td>
                              <td>
                                <span className="fw-semibold text-white">{u.username}</span>
                                {isSelf && (
                                  <Badge bg="info" className="ms-2 text-dark">Tú</Badge>
                                )}
                              </td>
                              <td className="text-muted">{u.email}</td>
                              <td>
                                <Badge bg={u.role === 'ROLE_ADMIN' ? 'danger' : 'secondary'}>
                                  {u.role === 'ROLE_ADMIN' ? 'ADMIN' : 'PROPIETARIO'}
                                </Badge>
                              </td>
                              <td>
                                <Badge bg={u.active ? 'success' : 'warning'}>
                                  {u.active ? 'Activo' : 'Inactivo'}
                                </Badge>
                              </td>
                              <td className="text-muted small">{formatDate(u.createdAt)}</td>
                              <td className="text-end">
                                <Button
                                  variant="outline-danger"
                                  size="sm"
                                  disabled={isSelf}
                                  onClick={() => setUserToDelete(u)}
                                >
                                  Eliminar
                                </Button>
                              </td>
                            </tr>
                          );
                        })}
                        {users.length === 0 && (
                          <tr>
                            <td colSpan="7" className="text-center py-4 text-muted">
                              No hay usuarios registrados.
                            </td>
                          </tr>
                        )}
                      </tbody>
                    </Table>
                  </div>
                </div>
              )}

              {/* VISTA DE LIGAS */}
              {activeTab === 'leagues' && (
                <div>
                  <div className="d-flex justify-content-between align-items-center mb-4">
                    <h3 className="text-white mb-0">Listado de Ligas ({leagues.length})</h3>
                  </div>

                  <div className="table-responsive">
                    <Table hover variant="dark" className="align-middle mb-0">
                      <thead>
                        <tr>
                          <th>ID</th>
                          <th>Nombre de la Liga</th>
                          <th>Temporada</th>
                          <th>Visibilidad</th>
                          <th>Clubs Máx</th>
                          <th>Estado</th>
                          <th>Fecha Inicio</th>
                          <th className="text-end">Acciones</th>
                        </tr>
                      </thead>
                      <tbody>
                        {leagues.map((l) => (
                          <tr key={l.id}>
                            <td><span className="text-muted">#{l.id}</span></td>
                            <td className="fw-semibold text-white">{l.name}</td>
                            <td className="text-muted">{l.season}</td>
                            <td>
                              <Badge bg={
                                l.visibility === 'PUBLIC' ? 'success' : 
                                l.visibility === 'PRIVATE' ? 'danger' : 'warning'
                              }>
                                {l.visibility === 'PUBLIC' ? 'Pública' :
                                 l.visibility === 'PRIVATE' ? 'Privada' : 'Amigos'}
                              </Badge>
                            </td>
                            <td className="text-muted">{l.maxClubs}</td>
                            <td>
                              <Badge bg={l.active ? 'success' : 'secondary'}>
                                {l.active ? 'Activa' : 'Finalizada'}
                              </Badge>
                            </td>
                            <td className="text-muted small">{l.startDate || '-'}</td>
                            <td className="text-end">
                              <Button
                                variant="outline-danger"
                                size="sm"
                                onClick={() => setLeagueToDelete(l)}
                              >
                                Eliminar
                              </Button>
                            </td>
                          </tr>
                        ))}
                        {leagues.length === 0 && (
                          <tr>
                            <td colSpan="8" className="text-center py-4 text-muted">
                              No hay ligas creadas en el sistema.
                            </td>
                          </tr>
                        )}
                      </tbody>
                    </Table>
                  </div>
                </div>
              )}
            </>
          )}
        </Card.Body>
      </Card>

      {/* MODAL: CREAR USUARIO */}
      <Modal 
        show={showCreateModal} 
        onHide={() => { setShowCreateModal(false); setFormError(null); }} 
        centered
        contentClassName="bg-dark text-white border-0 shadow-lg"
      >
        <Modal.Header closeButton closeVariant="white" className="border-bottom border-secondary p-3">
          <Modal.Title className="font-heading">Crear Nuevo Usuario</Modal.Title>
        </Modal.Header>
        <Modal.Body className="p-4">
          {formError && (
            <Alert variant="danger" className="border-0 py-2 small mb-3">
              {formError}
            </Alert>
          )}
          <Form onSubmit={handleCreateUser}>
            <Form.Group className="mb-3">
              <Form.Label>Nombre de Usuario</Form.Label>
              <Form.Control
                type="text"
                name="username"
                value={formData.username}
                onChange={handleInputChange}
                required
                placeholder="Nombre único, min. 3 caracteres"
                className="bg-black border-secondary text-white"
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Correo Electrónico</Form.Label>
              <Form.Control
                type="email"
                name="email"
                value={formData.email}
                onChange={handleInputChange}
                required
                placeholder="correo@ejemplo.com"
                className="bg-black border-secondary text-white"
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Contraseña</Form.Label>
              <Form.Control
                type="password"
                name="password"
                value={formData.password}
                onChange={handleInputChange}
                required
                placeholder="Mínimo 8 caracteres"
                className="bg-black border-secondary text-white"
              />
            </Form.Group>

            <Form.Group className="mb-4">
              <Form.Label>Rol del Sistema</Form.Label>
              <Form.Select
                name="role"
                value={formData.role}
                onChange={handleInputChange}
                className="bg-black border-secondary text-white"
              >
                <option value="ROLE_OWNER">Propietario de Club (ROLE_OWNER)</option>
                <option value="ROLE_ADMIN">Administrador (ROLE_ADMIN)</option>
              </Form.Select>
            </Form.Group>

            <div className="d-flex justify-content-end gap-2">
              <Button 
                variant="outline-secondary" 
                onClick={() => { setShowCreateModal(false); setFormError(null); }}
                disabled={submitting}
              >
                Cancelar
              </Button>
              <Button variant="primary" type="submit" disabled={submitting}>
                {submitting ? 'Creando...' : 'Crear Usuario'}
              </Button>
            </div>
          </Form>
        </Modal.Body>
      </Modal>

      {/* MODAL: CONFIRMAR ELIMINAR USUARIO */}
      <Modal
        show={!!userToDelete}
        onHide={() => setUserToDelete(null)}
        centered
        contentClassName="bg-dark text-white border-0 shadow-lg"
      >
        <Modal.Header closeButton closeVariant="white" className="border-bottom border-secondary">
          <Modal.Title className="text-danger font-heading">Confirmar Eliminación</Modal.Title>
        </Modal.Header>
        <Modal.Body className="p-4">
          <p>¿Estás seguro de que deseas eliminar permanentemente al usuario <strong className="text-white">"{userToDelete?.username}"</strong>?</p>
          <p className="text-warning small mb-0">
            <span className="fw-bold">Advertencia:</span> Esta acción no se puede deshacer. Se eliminarán sus tokens de sesión y amistades. Las ligas y clubes del usuario quedarán desvinculados o se verán afectados.
          </p>
        </Modal.Body>
        <Modal.Footer className="border-top-0">
          <Button variant="outline-secondary" onClick={() => setUserToDelete(null)}>
            Cancelar
          </Button>
          <Button variant="danger" onClick={handleDeleteUser}>
            Confirmar Borrado
          </Button>
        </Modal.Footer>
      </Modal>

      {/* MODAL: CONFIRMAR ELIMINAR LIGA */}
      <Modal
        show={!!leagueToDelete}
        onHide={() => setLeagueToDelete(null)}
        centered
        contentClassName="bg-dark text-white border-0 shadow-lg"
      >
        <Modal.Header closeButton closeVariant="white" className="border-bottom border-secondary">
          <Modal.Title className="text-danger font-heading">Confirmar Eliminación de Liga</Modal.Title>
        </Modal.Header>
        <Modal.Body className="p-4">
          <p>¿Estás seguro de que deseas eliminar la liga <strong className="text-white">"{leagueToDelete?.name}"</strong>?</p>
          <p className="text-danger small mb-0">
            <span className="fw-bold">Peligro:</span> Al eliminar la liga, se borrarán de forma automática e irreversible todos sus clubes inscritos, partidos programados y jugados, clasificaciones e invitaciones relacionadas.
          </p>
        </Modal.Body>
        <Modal.Footer className="border-top-0">
          <Button variant="outline-secondary" onClick={() => setLeagueToDelete(null)}>
            Cancelar
          </Button>
          <Button variant="danger" onClick={handleDeleteLeague}>
            Confirmar Borrado
          </Button>
        </Modal.Footer>
      </Modal>
    </Container>
  );
}

export default AdminPage;
