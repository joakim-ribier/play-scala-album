# Users schema
 
# --- !Ups
 
CREATE TABLE album_user_email (
    id serial,
    user_id integer UNIQUE NOT NULL,
    email varchar(255) UNIQUE NOT NULL,
    PRIMARY KEY (id)
);

ALTER TABLE album_user_email ADD CONSTRAINT album_user_email_user_fk FOREIGN KEY (user_id) REFERENCES album_user (id);