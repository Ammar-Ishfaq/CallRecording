package com.ammar.callrecording

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.koushikdutta.ion.Ion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
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

            // Retrieve the file path from input data
            val filePath = inputData.getString(KEY_FILE_PATH)
            if (filePath.isNullOrBlank()) {
                applicationContext.sendNotification(
                    "File Upload Failed",
                    "File upload failed empty."
                )
                return@withContext Result.failure()
            }

            val result = suspendCancellableCoroutine { continuation ->
                Ion.with(appContext)
                    .load("http://192.168.40.58:5050/api/Audio")
                    .setHeader(
                        "Content-Type",
                        "application/octet-stream"
                    ) // Set the content type to binary
                    .setByteArrayBody(getBytesFromFile(File(filePath)))
                    .asString()
                    .setCallback { e, response ->
                        if (e != null) {
                            applicationContext.sendNotification(
                                "File Upload Failed",
                                "File upload failed. $e"
                            )
                            Log.e("CallRec", "Error => $e")
                            continuation.resume(Result.failure())
                        } else {
                            applicationContext.sendNotification(
                                "File Upload Successful",
                                "File successfully uploaded."
                            )
                            Log.e("CallRec", "success upload ${response}")
                            // Handle success
                            continuation.resume(Result.success())
                        }
                    }
            }

            return@withContext result
        } catch (e: Exception) {
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
}
