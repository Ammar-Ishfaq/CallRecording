package com.ammar.callrecording

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import  android.Manifest
import android.view.View
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.ammar.callrecording.FileUploadWorker.Companion.sendNotification

class MainActivity : AppCompatActivity() {
    private val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission not granted, request it
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission already granted, proceed with audio recording
            startRecording()
        }

    }

    private fun startRecording() {
        // Start the BackgroundRecordingService
        val serviceIntent = Intent(this, BackgroundRecordingService::class.java)


        // Use startForegroundService for Android 8.0 (Oreo) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RECORD_AUDIO_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with audio recording
                    startRecording()
                } else {
                    // Permission denied, handle accordingly
                    // You may want to inform the user about the need for the permission
                }
            }
        }
    }

    fun upload(view: View) {
        val context = view.context
        val filePath =
            context.getExternalFilesDir(null)?.absolutePath + "/recorded_call.mp3"

        val uploadWorkRequest =
            OneTimeWorkRequest.Builder(FileUploadWorker::class.java)
                .setInputData(workDataOf(FileUploadWorker.KEY_FILE_PATH to filePath))
                .build()
        context.sendNotification("File Enque", "Enque.")
        WorkManager.getInstance(context).enqueue(uploadWorkRequest)

    }

}