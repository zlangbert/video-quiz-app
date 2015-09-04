# --- !Ups

CREATE TABLE user (
  id            VARCHAR(24) NOT NULL PRIMARY KEY,
  provider      VARCHAR(24) NOT NULL,
  email         VARCHAR(64) NOT NULL,
  username      VARCHAR(64) NOT NULL,
  first_name    VARCHAR(64),
  last_name     VARCHAR(64),
  avatar_url    VARCHAR(256),
  is_instructor BOOL DEFAULT FALSE
);

CREATE TABLE course (
  id       INT        NOT NULL PRIMARY KEY AUTO_INCREMENT,
  code     VARCHAR(8) NOT NULL,
  semester VARCHAR(3),
  section  INT
);

CREATE TABLE quiz (
  id          INT  NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name        VARCHAR(128),
  description TEXT NOT NULL
);

CREATE TABLE question (
  id     INT  NOT NULL PRIMARY KEY AUTO_INCREMENT,
  type   INT  NOT NULL,
  prompt TEXT NOT NULL
);

CREATE TABLE quiz_question (
  quiz_id     INT,
  question_id INT,
  FOREIGN KEY (quiz_id) REFERENCES quiz (id),
  FOREIGN KEY (question_id) REFERENCES question (id)
);

CREATE TABLE answer (
  user_id     VARCHAR(24),
  question_id INT,
  FOREIGN KEY (user_id) REFERENCES user (id),
  FOREIGN KEY (question_id) REFERENCES question (id)
);

# --- !Downs

DROP TABLE IF EXISTS answer;
DROP TABLE IF EXISTS quiz_question;
DROP TABLE IF EXISTS question;
DROP TABLE IF EXISTS quiz;
DROP TABLE IF EXISTS course;
DROP TABLE IF EXISTS user;