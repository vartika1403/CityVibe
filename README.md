# CityVibe 🎉 — Local Events Discovery

A native **Android (Kotlin)** events-discovery app backed by a **Spring Boot (Java) + MySQL** REST API.

```
/app
├── cityvibe-android/      # Native Android app (Kotlin, MVVM, Retrofit, RecyclerView)
│   └── dist/CityVibe-debug.apk   # Pre-built debug APK
├── backend-springboot/    # Spring Boot REST API (Java 17, Spring Data JPA, MySQL)
├── backend/server.py      # Thin ingress proxy (FastAPI) -> Spring Boot on :8090
└── scripts/               # Self-healing startup scripts for MySQL + Spring Boot
```

## Live API
The Spring Boot API is exposed through the preview ingress:

- `GET /api/events`              — list all events (optional `?category=Music|Comedy|Meetup`)
- `GET /api/events/{id}`         — single event
- Base URL: `https://event-finder-app-7.preview.emergentagent.com`

> Why the proxy? The platform ingress only exposes port 8001. `backend/server.py`
> transparently forwards every `/api/*` request to the real Spring Boot service on
> port 8090. All logic, persistence (MySQL) and data live in Spring Boot.

## Run the Android app
Open `cityvibe-android/` in **Android Studio** (Hedgehog+), let Gradle sync, then Run.
It points at the hosted API out of the box. See `cityvibe-android/README.md`.

## Run the backend locally
See `backend-springboot/README.md` (needs JDK 17 + MySQL).
