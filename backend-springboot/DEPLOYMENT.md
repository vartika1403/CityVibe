# CityVibe Backend — Deployment Guide (Railway / Render)

The Spring Boot backend ships with a multi-stage `Dockerfile`, so any Docker-capable
host can run it. The app reads all config from environment variables:

| Variable      | Purpose                              | Example                                                                 |
|---------------|--------------------------------------|-------------------------------------------------------------------------|
| `PORT`        | HTTP port (injected by the platform) | `8080`                                                                  |
| `DB_URL`      | JDBC connection string               | `jdbc:mysql://host:3306/cityvibe?useSSL=true&serverTimezone=UTC`        |
| `DB_USERNAME` | MySQL user                           | `cityvibe`                                                              |
| `DB_PASSWORD` | MySQL password                       | `********`                                                              |

The database is seeded automatically on first boot (8 Bengaluru events) — no manual SQL needed.
Health check endpoint: `GET /api/health`.

---

## Option A — Railway (recommended: has managed MySQL)

1. Push this repo to GitHub (use the **"Save to GitHub"** feature in Emergent chat).
2. Go to https://railway.app → **New Project → Deploy from GitHub repo**.
3. When asked for the root directory, set it to `backend-springboot/`.
   Railway detects the `Dockerfile` and `railway.json` automatically.
4. In the same project: **+ New → Database → MySQL**. Railway provisions it and
   exposes `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, `MYSQLPASSWORD`.
5. On the backend service → **Variables**, add (using Railway variable references):
   ```
   DB_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   DB_USERNAME=${{MySQL.MYSQLUSER}}
   DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
   ```
6. Deploy. Under **Settings → Networking → Generate Domain** to get a public URL.
7. Verify: `curl https://<your-domain>/api/events` → should return 8 events.

## Option B — Render (bring your own MySQL)

Render does not offer managed MySQL (only PostgreSQL), so pair it with a free
external MySQL such as **Aiven** (free tier) or **Clever Cloud**.

1. Create a MySQL instance at https://aiven.io (free plan) and note host, port,
   db name, user, password. Create a database named `cityvibe`.
2. Push the repo to GitHub, then on https://render.com → **New → Web Service →
   Build from a Git repository**, root directory `backend-springboot/`.
   Render picks up `render.yaml` / the `Dockerfile`.
3. Set environment variables:
   ```
   DB_URL=jdbc:mysql://<host>:<port>/cityvibe?useSSL=true&serverTimezone=UTC
   DB_USERNAME=<user>
   DB_PASSWORD=<password>
   ```
4. Deploy and verify `https://<service>.onrender.com/api/events`.
   (Free tier sleeps after 15 min idle — first request may take ~30s.)

---

## Point the Android app at the new backend

Edit `cityvibe-android/app/src/main/java/com/cityvibe/app/util/Constants.kt`:

```kotlin
const val BASE_URL = "https://<your-deployed-domain>/"
```

Then rebuild the APK:

```bash
cd cityvibe-android && ./gradlew assembleDebug
```

## Test the Docker image locally (optional)

```bash
cd backend-springboot
docker build -t cityvibe-backend .
docker run -p 8080:8080 \
  -e DB_URL="jdbc:mysql://host.docker.internal:3306/cityvibe?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
  -e DB_USERNAME=cityvibe -e DB_PASSWORD=cityvibe123 \
  cityvibe-backend
```
