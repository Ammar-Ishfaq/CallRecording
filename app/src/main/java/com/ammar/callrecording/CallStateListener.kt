package com.ammar.callrecording

import android.content.Context
import android.media.AudioManager
import android.media.MediaRecorder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.ammar.callrecording.FileUploadWorker.Companion.sendNotification
import java.util.Date

class CallStateListener(private val context: Context) : PhoneStateListener() {
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var filePath = ""
    override fun onCallStateChanged(state: Int, phoneNumber: String?) {
        super.onCallStateChanged(state, phoneNumber)
        when (state) {
            TelephonyManager.CALL_STATE_OFFHOOK -> startRecording()
            TelephonyManager.CALL_STATE_IDLE -> stopRecording()
            TelephonyManager.CALL_STATE_RINGING -> {

            }
        }
    }

    private fun startRecording() {
        if (!isRecording) {
            try {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager.requestAudioFocus(
                    null,
                    AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                )

                mediaRecorder = MediaRecorder()
                mediaRecorder?.apply {
                    setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
                    setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    setAudioSamplingRate(44100);
                    filePath =
                        context.getExternalFilesDir(null)?.absolutePath + "/recorded_call.mp3"
                    setOutputFile(filePath)



                    prepare()
                    start()
                    isRecording = true
                }
            } catch (e: Exception) {
//                e.printStackTrace()
                Log.e("recordingError", "err => ${e.toString()}")
            }
        }
    }

    private fun stopRecording() {
        if (isRecording) {
            try {
                mediaRecorder?.apply {
                    stop()
                    reset()
                    release()
                    isRecording = false

                    val uploadWorkRequest =
                        OneTimeWorkRequest.Builder(FileUploadWorker::class.java)
                            .setInputData(workDataOf(FileUploadWorker.KEY_FILE_PATH to filePath))
                            .build()
                    context.sendNotification("File Enque", "Enque.")

                    WorkManager.getInstance(context).enqueue(uploadWorkRequest)

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
