# Users schema
 
# --- !Ups
 
CREATE TABLE album_message (
    id serial,
    html text NOT NULL,
    created timestamp with time zone NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE album_message_notification (
    id serial,
    message integer NOT NULL,
    notification integer NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE album_notification (
    id serial,
    start_datetime_display timestamp with time zone NOT NULL,
    end_datetime_display timestamp with time zone NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE album_notification_user (
    id serial,
    notification integer NOT NULL,
    user_id integer NOT NULL,
    closed boolean NOT NULL,
    PRIMARY KEY (id)
);

ALTER TABLE album_message_notification ADD CONSTRAINT album_message_notification_fk_message FOREIGN KEY (message) REFERENCES album_message (id);
ALTER TABLE album_message_notification ADD CONSTRAINT album_message_notification_fk_notification FOREIGN KEY (notification) REFERENCES album_notification (id);

ALTER TABLE album_notification_user ADD CONSTRAINT album_notification_user_fk_notification FOREIGN KEY (notification) REFERENCES album_notification (id);
ALTER TABLE album_notification_user ADD CONSTRAINT album_notification_user_fk_user FOREIGN KEY (user_id) REFERENCES album_user (id);