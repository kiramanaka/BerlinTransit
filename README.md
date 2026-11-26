# Berlin Transit

A departure board app for the **Berlin-Brandenburg (VBB)** region with a classic
**split-flap display** (Solari board) look: the nearest stations to your position,
live departures with planned **and** expected times, station search, and up to
10 favorite stations — plus **homescreen widgets** for a favorite station or the
always-nearest station.

Data comes from [v6.vbb.transport.rest](https://v6.vbb.transport.rest/), a free
REST wrapper around VBB's HAFAS endpoint (realtime delays included, no API key).

## Features

- **Nearby stations** — uses the device location (`/locations/nearby`) and lists the
  closest stops with walking distance; the nearest one is highlighted.
- **Departure board** — split-flap display with staggered flip animations, showing
  line badge (with VBB line colors), direction, **planned time**, and the
  **expected time** derived from realtime data:
  - `ON TIME` / `+N min` / `N min early` status chips
  - `NO LIVE DATA` when the trip has no realtime prognosis
  - `CANCELLED` with struck-through planned time for cancelled trips
  - platform (`Gl.`), disruption/warning remarks, auto-refresh every 45 s
- **Station search** — debounced fuzzy search across all VBB stops.
- **Favorites** — star a station from its board, max 10, persisted via DataStore.
- **Homescreen widgets** (Glance) — one widget per favorite station, or a widget
  that always follows the nearest station. Updates every 30 minutes via
  WorkManager, on tap, and whenever the app is opened. The nearest-station
  widget refreshes in the background only if location is set to
  "Allow all the time" for the app.

## Screens

| Home | Search | Board |
|------|-------|-------|
| Nearby stations + favorites | Fuzzy search | Split-flap departures |

## Data source (tested API contract)

Base URL: `https://v6.vbb.transport.rest` — no auth, rate limit 100 req/min,
responses carry `ETag`/`Cache-Control: max-age=30`.

### Station search

```
GET /locations?query=alexanderplatz&results=12&stops=true&addresses=false&poi=false&language=de
```

Returns a list of stops. Note: only `stops=true` filtering gives clean station
results; addresses/POIs are disabled on purpose.

```json
[{
  "type": "stop",
  "id": "900100003",
  "name": "S+U Alexanderplatz Bhf (Berlin)",
  "location": { "latitude": 52.521508, "longitude": 13.411267 },
  "products": { "suburban": true, "subway": true, "tram": true, "bus": true, ... }
}]
```

### Nearby stations

```
GET /locations/nearby?latitude=52.521508&longitude=13.411035&results=8&distance=2000
```

Same stop shape, plus `"distance": 15` (meters). Empty array for coordinates
without stops (e.g. the ocean).

### Departures

```
GET /stops/900100003/departures?results=20&duration=60&remarks=true&language=de
```

```json
{
  "departures": [{
    "tripId": "1|3406|0|86|25112026",
    "when": "2026-11-26T00:58:00+01:00",
    "plannedWhen": "2026-11-26T00:57:00+01:00",
    "delay": 60,
    "platform": "3",
    "plannedPlatform": "3",
    "prognosisType": "prognosed",
    "direction": "S Ostkreuz Bhf (Berlin)",
    "cancelled": true,
    "line": {
      "name": "S5", "product": "suburban",
      "color": { "fg": "#fff", "bg": "#eb7405" }
    },
    "remarks": [{ "type": "hint", "code": "FK", "text": "Fahrradmitnahme möglich" }]
  }],
  "realtimeDataUpdatedAt": 1795651097
}
```

Observed data quirks (all handled by the app):

- Stop IDs are the new **9-digit** format (`900100003`). The old 12-digit IDs
  (`900000100003`) return `404 NOT_FOUND` — only use IDs returned by
  `/locations` or `/locations/nearby`.
- `delay` is in **seconds** and can be negative (early trips).
- `when` is the realtime expectation; it equals `plannedWhen` when on time.
- `cancelled` is only present when `true`; cancelled trips have
  `when: null`, `delay: null` and a status remark
  (`text.realtime.stop.cancelled`).
- `delay: null` + `prognosisType: null` means **no realtime data** — only the
  planned time is known.
- `realtimeDataUpdatedAt` is **epoch seconds** (converted to ms in the app).
- `remarks[].text` can contain HTML — stripped for display.
- Stop names carry a `" (Berlin)"` suffix — stripped for display.

### Errors

- Unknown stop ID → `404` with a HAFAS error JSON body
- Short/garbage queries → `200` with `[]`

## Building

### Android Studio

Open the project and run the `app` configuration (a device/emulator with
API 26+). JDK 17 is required.

### Command line

```bash
./gradlew assembleDebug     # debug APK
./gradlew test              # unit tests (mappers, parsing, favorites logic)
```

### CI

The GitHub Actions workflow (`.github/workflows/build.yml`) runs the unit tests
and builds a debug APK on every push; download it from the run's artifacts.

## Tech stack

- Kotlin 2.0, Jetpack Compose (Material 3), Navigation Compose
- Retrofit + OkHttp + kotlinx.serialization
- DataStore Preferences (favorites)
- Platform `LocationManager` (works without Google Play services)
- minSdk 26 (uses `java.time`), targetSdk 35

## Project layout

```
app/src/main/java/com/berlintransit/app/
├── BerlinTransitApplication.kt   # AppContainer (DI graph)
├── MainActivity.kt
├── data/
│   ├── api/                      # Retrofit interface + DTOs (VBB REST)
│   ├── TransitRepository.kt      # search / nearby / departures
│   ├── TransitMappers.kt         # pure mapping + time/delay logic (unit tested)
│   └── FavoritesStore.kt         # DataStore-backed favorites (max 3)
├── location/LocationProvider.kt # LocationManager wrapper
└── ui/
    ├── components/               # SplitFlap, DepartureRow, badges, chips…
    ├── home/  search/  board/    # screens + view models
    └── TransitApp.kt             # navigation graph
```

## Attribution

Data: [v6.vbb.transport.rest](https://v6.vbb.transport.rest/) (community-run
REST API for VBB/HAFAS — what the VBB app shows, incl. realtime data).
Not affiliated with VBB.
