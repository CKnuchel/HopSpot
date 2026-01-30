package com.kickpaws.hopspot.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Map : BottomNavItem(
        route = Route.Map.route,
        icon = Icons.Default.Map,
        label = "Karte"
    )

    object BenchList : BottomNavItem(
        route = Route.BenchList.route,
        icon = Icons.AutoMirrored.Filled.List,
        label = "Bänke"
    )

    object Visits : BottomNavItem(
        route = Route.Visits.route,
        icon = Icons.Default.History,
        label = "Besuche"
    )

    object Profile : BottomNavItem(
        route = Route.Profile.route,
        icon = Icons.Default.Person,
        label = "Profil"
    )
}