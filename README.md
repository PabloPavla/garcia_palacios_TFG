# Clash Manager (TFG - LoL Esports Club Manager)

![League of Legends](https://img.shields.io/badge/League_of_Legends-151A22?style=for-the-badge&logo=league-of-legends&logoColor=C89B3C)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

**Trabajo de Fin de Grado (TFG) - Pablo García Palacios**

Aplicación web integral para la gestión de clubes de esports centrada en *League of Legends*. Desarrollada utilizando una robusta arquitectura de microservicios en el backend y una interfaz moderna *mobile-first* en el frontend.

---

## 🏗 Arquitectura del Sistema

El proyecto sigue una arquitectura de **microservicios**, lo que garantiza alta disponibilidad, escalabilidad y un bajo acoplamiento entre los dominios de negocio:

* **`eureka-server` (Puerto 8761):** Registro y descubrimiento de servicios.
* **`api-gateway` (Puerto 8080):** Punto de entrada único. Gestiona enrutamiento, CORS y validación de seguridad (JWT).
* **`auth-service` (Puerto 8081):** Gestión de usuarios, autenticación (Spring Security) y emisión de JWT.
* **`club-service` (Puerto 8082):** Gestión de clubes, presupuestos y plantillas de jugadores.
* **`transfer-service` (Puerto 8083):** Sistema de mercado, ofertas de fichajes y transferencias entre clubes.
* **`league-service` (Puerto 8084):** Gestión de la competición, calendario de partidos y clasificación general.
* **`frontend` (React + Vite):** Interfaz gráfica de usuario con diseño premium (*Glassmorphism*, Modo Oscuro).

### Integridad de Datos (Reglas de Negocio en BD)
Se han implementado **triggers y stored procedures** en las bases de datos MySQL para garantizar la consistencia en el nivel más bajo:
- Limitación estricta de 2 jugadores por rol (`TOP`, `JUNGLE`, etc.) por club.
- Cancelación automática de ofertas paralelas al aceptar un traspaso.
- Actualización automática de la clasificación (`LeagueClub`) al registrar resultados de partidos.

---

## 🚀 Guía de Despliegue (Local)

El proyecto está preparado para desplegarse fácilmente en cualquier entorno local mediante contenedores.

### Requisitos Previos
- [Docker](https://www.docker.com/) y Docker Compose instalados.
- [Node.js](https://nodejs.org/) (Opcional, solo para desarrollo frontend local).

### Pasos para levantar el proyecto

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/PabloPavla/garcia_palacios_TFG.git
   cd garcia_palacios_TFG
   ```

2. **Levantar infraestructura (Bases de datos):**
   ```bash
   cd docker
   docker-compose up -d
   ```
   *(Esto levantará las bases de datos MySQL separadas para cada microservicio y ejecutará automáticamente las migraciones con Flyway).*

3. **Levantar Backend:**
   Al ser un proyecto Maven Multi-módulo, compila y ejecuta los servicios en este orden (desde el IDE o terminal):
   1. `eureka-server`
   2. `api-gateway`
   3. `auth-service`, `club-service`, `transfer-service`, `league-service`

4. **Levantar Frontend:**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

La aplicación estará disponible en: **`http://localhost:5173`**

---

## 📱 Acceso mediante Código QR (Exposición)

Para la presentación del TFG, el tribunal puede acceder a la aplicación desde sus teléfonos móviles (el diseño es 100% *mobile-first*). 

Si estás ejecutando el frontend en la red local de la universidad, puedes generar un QR apuntando a la IP local de tu máquina.

**¿Cómo obtener la IP?**
- En Windows (PowerShell): `ipconfig` (Busca la dirección IPv4, ej: `192.168.1.50`)
- Inicia el frontend para red local: `npm run dev -- --host`
- Entra a una web como [QRCode Monkey](https://www.qrcode-monkey.com/) y genera un QR con el texto: `http://192.168.1.50:5173`

*(Nota: En la fase de producción en Azure, el código QR apuntará directamente al dominio público generado).*

---

## 👨‍💻 Tecnologías Utilizadas

- **Frontend:** React 18, React Router v6, Bootstrap 5, React-Bootstrap, Axios.
- **Backend:** Java 21, Spring Boot 3, Spring Cloud Gateway, Spring Cloud Netflix Eureka, Spring Security, Spring Data JPA.
- **Bases de Datos:** MySQL 8, Flyway (Control de versiones de BD).
- **Herramientas:** Docker, Maven, Git.

---
*Desarrollado para el Trabajo de Fin de Grado en Ingeniería Informática.*
