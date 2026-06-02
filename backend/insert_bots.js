const mysql = require('mysql2/promise');

async function insertBots() {
    const clubConn = await mysql.createConnection({
        host: 'localhost',
        user: 'tfg_user',
        password: 'tfg_pass',
        database: 'club_db'
    });

    const leagueConn = await mysql.createConnection({
        host: 'localhost',
        user: 'tfg_user',
        password: 'tfg_pass',
        database: 'league_db'
    });

    try {
        console.log("Inserting 3 bot clubs into club_db...");
        // Delete previous if any
        await clubConn.execute(`DELETE FROM clubs WHERE owner_id IN (901, 902, 903)`);
        await leagueConn.execute(`DELETE FROM league_clubs WHERE club_id IN (901, 902, 903)`);

        // Insert clubs (forcing IDs 901, 902, 903)
        await clubConn.execute(`
            INSERT INTO clubs (id, name, acronym, division, riot_points, owner_id)
            VALUES 
            (901, 'Bot Team Alpha', 'BOTA', 'BRONZE', 10000, 901),
            (902, 'Bot Team Beta', 'BOTB', 'BRONZE', 10000, 902),
            (903, 'Bot Team Gamma', 'BOTG', 'BRONZE', 10000, 903)
        `);

        console.log("Enrolling bot clubs into league_db (League ID 2)...");
        await leagueConn.execute(`
            INSERT INTO league_clubs (league_id, club_id, points, wins, draws, losses, goals_for, goals_against)
            VALUES 
            (2, 901, 0, 0, 0, 0, 0, 0),
            (2, 902, 0, 0, 0, 0, 0, 0),
            (2, 903, 0, 0, 0, 0, 0, 0)
        `);

        console.log("Success! 3 bots inserted into League 2.");
    } catch (err) {
        console.error(err);
    } finally {
        await clubConn.end();
        await leagueConn.end();
    }
}

insertBots();
