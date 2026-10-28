UPDATE users
SET role = 'ADMIN'
WHERE email = LOWER(:'email');

SELECT id, name, email, role FROM users WHERE email = LOWER(:'email');
