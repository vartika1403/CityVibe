# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

CityVibe Android is a native Kotlin app (package `com.cityvibe.application`) that lists events in Bengaluru and shows event details. It follows MVVM with Retrofit networking. This directory (`cityvibe-android`) is one module of a larger monorepo that also contains a Spring Boot backend (`../backend-springboot`), a separate `../backend`, and a `../frontend`.

## Commands

```bash
./gradlew assembleDebug      # build debug APK -> app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug       # build + install on a connected device/emulator
./gradlew lint               # Android lint -> app/build/reports/lint-results-debug.html
./gradlew test               # JVM unit tests
./gradlew connectedAndroidTest   # instrumented tests (requires a device/emulator)
```

Run a single unit test class: `./gradlew test --tests "com.cityvibe.application.SomeTest"`.

Note: there are currently no test source sets (`app/src/test` / `app/src/androidTest` do not exist yet); the test commands are no-ops until tests are added.

## Architecture

Layered MVVM with one-way data flow. UI observes `LiveData<Resource<T>>`; ViewModels call the repository inside `viewModelScope`; the repository delegates to Retrofit.

- `data/remote/ApiService` — Retrofit interface. Two endpoints: `GET api/events?category=` and `GET api/events/{id}`. `RetrofitClient` builds the singleton `api` with the OkHttp logging interceptor.
- `data/repository/EventRepository` — thin pass-through over `RetrofitClient.api`; instantiated directly by ViewModels (no DI framework).
- `data/model/Event` — Gson-mapped response model.
- `ui/home` — `HomeActivity` + `HomeViewModel` + `EventAdapter` (RecyclerView). Category filter chips, pull-to-refresh.
- `ui/details` — `DetailsActivity` + `DetailsViewModel`. Collapsing cover image, sticky Book Now button.
- `util` — `Constants` (backend URL), `Resource` (sealed Loading/Success/Error wrapper), `Formatters` (date/time/price display).

### Conventions to preserve

- **Loading state**: every async result is wrapped in `util/Resource` (`Loading`/`Success`/`Error`). ViewModels catch exceptions and emit `Resource.Error(e.localizedMessage)` rather than throwing.
- **Category mapping**: UI chip labels are not always the backend category. In `HomeViewModel.loadEvents`, `"All"` maps to no filter (`null`) and the `"Meetups"` chip maps to backend category `"Meetup"`. Add new categories here.
- ViewBinding is enabled (`buildFeatures { viewBinding true }`); no Compose, no Kotlin synthetics.

## Backend URL

Configured by `BASE_URL` in `app/src/main/java/com/cityvibe/application/util/Constants.kt`. Defaults to the hosted demo API. To target a local Spring Boot instance (port 8090), use `http://10.0.2.2:8090/` on the emulator. Cleartext for `10.0.2.2`/`localhost` is already permitted via `res/xml/network_security_config.xml`.

## Tech

Kotlin 1.9.22, AGP 8.2.2, Gradle 8.2, JDK 17, compileSdk 34, minSdk 24. Retrofit 2.9 + Gson, OkHttp logging interceptor, Coroutines, Lifecycle/ViewModel/LiveData, Material 1.11, Glide 4.16.
