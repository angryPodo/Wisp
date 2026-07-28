package com.angrypodo.wisp.navigation

import com.angrypodo.wisp.annotations.Wisp
import kotlinx.serialization.Serializable

@Serializable
@Wisp("home")
data object Home

@Serializable
@Wisp("product/{productId}")
data class ProductDetail(
    val productId: Int,
    val showReviews: Boolean = false
)

@Serializable
@Wisp("settings")
data object Settings

@Serializable
@Wisp("user/{userId}")
data class UserRoute(val userId: Int)

@Serializable
@Wisp("splash")
data object Splash
