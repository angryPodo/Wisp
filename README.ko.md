# Wisp

[![CI](https://github.com/angrypodo/wisp/actions/workflows/ci.yml/badge.svg)](https://github.com/angrypodo/wisp/actions/workflows/ci.yml)

**Wisp**는 Jetpack Compose를 위한 타입 세이프(type-safe), 서버 주도(server-driven) 딥링크 라이브러리입니다. 단일 표준 URI를 기반으로 내비게이션 백스택을 동적으로 구성할 수 있게 하여, 표준 `navigation-compose` 라이브러리의 정적 백스택 한계를 극복합니다.

## 🤔 Wisp, 왜 필요한가요?

Jetpack Compose의 표준 딥링크 기능은 미리 정의된 정적 백스택을 만드는 데 주로 사용됩니다. 이 때문에 서버가 실시간으로 동적인 사용자 여정(예: `상품 화면 -> 쿠폰 화면 -> 결제 화면`)을 제어해야 하는 시나리오를 구현하기는 어렵습니다.

Wisp는 URI의 경로 세그먼트(path segments)로부터 전체 백스택을 생성하여 이 과정을 자동화합니다. 어노테이션 처리(KSP)를 사용하여 필요한 보일러플레이트 코드를 생성하므로, 개발자는 라우트 정의에만 집중할 수 있습니다.

## 🏛️ 아키텍처 및 요구사항

- **싱글 액티비티 아키텍처 (Single-Activity Architecture):** Wisp는 **싱글 액티비티 구조** 전용으로 설계되었으며, 여러 Activity 간의 내비게이션은 지원하지 않습니다. 이는 Jetpack Compose에 권장되는 최신 안드로이드 개발 방식과 일치합니다.
- **Jetpack Navigation 및 타입 안정성:** 이 라이브러리는 Jetpack Navigation Compose의 확장 기능이며, **타입 세이프(type-safe) 내비게이션** 패러다임 전용으로 설계되었습니다. `NavController`가 반드시 필요하며, 전통적인 문자열 기반의 라우트는 지원하지 않습니다.
- **멀티 모듈 지원 (Multi-Module Support):** Wisp는 멀티 모듈 프로젝트를 완벽하게 지원합니다. `ServiceLoader` 패턴을 사용하여, 라이브러리가 포함된 모든 모듈로부터 `@Wisp` 라우트 정의를 자동으로 탐지합니다.

## 🚀 사용법

**참고:** Wisp는 아직 Maven Central에 배포되지 않았습니다. 현재로서는 이 리포지토리를 클론하여 프로젝트에 로컬 모듈로 포함해야 합니다.

### 1. 라우트 정의하기

`@Serializable` 어노테이션이 달린 `data class`나 `object`에 `@Wisp` 어노테이션을 추가하여 딥링크 대상을 지정합니다. `@Wisp`에 전달하는 문자열은 딥링크 URI에서 사용될 경로 세그먼트가 됩니다.

라우트 클래스의 속성들은 URI의 **쿼리 파라미터**로부터 자동으로 값이 채워집니다. 만약 속성에 **기본값(default value)**이 있다면, 해당 속성은 선택적(optional)인 값이 됩니다.

```kotlin
// 내비게이션 또는 기능 모듈 내부
import com.angrypodo.wisp.annotations.Wisp
import kotlinx.serialization.Serializable

@Serializable
@Wisp("product") // "product" 경로와 매칭
data class ProductDetail(
    val productId: Int, // "?productId=..." 로부터 값을 받음
    val showReviews: Boolean = false // 선택적. "?showReviews=..." 값이 없으면 false 사용
)
```

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
        Wisp.initialize()
    }
}
```
> **참고:** `AndroidManifest.xml`의 `<application>` 태그에 `android:name=".SampleApplication"` 속성을 추가하는 것을 잊지 마세요.

### 4. URI 빌드 및 내비게이션

딥링크 URI를 구성하고 `NavController`의 `navigateTo` 확장 함수를 사용합니다.

- **URI 형식:** `scheme://host/경로1/경로2?파라미터Key=파라미터Value`
- **백스택:** 백스택은 URI의 **경로 세그먼트**로부터 생성됩니다.
- **파라미터:** 라우트 속성은 URI의 **쿼리 파라미터**로부터 채워집니다.

```kotlin
// "app://wisp/product/user?productId=123&userId=99" URI는
// ProductDetail(productId=123) -> UserRoute(userId=99) 백스택을 생성합니다.
val uri = "app://wisp/product/user?productId=123&userId=99".toUri()
navController.navigateTo(uri)
```

## 🧪 테스트 방법

### 샘플 앱 실행하기

1.  이 리포지토리를 클론하여 Android Studio에서 엽니다.
2.  `app` 실행 구성을 선택하고 에뮬레이터나 실제 기기에서 실행합니다.
3.  앱 내의 버튼을 눌러 내비게이션을 테스트합니다.

### ADB로 테스트하기

`adb`를 사용하여 커맨드 라인에서 직접 딥링크를 테스트할 수 있습니다. 이는 외부 소스로부터의 링크 클릭을 시뮬레이션하는 좋은 방법입니다.

```bash
adb shell am start -a android.intent.action.VIEW -d "app://wisp/product/user?productId=123&userId=99"
```

## 고급 사용법

### 커스텀 URI 파서

기본적으로 Wisp는 URI 경로를 `/` 구분자로 분리하여 백스택을 파싱합니다. 만약 딥링크 스킴이 다른 로직(예: `|` 구분자 사용)을 요구한다면, `WispUriParser` 인터페이스의 자신만의 구현체를 제공할 수 있습니다.

```kotlin
val myParser = DefaultWispUriParser(delimiter = "|")
Wisp.initialize(parser = myParser)
```

## ⚠️ 제약사항 요약

- **파라미터 소스:** 라우트 파라미터는 **오직 URI 쿼리 파라미터**를 통해서만 전달됩니다. 경로(path)는 백스택 순서를 정의하는 데에만 사용됩니다.
- **Kotlinx Serialization:** Wisp는 쿼리 파라미터를 라우트 데이터 클래스로 역직렬화하기 위해 `kotlinx.serialization`에 크게 의존합니다.
- **파라미터 이름:** URI의 쿼리 파라미터 키는 라우트 `data class`의 속성 이름과 정확히 일치해야 합니다.

## 📜 라이선스

```
Copyright 2024 angrypodo

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