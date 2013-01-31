DROP TABLE IF EXISTS revisions;

CREATE TABLE revisions (
    project VARCHAR(100),
    FileID INT NOT NULL AUTO_INCREMENT,
    DateTime DATETIME NOT NULL,
    DateTimeStr VARCHAR(50) NOT NULL,
    userID VARCHAR(100),
    FileName VARCHAR(300),
    Revision VARCHAR(50),
    sourcefile tinyint(1) ,
    logmessage text, 
    module VARCHAR(200),
    diffUrl VARCHAR(400),
    transactionid INT,
    state VARCHAR(50),
    PRIMARY KEY (FileID)
);