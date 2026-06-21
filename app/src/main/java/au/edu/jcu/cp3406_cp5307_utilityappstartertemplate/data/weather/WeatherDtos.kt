package au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.data.weather

import com.google.gson.annotations.SerializedName

data class OpenMeteoResponse(
    @SerializedName("current_weather")
    val current_weather: CurrentWeatherDTO?
)

data class CurrentWeatherDTO(
    val temperature: Double,
    val windspeed: Double,
    @SerializedName("weathercode")
    val weathercode: Int
)
