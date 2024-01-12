package com.ammar.callrecording

import android.content.Context
import android.media.MediaRecorder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager

class CallStateListener(private val context: Context) : PhoneStateListener() {
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false

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
                mediaRecorder = MediaRecorder()
                mediaRecorder?.apply {
                    setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    setOutputFile(context.getExternalFilesDir(null)?.absolutePath + "/recorded_call.3gp")

                    prepare()
                    start()
                    isRecording = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
