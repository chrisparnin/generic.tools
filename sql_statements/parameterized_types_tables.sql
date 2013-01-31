DROP TABLE IF EXISTS parameterized_types, parameterized_types_changed;

CREATE TABLE parameterized_types (
    id INT NOT NULL AUTO_INCREMENT,
    project VARCHAR(100),
    module VARCHAR(100),
    filename VARCHAR(255),
    revision VARCHAR(50),
    fileid int DEFAULT 0,
    container CHAR(255), 
    class_type CHAR(255), 
    type_args CHAR(255),
    count INT DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE TABLE parameterized_types_changed (
    id INT NOT NULL AUTO_INCREMENT,
    project VARCHAR(100),
    module VARCHAR(100),
    filename VARCHAR(255),
    revision VARCHAR(50),
    fileid int DEFAULT 0,
    added BOOLEAN,
    container CHAR(255), 
    class_type CHAR(255), 
    type_args CHAR(255),
    PRIMARY KEY (id)
);
