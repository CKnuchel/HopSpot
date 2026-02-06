package com.kickpaws.hopspot.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.kickpaws.hopspot.MainActivity
import com.kickpaws.hopspot.R
import java.io.File

class NearestSpotWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val spotName = prefs[stringPreferencesKey("spot_name")]
            val spotRating = prefs[intPreferencesKey("spot_rating")] ?: 0
            val spotDistance = prefs[stringPreferencesKey("spot_distance")] ?: ""
            val spotImagePath = prefs[stringPreferencesKey("spot_image_path")]
            val weatherTemp = prefs[stringPreferencesKey("weather_temp")] ?: ""
            val weatherIcon = prefs[stringPreferencesKey("weather_icon")] ?: ""
            val spotId = prefs[intPreferencesKey("spot_id")] ?: 0
            val errorState = prefs[stringPreferencesKey("error_state")]
            val isLoading = prefs[booleanPreferencesKey("is_loading")] ?: (spotName == null && errorState == null)

            GlanceTheme {
                when {
                    isLoading -> WidgetLoadingContent(context)
                    errorState != null -> WidgetErrorContent(context, errorState)
                    else -> NearestSpotContent(
                        context = context,
                        spotName = spotName ?: context.getString(R.string.widget_loading),
                        spotRating = spotRating,
                        spotDistance = spotDistance,
                        spotImagePath = spotImagePath,
                        weatherTemp = weatherTemp,
                        weatherIcon = weatherIcon,
                        spotId = spotId
                    )
                }
            }
        }
    }
}

@Composable
fun WidgetLoadingContent(context: Context) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .cornerRadius(16.dp)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "\uD83D\uDD0D",
                style = TextStyle(fontSize = 28.sp)
            )
            Spacer(GlanceModifier.height(8.dp))
            Text(
                text = context.getString(R.string.widget_loading),
                style = TextStyle(
                    fontSize = 14.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun WidgetErrorContent(context: Context, errorState: String) {
    val errorMessage = when (errorState) {
        "no_location" -> context.getString(R.string.widget_no_location)
        "no_spots" -> context.getString(R.string.widget_no_spots)
        else -> context.getString(R.string.widget_error)
    }

    val errorIcon = when (errorState) {
        "no_location" -> "\uD83D\uDCCD"
        "no_spots" -> "\uD83D\uDDFA\uFE0F"
        else -> "\u26A0\uFE0F"
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .cornerRadius(16.dp)
            .clickable(actionRunCallback<RefreshWidgetAction>())
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = GlanceModifier.fillMaxWidth()
        ) {
            Text(
                text = errorIcon,
                style = TextStyle(fontSize = 28.sp)
            )
            Spacer(GlanceModifier.height(6.dp))
            Text(
                text = errorMessage,
                style = TextStyle(
                    fontSize = 13.sp,
                    color = GlanceTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center
                ),
                maxLines = 2
            )
            Spacer(GlanceModifier.height(6.dp))
            Text(
                text = "\uD83D\uDD04 ${context.getString(R.string.widget_tap_refresh)}",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.primary
                )
            )
        }
    }
}

class RefreshWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        WidgetUpdateWorker.enqueue(context, expedited = true)
    }
}

@Composable
fun NearestSpotContent(
    context: Context,
    spotName: String,
    spotRating: Int,
    spotDistance: String,
    spotImagePath: String?,
    weatherTemp: String,
    weatherIcon: String,
    spotId: Int
) {
    val bitmap: Bitmap? = spotImagePath?.let { path ->
        try {
            val file = File(path)
            if (file.exists()) BitmapFactory.decodeFile(path) else null
        } catch (_: Exception) {
            null
        }
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .cornerRadius(16.dp)
            .clickable(
                actionStartActivity(
                    Intent(context, MainActivity::class.java).apply {
                        putExtra("navigate_to", "spot_detail")
                        putExtra("spot_id", spotId)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                )
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            if (bitmap != null) {
                Image(
                    provider = ImageProvider(bitmap),
                    contentDescription = spotName,
                    modifier = GlanceModifier
                        .size(56.dp)
                        .cornerRadius(8.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = GlanceModifier
                        .size(56.dp)
                        .background(GlanceTheme.colors.secondaryContainer)
                        .cornerRadius(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\uD83C\uDF7A",
                        style = TextStyle(fontSize = 24.sp)
                    )
                }
            }

            Spacer(GlanceModifier.width(12.dp))

            // Info
            Column(modifier = GlanceModifier.defaultWeight()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = spotName,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = GlanceTheme.colors.onSurface
                        ),
                        maxLines = 1
                    )
                    Spacer(GlanceModifier.defaultWeight())
                    if (weatherIcon.isNotBlank() && weatherTemp.isNotBlank()) {
                        Text(
                            text = "$weatherIcon $weatherTemp",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = GlanceTheme.colors.onSurface
                            )
                        )
                    }
                }
                Spacer(GlanceModifier.height(4.dp))
                if (spotRating > 0 || spotDistance.isNotBlank()) {
                    val detailText = buildString {
                        if (spotRating > 0) {
                            append("\u2B50 $spotRating")
                        }
                        if (spotDistance.isNotBlank()) {
                            if (spotRating > 0) append(" \u00B7 ")
                            append(spotDistance)
                        }
                    }
                    Text(
                        text = detailText,
                        style = TextStyle(
                            fontSize = 13.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}
