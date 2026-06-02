-- =============================================================
-- Script de inicialización de bases de datos para el TFG
-- Se ejecuta automáticamente al arrancar el contenedor MySQL
-- =============================================================

-- Crear base de datos para Auth Service
CREATE DATABASE IF NOT EXISTS auth_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Crear base de datos para Club Service
CREATE DATABASE IF NOT EXISTS club_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Crear base de datos para Transfer Service
CREATE DATABASE IF NOT EXISTS transfer_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Crear base de datos para League Service
CREATE DATABASE IF NOT EXISTS league_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Otorgar permisos completos al usuario tfg_user sobre cada BD
GRANT ALL PRIVILEGES ON auth_db.*     TO 'tfg_user'@'%';
GRANT ALL PRIVILEGES ON club_db.*     TO 'tfg_user'@'%';
GRANT ALL PRIVILEGES ON transfer_db.* TO 'tfg_user'@'%';
GRANT ALL PRIVILEGES ON league_db.*   TO 'tfg_user'@'%';

FLUSH PRIVILEGES;
