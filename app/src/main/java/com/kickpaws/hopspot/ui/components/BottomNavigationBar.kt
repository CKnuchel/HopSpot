package com.kickpaws.hopspot.ui.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kickpaws.hopspot.ui.navigation.BottomNavItem
import com.kickpaws.hopspot.ui.theme.Amber500
import com.kickpaws.hopspot.ui.theme.Brown300
import com.kickpaws.hopspot.ui.theme.Brown700
import com.kickpaws.hopspot.ui.theme.Cream

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    isAdmin: Boolean = false,
    modifier: Modifier = Modifier
){
    val items = buildList {
        add(BottomNavItem.Map)
        add(BottomNavItem.BenchList)
        add(BottomNavItem.Visits)
        add(BottomNavItem.Favorites)
        add(BottomNavItem.Profile)
        if (isAdmin) {
            add(BottomNavItem.Admin)  // Nur für Admins!
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = modifier,
        containerColor = Brown700,
        contentColor = Cream
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Amber500,
                    selectedTextColor = Amber500,
                    indicatorColor = Brown300.copy(alpha = 0.3f),
                    unselectedIconColor = Cream,
                    unselectedTextColor = Cream
                ),
                onClick = {
                    navController.navigate(item.route){
                        // Pop bis zum Start um Back-Stack nicht zu überladen
                        popUpTo(navController.graph.startDestinationId){
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}