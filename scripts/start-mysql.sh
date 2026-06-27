#!/bin/bash
# Ensures MariaDB (MySQL-compatible) is installed and running with a persistent
# datadir under /app (which survives container restarts). Used by supervisor.
set -e

DATADIR=/app/data/mysql
SOCKET=/run/mysqld/mysqld.sock

# Install server if the binary is missing (apt packages are ephemeral here).
if [ ! -x /usr/sbin/mariadbd ]; then
  export DEBIAN_FRONTEND=noninteractive
  apt-get update -qq
  apt-get install -y -qq default-mysql-server
fi

mkdir -p "$DATADIR" /run/mysqld
chown -R mysql:mysql "$DATADIR" /run/mysqld || true

# Initialise the persistent datadir on first run.
if [ ! -d "$DATADIR/mysql" ]; then
  mariadb-install-db --user=mysql --datadir="$DATADIR" >/dev/null 2>&1
fi

# Start the server in the background to ensure DB/user exist, then hand off.
mariadbd --user=mysql --datadir="$DATADIR" --socket="$SOCKET" \
         --bind-address=127.0.0.1 --port=3306 &
MPID=$!

for i in $(seq 1 40); do
  mysqladmin --socket="$SOCKET" ping >/dev/null 2>&1 && break
  sleep 1
done

mysql --socket="$SOCKET" -e "CREATE DATABASE IF NOT EXISTS cityvibe CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS 'cityvibe'@'localhost' IDENTIFIED BY 'cityvibe123';
CREATE USER IF NOT EXISTS 'cityvibe'@'127.0.0.1' IDENTIFIED BY 'cityvibe123';
GRANT ALL PRIVILEGES ON cityvibe.* TO 'cityvibe'@'localhost';
GRANT ALL PRIVILEGES ON cityvibe.* TO 'cityvibe'@'127.0.0.1';
FLUSH PRIVILEGES;" 2>/dev/null || true

# Keep the running server in the foreground for supervisor.
wait $MPID
