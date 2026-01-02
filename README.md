# Wisp

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
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

## 🏛️ Architecture & Prerequisites

- **Single-Activity Architecture:** Wisp is designed for a **Single-Activity Architecture** and does not support navigating between different Activities.
- **Jetpack Navigation & Type-Safety:** The library is exclusively designed for the **type-safe navigation** paradigm of Jetpack Navigation Compose. It requires a `NavController` and does not support traditional string-based routes.
- **Multi-Module Support:** Wisp fully supports multi-module projects using a `ServiceLoader` pattern.
- **Minimum Requirements:**
    - **minSdk:** 21 (Android 5.0)
    - **Kotlin:** 1.9.0 or higher (Compatible with KSP)

## Download

[![Maven Central](https://img.shields.io/maven-central/v/io.github.angrypodo/wisp-runtime)](https://central.sonatype.com/artifact/io.github.angrypodo/wisp-runtime)

### Version Catalog

If you're using Version Catalog, you can configure the dependency by adding it to your `libs.versions.toml` file as follows:

```toml
[versions]
#...
wisp = "0.1.0"

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
    implementation("io.github.angrypodo:wisp-runtime:0.1.0")
    ksp("io.github.angrypodo:wisp-processor:0.1.0")

    // if you're using Version Catalog
    // implementation(libs.wisp.runtime)
    // ksp(libs.wisp.processor)
}
```

## Usage

### 1. Define Routes

Designate a deep link destination by adding the `@Wisp` annotation to any `@Serializable` `data class` or `object`.

Route properties are automatically populated from the URI's **query parameters**. If a property has a **default value**, it is considered optional.

```kotlin
// In your navigation or feature module
import com.angrypodo.wisp.annotations.Wisp
import kotlinx.serialization.Serializable

@Serializable
@Wisp("product") // Matches path segment "product"
data class ProductDetail(
    val productId: Int, // Populated from "?productId=..."
    val showReviews: Boolean = false // Optional, populated from "?showReviews=..."
)
```

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
        Wisp.initialize()
    }
}
```
> **Note:** Don't forget to add `android:name=".SampleApplication"` to the `<application>` tag in your `AndroidManifest.xml`.

### 4. Build and Navigate

Construct a deep link URI and use the `navigateTo` extension function on your `NavController`.

- **URI Format:** `scheme://host/path1/path2?paramKey=paramValue`
- **Backstack:** The backstack is built from the URI's **path segments**.
- **Parameters:** Route properties are populated from the URI's **query parameters**.

```kotlin
// This URI creates a backstack: ProductDetail -> UserRoute
// - ProductDetail gets productId=123. 'showReviews' uses its default value (false).
// - UserRoute gets userId=99
val uri = "app://wisp/product/user?productId=123&userId=99".toUri()
navController.navigateTo(uri)
```

## Advanced Usage

### Custom URI Parser

By default, Wisp parses the backstack from the URI path by splitting it with a `/` delimiter. If your deep link scheme requires a different logic (e.g., using `|` as a delimiter), you can provide your own implementation of the `WispUriParser` interface.

```kotlin
val myParser = DefaultWispUriParser(delimiter = "|")
Wisp.initialize(parser = myParser)
```

## ⚠️ Constraints & Considerations Summary

- **Parameter Source:** Route parameters are populated **exclusively from URI query parameters**. The path is used only for defining the backstack sequence.
- **Kotlinx Serialization:** Wisp relies heavily on `kotlinx.serialization` to deserialize query parameters into your route data classes.
- **Parameter Naming:** The query parameter keys in the URI must exactly match the property names in your route `data class`.

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
