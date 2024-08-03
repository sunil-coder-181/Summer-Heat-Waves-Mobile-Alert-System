// LocationWorker.kt
import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.android.ApiService
import com.example.android.MainActivity
import com.example.android.PredictionRequest
import com.example.android.PredictionResponse
import com.example.android.R
import com.example.android.RetrofitClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LocationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    companion object {
        private const val BASE_URL = "https://flaskapp-1-2h2z.onrender.com/"
        private const val TAG = "LocationWorker"
    }

    @SuppressLint("MissingPermission")
    override fun doWork(): Result {
        Log.d(TAG, "LocationWorker started")

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                Log.d(TAG, "Location obtained: ${it.latitude}, ${it.longitude}")
                val latitude = it.latitude
                val longitude = it.longitude
                val token = "d21g60SvQs6qmjtP9tZehW:APA91bEUXTSJbqDAa_ZxE8zxKjkvh6Ehj-_-cG1v1iNqrZqLJA_enYhCdWERHY4Mgj08A5mabcVdvOSxTEVshFd-O6xL_OQi-XlMVwK2P8qr4ImIpeE1IPkUpp6WMPqxzbmM3ZTgVjAw"
                makeApiCall(token, latitude, longitude)
            } ?: run {
                Log.e(TAG, "Location is null")
            }
        }
        return Result.success()
    }

    private fun makeApiCall(token: String, latitude: Double, longitude: Double) {
        val apiService = RetrofitClient.getClient(BASE_URL).create(ApiService::class.java)
        val request = PredictionRequest(api_key = "c2a6cdaa610be9e3d046f18fa8c7e813", token = token, latitude = latitude, longitude = longitude)

        apiService.predictHeatWave(request).enqueue(object : Callback<PredictionResponse> {
            override fun onResponse(call: Call<PredictionResponse>, response: Response<PredictionResponse>) {
                if (response.isSuccessful) {
                    val prediction = response.body()?.prediction
                    val message = if (prediction == 1) {
                        // Send notification logic here
                        "Heat Wave Predicted! Stay Safe!"
                    } else {
                        // Optionally handle no heat wave prediction case
                    }
                    Log.d(TAG, message.toString())
                } else {
                    Log.e(TAG, "Prediction API call failed")
                }
            }

            override fun onFailure(call: Call<PredictionResponse>, t: Throwable) {
                Log.e(TAG, "API call failed: ${t.message}")
            }
        })
    }

}
