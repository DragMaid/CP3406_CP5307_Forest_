package au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.data.weather

import au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.WeatherCondition
import au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.extensions.mapWeatherCodeToCondition
import au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.network.OpenMeteoApi
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class WeatherRepository(private val api: OpenMeteoApi) {

    private var cacheCondition: WeatherCondition? = null
    private var cacheTimestamp: Long = 0L
    private val cacheMutex = Mutex()
    private val CACHE_TTL_MS = 5 * 60 * 1000L // 5 minutes

    suspend fun fetchWeather(latitude: Double, longitude: Double, forceRefresh: Boolean = false): WeatherCondition? {
        return cacheMutex.withLock {
            val now = System.currentTimeMillis()
            if (!forceRefresh && cacheCondition != null && (now - cacheTimestamp) < CACHE_TTL_MS) {
                return@withLock cacheCondition
            }

            try {
                val resp = api.getCurrentWeather(latitude = latitude, longitude = longitude, currentWeather = true)
                val code = resp.current_weather?.weathercode
                val cond = if (code != null) mapWeatherCodeToCondition(code) else null
                if (cond != null) {
                    cacheCondition = cond
                    cacheTimestamp = System.currentTimeMillis()
                }
                return@withLock cond
            } catch (_: Exception) {
                // propagate null on error (caller should handle)
                return@withLock null
            }
        }
    }

    // For UI or other callers to get cached value without network
    suspend fun getCached(): WeatherCondition? = cacheMutex.withLock { cacheCondition }
}
