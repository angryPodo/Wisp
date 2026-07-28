package com.angrypodo.wisp.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.angrypodo.wisp.navigation.Home
import com.angrypodo.wisp.navigation.ProductDetail
import com.angrypodo.wisp.navigation.Settings
import com.angrypodo.wisp.navigation.Splash
import com.angrypodo.wisp.navigation.UserRoute
import com.angrypodo.wisp.runtime.Wisp
import com.angrypodo.wisp.runtime.WispResult
import com.angrypodo.wisp.runtime.navigateTo
import com.angrypodo.wisp.runtime.navigateToDeferred
import com.angrypodo.wisp.ui.screens.HomeScreen
import com.angrypodo.wisp.ui.screens.ProductDetailScreen
import com.angrypodo.wisp.ui.screens.SettingsScreen
import com.angrypodo.wisp.ui.screens.SplashScreen
import com.angrypodo.wisp.ui.screens.UserScreen
import com.angrypodo.wisp.ui.theme.WispTheme

class MainActivity : ComponentActivity() {
    private var navController: NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Cold start: hold the deep link instead of executing it immediately.
        // It is executed via navigateToDeferred() after splash validation finishes.
        Wisp.defer(intent?.data)
        setContent {
            WispTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val controller = rememberNavController()
                    navController = controller
                    WispNavHost(controller)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Warm start (launchMode="singleTop"): this session already passed splash
        // validation, so execute immediately. If your app requires re-validation,
        // call Wisp.defer(intent.data) instead and run navigateToDeferred()
        // after the validation flow.
        intent.data?.let { uri -> navController?.navigateTo(uri) }
    }
}

@Composable
private fun WispNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Splash) {
        composable<Splash> {
            SplashScreen(
                onSplashFinished = {
                    val result = navController.navigateToDeferred()
                    if (result == null || result is WispResult.Failure) {
                        navController.navigate(Home) {
                            popUpTo(Splash) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable<Home> {
            HomeScreen(
                onNavigateToProduct = {
                    val uri = "app://wisp/product/123/settings?showReviews=true".toUri()
                    navController.navigateTo(uri)
                },
                onNavigateToSettings = {
                    val uri = "app://wisp/settings".toUri()
                    navController.navigateTo(uri)
                },
                onNavigateToMultiStack = {
                    val uri = "app://wisp/product/123/user/99".toUri()
                    navController.navigateTo(uri)
                }
            )
        }
        composable<ProductDetail> { backStackEntry ->
            val productDetail: ProductDetail = backStackEntry.toRoute()
            ProductDetailScreen(
                productId = productDetail.productId,
                showReviews = productDetail.showReviews
            )
        }
        composable<UserRoute> { backStackEntry ->
            val userRoute: UserRoute = backStackEntry.toRoute()
            UserScreen(userId = userRoute.userId)
        }
        composable<Settings> {
            SettingsScreen()
        }
    }
}
