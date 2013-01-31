DROP TABLE IF EXISTS rawtypes;
DROP TABLE IF EXISTS rawtypes_changed;

CREATE TABLE rawtypes (
    id INT NOT NULL AUTO_INCREMENT, 
    project VARCHAR(100),
    module VARCHAR(100),
    filename VARCHAR(255),
    revision VARCHAR(50),
    fileid INT DEFAULT 0,
    rawtype_linenumber int,
    rawtype_container CHAR(255), 
    rawtype_type CHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE rawtypes_changed (
    id INT NOT NULL AUTO_INCREMENT, 
    project VARCHAR(100),
    module VARCHAR(100),
    filename VARCHAR(255),
    revision VARCHAR(50),
    fileid INT DEFAULT 0,
    added boolean,
    container CHAR(255), 
    type CHAR(255),
    PRIMARY KEY (id)
);
