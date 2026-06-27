# CityVibe — PRD

## Problem statement
Build a native Android app "CityVibe" — a local events discovery app with two
screens (Home feed + Event details), plus a Spring Boot REST backend and MySQL DB.
Frontend: Kotlin (Android SDK, Jetpack, MVVM), Retrofit, RecyclerView.
Backend: Spring Boot (Java), Spring Data JPA/Hibernate, MySQL.

## User choices (confirmed)
- Delivery: Full Android Studio project source code (+ a verified debug APK).
- Backend: strictly Spring Boot + MySQL.
- City: Bengaluru. Real stock images. Book Now -> confirmation dialog only.

## Architecture
- Android (Kotlin, MVVM): HomeActivity/Details + ViewModels + Retrofit + Glide.
- Spring Boot 3.2 (Java 17): Event entity, EventRepository (JPA), EventController.
- MySQL (MariaDB-compatible) persistent at /app/data/mysql.
- Ingress: platform exposes :8001 only; FastAPI (backend/server.py) proxies /api/*
  to Spring Boot on :8090. Public URL serves the real Spring Boot API.

## Data model — Event
id, title, category, description, imageUrl, dateTime, duration, venueName,
venueAddress, organizer, price. Categories: Music, Comedy, Meetup, Gathering.

## Endpoints
- GET /api/events            (optional ?category= filter)
- GET /api/events/{id}       (404 if missing)

## Implemented (2026-06-27)
- Spring Boot backend + MySQL, 8 seeded events across categories. Verified:
  list / category filter / single / 404 all pass through the public URL.
- Native Android app (Home feed with filter chips + pull-to-refresh, Details with
  collapsing cover, Book Now confirmation, up navigation). Material UI, vibrant
  coral theme, per-category color tags.
- Project builds: debug APK produced (com.cityvibe.app) at
  cityvibe-android/dist/CityVibe-debug.apk.
- Self-healing supervisor scripts so backend survives container recycling.

## Backlog / Next
- P1: Search bar + multi-city support; date/relative formatting polish.
- P1: Real RSVP persisted to backend (POST /api/events/{id}/rsvp).
- P2: Favorites/bookmarks, share event, maps deep-link for venue.
- P2: Pagination, image caching tuning, dark theme.
- Note: Android UI not run on an emulator in this env (build verified only).
