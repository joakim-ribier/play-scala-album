# Users schema
 
# --- !Ups
 
CREATE TABLE album_user (
    id serial,
    login varchar(255) UNIQUE NOT NULL,
    password varchar(255) NOT NULL,
    created timestamp with time zone NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE album_photo (
    id serial,
    filename varchar(255) UNIQUE NOT NULL,
    title varchar(30) NOT NULL,
    description text,
 		public boolean NOT NULL,    
    created timestamp with time zone NOT NULL,
    PRIMARY KEY (id)
);
CREATE TABLE album_tag (
    id serial,
		photo integer NOT NULL,
		tag varchar(255) NOT NULL,
    PRIMARY KEY (id)
);

ALTER TABLE album_tag ADD CONSTRAINT album_tag_photo_fk FOREIGN KEY (photo) REFERENCES album_photo (id);