const bcrypt = require('bcryptjs');
const mysql = require('mysql2/promise');

async function fixTestUserPassword() {
  const hash = await bcrypt.hash('123456', 10);

  const connection = await mysql.createConnection({
    host: 'localhost',
    user: 'tfg_user',
    password: 'tfg_pass',
    database: 'auth_db'
  });

  await connection.execute('UPDATE users SET password = ? WHERE username = "testuser"', [hash]);
  console.log('Testuser password updated to 123456!');
  await connection.end();
}

fixTestUserPassword();
