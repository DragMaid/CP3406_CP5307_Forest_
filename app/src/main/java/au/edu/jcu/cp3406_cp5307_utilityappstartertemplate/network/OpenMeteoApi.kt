package au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.network

import au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.data.weather.OpenMeteoResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoApi {
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        // Request current weather block; Open-Meteo provides current_weather with weathercode
        @Query("current_weather") currentWeather: Boolean = true
    ): OpenMeteoResponse
}
