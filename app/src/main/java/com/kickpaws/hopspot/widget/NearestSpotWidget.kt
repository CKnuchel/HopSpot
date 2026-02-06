package com.kickpaws.hopspot.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
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
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.kickpaws.hopspot.MainActivity
import com.kickpaws.hopspot.R
import java.io.File

class NearestSpotWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val spotName = prefs[stringPreferencesKey("spot_name")] ?: context.getString(R.string.widget_loading)
            val spotRating = prefs[intPreferencesKey("spot_rating")] ?: 0
            val spotDistance = prefs[stringPreferencesKey("spot_distance")] ?: ""
            val spotImagePath = prefs[stringPreferencesKey("spot_image_path")]
            val weatherTemp = prefs[stringPreferencesKey("weather_temp")] ?: ""
            val weatherIcon = prefs[stringPreferencesKey("weather_icon")] ?: ""
            val spotId = prefs[intPreferencesKey("spot_id")] ?: 0
            val isError = prefs[stringPreferencesKey("error_state")]

            GlanceTheme {
                NearestSpotContent(
                    context = context,
                    spotName = spotName,
                    spotRating = spotRating,
                    spotDistance = spotDistance,
                    spotImagePath = spotImagePath,
                    weatherTemp = weatherTemp,
                    weatherIcon = weatherIcon,
                    spotId = spotId,
                    isError = isError != null
                )
            }
        }
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
    spotId: Int,
    isError: Boolean
) {
    val bitmap: Bitmap? = spotImagePath?.let { path ->
        try {
            val file = File(path)
            if (file.exists()) BitmapFactory.decodeFile(path) else null
        } catch (e: Exception) {
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
                        text = if (isError) "!" else "\uD83D\uDCCD",
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
