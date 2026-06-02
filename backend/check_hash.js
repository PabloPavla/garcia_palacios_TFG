const bcrypt = require('bcryptjs');

const hash = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTTyGMu9bXi';
const password = 'Admin1234!';

bcrypt.compare(password, hash).then(res => console.log('Match:', res));
