DROP TABLE IF EXISTS casts, casts_changed;

CREATE TABLE casts (
    id INT NOT NULL AUTO_INCREMENT, 
    project VARCHAR(100),
    module VARCHAR(100),
    filename VARCHAR(255),
    revision VARCHAR(50),
    fileid INT DEFAULT 0,
    container CHAR(255), 
    type CHAR(255), 
    count INT default 1,
    PRIMARY KEY (id)
);

CREATE TABLE casts_changed (
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
