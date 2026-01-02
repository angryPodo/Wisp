package com.angrypodo.wisp.ui.screens

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
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onNavigateToProduct: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToMultiStack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Home Screen", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onNavigateToProduct) {
            Text(text = "Go to Product 123 -> Settings")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToSettings) {
            Text(text = "Go to Settings")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToMultiStack) {
            Text(text = "Product(123) -> User(99)")
        }
    }
}

@Composable
fun ProductDetailScreen(productId: Int, showReviews: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Product Detail Screen", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Product ID: $productId", fontSize = 20.sp)
        Text(text = "Show Reviews: $showReviews", fontSize = 16.sp)
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
fun UserScreen(userId: Int) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "User Screen", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "User ID: $userId", fontSize = 20.sp)
    }
}

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // A real app would check login status, fetch initial data, etc.
    // Here we just simulate a delay.
    LaunchedEffect(Unit) {
        delay(1500)
        onSplashFinished()
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
