# 🏆 Clash Manager - La Revolución en Gestión de eSports

**Desarrollo de Aplicaciones Web (DAW)**  
**Autor:** Pablo Garcia Palacios

---

## 📑 Índice

1. [Introducción](#-introducción)
2. [Funcionalidades y Tecnologías](#-funcionalidades-y-tecnologías)
3. [Guía de Instalación](#-guía-de-instalación)
4. [Guía de Uso](#-guía-de-uso)
5. [Enlaces de Interés](#-enlaces-de-interés)
6. [Conclusión](#-conclusión)
7. [Contribuciones y Licencias](#-contribuciones-y-licencias)
8. [Contacto](#-contacto)

---

## 🚀 Introducción

**Clash Manager** es un proyecto nacido de la pasión por los deportes electrónicos (eSports), diseñado para simular y gestionar un ecosistema competitivo completo. Inspirado en mecánicas de los juegos *manager* deportivos tradicionales, este proyecto lleva la gestión estratégica al mundo de League of Legends.

### Justificación y Objetivos
El crecimiento de los eSports requiere de herramientas y plataformas cada vez más complejas. El objetivo principal de este proyecto es aplicar los conocimientos adquiridos durante el ciclo de DAW para construir una arquitectura de microservicios escalable que permita la gestión de usuarios, clubes, jugadores y mercados de fichajes en tiempo real.

### Motivación
*(Por añadir: Puedes detallar aquí tu motivación personal para elegir este proyecto).*

---

## ⚙️ Funcionalidades y Tecnologías

### Funcionalidades Implementadas
- **Sistema de Autenticación y Autorización**: Registro seguro con JWT, roles de usuario y permisos granulares. Panel de administrador para control total.
- **Gestión de Clubes**: Creación de clubes, personalización (logo, región) y gestión del presupuesto (Riot Points - RP).
- **Mercado de Fichajes**: 
  - Mercado de agentes libres con subastas y pujas en tiempo real.
  - Sistema de negociación e intercambio de jugadores entre clubes.
- **Gestión de Ligas**: 
  - Creación de ligas privadas con enlaces o códigos de invitación.
  - Simulación de jornadas y actualización de la clasificación (Standings).
- **Sistema de Torneos**: Generación automática de emparejamientos y resolución de partidos simulados por estadísticas.
- **Social**: Sistema de solicitudes de amistad entre usuarios.

### Tecnologías Utilizadas
**Backend (Microservicios):**
- Java 17 + Spring Boot 3
- Spring Cloud Gateway y Eureka Server para el enrutamiento y registro de servicios.
- Spring Security + JWT
- Spring Data JPA + Hibernate
- Bases de datos PostgreSQL (una por microservicio)

**Frontend:**
- React.js + Vite
- React Router DOM
- React Bootstrap (UI dinámica y responsive)
- Axios (Integración de API)

**Infraestructura y Despliegue:**
- Docker y Docker Compose (Entornos locales)
- Azure Container Apps y Azure Container Registry (Entornos de producción)
- GitHub Actions (CI/CD)

---

## 🛠️ Guía de Instalación

Para ejecutar el proyecto en local, necesitarás tener instalado **Docker**, **Docker Compose** y **Node.js**.

1. **Clonar el repositorio:**
   ```bash
   git clone <tu-repositorio>
   cd proyecto_TFG
   ```

2. **Levantar la base de datos y la infraestructura backend:**
   ```bash
   cd backend
   docker-compose up -d
   ```
   *Nota: Esto iniciará todas las bases de datos y los microservicios en sus respectivos contenedores.*

3. **Iniciar el Frontend:**
   Abre una nueva terminal:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

4. **Acceder a la aplicación:**
   - **Local:** Abre tu navegador en `http://localhost:5173`.
   - **Producción:** La aplicación está desplegada y disponible en `https://frontend.happyrock-6898a204.spaincentral.azurecontainerapps.io/login`.

---

## 🎮 Guía de Uso

1. **Registro e Inicio de Sesión**: Crea una nueva cuenta de usuario o inicia sesión con credenciales de administrador.
2. **Tu Primer Club**: Al entrar, se te pedirá crear o unirte a un club. Escoge un nombre y la región de tu equipo.
3. **Explorar el Mercado**: Visita la sección de "Mercado" para visualizar agentes libres y empezar a pujar por los mejores jugadores usando tu presupuesto inicial.
4. **Competir**: Únete a una liga mediante un enlace de invitación o crea la tuya propia. Revisa el calendario de partidos y asegura que tus jugadores están listos para competir.

---

## 🔗 Enlaces de Interés

- **Enlace a la documentación**: *(Por añadir)*
- **Enlace al diseño en Figma**: *(Por añadir)*

---

## 🏁 Conclusión

Clash Manager representa la culminación de los estudios de Desarrollo de Aplicaciones Web, integrando de manera exitosa múltiples tecnologías modernas en una arquitectura robusta. A pesar de los desafíos técnicos asociados a los sistemas distribuidos y la infraestructura en la nube, el resultado es una plataforma funcional, escalable y con un gran potencial de expansión.

---

## 🤝 Contribuciones y Licencias

*(Por añadir: Contribuciones, agradecimientos y referencias)*

Este proyecto se distribuye bajo la licencia **MIT**. Puedes consultar el archivo `LICENSE` para más detalles.

---

## 📬 Contacto

Para cualquier consulta relacionada con el desarrollo de este proyecto, puedes contactarme en:

✉️ **Email:** pablogarpa16@gmail.com
