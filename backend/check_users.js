const mysql = require('mysql2/promise');

async function checkUsers() {
  try {
    const connection = await mysql.createConnection({
      host: 'localhost',
      user: 'tfg_user',
      password: 'tfg_pass',
      database: 'auth_db'
    });

    const [rows, fields] = await connection.execute('SELECT id, username, email, role, active FROM users');
    console.log(JSON.stringify(rows, null, 2));
    await connection.end();
  } catch (err) {
    console.error('Error:', err.message);
  }
}

checkUsers();
