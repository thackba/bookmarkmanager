# tables for the BookmarkManager

# --- !Ups

CREATE SEQUENCE seq_account_id;
CREATE SEQUENCE seq_bookmark_id;

CREATE TABLE account (
  id BIGINT NOT NULL DEFAULT nextval('seq_account_id'),
  username VARCHAR(64) NOT NULL,
  username_low VARCHAR(64) NOT NULL,
  identifier VARCHAR(32) NOT NULL,
  email VARCHAR(64) NOT NULL,
  password VARCHAR(32) NULL,
  salt VARCHAR(32) NOT NULL,
  forename VARCHAR(64) NOT NULL,
  surname VARCHAR(64) NULL,
  activation VARCHAR(32) NULL,
  createdate DATETIME NOT NULL,
  lastlogin DATETIME NULL
);

CREATE TABLE bookmark (
    id BIGINT NOT NULL DEFAULT nextval('seq_bookmark_id'),
    owner BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    url VARCHAR(200) NOT NULL,
    description TEXT
);

# --- !Downs

DROP TABLE bookmark;
DROP TABLE account;

DROP SEQUENCE seq_bookmark_id;
DROP SEQUENCE seq_account_id;

