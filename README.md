# Berlin Transit

A departure board for Berlin & Brandenburg (VBB) styled after the classic
**split-flap displays** (Solari boards) that used to hang in the city's
stations — right down to the seam line across every row, the official
BVG line colors and Verkehrsgelb lettering.

## Features

- **Nearest stations** — finds the stops around you with walking distance;
  the closest one is highlighted.
- **Station search** — fuzzy search across every stop in the VBB network,
  from Alexanderplatz to Potsdam and beyond.
- **The board** — every departure shows the line badge (in its official
  color), the destination, and the **expected time** alongside the planned
  time. Delays are color-coded: green means punctual or early, red means
  delayed or cancelled, yellow means early. Platform changes show up in
  red, cancelled trips are struck through, and disruption remarks
  (in proper German) are included.
- **Tap a departure** for the full picture: untruncated destination,
  planned/expected times, status, and platform (including the planned one
  when it changed).
- **Keeps itself fresh** — the board auto-refreshes every 45 seconds, and
  for quiet stops it extends its search window step by step up to 24 hours
  so you always see the next departure.
- **Favorites** — pin up to 10 stations for one-tap access.
- **Homescreen widgets** — one widget per favorite station, or a widget
  that always follows the *nearest* station. Widgets show the full
  scrollable departure list, have a refresh button, open the station in the
  app when tapped, and refresh every 30 minutes, on tap, and whenever you
  open the app.

## Getting the app

Grab an APK from the artifacts of a **Build** workflow run on GitHub
(signed with a debug key, both `debug` and `release` variants are
provided — prefer `release`, it is faster). Enable installing apps from
unknown sources when asked. There is no Play Store listing.

## Widget notes

- Widgets are configured when you add them: pick **Nearest station** or
  one of your favorites.
- The *nearest* widget can only resolve your location in the background
  if you grant the app location access **"Allow all the time"**
  (Android restriction). Without it, it updates whenever you open the app
  or tap refresh.
- Tapping a widget opens that station's board in the app.

## Privacy

The app contains **no analytics or tracking**. Your location is only used
on-device to find nearby stations and to resolve the nearest-station
widget. Departure data is fetched directly from a public API — nothing
about you is sent anywhere beyond those anonymous, data-less requests.

## Data & credits

Departure data comes from
[v6.vbb.transport.rest](https://v6.vbb.transport.rest/) — a free,
public REST API (no key required) by **Jannis Ritter ([derhuerst](https://github.com/derhuerst))**,
open source under the [ISC license](https://github.com/derhuerst/vbb-rest),
which wraps the official VBB/HAFAS endpoint. It provides whatever the
VBB app shows, including realtime delays and disruptions.

Line badge colors follow the official BVG corporate design
(Verkehrsblau, Verkehrsrot, Verkehrspurpur, Lichtblau, the U1–U9
Kennfarben and Verkehrsgelb).

This app is a personal project and is **not affiliated with VBB, BVG
or S-Bahn Berlin**.

## Disclaimer

This app was made for personal use and is provided to the public
**as-is, with no guarantees of any kind**. Transit data can be wrong or
stale — always double-check departures on official channels before
making plans.

## AI disclosure

This app was **fully vibe coded** — every line of code, from the UI to
the tests to the CI setup, was written by an AI from natural-language
conversation. It was not written by a human, and it was not "AI-assisted
development"; the humans involved only described what they wanted,
reviewed, tested and shipped. Expect it to work, but judge the code
accordingly.

## For developers

Manual build via the *Build* workflow or locally with Android Studio /
`./gradlew test assembleDebug assembleRelease` (JDK 17). Stack: Kotlin,
Jetpack Compose (Material 3), Retrofit + kotlinx.serialization, DataStore,
Glance (widgets), WorkManager. The split-flap components live in
`ui/components/SplitFlap.kt`; all API mapping and business rules are
unit-tested against real captured API responses in `app/src/test`.
