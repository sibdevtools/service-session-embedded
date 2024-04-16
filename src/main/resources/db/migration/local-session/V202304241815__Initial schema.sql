CREATE TABLE IF NOT EXISTS permission
(
    id   INTEGER PRIMARY KEY AUTOINCREMENT,
    code varchar(255) NOT NULL,
    UNIQUE (code COLLATE NOCASE)
);

CREATE TABLE IF NOT EXISTS session
(
    uid        varchar(255) NOT NULL PRIMARY KEY,
    owner_type varchar(16)  NOT NULL,
    owner_id   varchar(255) NOT NULL,
    created_at timestamp    NOT NULL
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
    PRIMARY KEY (uid, version),
    FOREIGN KEY (uid) REFERENCES session (uid)
);