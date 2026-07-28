# Wisp

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![API](https://img.shields.io/badge/API-28%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=28)
[![Build Status](https://github.com/angrypodo/wisp/actions/workflows/ci.yml/badge.svg)](https://github.com/angrypodo/wisp/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.angrypodo/wisp-runtime)](https://central.sonatype.com/artifact/io.github.angrypodo/wisp-runtime)

translation: [Read in Korean (한국어)](./README.ko.md)

<br>

<p align="center">
  <strong>Wisp</strong> is a type-safe, server-driven deep link library for Jetpack Compose.<br>
  It allows you to dynamically build your navigation backstack from a single, standard URI,<br>
  overcoming the static backstack limitations of the <code>navigation-compose</code> library.
</p>

<br>

## 🤔 Why Wisp?

Standard deep links in Jetpack Compose often lead to predefined, static backstacks. It's challenging to implement scenarios where a server needs to dictate a dynamic user journey on the fly (e.g., `Product Screen -> Coupon Screen -> Checkout Screen`).

Wisp automates this process by building the entire backstack from the URI's path segments. It uses annotation processing (KSP) to generate the necessary boilerplate, allowing you to focus solely on defining your routes.

### Standard Deep Links vs Wisp

|  | Standard Deep Links (`navDeepLink`) | Wisp |
|---|---|---|
| What a URI maps to | A single destination | The **entire backstack** |
| Backstack shape | Synthesized from your nested graph structure — fixed at compile time | Built from the URI's path segments — decided by the server at runtime |
| Changing a user journey | Restructure the nav graph → **app release required** | Change the URI the server sends → **no release** |
| Setup per screen | `deepLinks = listOf(navDeepLink<...>(...))` on each destination + graph design | One `@Wisp("path")` annotation |
| Unregistered link | Crash or silent fallback you must hand-roll | `WispResult.Failure` + `onError` callback |

With standard deep links, `app://shop/checkout` can only ever open Checkout on top of the hierarchy you hard-coded. With Wisp, the server can send `app://wisp/product/123/coupon/42/checkout` today and `app://wisp/checkout` tomorrow — **the journey is data, not code.**

## 🏛️ Architecture & Prerequisites

- **Single-Activity Architecture:** Wisp is designed for a **Single-Activity Architecture** and does not support navigating between different Activities.
- **Jetpack Navigation & Type-Safety:** The library is exclusively designed for the **type-safe navigation** paradigm of Jetpack Navigation Compose. It requires a `NavController` and does not support traditional string-based routes.
- **Multi-Module Support:** Wisp fully supports multi-module projects using a `ServiceLoader` pattern.
- **Minimum Requirements:**
    - **minSdk:** 28 (Android 9.0)
    - **Kotlin:** 2.0 or higher (with a KSP version matching your Kotlin version)

## Download

[![Maven Central](https://img.shields.io/maven-central/v/io.github.angrypodo/wisp-runtime)](https://central.sonatype.com/artifact/io.github.angrypodo/wisp-runtime)

### Version Catalog

If you're using Version Catalog, you can configure the dependency by adding it to your `libs.versions.toml` file as follows:

```toml
[versions]
#...
wisp = "0.2.0"

[libraries]
#...
wisp-runtime = { module = "io.github.angrypodo:wisp-runtime", version.ref = "wisp" }
wisp-processor = { module = "io.github.angrypodo:wisp-processor", version.ref = "wisp" }
```

### Gradle

Add the KSP plugin to your project-level `build.gradle.kts`. **Make sure to use a KSP version that matches your Kotlin version.** (Check [KSP Releases](https://github.com/google/ksp/releases))

```kotlin
plugins {
    id("com.google.devtools.ksp") version "YOUR_KSP_VERSION" apply false
}
```

Then, add the dependencies to your **module**'s `build.gradle.kts` file:

```kotlin
plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    implementation("io.github.angrypodo:wisp-runtime:0.2.0")
    ksp("io.github.angrypodo:wisp-processor:0.2.0")

    // if you're using Version Catalog
    // implementation(libs.wisp.runtime)
    // ksp(libs.wisp.processor)
}
```

## Usage

### 1. Define Routes

Designate a deep link destination by adding the `@Wisp` annotation to any `@Serializable` `data class` or `object`.

Route properties are populated from the URI's **path parameters** (declared as `{placeholder}` segments) and **query parameters**. If a property has a **default value**, it is considered optional.

```kotlin
// In your navigation or feature module
import com.angrypodo.wisp.annotations.Wisp
import kotlinx.serialization.Serializable

@Serializable
@Wisp("product/{productId}") // Matches "product/123" and captures productId
data class ProductDetail(
    val productId: Int, // Populated from the {productId} path parameter
    val showReviews: Boolean = false // Optional, populated from "?showReviews=..."
)
```

> **Rule of thumb — required goes in the path, optional goes in the query.**
> Declare **required** properties as `{placeholder}` path segments, and pass **optional** properties (those with default values) as query parameters. Multiple required values are fine: `@Wisp("board/{boardId}/post/{postId}")`. This mirrors REST conventions, so the contract with your server team is one sentence long.

### 2. Configure the Manifest

Register an `<intent-filter>` in your `AndroidManifest.xml`. Both `scheme` and `host` are required.

```xml
<!-- In AndroidManifest.xml -->
<activity ... >
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="app" android:host="wisp" />
    </intent-filter>
</activity>
```

### 3. Initialize Wisp

In your `Application` class, call `Wisp.initialize()`.

```kotlin
// In your app's Application class
import android.app.Application
import com.angrypodo.wisp.runtime.Wisp

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Wisp. It will automatically find all route registries.
        // onError is invoked whenever a deep link fails to resolve or navigate.
        Wisp.initialize(
            onError = { error -> Log.e("Wisp", "Deep link failed", error) }
        )
    }
}
```
> **Note:** Don't forget to add `android:name=".SampleApplication"` to the `<application>` tag in your `AndroidManifest.xml`.

### 4. Build and Navigate

Construct a deep link URI and use the `navigateTo` extension function on your `NavController`.

- **URI Format:** `scheme://host/pattern1/pattern2?paramKey=paramValue`
- **Backstack:** The backstack is built from the URI's **path segments**. A single route pattern can consume multiple segments (e.g. `product/{productId}` consumes `product/123`).
- **Parameters:** Route properties are populated from **path parameters** and **query parameters**. When both provide the same key, the path parameter wins.

```kotlin
// This URI creates a backstack: ProductDetail -> UserRoute
// - ProductDetail gets productId=123 from the path. 'showReviews' uses its default value (false).
// - UserRoute gets userId=99 from the path.
val uri = "app://wisp/product/123/user/99".toUri()
val result: WispResult = navController.navigateTo(uri)
```

`navigateTo` never throws. It returns a `WispResult` (`Success` or `Failure`), and every failure is also delivered to the `onError` callback passed to `Wisp.initialize()`. This matters for FCM-driven deep links: an older app version receiving a newer link degrades gracefully instead of crashing.

### 5. Gate Deep Links Behind Your Splash Screen (Optional)

If your start destination performs login/token validation, do not execute the deep link immediately — the navigation would remove the splash screen before its validation completes. Instead, **defer** the deep link and execute it after validation:

```kotlin
// In your Activity
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Wisp.defer(intent?.data) // Hold the deep link instead of executing it now.
    setContent { /* ... */ }
}
```

> `defer` performs **no navigation** — it only stores the URI. Your NavHost still starts at the splash destination as usual, and the stored link is executed only when you call `navigateToDeferred()`.

```kotlin
// In your splash screen, after validation succeeds:
val result = navController.navigateToDeferred()
if (result == null || result is WispResult.Failure) {
    // No deep link was pending (or it failed) — go to your default destination.
    navController.navigate(Home) { popUpTo(Splash) { inclusive = true } }
}
```

> **Warm starts:** Declare `android:launchMode="singleTop"` on your single Activity and receive warm-start deep links in `onNewIntent`. Since that session already passed splash validation, you can usually execute them immediately with `navController.navigateTo(uri)` — or defer them again if your app requires re-validation.

## 🧪 Testing

### Running the Sample App

1.  Clone this repository and open it in Android Studio.
2.  Select the `app` run configuration and run it on an emulator or a physical device.
3.  Use the buttons in the app to test navigation.

### Testing with ADB

You can test your deep links directly from the command line using `adb`. This is a great way to simulate a link click from an external source.

**Important:** When testing multiple parameters on the command line, you must escape the `&` character (`\&`) or wrap the entire URI in single quotes to prevent the shell from interpreting it as a background command.

```bash
# Escape the '&' character with a backslash
adb shell am start -a android.intent.action.VIEW -d "app://wisp/product/123/user/99?showReviews=true"
```

## Advanced Usage

### Custom URI Parser

By default, Wisp parses the backstack from the URI path by splitting it with a `/` delimiter. If your deep link scheme requires a different logic (e.g., using `|` as a delimiter), you can provide your own implementation of the `WispUriParser` interface.

```kotlin
val myParser = DefaultWispUriParser(delimiter = "|")
Wisp.initialize(parser = myParser)
```

## ⚠️ Constraints & Considerations Summary

- **Parameter Source:** Route parameters are populated from **path parameters** (`{placeholder}` segments) and **query parameters**. Path parameters take precedence when both provide the same key.
- **Query Parameters Are Shared:** Query parameters are visible to **every** route in the URI. If two routes in one backstack need different values for the same property name, pass them as path parameters instead.
- **Pattern Rules:** A `@Wisp` path must start with a literal segment, and every `{placeholder}` must match a constructor property name. Violations are reported at **compile time** by the KSP processor.
- **Kotlinx Serialization:** Wisp relies heavily on `kotlinx.serialization` to deserialize parameters into your route data classes.
- **Parameter Naming:** Path placeholder names and query parameter keys must exactly match the property names in your route `data class`.
- **No Crashes on Unknown Links:** Unregistered paths, missing parameters, and conversion failures return `WispResult.Failure` (and invoke `onError`) instead of throwing.

# License

```xml
Copyright 2025 angrypodo

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```