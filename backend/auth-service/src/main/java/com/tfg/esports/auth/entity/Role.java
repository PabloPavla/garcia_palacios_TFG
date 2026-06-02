package com.tfg.esports.auth.entity;

/**
 * Enumeración de roles de usuario en la aplicación.
 *
 * <ul>
 *   <li>{@link #ROLE_ADMIN} – Administrador de la liga. Puede gestionar
 *       todas las ligas, partidos y jugadores.</li>
 *   <li>{@link #ROLE_OWNER} – Propietario de un club. Puede gestionar
 *       su propio club y realizar fichajes.</li>
 * </ul>
 *
 * @author Pablo García Palacios
 */
public enum Role {

    /** Administrador del sistema con acceso completo */
    ROLE_ADMIN,

    /** Propietario de un club de esports */
    ROLE_OWNER
}
