package au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.data.location

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationRepository(application: Application) {
    private val client: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { cont ->
        try {
            val task = client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            task.addOnSuccessListener { loc ->
                cont.resume(loc)
            }
            task.addOnFailureListener { ex ->
                cont.resumeWithException(ex)
            }
        } catch (ex: Exception) {
            cont.resumeWithException(ex)
        }
    }
}
