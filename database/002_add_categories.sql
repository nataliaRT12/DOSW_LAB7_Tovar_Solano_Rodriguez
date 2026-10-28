CREATE TABLE categories (

    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(40) NOT NULL,

    color VARCHAR(10) NOT NULL,

    CONSTRAINT uq_category_name UNIQUE (name),

    CONSTRAINT chk_category_color
        CHECK (color IN ('BLUE', 'GREEN', 'ORANGE', 'PURPLE'))
);

ALTER TABLE tasks
    ADD COLUMN category_id BIGINT NULL;

ALTER TABLE tasks
    ADD CONSTRAINT fk_task_category
        FOREIGN KEY (category_id) REFERENCES categories (id)
        ON DELETE RESTRICT;
