# Users schema
 
# --- !Ups

CREATE TABLE album_media_post (
    id serial,
    album_media integer NOT NULL,
	created timestamp with time zone NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE album_media_post_message (
    id serial,
    album_media_post integer NOT NULL,
    album_user integer NOT NULL,
    message text NOT NULL,
    created timestamp with time zone NOT NULL,
    PRIMARY KEY (id)
);

ALTER TABLE album_media_post ADD CONSTRAINT album_media_post_media_fk FOREIGN KEY (album_media) REFERENCES album_media (id);
ALTER TABLE album_media_post_message ADD CONSTRAINT album_media_post_message_post_fk FOREIGN KEY (album_media_post) REFERENCES album_media_post (id);
ALTER TABLE album_media_post_message ADD CONSTRAINT album_media_post_message_user_fk FOREIGN KEY (album_user) REFERENCES album_user (id);