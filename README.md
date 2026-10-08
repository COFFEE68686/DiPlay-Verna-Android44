# DiPlay-Verna-Android44

This repository stores the step-by-step Android 4.4.4 compatibility plan for adapting the upstream DiPlay project to a Hyundai Verna-style head unit.

Important:
- Upstream DiPlay is designed for Android 9+ (API 28+).
- Android 4.4.4 (API 19) is much older and requires compatibility shims.
- This guide focuses on a practical USB/basic CarPlay path and intentionally disables modern-only features such as Wi-Fi Direct and newer permission/service APIs.

## Goal

Adapt the upstream DiPlay codebase so it can compile and run on an Android 4.4.4 head unit with:
- USB CarPlay support first
- no Wi-Fi Direct
- no Android 10+ permission model requirements
- no modern Foreground Service requirements
- limited feature set while preserving the core CarPlay path

## Step-by-step plan

### Step 1: Start from the upstream project

Use the official upstream repo as the source of truth.

```bash
git clone https://github.com/shihabal3amri/DiPlay.git
cd DiPlay
git checkout -b adapt-verna-android44
```

If you are maintaining your own fork, use this repo as the adaptation branch and port the changes into your fork later.

### Step 2: Lower the Android SDK target

Main file to edit:
- `shared/build.gradle`

Set legacy compatibility values:

```gradle
android {
    namespace = 'com.shilapi.xcertplay.shared'
    compileSdk = 28

    defaultConfig {
        minSdk = 19
        targetSdk = 28
        multiDexEnabled = true

        ndk {
            abiFilters 'armeabi-v7a', 'arm64-v8a'
        }

        externalNativeBuild {
            ndkBuild {
                arguments 'APP_PLATFORM=android-19'
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
```

Why:
- Android 4.4.4 is API 19
- upstream project targets modern Android and API 28+
- the app must not request a minimum version unsupported by the head unit

### Step 3: Remove modern-only build features

Edit the project Gradle files and remove or reduce features not supported by API 19.

For example:

```gradle
dependencies {
    implementation 'androidx.multidex:multidex:2.0.1'
    implementation 'androidx.appcompat:appcompat:1.3.1'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.0'
}
```

Also remove any dependency that requires a newer AndroidX or Jetpack API profile.

### Step 4: Disable Wi-Fi Direct and other newer connection paths

The upstream app includes modern Wi-Fi Direct, Android 10+ hotspot logic, and more recent connection flow assumptions.

For a Verna port, start by disabling the newer paths and keep only USB/basic hotspot logic.

Recommended approach:
- remove or guard all `WifiP2p*` classes
- remove or guard all Android 10+ hotspot setup code
- retain only the simpler USB transport and manual hotspot setup

If a file is not needed, remove it from the project temporarily.

Example rule:

```java
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
    // keep legacy path only
} else {
    // fallback path for API 19
}
```

### Step 5: Remove Android runtime permission requirements

Android 4.4.4 does not use the modern runtime permission model the way newer Android versions do.

Replace calls like this:

```java
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 1);
}
```

with a compatibility path such as:

```java
// Android 4.4 uses install-time permissions and does not require runtime prompts.
// Keep the permission declarations in the manifest, but skip runtime requests.
```

Also review all code that checks for:
- `Build.VERSION_CODES.M`
- `POST_NOTIFICATIONS`
- `FOREGROUND_SERVICE`
- `ACCESS_FINE_LOCATION` for runtime request flow

### Step 6: Replace Foreground Service logic

Upstream DiPlay uses newer foreground service APIs.

Android 4.4.4 does not support the same service model in the same way.

Refactor any service launch code like:

```java
startForegroundService(intent);
```

into a legacy-safe version:

```java
startService(intent);
```

If the service requires a notification, create a simple legacy notification using the old Android notification methods.

### Step 7: Lower Java compatibility

If the build fails on Java version mismatch, set the project to Java 8.

```gradle
compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
```

### Step 8: Remove or guard modern media APIs

The upstream app depends on newer `MediaCodec`, `AudioTrack`, and other modern media APIs.

The most practical approach is:
- keep only the basic AAC/PCM path
- disable experimental features such as smooth video and newer encoder/decoder features
- treat unsupported codecs as a fallback rather than a hard failure

Example fallback:

```java
try {
    // modern codec path
} catch (Exception e) {
    // older device fallback path
}
```

### Step 9: Simplify the USB communication path

The USB path is the most realistic first target.

Keep:
- USB detection
- USB permission handling
- simple device attach flow
- minimal CarPlay session setup

Remove:
- complex Wi-Fi Direct setup
- advanced cluster rendering
- advanced dashboard features
- modern overlay logic

### Step 10: Test on a real vehicle

After each compatibility change, build and install the APK on the actual head unit.

Recommended testing order:
1. app installs and launches
2. USB detection works
3. CarPlay session starts
4. audio path works
5. touch/input works
6. reconnect behavior works
7. map/navigation features disabled unless tested

### Step 11: Only re-enable features after proving compatibility

The correct order is:
- install works
- USB works
- audio works
- touch works
- CarPlay session survives reconnect
- then add optional power features
- then consider Bluetooth/hotspot support

Do not re-enable Wi‑Fi Direct or advanced features before the base USB path is stable.

## Recommended target feature list for Android 4.4.4

This is the realistic target set for a first adapted build:

- USB CarPlay connection
- basic audio
- basic touch routing
- simple app launch
- no Wi‑Fi Direct
- no advanced cluster render features
- no unsupported modern Android permissions
- no experimental video path

## Suggested compatibility checklist

Before you call the port successful, verify all of these:

- app installs on Android 4.4.4
- app launches without crash
- USB device attach is detected
- CarPlay session starts
- iPhone sees the head unit
- audio plays
- touch input reaches the CarPlay session
- app can reconnect after disconnect
- no unsupported API calls remain in build logs

## One important reality check

This is not a simple binary patch. The upstream DiPlay project is built around modern Android assumptions. To bring it to API 19, you must remove or bypass several modern features. The Verna port will likely be a custom, minimal, legacy branch rather than a drop-in replacement.

This repo is intended as a practical adaptation guide and working checklist for that effort.

## Next step

The next meaningful action is to create the actual compatibility patch files in a forked/working branch. That would involve:
- a trimmed `build.gradle`
- a reduced manifest
- a minimal permission manager
- a legacy-safe service wrapper
- a compatibility notes doc

If you want, I can continue by creating those actual patch files in this repo, one by one, so the branch becomes a real Android 4.4.4 adaptation skeleton.
