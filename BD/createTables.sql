DROP TABLE IF EXISTS payment_credit;
DROP TABLE IF EXISTS currency;
DROP TABLE IF EXISTS client;
DROP TABLE IF EXISTS manager;

create table client(
                          client_id INT NOT NULL,
                          client_name VARCHAR(40) NOT NULL,
                          PRIMARY KEY ( client_id )
);

CREATE TABLE currency (
                          id		 INT NOT NULL,
                          name		 VARCHAR(512) UNIQUE NOT NULL,
                          exchange_rate DOUBLE PRECISION NOT NULL,
                          PRIMARY KEY(id)
);
