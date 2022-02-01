#!/bin/bash
set -e
export PGPASSWORD=$POSTGRES_PASSWORD;
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
  CREATE USER $APP_DB_USER WITH PASSWORD '$APP_DB_PASS';
  CREATE DATABASE $APP_DB_NAME;
  GRANT ALL PRIVILEGES ON DATABASE $APP_DB_NAME TO $APP_DB_USER;
#  \connect $APP_DB_NAME $APP_DB_USER
#  BEGIN;
#    CREATE TABLE IF NOT EXISTS cart (
#                                id uuid NOT NULL,
#                                state varchar(255) NULL,
#                                CONSTRAINT cart_pkey PRIMARY KEY (id)
#	);
#	CREATE TABLE IF NOT EXISTS product (
#	                                    id uuid NOT NULL,
#                                      "name" varchar(255) NULL,
#                                      sku varchar(255) NULL,
#                                      description varchar(255) NULL,
#                                      has_discount bool NULL,
#                                      price float8 NULL,
#                                      CONSTRAINT product_pkey PRIMARY KEY (id)
#	);
#	CREATE TABLE IF NOT EXISTS cart_item(
#	                                      id int8 NOT NULL,
#                                        quantity int4 NULL,
#                                        product_id uuid NULL,
#                                        cart_id uuid NULL,
#                                        CONSTRAINT cart_item_pkey PRIMARY KEY (id),
#                                        CONSTRAINT cart_item_product_id_cart_id_unique UNIQUE (product_id, cart_id)
#	);
#	ALTER TABLE cart_item ADD CONSTRAINT fk_cart_item__cart_id FOREIGN KEY (cart_id) REFERENCES cart(id);
#  ALTER TABLE cart_item ADD CONSTRAINT fk_cart_item__product_id FOREIGN KEY (product_id) REFERENCES product(id);
  COMMIT;
EOSQL
