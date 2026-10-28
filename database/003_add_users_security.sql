BEGIN;

CREATE TABLE users (

    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(100) NOT NULL,

    email VARCHAR(255) NOT NULL,

    password_hash VARCHAR(100) NOT NULL,

    role VARCHAR(20) NOT NULL DEFAULT 'USER',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_users_email UNIQUE (email),

    CONSTRAINT chk_users_email_lowercase
        CHECK (email = LOWER(email)),

    CONSTRAINT chk_users_role
        CHECK (role IN ('USER', 'ADMIN'))
);

INSERT INTO users (name, email, password_hash, role)
VALUES ('Tareas anteriores', 'legacy@todo.local', 'LOCKED', 'USER');

ALTER TABLE tasks
    ADD COLUMN user_id BIGINT NULL;

UPDATE tasks
SET user_id = (SELECT id FROM users WHERE email = 'legacy@todo.local')
WHERE user_id IS NULL;

ALTER TABLE tasks
    ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE tasks
    ADD CONSTRAINT fk_task_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE;

CREATE INDEX idx_tasks_user_id ON tasks (user_id);

COMMIT;
