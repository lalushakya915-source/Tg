# TGDownloader Android

A personal Telegram video downloader built on the official TDLib Java/JNI interface.

## Important
- Use only for Telegram content you are authorized to access/download.
- You must provide your own Telegram API ID and API hash.
- This project does NOT include TDLib native binaries or generated TdApi.java.
- The setup script downloads/builds official TDLib and copies its generated Java/JNI output into this project.

## Requirements
- Android Studio
- JDK 17
- Linux/macOS, or Windows via WSL2/Git Bash
- Git, Bash, PHP, Perl, gperf, CMake, Ninja
- Android SDK platform 34 and CMake 3.22.1
- Enough disk/RAM for building TDLib

## 1. Get Telegram API credentials
Create an application at:
https://my.telegram.org

Then edit gradle.properties:
TELEGRAM_API_ID=123456
TELEGRAM_API_HASH=your_hash

## 2. Build official TDLib
From this project directory:

bash setup_tdlib.sh

The script clones the official tdlib/td repository and runs its official Android build scripts. It then copies:
tdlib/java -> app/src/main/java
tdlib/libs -> app/src/main/jniLibs

## 3. Open in Android Studio
Open the TGDownloader folder, sync Gradle, and run the app.

## 4. Features
- Telegram phone login
- OTP
- 2FA password
- Resolve public @username
- Search video messages
- Load older pages
- Select individual videos / select all
- Sequential bulk download
- Progress updates
- Pause/cancel current download
- Save completed files into Movies/TGDownloader

## Limitations
- Public username resolution is implemented first.
- Private invite-link resolution and protected-content handling are intentionally not bypassed.
- The app downloads files that TDLib exposes to the authenticated account.
- The app uses one active download at a time for predictable storage/network behavior.
