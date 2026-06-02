const bcrypt = require('bcryptjs');
const mysql = require('mysql2/promise');

async function fixAdminPassword() {
  const hash = await bcrypt.hash('Admin1234!', 10);
  console.log('New hash:', hash);

  const connection = await mysql.createConnection({
    host: 'localhost',
    user: 'tfg_user',
    password: 'tfg_pass',
    database: 'auth_db'
  });

  await connection.execute('UPDATE users SET password = ? WHERE username = "admin"', [hash]);
  console.log('Admin password updated in DB!');
  await connection.end();
}

fixAdminPassword();
