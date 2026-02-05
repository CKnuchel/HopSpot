package com.kickpaws.hopspot.ui.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

// Weather Icons Font - https://erikflowers.github.io/weather-icons/
object WeatherIcons {
    // Font laden from assets
    val fontFamily: FontFamily
        @Composable
        get() {
            val context = LocalContext.current
            return remember {
                FontFamily(
                    Font(
                        path = "fonts/weathericons-regular-webfont.ttf",
                        assetManager = context.assets,
                        weight = FontWeight.Normal
                    )
                )
            }
        }

    // WMO Weather Codes -> Weather Icons Unicode
    // https://erikflowers.github.io/weather-icons/api-list.html
    object Codes {
        // Clear
        const val DAY_SUNNY = '\uf00d'
        const val NIGHT_CLEAR = '\uf02e'

        // Partly Cloudy
        const val DAY_CLOUDY = '\uf002'
        const val NIGHT_CLOUDY = '\uf031'

        // Cloudy / Overcast
        const val CLOUDY = '\uf013'
        const val CLOUD = '\uf041'

        // Fog
        const val FOG = '\uf014'
        const val DAY_FOG = '\uf003'

        // Drizzle
        const val SPRINKLE = '\uf01c'
        const val DAY_SPRINKLE = '\uf00b'

        // Rain
        const val RAIN = '\uf019'
        const val DAY_RAIN = '\uf008'
        const val SHOWERS = '\uf01a'
        const val DAY_SHOWERS = '\uf009'

        // Snow
        const val SNOW = '\uf01b'
        const val DAY_SNOW = '\uf00a'
        const val SNOWFLAKE_COLD = '\uf076'

        // Thunderstorm
        const val THUNDERSTORM = '\uf01e'
        const val DAY_THUNDERSTORM = '\uf010'

        // Sleet / Freezing Rain
        const val SLEET = '\uf0b5'
        const val DAY_SLEET = '\uf0b2'

        // Hail
        const val HAIL = '\uf015'
        const val DAY_HAIL = '\uf004'

        // Wind directions (wind coming FROM this direction)
        const val WIND_N = '\uf060'   // Towards South
        const val WIND_NE = '\uf05e'
        const val WIND_E = '\uf061'   // Towards West
        const val WIND_SE = '\uf05c'
        const val WIND_S = '\uf05a'   // Towards North
        const val WIND_SW = '\uf059'
        const val WIND_W = '\uf05d'   // Towards East
        const val WIND_NW = '\uf05b'

        // Thermometer
        const val THERMOMETER = '\uf055'

        // Misc
        const val NA = '\uf07b'
    }

    /**
     * Get weather icon for WMO weather code
     * https://open-meteo.com/en/docs (WMO Weather interpretation codes)
     */
    fun getIconForCode(weatherCode: Int, isDay: Boolean = true): Char {
        return when (weatherCode) {
            0 -> if (isDay) Codes.DAY_SUNNY else Codes.NIGHT_CLEAR
            1 -> if (isDay) Codes.DAY_SUNNY else Codes.NIGHT_CLEAR
            2 -> if (isDay) Codes.DAY_CLOUDY else Codes.NIGHT_CLOUDY
            3 -> Codes.CLOUDY
            45, 48 -> if (isDay) Codes.DAY_FOG else Codes.FOG
            51, 53, 55 -> if (isDay) Codes.DAY_SPRINKLE else Codes.SPRINKLE
            56, 57 -> if (isDay) Codes.DAY_SLEET else Codes.SLEET
            61, 63, 65 -> if (isDay) Codes.DAY_RAIN else Codes.RAIN
            66, 67 -> if (isDay) Codes.DAY_SLEET else Codes.SLEET
            71, 73, 75 -> if (isDay) Codes.DAY_SNOW else Codes.SNOW
            77 -> Codes.SNOWFLAKE_COLD
            80, 81, 82 -> if (isDay) Codes.DAY_SHOWERS else Codes.SHOWERS
            85, 86 -> if (isDay) Codes.DAY_SNOW else Codes.SNOW
            95 -> if (isDay) Codes.DAY_THUNDERSTORM else Codes.THUNDERSTORM
            96, 99 -> if (isDay) Codes.DAY_HAIL else Codes.HAIL
            else -> Codes.NA
        }
    }

    /**
     * Get wind direction icon based on degrees
     */
    fun getWindDirectionIcon(degrees: Int): Char {
        // Wind direction is where wind comes FROM
        return when ((degrees + 22) % 360 / 45) {
            0 -> Codes.WIND_N
            1 -> Codes.WIND_NE
            2 -> Codes.WIND_E
            3 -> Codes.WIND_SE
            4 -> Codes.WIND_S
            5 -> Codes.WIND_SW
            6 -> Codes.WIND_W
            7 -> Codes.WIND_NW
            else -> Codes.WIND_N
        }
    }
}
