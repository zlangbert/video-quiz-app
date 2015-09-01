# --- !Ups

CREATE TABLE user (
  id VARCHAR (24) NOT NULL PRIMARY KEY,
  provider VARCHAR(24) NOT NULL,
  email VARCHAR(64) NOT NULL,
  first_name VARCHAR(64),
  last_name VARCHAR (64)
);

# --- !Downs

DROP TABLE user;