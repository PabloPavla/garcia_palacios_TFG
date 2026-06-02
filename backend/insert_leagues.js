const mysql = require('mysql2/promise');

async function insertLeagues() {
  const connection = await mysql.createConnection({
    host: 'localhost',
    user: 'tfg_user',
    password: 'tfg_pass',
    database: 'league_db'
  });

  const [rows] = await connection.execute('SELECT * FROM leagues');
  console.log('Current leagues:', rows);

  if (rows.length === 0) {
      await connection.execute(`
        INSERT INTO leagues (name, season, start_date, active, visibility, creator_user_id) 
        VALUES ('Liga Mundial', '2026', '2026-01-01', 1, 'PUBLIC', 1),
               ('Liga Regional', '2026', '2026-01-01', 1, 'PUBLIC', 1)
      `);
      console.log('Inserted 2 public leagues.');
  }

  await connection.end();
}

insertLeagues().catch(console.error);
