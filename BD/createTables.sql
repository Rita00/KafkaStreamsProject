CREATE TABLE client (
                        id	 BIGINT,
                        username	 VARCHAR(512) UNIQUE NOT NULL,
                        password	 VARCHAR(512) NOT NULL,
                        manager_id BIGINT NOT NULL,
                        PRIMARY KEY(id)
);

CREATE TABLE manager (
                         id	 BIGINT,
                         username VARCHAR(512) UNIQUE NOT NULL,
                         PRIMARY KEY(id)
);

CREATE TABLE currency (
                          id		 BIGINT,
                          name		 VARCHAR(512) UNIQUE NOT NULL,
                          exchange_rate DOUBLE PRECISION NOT NULL,
                          PRIMARY KEY(id)
);

ALTER TABLE client ADD CONSTRAINT client_fk1 FOREIGN KEY (manager_id) REFERENCES manager(id);