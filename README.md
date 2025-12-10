# 📘 LogKit – Safe, Clickable, Automotive-Aware Logging for Android

[![](https://jitpack.io/v/mohanchandrasekar/LogKit.svg)](https://jitpack.io/#mohanchandrasekar/LogKit)

**LogKit** is a lightweight logging library for Android & Android Automotive apps, designed with:

✅ Simple Kotlin & Java API (similar to Timber)
✅ Clickable file/line navigation in Logcat
✅ Automatic hide of VERBOSE/DEBUG logs in release
✅ Sensitive-data detection (VIN, VHAL, SOC, GPS, tokens, PII…)
✅ No configuration required
✅ BuildConfig-aware runtime behavior

---

## 🚀 Features

### ✔ Debug vs Release Behavior

| Log Type  | Debug Build | Release Build |
| --------- | ----------- | ------------- |
| `VERBOSE` | ✔ shown     | ❌ hidden      |
| `DEBUG`   | ✔ shown     | ❌ hidden      |
| `INFO`    | ✔ shown     | ✔ shown       |
| `WARN`    | ✔ shown     | ✔ shown       |
| `ERROR`   | ✔ shown     | ✔ shown       |

---

### ✔ Sensitive Log Protection

LogKit automatically detects automotive-sensitive terms:

* VIN, vehicle ID, chassis ID
* VHAL / **VehiclePropertyIds**
* Battery / SOC / range
* GPS / lat-lng
* Tokens, passwords, session IDs
* User PII (email, phone, contact)
* Internal API URLs / endpoints

`BuildConfig.ALLOW_SENSITIVE_LOGS` controls behavior:

* **Debug** → Allowed (but warning shown)
* **Release** → ❌ Blocked completely

---

### ✔ Clickable Logs (file/line)

LogKit adds:

```
at com.example.MainActivity.onCreate(MainActivity.kt:42)
```

Android Studio makes it clickable → jumps to that exact line.

---

# 📦 Installation (via JitPack)

### 1️⃣ Add JitPack repository

In **settings.gradle.kts**:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

---

### 2️⃣ Add the LogKit dependency

In **app/build.gradle.kts**:

```kotlin
dependencies {
    implementation("com.github.mohanchandrasekar:logKit:v1.0.0")
}
```

👉 Always check the latest version:
[https://jitpack.io/#mohanchandrasekar/LogKit](https://jitpack.io/#mohanchandrasekar/LogKit)

---

# ⚙️ App Setup

### 3️⃣ Enable BuildConfig flags in your app

Add inside **app module** `build.gradle.kts`:

```kotlin
android {
    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "ALLOW_SENSITIVE_LOGS", "true")
        }
        release {
            buildConfigField("boolean", "ALLOW_SENSITIVE_LOGS", "false")
            isMinifyEnabled = true
        }
    }
}
```

---

### 4️⃣ Initialize LogKit in Application

`MyApp.kt`:

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        LogKit.init(
            debug = BuildConfig.DEBUG,
            allowSensitive = BuildConfig.ALLOW_SENSITIVE_LOGS,
            LogcatTree(minPriority = LogKit.VERBOSE)
        )
    }
}
```

Register in `AndroidManifest.xml`:

```xml
<application
    android:name=".MyApp"
    ... >
```

---

# 🧪 Example Usage (Demo App Code)

```kotlin
private const val TAG = "LogKitDemo"

private fun testNormalLogs() {
    LogKit.v(TAG) { "Verbose log – only in DEBUG" }
    LogKit.d(TAG) { "Debug log – only in DEBUG" }
    LogKit.i(TAG) { "Info log – DEBUG + RELEASE" }
    LogKit.w(TAG) { "Warning log – DEBUG + RELEASE" }
    LogKit.e(TAG) { "Error log – DEBUG + RELEASE" }
}

private fun testSensitiveLogs() {
    val vin = "W0L000000000VIN1234"

    LogKit.i(TAG) { "Car VIN=$vin" }
    LogKit.d(TAG) { "Sending token=ABC123XYZ" }
}
```

---

# 🔍 Logcat Output

### Debug Build

```
V/LogKitDemo: Verbose log – only in DEBUG
D/LogKitDemo: Debug log – only in DEBUG
I/LogKitDemo: Info log – shown
```

Sensitive logs allowed (with guard warning).

---

### Release Build

```
I/LogKitDemo: Info log – shown
W/LogKitDemo: Warning log – shown
E/LogKitDemo: Error log – shown
```

Sensitive logs → **hidden**
Verbose/Debug → **hidden**

---

# 🔐 Sensitive Data Guard Logic

Examples:

```kotlin
LogKit.i("TAG") { "VIN=W0L123..." }  // RELEASE → BLOCKED
LogKit.d("TAG") { "token=abcd" }    // RELEASE → BLOCKED
LogKit.e("TAG") { "email=a@b.com" } // RELEASE → BLOCKED
```

LogKit will block these and print a safe replacement.

---
