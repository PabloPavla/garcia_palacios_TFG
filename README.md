# 🎮 LoL Esports Club Manager – TFG

Aplicación web de gestión de clubes de esports del juego **League of Legends**, desarrollada como Trabajo de Fin de Grado.

## 🚀 Tecnologías

| Capa | Tecnología |
|------|-----------|
| **Frontend** | React 18 + Vite + Bootstrap 5 + React-Bootstrap |
| **Backend** | Spring Boot 3 (Java 21) |
| **Auth** | Spring Security + JWT |
| **Base de datos** | PostgreSQL 16 (con Flyway) |
| **Orquestación** | Docker + Docker Compose |
| **Despliegue** | Railway (backend) + Vercel (frontend) |

## 📁 Estructura del proyecto

```
proyecto_TFG/
├── frontend/          # Aplicación React
├── backend/
│   ├── api-gateway/   # Spring Cloud Gateway
│   ├── auth-service/  # Autenticación y autorización JWT
│   ├── club-service/  # Gestión de clubes y jugadores
│   ├── transfer-service/ # Fichajes y transferencias
│   └── league-service/   # Liga, jornadas y clasificación
└── docker/            # Docker Compose y configuración
```

## ⚙️ Arrancar en local

### Requisitos previos
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Node.js 20+](https://nodejs.org/)
- [Java 21+](https://adoptium.net/)

### 1. Base de datos y servicios backend
```bash
cd docker
docker-compose up -d
```

### 2. Frontend
```bash
cd frontend
npm install
npm run dev
```

La aplicación estará disponible en `http://localhost:5173`

## 🔗 Demo en producción
> 🔗 [Enlace al despliegue] *(disponible próximamente)*

## 👨‍💻 Autor
**Pablo García Palacios** – TFG  
Universidad – Curso 2025/2026
