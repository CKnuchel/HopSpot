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
    fun logSpotCreated(spotId: Int) {
        analytics.logEvent("spot_created") {
            param("spot_id", spotId.toLong())
        }
    }

    fun logSpotViewed(spotId: Int) {
        analytics.logEvent("spot_viewed") {
            param("spot_id", spotId.toLong())
        }
    }

    fun logSpotFavorited(spotId: Int, added: Boolean) {
        analytics.logEvent("spot_favorited") {
            param("spot_id", spotId.toLong())
            param("added", if (added) 1L else 0L)
        }
    }

    fun logVisitAdded(spotId: Int) {
        analytics.logEvent("visit_added") {
            param("spot_id", spotId.toLong())
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

    fun logNotificationOpened(spotId: Int?) {
        analytics.logEvent("notification_opened") {
            spotId?.let { param("spot_id", it.toLong()) }
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
