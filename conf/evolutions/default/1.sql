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
  semester VARCHAR(3) NOT NULL,
  section  INT        NOT NULL
);

CREATE TABLE user_course (
  user_id   VARCHAR(24) NOT NULL,
  course_id INT         NOT NULL,
  PRIMARY KEY (user_id, course_id),
  FOREIGN KEY (user_id) REFERENCES user (id),
  FOREIGN KEY (course_id) REFERENCES course (id)
);

CREATE TABLE quiz (
  id          INT          NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name        VARCHAR(128) NOT NULL,
  description TEXT         NOT NULL
);

CREATE TABLE user_quiz (
  user_id VARCHAR(24) NOT NULL,
  quiz_id INT         NOT NULL,
  PRIMARY KEY (user_id, quiz_id),
  FOREIGN KEY (user_id) REFERENCES user (id),
  FOREIGN KEY (quiz_id) REFERENCES quiz (id)
);

CREATE TABLE course_quiz (
  course_id  INT      NOT NULL,
  quiz_id    INT      NOT NULL,
  open_time  DATETIME NOT NULL,
  close_time DATETIME NOT NULL,
  PRIMARY KEY (course_id, quiz_id),
  FOREIGN KEY (course_id) REFERENCES course (id),
  FOREIGN KEY (quiz_id) REFERENCES quiz (id)
);

CREATE TABLE question (
  id     INT  NOT NULL PRIMARY KEY AUTO_INCREMENT,
  type   INT  NOT NULL,
  prompt TEXT NOT NULL
);

CREATE TABLE quiz_question (
  quiz_id     INT NOT NULL,
  question_id INT NOT NULL,
  PRIMARY KEY (quiz_id, question_id),
  FOREIGN KEY (quiz_id) REFERENCES quiz (id),
  FOREIGN KEY (question_id) REFERENCES question (id)
);

CREATE TABLE answer (
  user_id     VARCHAR(24) NOT NULL,
  quiz_id     INT         NOT NULL,
  question_id INT         NOT NULL,
  is_correct  BOOL        NOT NULL,
  PRIMARY KEY (user_id, quiz_id, question_id),
  FOREIGN KEY (user_id) REFERENCES user (id),
  FOREIGN KEY (quiz_id) REFERENCES quiz (id),
  FOREIGN KEY (question_id) REFERENCES question (id)
);

# --- !Downs

DROP TABLE IF EXISTS answer;
DROP TABLE IF EXISTS quiz_question;
DROP TABLE IF EXISTS question;
DROP TABLE IF EXISTS user_quiz;
DROP TABLE IF EXISTS course_quiz;
DROP TABLE IF EXISTS quiz;
DROP TABLE IF EXISTS user_course;
DROP TABLE IF EXISTS course;
DROP TABLE IF EXISTS user;