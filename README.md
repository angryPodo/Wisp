# Wisp: 타입 세이프 서버 주도 딥링크 라이브러리

**Wisp**는 Jetpack Compose의 타입 세이프 네비게이션 환경에서, 서버가 동적으로 정의하는 백스택을 화면 깜빡임 없이 손쉽게 탐색할 수 있게 해주는 어노테이션 기반 딥링크 라이브러리입니다.

## 🤔 왜 Wisp인가요?

Jetpack Compose 환경에서 `navigation-compose`의 기본 딥링크는 정적인 백스택만 생성할 수 있어, 서버가 동적으로 사용자 여정(User Journey)을 제어하려는 요구사항을 충족하기 어렵습니다. Wisp는 이 과정을 자동화하여 개발자가 오직 **라우트 정의**에만 집중할 수 있도록 돕습니다.

## ✨ 핵심 원칙

-   **서버 주도 (Server-Driven):** 백스택 구성의 모든 권한은 서버가 갖습니다.
-   **단순함 (Simplicity):** 개발자는 라우트 클래스 정의와 어노테이션 추가 외에 복잡한 로직을 신경 쓰지 않습니다.
-   **유연성 (Flexibility):** URI 파싱 로직을 외부에서 주입할 수 있어, 어떤 형태의 딥링크 URI 스킴(Scheme)이라도 지원할 수 있습니다.

## 🛠️ 설치

**1. `build.gradle.kts` (Project Level)**
`settings.gradle.kts`가 아닌 프로젝트 레벨의 `build.gradle.kts`에 KSP 플러그인을 추가합니다.
```kotlin
plugins {
    // ...
    alias(libs.plugins.ksp) apply false
}
```

**2. `build.gradle.kts` (App Module Level)**
`app` 모듈의 `build.gradle.kts`에 플러그인과 의존성을 추가합니다.
```kotlin
plugins {
    // ...
    alias(libs.plugins.ksp)
}

dependencies {
    // Wisp
    implementation(project(":wisp-runtime"))
    ksp(project(":wisp-processor"))

    // ... 기타 의존성
}
```

## 🚀 사용법

### 1. 라우트 정의

`@Serializable` 어노테이션이 붙은 `data class` 또는 `object`에 `@Wisp` 어노테이션을 추가하여 딥링크 대상으로 지정합니다.

```kotlin
// app/src/main/java/com/example/app/Routes.kt

import com.angrypodo.wisp.annotations.Wisp
import kotlinx.serialization.Serializable

@Serializable
@Wisp("home")
data object Home

@Serializable
@Wisp("product/{productId}")
data class ProductDetail(val productId: String)

@Serializable
@Wisp("settings")
data object Settings
```

### 2. 라이브러리 초기화

`Application` 클래스의 `onCreate()`에서, KSP가 생성한 `WispRegistry`를 사용하여 `Wisp` 라이브러리를 초기화합니다.

```kotlin
// app/src/main/java/com/example/app/MyApplication.kt

import android.app.Application
import com.angrypodo.wisp.generated.WispRegistry
import com.angrypodo.wisp.runtime.Wisp

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Wisp.initialize(WispRegistry)
    }
}
```
**주의:** `AndroidManifest.xml`의 `<application>` 태그에 `android:name=".MyApplication"` 속성을 추가하는 것을 잊지 마세요.

### 3. NavHost 설정

Compose `Activity`에서 `NavHost`를 설정하고, 정의한 라우트와 Composable 화면을 연결합니다.

```kotlin
// app/src/main/java/com/example/app/MainActivity.kt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = Home) {
                composable<Home> { HomeScreen(navController) }
                composable<ProductDetail> { backStackEntry ->
                    val product = backStackEntry.toRoute<ProductDetail>()
                    ProductDetailScreen(product.productId)
                }
                composable<Settings> { SettingsScreen() }
            }
        }
    }
}
```

### 4. 딥링크 탐색 실행

이제 앱의 어느 곳에서든 `NavController`만 있다면 `navigateTo(uri)` 확장 함수를 사용하여 동적 백스택을 탐색할 수 있습니다.

- **URI 형식:** `scheme://host?stack={encoded_stack}`
- **`stack` 파라미터:**
    - 개별 백스택 경로는 `|` 문자로 구분합니다.
    - URL 인코딩이 필요할 수 있습니다.

```kotlin
// HomeScreen.kt 에서 버튼 클릭 시 딥링크 실행

Button(onClick = {
    // 백스택: ProductDetail(productId="123") -> Settings
    val uri = "app://wisp?stack=product/123|settings".toUri()
    navController.navigateTo(uri)
}) {
    Text("Deep Link Navigation")
}
```


https://github.com/user-attachments/assets/08b18c00-3a59-4300-96f0-b78ef3119932

