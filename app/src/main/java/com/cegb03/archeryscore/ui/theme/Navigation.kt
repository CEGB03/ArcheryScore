package com.cegb03.archeryscore.ui.theme

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")  
    object Register : Screen("register")
    object Feed : Screen("feed")
    object Detail : Screen("detail/{productId}") {
        fun createRoute(productId: Int) = "detail/$productId"
    }
    object Settings : Screen("settings") 
    object FavoritesOnly : Screen("favorites_only")
    object Access : Screen("access")
    object Cart : Screen("cart")
}