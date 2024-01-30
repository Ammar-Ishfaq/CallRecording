package com.ammar.callrecording

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.koushikdutta.ion.Ion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

class FileUploadWorker(
    val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            applicationContext.sendNotification(
                "File Upload in Progress",
                "File upload in Progress."
            )

            var isConnected = isInternetConnected(appContext)

            while (!isConnected) {
                delay(5000)

                isConnected = isInternetConnected(appContext)
            }

            // Retrieve the file path from input data
            val filePath = inputData.getString(KEY_FILE_PATH)
            if (filePath.isNullOrBlank()) {
                applicationContext.sendNotification(
                    "File Upload Failed",
                    "File upload failed empty."
                )
                return@withContext Result.failure()
            }

            val file = File(filePath)

            val description = RequestBody.create(MultipartBody.FORM, "Audio file description")
            val requestFile = RequestBody.create(MultipartBody.FORM, file)
            val filePart = MultipartBody.Part.createFormData("audioFile", file.name, requestFile)

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(300, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(300, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("http://192.168.0.116:5050/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(FileUploadService::class.java)

            val call = service.uploadAudioFile(filePart)
            val response = call.execute()

            if (response.isSuccessful) {
                applicationContext.sendNotification(
                    "File Upload Successful",
                    "File successfully uploaded."
                )
                Log.e("CallRec", "success upload ${response.body()}")
                return@withContext Result.success()
            } else {
                applicationContext.sendNotification(
                    "File Upload Failed",
                    "File upload failed. ${response.errorBody()}"
                )
                Log.e("CallRec", "Error => ${response.errorBody()}")

                // Retry the upload after a delay
                delay(5000)  // Adjust the delay time as needed
            }

            return@withContext Result.failure()
        } catch (e: Exception) {
            Log.e("CallRec", "Exception => $e")
            return@withContext Result.failure()
        }
    }


    private fun getBytesFromFile(file: File): ByteArray {
        val stream = FileInputStream(file)
        val length = file.length().toInt()
        val bytes = ByteArray(length)
        stream.read(bytes, 0, length)
        stream.close()
        return bytes
    }


    companion object {
        const val KEY_FILE_PATH = "key_file_path"
        fun Context.sendNotification(title: String, message: String) {
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "file_upload_channel",
                    "File Upload Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager.createNotificationChannel(channel)
            }

            val notificationBuilder = NotificationCompat.Builder(
                applicationContext,
                "file_upload_channel"
            )
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher_round) // Replace with your notification icon

            notificationManager.notify(1, notificationBuilder.build())
        }

    }

    private suspend fun isInternetConnected(context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val connectivityManager =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

                val networkCapabilities = connectivityManager.activeNetwork ?: return@withContext false
                val activeNetwork =
                    connectivityManager.getNetworkCapabilities(networkCapabilities)
                        ?: return@withContext false

                return@withContext activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } catch (e: Exception) {
                // Handle exceptions if needed
                return@withContext false
            }
        }
    }
}
