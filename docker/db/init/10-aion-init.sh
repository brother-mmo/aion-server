#!/bin/sh
set -eu

ROOT_CMD="mariadb -uroot -p${MARIADB_ROOT_PASSWORD}"

${ROOT_CMD} <<SQL
CREATE DATABASE IF NOT EXISTS aion_ls CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS aion_gs CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS aion_cs CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS '${AION_DB_USER}'@'%' IDENTIFIED BY '${AION_DB_PASSWORD}';
GRANT ALL PRIVILEGES ON aion_ls.* TO '${AION_DB_USER}'@'%';
GRANT ALL PRIVILEGES ON aion_gs.* TO '${AION_DB_USER}'@'%';
GRANT ALL PRIVILEGES ON aion_cs.* TO '${AION_DB_USER}'@'%';
FLUSH PRIVILEGES;
SQL

${ROOT_CMD} aion_ls < /srv/aion/sql/login/aion_ls.sql
${ROOT_CMD} aion_gs < /srv/aion/sql/game/aion_gs.sql
${ROOT_CMD} aion_cs < /srv/aion/sql/chat/aion_cs.sql

${ROOT_CMD} aion_ls <<SQL
INSERT INTO gameservers (id, mask, password)
VALUES (${AION_GS_ID}, '${AION_GS_MASK}', '${AION_GS_PASSWORD}')
ON DUPLICATE KEY UPDATE
  mask = VALUES(mask),
  password = VALUES(password);
SQL
