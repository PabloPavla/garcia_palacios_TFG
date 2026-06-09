# 🏆 Clash Manager: El Manager Definitivo de LoL Esports
**Proyecto de Fin de Ciclo (DAW) - Desarrollo de Aplicaciones Web**  
**Autor:** Pablo García Palacios

![League of Legends](https://img.shields.io/badge/League_of_Legends-151A22?style=for-the-badge&logo=league-of-legends&logoColor=C89B3C)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Azure](https://img.shields.io/badge/Azure-0089D6?style=for-the-badge&logo=microsoft-azure&logoColor=white)

---

## 📑 Índice
1. [Introducción](#-introducción)
2. [Funcionalidades y Tecnologías](#-funcionalidades-y-tecnologías)
3. [Guía de Instalación](#-guía-de-instalación)
4. [Guía de Uso](#-guía-de-uso)
5. [Enlaces de Interés](#-enlaces-de-interés)
6. [Conclusión](#-conclusión)
7. [Contribuciones y Referencias](#-contribuciones-y-referencias)
8. [Licencias](#-licencias)
9. [Contacto](#-contacto)

---

## 🚀 Introducción

**Descripción del Proyecto**
Clash Manager es una aplicación web integral diseñada para la gestión de clubes de esports, fuertemente centrada en el ecosistema de *League of Legends*. Permite a los usuarios crear clubes, competir en ligas privadas, fichar jugadores en el mercado, realizar ofertas a otros clubes y gestionar presupuestos mediante la moneda virtual *Riot Points*. 

**Justificación**
El sector de los esports y el gaming competitivo carece de herramientas ligeras, accesibles y orientadas a grupos de amigos o comunidades pequeñas para simular la gestión de clubes y organizar competiciones propias de manera estructurada y automatizada. Clash Manager llena ese vacío.

**Objetivos**
- Proveer un entorno robusto basado en microservicios y escalable.
- Simular un mercado de fichajes realista con presupuestos (RP) y restricciones de roster.
- Ofrecer un sistema automatizado de calendarios y clasificación de ligas.
- Brindar una experiencia visual de primera clase mediante *Glassmorphism* y componentes inmersivos.

**Motivación**
*(Por añadir)*

---

## ⚙️ Funcionalidades y Tecnologías

### Funcionalidades Principales
- **Sistema de Ligas y Amigos:** Creación de ligas privadas, invitación mediante enlaces y sistema de peticiones de amistad entre usuarios.
- **Mercado de Fichajes:** Fichaje de agentes libres o negociaciones directas con otros managers usando Riot Points (RP).
- **Control de Roster:** Algoritmos a nivel de BD y aplicación que garantizan plantillas válidas (máx. 2 jugadores por rol: TOP, JUNGLE, MID, ADC, SUPPORT).
- **Dashboard y Estadísticas:** Panel central con información económica, últimos partidos, clasificación e invitaciones pendientes.
- **Gestión Automatizada de Partidos:** Al finalizar un partido, los equipos ganan/pierden RP y su posición en la clasificación se actualiza automáticamente a través de Triggers en la BD.

### Stack Tecnológico
- **Frontend:** React 18, React Router v6, Bootstrap 5 (Modo Oscuro Glassmorphism), Axios, SweetAlert2.
- **Backend (Microservicios):** Java 21, Spring Boot 3, Spring Cloud Gateway, Eureka Server, Spring Security (JWT).
- **Microservicios Implementados:** `api-gateway`, `auth-service`, `club-service`, `league-service`, `transfer-service`.
- **Bases de Datos:** MySQL 8, Flyway (Control de versiones y migraciones).
- **Despliegue:** Docker, Azure Container Apps, Azure Flexible Server MySQL.

---

## 🛠️ Guía de Instalación

El proyecto se encuentra totalmente contenedorizado, permitiendo un fácil despliegue local o en la nube.

### Requisitos Previos
- Docker y Docker Compose instalados.
- Node.js (Solo si deseas ejecutar el frontend nativamente).
- Git.

### Instalación Local
1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/PabloPavla/garcia_palacios_TFG.git
   cd garcia_palacios_TFG
   ```

2. **Levantar el Backend y Bases de Datos (Vía Docker Compose):**
   ```bash
   cd docker
   docker-compose up -d
   ```
   *Esto levantará las bases de datos de todos los microservicios y aplicará las migraciones SQL con Flyway.*

3. **Ejecutar Microservicios:**
   Abre el proyecto en tu IDE favorito (IntelliJ, VSCode) y lanza los servicios en el siguiente orden:
   `eureka-server` -> `api-gateway` -> `auth-service`, `club-service`, `league-service`, `transfer-service`.

4. **Levantar el Frontend:**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   La aplicación estará disponible en `http://localhost:5173`.

---

## 🎮 Guía de Uso

1. **Registro:** Crea una cuenta o pide a un amigo su enlace de invitación a una liga.
2. **Dashboard:** Crea tu club de esports, diseña su nombre y acrónimo.
3. **Fichajes:** Accede al *Mercado* para contratar jugadores libres a cambio de RP, o negocia con otros usuarios en *Traspasos*.
4. **Ligas:** Organiza partidos contra otros miembros de tu liga. Al ganar, tu equipo subirá en la clasificación y ganarás Riot Points para nuevos fichajes.
5. **Configuración:** Utiliza el panel de Perfil para cambiar tu avatar de invocador.

---

## 🔗 Enlaces de Interés

- **Documentación del Proyecto:** *(Por añadir)*
- **Diseño de Interfaz (Figma):** *(Por añadir)*

---

## 🎯 Conclusión

El desarrollo de *Clash Manager* ha supuesto un reto arquitectónico considerable al aplicar patrones avanzados como Microservicios, API Gateway y Service Discovery, demostrando la viabilidad de aplicaciones complejas con un stack tecnológico de última generación.

---

## 🤝 Contribuciones y Referencias

*(Por añadir)*

---

## 📄 Licencias

Este proyecto está bajo licencia MIT. Eres libre de utilizar y modificar el código de acuerdo a los términos de la misma. Todos los derechos sobre marcas registradas e imágenes de *League of Legends* pertenecen a Riot Games.

---

## 📬 Contacto

Si tienes alguna duda o sugerencia, no dudes en contactarme:
- **Email:** [pablogarpa16@gmail.com](mailto:pablogarpa16@gmail.com)
- **GitHub:** [PabloPavla](https://github.com/PabloPavla)
