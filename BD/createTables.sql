DROP TABLE IF EXISTS payment_credit;
DROP TABLE IF EXISTS currency;
DROP TABLE IF EXISTS client;
DROP TABLE IF EXISTS manager;

CREATE TABLE client (
                        id		 BIGINT,
                        total_payments	 DOUBLE PRECISION NOT NULL,
                        total_credits	 BOOL,
                        last_payment_date DATE NOT NULL,
                        manager_id	 BIGINT NOT NULL,
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
