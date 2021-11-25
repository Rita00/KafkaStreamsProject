DROP TABLE IF EXISTS client;
DROP TABLE IF EXISTS manager;
DROP TABLE IF EXISTS currency;
DROP TABLE IF EXISTS payment_credit;

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

CREATE TABLE payment_credit (
                                id		 BIGINT,
                                payment_date	 DATE NOT NULL,
                                price		 FLOAT(8) NOT NULL,
                                credit_id		 BIGINT UNIQUE NOT NULL,
                                credit_credit_date DATE NOT NULL,
                                currency_id	 BIGINT NOT NULL,
                                client_id		 BIGINT NOT NULL,
                                PRIMARY KEY(id)
);

ALTER TABLE client ADD CONSTRAINT client_fk1 FOREIGN KEY (manager_id) REFERENCES manager(id);
ALTER TABLE payment_credit ADD CONSTRAINT payment_credit_fk1 FOREIGN KEY (currency_id) REFERENCES currency(id);
ALTER TABLE payment_credit ADD CONSTRAINT payment_credit_fk2 FOREIGN KEY (client_id) REFERENCES client(id);
