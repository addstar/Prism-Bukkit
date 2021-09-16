CREATE TABLE prism_actions (
    action_id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,
    action varchar(25) NOT NULL UNIQUE
);

CREATE TABLE prism_data (
    id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,
    epoch int NOT NULL,
    action_id int NOT NULL,
    player_id int NOT NULL,
    world_id int NOT NULL,
    x int NOT NULL,
    y int NOT NULL,
    z int NOT NULL,
    block_id int DEFAULT NULL,
    block_subid int DEFAULT NULL,
    old_block_id int DEFAULT NULL,
    old_block_subid int DEFAULT NULL
);

CREATE INDEX epoch ON prism_data (epoch);
CREATE INDEX location ON prism_data (world_id, x, z, y, action_id);
CREATE INDEX player ON prism_data (player_id);

CREATE TABLE prism_data_extra (
    extra_id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,
    data_id int NOT NULL CONSTRAINT prism_data_extra_ibfk_1 REFERENCES prism_data (id) ON DELETE CASCADE ON UPDATE NO ACTION,
    data long varchar,
    te_data long varchar
);

CREATE TABLE prism_meta (
    id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,
    k varchar(25) NOT NULL,
    v varchar(255) NOT NULL
);

CREATE TABLE prism_players (
    player_id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,
    player varchar(255) NOT NULL UNIQUE,
    player_uuid char(16) NOT NULL UNIQUE
);

CREATE TABLE prism_worlds (
    world_id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,
    world varchar(255) NOT NULL UNIQUE
);

CREATE TABLE prism_id_map (
    material varchar(63) NOT NULL,
    state varchar(255) NOT NULL,
    block_id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    block_subid int NOT NULL DEFAULT 0,
    PRIMARY KEY (material, state),
    UNIQUE (block_id, block_subid)
);