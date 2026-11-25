# Berlin Transit

An Android app that shows real-time public transit departure times for the Berlin-Brandenburg region (VBB - Verkehrsverbund Berlin-Brandenburg).

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![API](https://img.shields.io/badge/API-26+-blue?style=for-the-badge)

## Features

- **Real-time departures**: Shows upcoming departures from the nearest transit station
- **Location-based**: Automatically finds nearby stations using your device's location
- **Home screen widget**: Auto-updating widget showing the next 4 departures (updates every 15 minutes)
- **Delay information**: Displays real-time delay information for each departure
- **Color-coded lines**: Different transport types (S-Bahn, U-Bahn, Bus, Regional) are color-coded
- **Dark mode support**: Automatic dark theme based on system settings
- **Pull to refresh**: Swipe down to refresh departure times
- **Persistent station**: Remembers your preferred station between app sessions

## Supported Transport Types

- S-Bahn (suburban trains)
- U-Bahn (subway)
- Regional trains (RE, RB)
- Metro buses (M)
- Regular buses
- Express buses (X)

## API

This app uses the free VBB REST API: https://v5.vbb.transport.rest/

No API key required.

## Building

### Requirements

- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK 34
- JDK 17

### Build Steps

#### Option A: Android Studio (Computer)
1. Open the project in Android Studio
2. Sync Gradle files
3. Build → Build APK(s)
4. APK will be at `app/build/outputs/apk/debug/app-debug.apk`

#### Option B: GitHub Actions (From Phone)
1. Create a GitHub repository
2. Upload all project files
3. Go to **Actions** tab → **Build APK** → **Run workflow**
4. Download APK from artifacts after build completes (~5 min)

See [MOBILE_BUILD_GUIDE.md](MOBILE_BUILD_GUIDE.md) for detailed phone build instructions.

```bash
./gradlew assembleDebug
```

### Minimum SDK

- API 26 (Android 8.0 Oreo)

### Target SDK

- API 34 (Android 14)

## Permissions

The app requires the following permissions:

- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`: To find nearby stations
- `INTERNET`: To fetch real-time departure data
- `ACCESS_NETWORK_STATE`: To check network connectivity

## Widget

The home screen widget displays:
- Station name (uses your preferred station or Berlin Hauptbahnhof)
- Next 4 departures with line, direction, and time
- Delay information (if applicable)
- Last update timestamp

The widget auto-updates every 15 minutes using WorkManager for reliable background updates. You can also tap the widget to open the main app.

## Project Structure

```
app/
├── src/main/
│   ├── java/com/berlintransit/app/
│   │   ├── api/              # VBB API client and repository
│   │   ├── location/         # Location services with FusedLocationProvider
│   │   ├── widget/           # Home screen widget and WorkManager worker
│   │   ├── model/            # Data models (Departure, Station)
│   │   ├── MainActivity.kt   # Main UI activity
│   │   ├── DepartureAdapter.kt
│   │   └── PreferencesManager.kt  # Station preferences storage
│   ├── res/
│   │   ├── layout/           # UI layouts
│   │   ├── drawable/         # Icons and backgrounds
│   │   ├── menu/             # Toolbar menu
│   │   ├── values/           # Strings, colors, themes
│   │   ├── values-night/     # Dark theme
│   │   └── xml/              # Widget configuration
│   └── AndroidManifest.xml
└── build.gradle.kts
```

## Technical Details

### API Integration
- Uses the free VBB REST API (https://v5.vbb.transport.rest/)
- No API key required
- Fetches nearby stations and real-time departures

### Location Services
- Uses Google Play Services FusedLocationProvider
- Requests fresh location when last known is stale
- Falls back to Berlin Hauptbahnhof if location unavailable

### Widget Updates
- Uses WorkManager for reliable background updates
- Updates every 15 minutes
- Persists preferred station in SharedPreferences

### Architecture
- MVVM-like pattern with Repository
- Kotlin Coroutines for async operations
- Retrofit for API calls
- Material Design 3 components

## Troubleshooting

### Widget not updating
- Make sure the app has been opened at least once to set your preferred station
- Check that background data is enabled for the app
- Try removing and re-adding the widget

### Location not working
- Ensure location services are enabled on your device
- Grant location permission when prompted
- The app falls back to Berlin Hauptbahnhof if location is unavailable

### No departures showing
- Check your internet connection
- The station might have no service at that time (e.g., late night)
- Try refreshing or finding a different station

## License

This project is provided as-is for educational purposes.

## Credits

- Transit data: VBB (Verkehrsverbund Berlin-Brandenburg)
- API: https://github.com/public-transport/public-transport
- Icons: Android Material Icons
