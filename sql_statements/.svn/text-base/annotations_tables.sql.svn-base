DROP TABLE IF EXISTS annotations, annotations_changed;

CREATE TABLE annotations (
    id INT NOT NULL AUTO_INCREMENT, 
    project VARCHAR(100),
    module VARCHAR(100),
    filename VARCHAR(255),
    revision VARCHAR(50),
    fileid INT DEFAULT 0,
    annotation_container CHAR(255), 
    annotation_property CHAR(255),
    annotation_type CHAR(255), 
    PRIMARY KEY (id)
);
