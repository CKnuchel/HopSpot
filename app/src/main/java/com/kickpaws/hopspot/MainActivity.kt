// MainActivity.kt
package com.kickpaws.hopspot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kickpaws.hopspot.data.local.CurrentUserManager
import com.kickpaws.hopspot.ui.components.BottomNavigationBar
import com.kickpaws.hopspot.ui.navigation.HopSpotNavGraph
import com.kickpaws.hopspot.ui.navigation.Route
import com.kickpaws.hopspot.ui.theme.Brown700
import com.kickpaws.hopspot.ui.theme.HopSpotTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var currentUserManager: CurrentUserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val benchIdFromNotification = intent.extras?.getInt("bench_id")?.takeIf { it > 0 }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Brown700.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(Brown700.toArgb())
        )

        setContent {
            HopSpotTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val currentUser by currentUserManager.currentUser.collectAsState()
                val isAdmin = currentUser?.role == "admin"

                val showBottomBar = currentRoute in listOf(
                    Route.Map.route,
                    Route.BenchList.route,
                    Route.Visits.route,
                    Route.Profile.route,
                    Route.Admin.route
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavigationBar(
                                navController = navController,
                                isAdmin = isAdmin
                            )
                        }
                    }
                ) { innerPadding ->
                    HopSpotNavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                        deepLinkBenchId = benchIdFromNotification
                    )
                }
            }
        }
    }
}