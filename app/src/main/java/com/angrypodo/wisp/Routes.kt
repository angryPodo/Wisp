package com.angrypodo.wisp

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
