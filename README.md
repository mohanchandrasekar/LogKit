Here is a polished, **GitHub-ready README.md**, fully aligned with your library, setup, and test app code.
Completely copy-pasteable. Professional, clean, and developer-friendly.

---

# 📘 LogKit – Safe, Clickable, Automotive-Aware Logging for Android

**LogKit** is a lightweight logging library for Android & Android Automotive apps, designed with:

✅ Simple Kotlin & Java API (similar to Timber)
✅ Clickable file/line navigation in Logcat
✅ Automatic removal of VERBOSE/DEBUG logs in release builds
✅ Sensitive-data detection & blocking (VIN, VehiclePropertyIds, tokens, GPS, PII…)
✅ Zero-config integration
✅ BuildConfig-aware behavior

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

### ✔ Sensitive Log Protection

LogKit scans messages for automotive-sensitive terms:

* **VIN**, vehicle ID, chassis ID
* **VehiclePropertyIds**, VHAL values
* **Battery / SOC / range**
* **Location (lat/lng/gps)**
* **Tokens / session IDs / passwords**
* **User PII (email, phone, contacts)**
* **API URLs, internal endpoints**

Behavior based on `BuildConfig.ALLOW_SENSITIVE_LOGS`:

* Debug: allowed but warns (`LogKitGuard`)
* Release: **blocked completely**

### ✔ Clickable file/line in Logcat

LogKit injects:

```
at com.example.MainActivity.onCreate(MainActivity.kt:42)
```

Android Studio makes this clickable → instantly jumps to the source.

---

# 📦 Installation

### 1️⃣ Add module to project

Place LogKit inside your project:

```
root/
 ├─ app/
 └─ logkit/   ← this library module
```

In `settings.gradle.kts`:

```kotlin
include(":app", ":logkit")
```

In `app/build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":logkit"))
}
```

---

# ⚙️ App Setup

### 2️⃣ Enable BuildConfig flags

Inside **app module** `app/build.gradle.kts`:

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

### 3️⃣ Initialize LogKit in Application

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

Add in `AndroidManifest.xml`:

```xml
<application
    android:name=".MyApp"
    ... >
```

---

# 🧪 Example Usage (Your Test App Code)

### MainActivity

```kotlin
package com.logkit.core

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

private const val TAG = "LogKitDemo"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        testNormalLogs()
        testSensitiveLogs()
    }

    private fun testNormalLogs() {
        LogKit.v(TAG) { "Verbose log – should appear only in DEBUG build" }
        LogKit.d(TAG) { "Debug log – should appear only in DEBUG build" }
        LogKit.i(TAG) { "Info log – should appear in DEBUG + RELEASE" }
        LogKit.w(TAG) { "Warning log – should appear in DEBUG + RELEASE" }
        LogKit.e(TAG) { "Error log – should appear in DEBUG + RELEASE" }
    }

    private fun testSensitiveLogs() {
        val vin = "W0L000000000VIN1234"

        LogKit.i(TAG) { "Car VIN=$vin" }
        LogKit.d(TAG) { "Debug: sending token=ABC123XYZ for telemetry" }
    }
}
```

---

# 🔍 What You'll See in Logcat

### Debug build:

```
at com.example.MainActivity.testNormalLogs(MainActivity.kt:34)
Verbose log – should appear only in DEBUG build
```

### Release build:

```
Info log – should appear in DEBUG + RELEASE
Warning log – should appear in DEBUG + RELEASE
Error log – should appear in DEBUG + RELEASE
```

Sensitive logs:

* Allowed in debug (with a guard warning)
* Blocked in release

---

# 🔐 Sensitive Data Guard Logic

LogKit auto-detects and blocks:

* VIN
* VehiclePropertyIds, VHAL
* SOC, battery, range
* lat/lng/GPS
* token, access_token, refresh_token
* email, phone_number, user_id
* internal API URLs

Behavior example:

```kotlin
LogKit.i("TAG") { "VIN=W0L123..." }  // RELEASE → BLOCKED
LogKit.d("TAG") { "token=abcd" }    // RELEASE → BLOCKED
```
