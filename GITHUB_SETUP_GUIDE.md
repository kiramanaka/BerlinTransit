# GitHub Upload Guide - Step by Step

## Before You Start

Make sure you have:
- A GitHub account (free at github.com)
- All project files in `/workspace/BerlinTransit/`
- A web browser on your phone

---

## Step 1: Create GitHub Account (if you don't have one)

1. Open browser → Go to **github.com**
2. Tap **Sign up**
3. Enter your email
4. Create a password
5. Enter a username
6. Complete the puzzle verification
7. Check your email for verification code
8. Enter the code to verify

---

## Step 2: Create New Repository

1. After logging in, tap your **profile picture** (top right)
2. Tap **Your repositories**
3. Tap the **New** button (or **+** icon)
4. Fill in:
   - **Repository name**: `BerlinTransit`
   - **Description**: `Real-time Berlin-Brandenburg public transit app`
   - **Visibility**: Choose Public or Private
5. **DO NOT** check "Add README" or other options
6. Tap **Create repository**

---

## Step 3: Upload Files

### Important: Upload in This Order

#### First - Create Folder Structure

1. On your repository page, tap **Add file** → **Create new file**
2. In the filename box, type: `app/src/main/java/com/berlintransit/app/.gitkeep`
3. Scroll down → Tap **Commit new file**

4. Repeat for these folders:
   - `app/src/main/res/drawable/.gitkeep`
   - `app/src/main/res/layout/.gitkeep`
   - `app/src/main/res/menu/.gitkeep`
   - `app/src/main/res/values/.gitkeep`
   - `app/src/main/res/values-night/.gitkeep`
   - `app/src/main/res/xml/.gitkeep`
   - `app/src/main/res/mipmap-anydpi-v26/.gitkeep`
   - `.github/workflows/.gitkeep`
   - `gradle/wrapper/.gitkeep`

#### Then - Upload Root Files

1. Tap **Add file** → **Upload files**
2. Select these files from `/workspace/BerlinTransit/`:
   - `build.gradle.kts`
   - `settings.gradle.kts`
   - `gradle.properties`
   - `gradlew`
   - `gradlew.bat`
   - `local.properties`
   - `README.md`
   - `MOBILE_BUILD_GUIDE.md`
   - `.gitignore`

3. Tap **Commit changes**

#### Upload Gradle Wrapper

1. Tap **Add file** → **Upload files**
2. Navigate to `gradle/wrapper/` folder
3. Select:
   - `gradle-wrapper.jar`
   - `gradle-wrapper.properties`

4. Tap **Commit changes**

#### Upload App Build File

1. Tap **Add file** → **Upload files**
2. Navigate to `app/` folder
3. Select:
   - `build.gradle.kts`
   - `proguard-rules.pro`

4. Tap **Commit changes**

#### Upload Source Code

1. Tap **Add file** → **Upload files**
2. Navigate to `app/src/main/java/com/berlintransit/app/`
3. Select ALL `.kt` files:
   - `MainActivity.kt`
   - `DepartureAdapter.kt`
   - `PreferencesManager.kt`
   - And all files in subfolders (api/, location/, model/, widget/)

4. Tap **Commit changes**

#### Upload Resources

1. Tap **Add file** → **Upload files**
2. Upload from `app/src/main/res/`:
   - All `layout/` XML files
   - All `drawable/` XML files
   - All `values/` XML files
   - All `values-night/` XML files
   - All `xml/` XML files
   - All `menu/` XML files
   - All `mipmap-anydpi-v26/` XML files

3. Tap **Commit changes**

#### Upload AndroidManifest

1. Tap **Add file** → **Upload files**
2. Navigate to `app/src/main/`
3. Select `AndroidManifest.xml`
4. Tap **Commit changes**

#### Upload GitHub Actions Workflow

1. Tap **Add file** → **Upload files**
2. Navigate to `.github/workflows/`
3. Select `build.yml`
4. Tap **Commit changes**

---

## Step 4: Verify All Files

Your repository should have this structure:

```
BerlinTransit/
├── .github/
│   └── workflows/
│       └── build.yml
├── app/
│   ├── src/main/
│   │   ├── java/com/berlintransit/app/
│   │   │   ├── api/
│   │   │   │   ├── ApiClient.kt
│   │   │   │   ├── TransitRepository.kt
│   │   │   │   └── VbbApiService.kt
│   │   │   ├── location/
│   │   │   │   └── LocationService.kt
│   │   │   ├── model/
│   │   │   │   ├── Departure.kt
│   │   │   │   └── Station.kt
│   │   │   ├── widget/
│   │   │   │   ├── DepartureWidgetProvider.kt
│   │   │   │   ├── WidgetUpdateService.kt
│   │   │   │   └── WidgetUpdateWorker.kt
│   │   │   ├── MainActivity.kt
│   │   │   ├── DepartureAdapter.kt
│   │   │   └── PreferencesManager.kt
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   ├── drawable/
│   │   │   ├── menu/
│   │   │   ├── values/
│   │   │   ├── values-night/
│   │   │   ├── xml/
│   │   │   └── mipmap-anydpi-v26/
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── local.properties
├── .gitignore
└── README.md
```

---

## Step 5: Build the APK

1. Tap **Actions** tab at the top of your repository
2. You'll see "Build APK" workflow
3. Tap on **Build APK**
4. Tap **Run workflow** button (dropdown on right)
5. Select branch: `main` or `master`
6. Tap **Run workflow**

### Wait for Build (3-5 minutes)

- You'll see a yellow dot → build is running
- Wait for it to turn green ✓ → build successful
- If it turns red ✗ → there was an error (check logs)

---

## Step 6: Download APK

1. Tap the **green checkmark** (successful build)
2. Scroll down to **Artifacts** section
3. Tap **berlin-transit-debug**
4. APK will download to your phone

---

## Step 7: Install on Xiaomi 17T

1. Open **File Manager** app
2. Go to **Downloads** folder
3. Find `app-debug.apk` (or similar name)
4. Tap to install
5. When prompted:
   - Tap **Settings**
   - Enable **Allow from this source**
   - Go back and tap **Install**
6. Tap **Open** to launch the app

---

## Step 8: First Launch

1. **Grant location permission** when prompted
2. App will find your nearest station
3. View real-time departures!

### Add Widget:
1. Long-press on empty home screen area
2. Tap **Widgets**
3. Find **Berlin Transit Widget**
4. Drag to home screen
5. Widget will show next 4 departures

---

## Troubleshooting

### Build Failed?

1. Go to **Actions** tab
2. Tap the failed build (red X)
3. Read the error message
4. Common issues:
   - Missing file → Upload the missing file
   - gradle-wrapper.jar missing → Re-upload it
   - Syntax error → Check the file for typos

### Can't Upload Files?

- GitHub mobile has a file size limit
- Try uploading in smaller batches
- Or use the GitHub mobile app

### APK Won't Install?

- Go to Settings → Apps → Special app access
- Tap **Install unknown apps**
- Enable for your browser/file manager
- Try installing again

---

## Need More Help?

Reply with:
- What step you're on
- What error you're seeing
- Screenshot if possible

Good luck! 🚇
