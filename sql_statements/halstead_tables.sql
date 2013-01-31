DROP TABLE IF EXISTS halstead;

CREATE TABLE halstead (
    id INT NOT NULL AUTO_INCREMENT, 
    project VARCHAR(100),
    module VARCHAR(100),
    filename VARCHAR(255),
    revision VARCHAR(50),
    fileid INT DEFAULT 0,
    distinct_operands INT, 
    distinct_operators INT, 
    total_operands INT,
    total_operators INT,
    PRIMARY KEY (id)
);

