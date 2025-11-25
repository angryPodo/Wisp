package com.angrypodo.wisp.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.angrypodo.wisp.navigation.Home
import com.angrypodo.wisp.runtime.Wisp
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(onNavigateToProduct: () -> Unit, onNavigateToSettings: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Home Screen", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onNavigateToProduct) {
            Text(text = "Go to Product 123 -> Settings (Deep Link)")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToSettings) {
            Text(text = "Go to Settings (Single Deep Link)")
        }
    }
}

@Composable
fun ProductDetailScreen(productId: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Product Detail Screen", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Product ID: $productId", fontSize = 20.sp)
    }
}

@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Settings Screen", fontSize = 24.sp)
    }
}

@Composable
fun SplashScreen(
    navController: NavController,
    uri: Uri?
) {
    val wisp = Wisp.getDefaultInstance()

    // A real app would check login status, fetch initial data, etc.
    // Here we just simulate a delay.
    LaunchedEffect(Unit) {
        delay(1500)

        val routes = uri?.let { wisp.resolveRoutes(it) }

        if (routes.isNullOrEmpty()) {
            // No deep link, navigate to home as usual.
            navController.navigate(Home) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        } else {
            // Deep link found.
            // The first route is "splash", which we are already on.
            // We navigate to the rest of the stack.
            val routesToNavigate = routes.drop(1)
            wisp.navigateTo(navController, routesToNavigate)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Wisp", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Loading...", fontSize = 24.sp)
    }
}
