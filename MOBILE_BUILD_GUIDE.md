# Build Berlin Transit APK from Your Phone

## Method 1: GitHub Actions (Easiest - Recommended)

### Step 1: Create GitHub Account
1. Open browser on your phone
2. Go to https://github.com
3. Sign up for a free account

### Step 2: Create Repository
1. Tap your profile picture → **Your repositories**
2. Tap **New** (or the + icon)
3. Repository name: `BerlinTransit`
4. Make it **Public** or **Private** (your choice)
5. Tap **Create repository**

### Step 3: Upload Project Files
**Option A: Using GitHub Mobile App**
1. Install GitHub app from Play Store
2. Open your BerlinTransit repository
3. Tap **Add file** → **Upload files**
4. Select all files from `/workspace/BerlinTransit/` folder
5. Tap **Commit changes**

**Option B: Using Browser**
1. Go to your repository on github.com
2. Tap **Add file** → **Upload files**
3. Select all project files
4. Tap **Commit changes**

**Important:** Make sure to include:
- All files in `app/src/main/`
- `build.gradle.kts` (both root and app/)
- `settings.gradle.kts`
- `gradle.properties`
- `gradlew` and `gradlew.bat`
- `gradle/wrapper/` folder
- `.github/workflows/build.yml`

### Step 4: Build APK
1. In your repository, tap **Actions** tab
2. Tap **Build APK** workflow
3. Tap **Run workflow** (dropdown) → **Run workflow** button
4. Wait 3-5 minutes for build to complete
5. Tap the build run → Scroll down to **Artifacts**
6. Tap **berlin-transit-debug** to download APK

### Step 5: Install on Xiaomi 17T
1. Open **File Manager** on your phone
2. Find the downloaded `app-debug.apk`
3. Tap to install
4. When prompted, enable **Install unknown apps** for your browser/file manager
5. Complete installation

---

## Method 2: Build Directly on Phone with AIDE

### Step 1: Install AIDE
1. Open Play Store (or download from https://www.aide-app.com)
2. Search for **AIDE - IDE for Android Java C++**
3. Install the app

### Step 2: Import Project
1. Open AIDE
2. Tap **Open Folder**
3. Navigate to `/workspace/BerlinTransit/`
4. Tap **Open**

### Step 3: Build APK
1. AIDE will analyze the project
2. Tap the **Build** button (hammer icon)
3. Wait for build to complete
4. APK will be in `app/build/outputs/apk/debug/`

**Note:** AIDE free version has limitations. You may need AIDE Premium for full Gradle support.

---

## Method 3: Build with Termux (Advanced)

### Step 1: Install Termux
1. Download from F-Droid (recommended) or Play Store
2. Install **Termux**

### Step 2: Setup Build Environment
```bash
pkg update
pkg install openjdk-17 wget unzip
export JAVA_HOME=/data/data/com.termux/files/usr/lib/jvm/java-17-openjdk
```

### Step 3: Build
```bash
cd /workspace/BerlinTransit
chmod +x gradlew
./gradlew assembleDebug
```

**Note:** This may not work due to ARM64 architecture limitations with Android build tools.

---

## Method 4: Online Build Services (From Phone Browser)

### Bitrise (https://bitrise.io)
1. Sign up for free account
2. Create new project
3. Connect your GitHub repository
4. Use default Android workflow
5. Start build → Download APK

### Codemagic (https://codemagic.io)
1. Sign up with GitHub
2. Add your repository
3. Configure Android build
4. Start build → Download APK

---

## Troubleshooting

### Build Fails on GitHub Actions
- Check **Actions** tab for error logs
- Make sure all files were uploaded correctly
- Verify `gradle/wrapper/gradle-wrapper.jar` exists

### Can't Install APK
- Go to Settings → Apps → Special app access → Install unknown apps
- Enable for your browser or file manager
- Try again

### Widget Not Working
- Open the app first to set your location
- Long-press home screen → Widgets
- Find **Berlin Transit Widget**
- Drag to home screen

---

## Quick Start After Installation

1. **Open the app** - Grant location permission
2. **View departures** - See real-time transit times
3. **Add widget** - Long-press home → Widgets → Berlin Transit
4. **Refresh** - Pull down or tap refresh icon
5. **Find your station** - Tap location icon in toolbar

Enjoy your Berlin Transit app! 🚇🚌
