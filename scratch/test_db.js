const mysql = require('mysql2/promise');

async function main() {
    try {
        const connection = await mysql.createConnection({
            host: 'mysql-clashmanager-70551.mysql.database.azure.com',
            user: 'tfg_user',
            password: 'Tfg_Password_2026!',
            port: 3306
        });

        console.log("Connected. Checking leagues...");
        const [leagues] = await connection.execute('SELECT id, name, initial_rp FROM league_db.leagues ORDER BY id DESC LIMIT 5');
        console.table(leagues);

        console.log("Checking clubs...");
        const [clubs] = await connection.execute('SELECT id, name, riot_points FROM club_db.clubs ORDER BY id DESC LIMIT 5');
        console.table(clubs);

        await connection.end();
    } catch(e) {
        console.error(e);
    }
}
main();
