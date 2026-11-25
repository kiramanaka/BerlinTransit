# Build Instructions for Berlin Transit

## Issue
The build environment is ARM64, but Android build tools require x86-64. You have two options:

## Option 1: Build on Your Computer (Recommended)

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34

### Steps
1. Clone or copy this project to your computer
2. Open in Android Studio
3. Let Gradle sync complete
4. Build > Build Bundle(s) / APK(s) > Build APK(s)
5. The APK will be in `app/build/outputs/apk/debug/`

## Option 2: Build via GitHub Actions

1. Create a GitHub repository for this project
2. Add the following `.github/workflows/build.yml`:

```yaml
name: Build APK

on:
  push:
    branches: [ main ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Setup Android SDK
      uses: android-actions/setup-android@v3
    
    - name: Build Debug APK
      run: ./gradlew assembleDebug
    
    - name: Upload APK
      uses: actions/upload-artifact@v4
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
```

3. Push to GitHub
4. Go to Actions tab and run the build
5. Download the APK from the artifacts

## Option 3: Use an Online Build Service

Services like:
- Bitrise (https://bitrise.io)
- Codemagic (https://codemagic.io)
- GitHub Actions (see Option 2)

Can build Android APKs in the cloud.

## Installing on Xiaomi 17T

Once you have the APK:
1. Enable "Install unknown apps" on your Xiaomi device
2. Transfer the APK to your phone
3. Install it
4. Grant location permission when prompted
5. Add the widget to your home screen

