# Users schema
 
# --- !Ups

CREATE TABLE album_media_type (
    id serial,
    media_type varchar(25) UNIQUE NOT NULL,
    PRIMARY KEY (id)
);
INSERT INTO album_media_type (id, media_type) values('1', 'photo');
INSERT INTO album_media_type (id, media_type) values('2', 'video');

ALTER TABLE album_tag DROP CONSTRAINT album_tag_photo_fk;

ALTER TABLE album_photo RENAME TO album_media;
ALTER TABLE album_tag RENAME COLUMN photo TO media;

ALTER TABLE album_media ADD COLUMN media_type integer;
UPDATE album_media set media_type = '1';
ALTER TABLE album_media ALTER COLUMN media_type SET NOT NULL;

ALTER TABLE album_tag ADD CONSTRAINT album_tag_media_fk FOREIGN KEY (media) REFERENCES album_media (id);
ALTER TABLE album_media ADD CONSTRAINT album_media_type_fk FOREIGN KEY (media_type) REFERENCES album_media_type (id);
