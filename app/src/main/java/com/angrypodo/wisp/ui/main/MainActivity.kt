package com.angrypodo.wisp.ui.main

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.angrypodo.wisp.navigation.Home
import com.angrypodo.wisp.navigation.ProductDetail
import com.angrypodo.wisp.navigation.Settings
import com.angrypodo.wisp.navigation.Splash
import com.angrypodo.wisp.navigation.UserRoute
import com.angrypodo.wisp.runtime.navigateTo
import com.angrypodo.wisp.ui.screens.HomeScreen
import com.angrypodo.wisp.ui.screens.ProductDetailScreen
import com.angrypodo.wisp.ui.screens.SettingsScreen
import com.angrypodo.wisp.ui.screens.SplashScreen
import com.angrypodo.wisp.ui.screens.UserScreen
import com.angrypodo.wisp.ui.theme.WispTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val deepLinkUri: Uri? = intent?.data
        setContent {
            WispTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WispNavHost(deepLinkUri = deepLinkUri)
                }
            }
        }
    }
}

@Composable
private fun WispNavHost(deepLinkUri: Uri?) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Splash) {
        composable<Splash> {
            SplashScreen(
                onSplashFinished = {
                    if (deepLinkUri != null) {
                        navController.navigateTo(deepLinkUri)
                    } else {
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
                    val uri = "app://wisp/product/settings?productId=123&showReviews=true".toUri()
                    navController.navigateTo(uri)
                },
                onNavigateToSettings = {
                    val uri = "app://wisp/settings".toUri()
                    navController.navigateTo(uri)
                },
                onNavigateToMultiStack = {
                    val uri = "app://wisp/product/user?productId=123&userId=99".toUri()
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
