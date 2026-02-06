package com.kickpaws.hopspot.data.analytics

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val analytics = Firebase.analytics

    // Screen Views
    fun logScreenView(screenName: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }
    }

    // Custom Events
    fun logBenchCreated(benchId: Int) {
        analytics.logEvent("bench_created") {
            param("bench_id", benchId.toLong())
        }
    }

    fun logBenchViewed(benchId: Int) {
        analytics.logEvent("bench_viewed") {
            param("bench_id", benchId.toLong())
        }
    }

    fun logBenchFavorited(benchId: Int, added: Boolean) {
        analytics.logEvent("bench_favorited") {
            param("bench_id", benchId.toLong())
            param("added", if (added) 1L else 0L)
        }
    }

    fun logVisitAdded(benchId: Int) {
        analytics.logEvent("visit_added") {
            param("bench_id", benchId.toLong())
        }
    }

    fun logLogin(method: String) {
        analytics.logEvent(FirebaseAnalytics.Event.LOGIN) {
            param(FirebaseAnalytics.Param.METHOD, method)
        }
    }

    fun logLogout() {
        analytics.logEvent("logout", null)
    }

    fun logNotificationOpened(benchId: Int?) {
        analytics.logEvent("notification_opened") {
            benchId?.let { param("bench_id", it.toLong()) }
        }
    }

    // User Properties
    fun setUserRole(role: String) {
        analytics.setUserProperty("user_role", role)
    }

    fun setUserId(userId: Int?) {
        analytics.setUserId(userId?.toString())
    }
}
