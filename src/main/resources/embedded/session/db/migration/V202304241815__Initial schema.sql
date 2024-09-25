CREATE SCHEMA IF NOT EXISTS session_service;

SET SCHEMA session_service;

CREATE TABLE IF NOT EXISTS permission
(
    id   bigint                  NOT NULL AUTO_INCREMENT,
    code VARCHAR_IGNORECASE(512) NOT NULL UNIQUE,
    CONSTRAINT permission_pk PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS session
(
    uid        varchar(255) NOT NULL,
    owner_type varchar(16)  NOT NULL,
    owner_id   varchar(255) NOT NULL,
    created_at timestamp    NOT NULL,
    CONSTRAINT session_pk PRIMARY KEY (uid)
);

CREATE TABLE IF NOT EXISTS session_permission
(
    session_uid   varchar(255) NOT NULL REFERENCES session (uid),
    permission_id INTEGER      NOT NULL REFERENCES permission (id)
);

CREATE TABLE IF NOT EXISTS session_snapshot
(
    uid               varchar(255) NOT NULL,
    version           INTEGER      NOT NULL,
    snapshot          BLOB         NOT NULL,
    snapshot_ser_type varchar(16)  NOT NULL,
    created_at        timestamp    NOT NULL,
    CONSTRAINT session_snapshot_pk PRIMARY KEY (uid, version),
    FOREIGN KEY (uid) REFERENCES session (uid)
);