# File Upload Checklist ✓

Use this checklist when uploading to GitHub. Check off each file as you upload it.

## 📁 Root Files (7 files)
- [ ] `build.gradle.kts`
- [ ] `settings.gradle.kts`
- [ ] `gradle.properties`
- [ ] `gradlew`
- [ ] `gradlew.bat`
- [ ] `local.properties`
- [ ] `.gitignore`

## 📁 gradle/wrapper/ (2 files)
- [ ] `gradle-wrapper.jar`
- [ ] `gradle-wrapper.properties`

## 📁 app/ (2 files)
- [ ] `build.gradle.kts`
- [ ] `proguard-rules.pro`

## 📁 app/src/main/java/com/berlintransit/app/ (9 files)
- [ ] `MainActivity.kt`
- [ ] `DepartureAdapter.kt`
- [ ] `PreferencesManager.kt`
- [ ] `api/ApiClient.kt`
- [ ] `api/TransitRepository.kt`
- [ ] `api/VbbApiService.kt`
- [ ] `location/LocationService.kt`
- [ ] `model/Departure.kt`
- [ ] `model/Station.kt`
- [ ] `widget/DepartureWidgetProvider.kt`
- [ ] `widget/WidgetUpdateService.kt`
- [ ] `widget/WidgetUpdateWorker.kt`

## 📁 app/src/main/res/layout/ (3 files)
- [ ] `activity_main.xml`
- [ ] `departure_widget.xml`
- [ ] `item_departure.xml`

## 📁 app/src/main/res/drawable/ (8 files)
- [ ] `ic_launcher_background.xml`
- [ ] `ic_launcher_foreground.xml`
- [ ] `ic_location.xml`
- [ ] `ic_refresh.xml`
- [ ] `line_badge_background.xml`
- [ ] `widget_background.xml`
- [ ] `widget_preview.xml`

## 📁 app/src/main/res/menu/ (1 file)
- [ ] `menu_main.xml`

## 📁 app/src/main/res/values/ (3 files)
- [ ] `strings.xml`
- [ ] `colors.xml`
- [ ] `themes.xml`

## 📁 app/src/main/res/values-night/ (1 file)
- [ ] `themes.xml`

## 📁 app/src/main/res/xml/ (2 files)
- [ ] `departure_widget_info.xml`
- [ ] `network_security_config.xml`

## 📁 app/src/main/res/mipmap-anydpi-v26/ (2 files)
- [ ] `ic_launcher.xml`
- [ ] `ic_launcher_round.xml`

## 📁 app/src/main/ (1 file)
- [ ] `AndroidManifest.xml`

## 📁 .github/workflows/ (1 file)
- [ ] `build.yml`

---

## Upload Order (Recommended)

1. **First**: Create folder structure with `.gitkeep` files
2. **Then**: Upload in this order:
   1. Root files
   2. gradle/wrapper/
   3. app/ build files
   4. Source code (.kt files)
   5. Resource files (.xml)
   6. AndroidManifest.xml
   7. GitHub Actions workflow

---

## Total: 43 files

When all boxes are checked, you're ready to build! 🎉

Go to **Actions** tab → **Build APK** → **Run workflow**
