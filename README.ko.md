# Wisp

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![API](https://img.shields.io/badge/API-28%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=28)
[![Build Status](https://github.com/angrypodo/wisp/actions/workflows/ci.yml/badge.svg)](https://github.com/angrypodo/wisp/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.angrypodo/wisp-runtime)](https://central.sonatype.com/artifact/io.github.angrypodo/wisp-runtime)

번역: [Read in English](README.md)

<br>

<p align="center">
  <strong>Wisp</strong>는 Jetpack Compose를 위한 타입 세이프(type-safe), 서버 주도(server-driven) 딥링크 라이브러리입니다.<br>
  단일 표준 URI를 기반으로 내비게이션 백스택을 동적으로 구성할 수 있게 하여,<br>
  표준 <code>navigation-compose</code> 라이브러리의 정적 백스택 한계를 극복합니다.
</p>

<br>

## 🤔 Wisp, 왜 필요한가요?

Jetpack Compose의 표준 딥링크 기능은 미리 정의된 정적 백스택을 만드는 데 주로 사용됩니다. 이 때문에 서버가 실시간으로 동적인 사용자 여정(예: `상품 화면 -> 쿠폰 화면 -> 결제 화면`)을 제어해야 하는 시나리오를 구현하기는 어렵습니다.

Wisp는 URI의 경로 세그먼트(path segments)로부터 전체 백스택을 생성하여 이 과정을 자동화합니다. 어노테이션 처리(KSP)를 사용하여 필요한 보일러플레이트 코드를 생성하므로, 개발자는 라우트 정의에만 집중할 수 있습니다.

### 표준 딥링크 vs Wisp

|  | 표준 딥링크 (`navDeepLink`) | Wisp |
|---|---|---|
| URI가 가리키는 것 | 단일 목적지 하나 | **백스택 전체** |
| 백스택 모양 | 중첩 그래프 구조로부터 합성 — 컴파일 타임에 고정 | URI 경로 세그먼트로부터 생성 — 서버가 런타임에 결정 |
| 사용자 여정 변경 | 내비게이션 그래프 재구성 → **앱 배포 필요** | 서버가 보내는 URI만 변경 → **배포 불필요** |
| 화면당 설정 | 목적지마다 `deepLinks = listOf(navDeepLink<...>(...))` + 그래프 설계 | `@Wisp("path")` 어노테이션 하나 |
| 미등록 링크 수신 | 크래시 또는 직접 구현해야 하는 fallback | `WispResult.Failure` + `onError` 콜백 |

표준 딥링크에서 `app://shop/checkout`은 하드코딩된 계층 위에 Checkout을 여는 것만 가능합니다. Wisp에서는 서버가 오늘은 `app://wisp/product/123/coupon/42/checkout`을, 내일은 `app://wisp/checkout`을 보낼 수 있습니다 — **여정은 코드가 아니라 데이터입니다.**

## 🏛️ 아키텍처 및 요구사항

- **싱글 액티비티 아키텍처:** Wisp는 싱글 액티비티 구조(Single-Activity Architecture)를 위해 설계되었으며, 여러 Activity 간의 내비게이션은 지원하지 않습니다. 이는 Jetpack Compose에 권장되는 최신 안드로이드 개발 방식과 일치합니다.
- **Jetpack Navigation 및 타입 안정성:** 이 라이브러리는 Jetpack Navigation Compose의 **타입 세이프(type-safe) 내비게이션** 패러다임 전용으로 설계되었습니다. `NavController`가 반드시 필요하며, 전통적인 문자열 기반의 라우트는 지원하지 않습니다.
- **멀티 모듈 지원:** Wisp는 멀티 모듈 프로젝트를 완벽하게 지원합니다. `ServiceLoader` 패턴을 사용하여, 라이브러리가 포함된 모든 모듈로부터 `@Wisp` 라우트 정의를 자동으로 탐지합니다.
- **최소 요구사항:**
    - **minSdk:** 28 (Android 9.0)
    - **Kotlin:** 2.0 이상 (사용하는 Kotlin 버전과 일치하는 KSP 버전 필요)

## 다운로드 (Download)

[![Maven Central](https://img.shields.io/maven-central/v/io.github.angrypodo/wisp-runtime)](https://central.sonatype.com/artifact/io.github.angrypodo/wisp-runtime)

### Version Catalog

Version Catalog를 사용 중이라면, `libs.versions.toml` 파일에 다음과 같이 의존성을 추가할 수 있습니다:

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

프로젝트 수준의 `build.gradle.kts` 파일에 KSP 플러그인을 추가합니다. **반드시 사용하는 Kotlin 버전과 호환되는 KSP 버전을 사용하세요.** ([KSP 릴리즈 확인](https://github.com/google/ksp/releases))

```kotlin
plugins {
    id("com.google.devtools.ksp") version "YOUR_KSP_VERSION" apply false
}
```

그리고 **모듈** 수준의 `build.gradle.kts` 파일에 의존성을 추가합니다:

```kotlin
plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    implementation("io.github.angrypodo:wisp-runtime:0.2.0")
    ksp("io.github.angrypodo:wisp-processor:0.2.0")

    // Version Catalog를 사용하는 경우
    // implementation(libs.wisp.runtime)
    // ksp(libs.wisp.processor)
}
```

## 사용법 (Usage)

### 1. 라우트 정의하기

`@Serializable` 어노테이션이 달린 `data class`나 `object`에 `@Wisp` 어노테이션을 추가하여 딥링크 대상을 지정합니다.

라우트 클래스의 속성들은 URI의 **패스 파라미터**(`{placeholder}` 세그먼트)와 **쿼리 파라미터**로부터 값이 채워집니다. 만약 속성에 기본값(default value)이 있다면, 해당 속성은 선택적(optional)인 값이 됩니다.

```kotlin
// 내비게이션 또는 기능 모듈 내부
import com.angrypodo.wisp.annotations.Wisp
import kotlinx.serialization.Serializable

@Serializable
@Wisp("product/{productId}") // "product/123"과 매칭되며 productId를 추출
data class ProductDetail(
    val productId: Int, // {productId} 패스 파라미터로부터 값을 받음
    val showReviews: Boolean = false // 선택적. "?showReviews=..." 값이 없으면 false 사용
)
```

> **규칙 — 필수는 패스에, 옵션은 쿼리에.**
> **필수** 프로퍼티는 `{placeholder}` 경로 세그먼트로 선언하고, **선택** 프로퍼티(기본값이 있는 것)는 쿼리 파라미터로 전달하세요. 필수 값이 여러 개여도 됩니다: `@Wisp("board/{boardId}/post/{postId}")`. REST 관례 그대로라서 서버 팀과의 계약이 이 한 문장으로 끝납니다.

### 2. Manifest 설정하기

딥링크를 앱 외부에서 사용 가능하게 하려면 `AndroidManifest.xml`에 `<intent-filter>`를 등록해야 합니다. `scheme`과 `host`가 모두 필요합니다.

```xml
<!-- AndroidManifest.xml 내부 -->
<activity ... >
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="app" android:host="wisp" />
    </intent-filter>
</activity>
```

### 3. Wisp 초기화하기

`Application` 클래스에서 `Wisp.initialize()`를 호출합니다.

```kotlin
// 앱의 Application 클래스 내부
import android.app.Application
import com.angrypodo.wisp.runtime.Wisp

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Wisp를 초기화합니다. 모든 모듈의 라우트 레지스트리를 자동으로 찾습니다.
        // onError는 딥링크 해석/탐색에 실패할 때마다 호출됩니다.
        Wisp.initialize(
            onError = { error -> Log.e("Wisp", "Deep link failed", error) }
        )
    }
}
```
> **참고:** `AndroidManifest.xml`의 `<application>` 태그에 `android:name=".SampleApplication"` 속성을 추가하는 것을 잊지 마세요.

### 4. URI 빌드 및 내비게이션

딥링크 URI를 구성하고 `NavController`의 `navigateTo` 확장 함수를 사용합니다.

- **URI 형식:** `scheme://host/패턴1/패턴2?파라미터Key=파라미터Value`
- **백스택:** 백스택은 URI의 **경로 세그먼트**로부터 생성됩니다. 하나의 라우트 패턴이 여러 세그먼트를 소비할 수 있습니다. (예: `product/{productId}`는 `product/123`을 소비)
- **파라미터:** 라우트 속성은 **패스 파라미터**와 **쿼리 파라미터**로부터 채워집니다. 같은 키가 양쪽에 있으면 패스 파라미터가 우선합니다.

```kotlin
// 이 URI는 ProductDetail -> UserRoute 백스택을 생성합니다.
// - ProductDetail은 경로에서 productId=123을 받습니다. 'showReviews'는 기본값(false)을 사용합니다.
// - UserRoute는 경로에서 userId=99를 받습니다.
val uri = "app://wisp/product/123/user/99".toUri()
val result: WispResult = navController.navigateTo(uri)
```

`navigateTo`는 예외를 던지지 않습니다. `WispResult`(`Success` 또는 `Failure`)를 반환하며, 모든 실패는 `Wisp.initialize()`에 전달한 `onError` 콜백으로도 전달됩니다. FCM 기반 딥링크에서 특히 중요합니다: 구버전 앱이 신버전 링크를 받아도 크래시 없이 안전하게 처리됩니다.

### 5. 스플래시 화면 뒤로 딥링크 보류하기 (선택)

시작 목적지(스플래시)에서 로그인/토큰 검증을 수행한다면, 딥링크를 즉시 실행하면 안 됩니다. 검증이 끝나기 전에 내비게이션이 스플래시 화면을 제거해버리기 때문입니다. 대신 딥링크를 **보류(defer)**했다가 검증 완료 후 실행하세요:

```kotlin
// Activity 내부
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Wisp.defer(intent?.data) // 딥링크를 즉시 실행하지 않고 보류합니다.
    setContent { /* ... */ }
}
```

> `defer`는 **내비게이션을 수행하지 않습니다** — URI를 저장만 합니다. NavHost는 평소처럼 스플래시(startDestination)부터 시작하며, 저장된 딥링크는 `navigateToDeferred()`를 호출할 때 비로소 실행됩니다.

```kotlin
// 스플래시 화면에서 검증이 성공한 뒤:
val result = navController.navigateToDeferred()
if (result == null || result is WispResult.Failure) {
    // 보류된 딥링크가 없거나 실패했으면 기본 목적지로 이동합니다.
    navController.navigate(Home) { popUpTo(Splash) { inclusive = true } }
}
```

> **웜 스타트:** 싱글 액티비티에 `android:launchMode="singleTop"`을 선언하고, 앱이 살아있는 상태의 딥링크는 `onNewIntent`에서 받으세요. 해당 세션은 이미 스플래시 검증을 통과했으므로 보통 `navController.navigateTo(uri)`로 즉시 실행하면 되고, 재검증이 필요한 앱이라면 다시 보류(defer)하면 됩니다.

## 🧪 테스트 방법 (Testing)

### 샘플 앱 실행하기

1.  이 리포지토리를 클론하여 Android Studio에서 엽니다.
2.  `app` 실행 구성을 선택하고 에뮬레이터나 실제 기기에서 실행합니다.
3.  앱 내의 버튼을 눌러 내비게이션을 테스트합니다.

### ADB로 테스트하기

`adb`를 사용하여 커맨드 라인에서 직접 딥링크를 테스트할 수 있습니다. 이는 외부 소스로부터의 링크 클릭을 시뮬레이션하는 좋은 방법입니다.

**중요:** 커맨드 라인에서 여러 파라미터를 테스트할 때는 `&` 문자가 백그라운드 실행 명령으로 인식되지 않도록 `\&`로 이스케이프하거나 전체 URI를 따옴표로 감싸야 합니다.

```bash
# '&' 문자를 백슬래시(\)로 이스케이프합니다
adb shell am start -a android.intent.action.VIEW -d "app://wisp/product/123/user/99?showReviews=true"
```

## 고급 사용법 (Advanced Usage)

### 커스텀 URI 파서

기본적으로 Wisp는 URI 경로를 `/` 구분자로 분리하여 백스택을 파싱합니다. 만약 딥링크 스킴이 다른 로직(예: `|` 구분자 사용)을 요구한다면, `WispUriParser` 인터페이스의 자신만의 구현체를 제공할 수 있습니다.

```kotlin
val myParser = DefaultWispUriParser(delimiter = "|")
Wisp.initialize(parser = myParser)
```

## ⚠️ 제약사항 요약

- **파라미터 소스:** 라우트 파라미터는 **패스 파라미터**(`{placeholder}` 세그먼트)와 **쿼리 파라미터**로부터 채워집니다. 같은 키가 양쪽에 있으면 패스 파라미터가 우선합니다.
- **쿼리 파라미터는 공유됩니다:** 쿼리 파라미터는 URI에 포함된 **모든** 라우트에 전달됩니다. 한 백스택의 두 라우트가 같은 속성 이름에 서로 다른 값을 필요로 한다면 패스 파라미터를 사용하세요.
- **패턴 규칙:** `@Wisp` 경로는 리터럴 세그먼트로 시작해야 하며, 모든 `{placeholder}`는 생성자 프로퍼티 이름과 일치해야 합니다. 위반 시 KSP 프로세서가 **컴파일 타임**에 에러를 보고합니다.
- **Kotlinx Serialization:** Wisp는 파라미터를 라우트 데이터 클래스로 역직렬화하기 위해 `kotlinx.serialization`에 크게 의존합니다.
- **파라미터 이름:** 패스 placeholder 이름과 쿼리 파라미터 키는 라우트 `data class`의 속성 이름과 정확히 일치해야 합니다.
- **알 수 없는 링크에도 크래시 없음:** 미등록 경로, 누락된 파라미터, 타입 변환 실패는 예외를 던지지 않고 `WispResult.Failure`를 반환하며 `onError`를 호출합니다.

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
