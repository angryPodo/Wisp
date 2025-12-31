# Wisp

[![CI](https://github.com/angrypodo/wisp/actions/workflows/ci.yml/badge.svg)](https://github.com/angrypodo/wisp/actions/workflows/ci.yml)

**Wisp** is a type-safe, server-driven deep link library for Jetpack Compose. It allows you to dynamically build your navigation backstack from a single, standard URI, overcoming the static backstack limitations of the `navigation-compose` library.

translation: [Read in Korean (한국어)](./README.ko.md)

## 🤔 Why Wisp?

Standard deep links in Jetpack Compose often lead to predefined, static backstacks. It's challenging to implement scenarios where a server needs to dictate a dynamic user journey on the fly (e.g., `Product Screen -> Coupon Screen -> Checkout Screen`).

Wisp automates this process by building the entire backstack from the URI's path segments. It uses annotation processing (KSP) to generate the necessary boilerplate, allowing you to focus solely on defining your routes.

## 🏛️ Architecture & Prerequisites

- **Single-Activity Architecture:** Wisp is designed for a **Single-Activity Architecture** and does not support navigating between different Activities. This aligns with the modern Android development practices recommended for Jetpack Compose.
- **Jetpack Navigation & Type-Safety:** The library is an extension of Jetpack Navigation Compose and is exclusively designed for its **type-safe navigation** paradigm. It requires a `NavController` and does not support traditional string-based routes.
- **Multi-Module Support:** Wisp fully supports multi-module projects. It automatically discovers `@Wisp` route definitions from all modules that include the library, using a `ServiceLoader` pattern.

## 🚀 How to Use

**Note:** Wisp is not yet published to Maven Central. To use it, you currently need to clone this repository and include the modules in your project locally.

### 1. Define Routes

Designate a deep link destination by adding the `@Wisp` annotation to any `@Serializable` `data class` or `object`. The string passed to `@Wisp` is the path segment that will be used in the deep link URI.

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

For deep links to be accessible from outside your app, you must register an `<intent-filter>` in your `AndroidManifest.xml`. Both `scheme` and `host` are required.

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

## 🧪 Testing

### Running the Sample App

1.  Clone this repository and open it in Android Studio.
2.  Select the `app` run configuration and run it on an emulator or a physical device.
3.  Use the buttons in the app to test navigation.

### Testing with ADB

You can test your deep links directly from the command line using `adb`. This is a great way to simulate a link click from an external source.

```bash
adb shell am start -a android.intent.action.VIEW -d "app://wisp/product/user?productId=123&userId=99"
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

## 📜 License

```
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
