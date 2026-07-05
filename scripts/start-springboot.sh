#!/bin/bash
# Ensures a JDK is available and the Spring Boot jar is built, then runs it.
# Used by supervisor. The jar is built once and cached under /app (persistent).
set -e

JAR=/app/backend-springboot/target/cityvibe-backend-1.0.0.jar

# Install JDK + Maven if missing (apt packages are ephemeral in this env).
if ! command -v java >/dev/null 2>&1; then
  export DEBIAN_FRONTEND=noninteractive
  exec 9>/tmp/apt.lock
  flock 9
  dpkg --configure -a || true
  apt-get update -qq
  apt-get install -y -qq openjdk-17-jdk-headless maven
  exec 9>&-
fi

# Build the jar if it does not exist yet.
if [ ! -f "$JAR" ]; then
  command -v mvn >/dev/null 2>&1 || { export DEBIAN_FRONTEND=noninteractive; apt-get install -y -qq maven; }
  (cd /app/backend-springboot && mvn -q -DskipTests clean package)
fi

# Wait for MySQL to accept connections before starting.
for i in $(seq 1 60); do
  (echo > /dev/tcp/127.0.0.1/3306) >/dev/null 2>&1 && break
  sleep 2
done

exec java -jar "$JAR" --server.port=8090
