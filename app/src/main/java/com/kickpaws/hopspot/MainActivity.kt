// MainActivity.kt
package com.kickpaws.hopspot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
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
import com.kickpaws.hopspot.data.network.NetworkMonitor
import com.kickpaws.hopspot.data.sync.SyncManager
import com.kickpaws.hopspot.ui.components.BottomNavigationBar
import com.kickpaws.hopspot.ui.components.OfflineBanner
import com.kickpaws.hopspot.ui.components.SyncStatusBar
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

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var syncManager: SyncManager

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

                val isOnline by networkMonitor.isOnline.collectAsState(initial = true)
                val syncProgress by syncManager.syncProgress.collectAsState()

                val showBottomBar = currentRoute in listOf(
                    Route.Map.route,
                    Route.BenchList.route,
                    Route.Visits.route,
                    Route.Profile.route,
                    Route.Admin.route
                )

                val showStatusBars = currentRoute !in listOf(
                    Route.Splash.route,
                    Route.Login.route,
                    Route.Register.route
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (showStatusBars) {
                            Column {
                                OfflineBanner(isVisible = !isOnline)
                                SyncStatusBar(
                                    syncProgress = syncProgress,
                                    onSyncClick = { syncManager.triggerSync() }
                                )
                            }
                        }
                    },
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