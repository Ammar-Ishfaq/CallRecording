package com.ammar.callrecording

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.ammar.callrecording.FileUploadWorker.Companion.sendNotification

class MainActivity : AppCompatActivity() {
    private val PERMISSION_REQUEST_CODE = 123
    private val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check and request permissions
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val notGrantedPermissions = ArrayList<String>()

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notGrantedPermissions.add(permission)
            }
        }

        if (notGrantedPermissions.isNotEmpty()) {
            // Request permissions
            ActivityCompat.requestPermissions(
                this,
                notGrantedPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        } else {
            // All permissions granted, proceed with audio recording
            sendNotification("Permission Granted", "Permission Granted Successfully")
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
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // All permissions granted, proceed with audio recording
                    startRecording()
                } else {
                    // Permission denied, inform the user and ask to enable from settings
                    showPermissionDialog()
                }
            }
        }
    }

    private fun showPermissionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permissions Required")
            .setMessage("Please enable the required permissions from the app settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, PERMISSION_REQUEST_CODE)
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
