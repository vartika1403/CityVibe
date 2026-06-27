# CityVibe — Android App (Kotlin)

Native Android app following **MVVM**, using **Retrofit** for networking, **RecyclerView**
for the feed, **Glide** for images and **Material Components** for UI.

## Screens
- **Home (`HomeActivity`)** — header (`Events in Bengaluru`), category filter chips
  (All / Music / Comedy / Meetups), and a scrollable RecyclerView of event cards
  (cover image, title, category tag, date & time, venue, price). Pull-to-refresh.
- **Details (`DetailsActivity`)** — large collapsing cover image, title, category,
  description, date & time, duration, venue + address, organizer, price, and a
  sticky **Book Now** button (shows a confirmation dialog). Toolbar up-navigation.

## Architecture
```
ui/home/HomeActivity + HomeViewModel + EventAdapter
ui/details/DetailsActivity + DetailsViewModel
data/model/Event
data/remote/ApiService (Retrofit) + RetrofitClient
data/repository/EventRepository
util/Constants, Resource, Formatters
```

## Backend URL
Configured in `app/src/main/java/com/cityvibe/app/util/Constants.kt`.
Defaults to the hosted demo API (works on emulator + real devices). To use a
local Spring Boot instance, switch `BASE_URL` to `http://10.0.2.2:8090/`
(emulator) — cleartext for `10.0.2.2`/`localhost` is already allowed via
`res/xml/network_security_config.xml`.

## Build / Run
1. Open this folder in **Android Studio** and let it sync Gradle.
2. Press **Run** on an emulator (API 24+) or device.

Command line:
```bash
./gradlew assembleDebug      # -> app/build/outputs/apk/debug/app-debug.apk
```

A pre-built debug APK is included at `dist/CityVibe-debug.apk`
(package `com.cityvibe.app`).

### Tech
- Kotlin 1.9.22, AGP 8.2.2, Gradle 8.2, compileSdk 34, minSdk 24
- Retrofit 2.9 + Gson, OkHttp logging, Coroutines, Lifecycle/ViewModel/LiveData
- Material 1.11, Glide 4.16, ViewBinding
