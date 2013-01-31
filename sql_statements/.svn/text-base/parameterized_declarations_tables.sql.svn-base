DROP TABLE IF EXISTS parameterized_declarations, parameterized_declarations_changed;

CREATE TABLE parameterized_declarations (
    id INT NOT NULL AUTO_INCREMENT, 
    project VARCHAR(100),
    module VARCHAR(100),
    filename VARCHAR(255),
    revision VARCHAR(50),
    fileid INT DEFAULT 0,
    kind CHAR(255), 
    class_type CHAR(255),
    type_args CHAR(255), 
    PRIMARY KEY (id)
);

CREATE TABLE parameterized_declarations_changed (
    id INT NOT NULL AUTO_INCREMENT, 
    project VARCHAR(100),
    module VARCHAR(100),
    filename VARCHAR(255),
    revision VARCHAR(50),
    fileid INT DEFAULT 0,
    added BOOLEAN,
    kind CHAR(255), 
    class_type CHAR(255),
    type_args CHAR(255), 
    PRIMARY KEY (id)
);
