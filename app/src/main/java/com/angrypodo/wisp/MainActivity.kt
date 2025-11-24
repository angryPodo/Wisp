package com.angrypodo.wisp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.angrypodo.wisp.runtime.navigateTo
import com.angrypodo.wisp.ui.screens.HomeScreen
import com.angrypodo.wisp.ui.screens.ProductDetailScreen
import com.angrypodo.wisp.ui.screens.SettingsScreen
import com.angrypodo.wisp.ui.theme.WispTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WispTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = Home) {
                        composable<Home> {
                            HomeScreen(
                                onNavigateToProduct = {
                                    // "product/123"과 "settings"를 백스택으로 하는 딥링크 URI
                                    val deepLinkUri =
                                        "app://wisp?stack=product/123|settings".toUri()
                                    navController.navigateTo(deepLinkUri)
                                },
                                onNavigateToSettings = {
                                    // 단일 목적지로 이동하는 딥링크 URI
                                    val settingsUri = "app://wisp?stack=settings".toUri()
                                    navController.navigateTo(settingsUri)
                                }
                            )
                        }
                        composable<ProductDetail> { backStackEntry ->
                            val productDetail: ProductDetail = backStackEntry.toRoute()
                            ProductDetailScreen(productId = productDetail.productId)
                        }
                        composable<Settings> {
                            SettingsScreen()
                        }
                    }
                }
            }
        }
    }
}
