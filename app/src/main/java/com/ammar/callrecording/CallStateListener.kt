package com.ammar.callrecording

import android.content.Context
import android.media.AudioManager
import android.media.MediaRecorder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import java.util.Date

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

                    setOutputFile(context.getExternalFilesDir(null)?.absolutePath + "/recorded_call_${Date().time}.mp3")



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
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
