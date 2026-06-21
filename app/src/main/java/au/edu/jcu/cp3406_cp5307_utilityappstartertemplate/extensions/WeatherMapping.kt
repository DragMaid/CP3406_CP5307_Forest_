package au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.extensions

import au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.WeatherCondition

fun mapWeatherCodeToCondition(code: Int): WeatherCondition {
    return when (code) {
        0 -> WeatherCondition.SUNNY
        1, 2, 3, 45, 48 -> WeatherCondition.CLOUDY
        51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> WeatherCondition.RAINY
        71, 73, 75, 77, 85, 86 -> WeatherCondition.WINDY
        95, 96, 99 -> WeatherCondition.STORM
        else -> WeatherCondition.CLOUDY
    }
}
