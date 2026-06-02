-- ============================================================
-- V1__init_auth.sql
-- Migración inicial del Auth Service
-- Crea las tablas users y refresh_tokens con índices y constraints
-- ============================================================

-- Tabla principal de usuarios
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL,
    email       VARCHAR(100) NOT NULL,
    password    VARCHAR(255) NOT NULL,  -- Hash BCrypt
    role        ENUM('ROLE_ADMIN', 'ROLE_OWNER') NOT NULL DEFAULT 'ROLE_OWNER',
    active      TINYINT(1) NOT NULL DEFAULT 1,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints de unicidad
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email    UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices para búsqueda rápida por email y username
CREATE INDEX idx_users_email    ON users (email);
CREATE INDEX idx_users_username ON users (username);

-- ──────────────────────────────────────────────────────────────
-- Tabla de refresh tokens (sesiones activas)
-- ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    token       VARCHAR(255) NOT NULL,
    expires_at  TIMESTAMP NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Si el usuario se elimina, se eliminan también sus tokens (CASCADE)
    CONSTRAINT fk_rt_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,

    CONSTRAINT uq_refresh_token UNIQUE (token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índice sobre el token para búsqueda O(log n)
CREATE INDEX idx_refresh_token ON refresh_tokens (token);

-- ──────────────────────────────────────────────────────────────
-- Insertar usuario ADMIN por defecto (contraseña: Admin1234!)
-- Hash BCrypt de "Admin1234!" generado con coste 10
-- ──────────────────────────────────────────────────────────────
INSERT INTO users (username, email, password, role, active)
VALUES (
    'admin',
    'admin@esports.tfg',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTTyGMu9bXi',
    'ROLE_ADMIN',
    1
);
