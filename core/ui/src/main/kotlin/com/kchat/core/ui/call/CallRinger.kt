package com.kchat.core.ui.call

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class CallRinger(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var ringtone: Ringtone? = null
    private var toneGenerator: ToneGenerator? = null
    private var isRinging = false

    @Synchronized
    fun startIncomingRinging() {
        if (isRinging) return
        isRinging = true
        Log.i(TAG, "Starting incoming call ringing and vibration")

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val ringerMode = audioManager?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL

        // 1. Play incoming ringtone if ringer mode allows
        if (ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            if (ringtoneUri != null) {
                try {
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(context, ringtoneUri)
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build(),
                        )
                        isLooping = true
                        prepare()
                        start()
                    }
                    Log.i(TAG, "Playing ringtone via MediaPlayer")
                } catch (e: Exception) {
                    Log.w(TAG, "MediaPlayer failed, falling back to Ringtone", e)
                    try {
                        ringtone = RingtoneManager.getRingtone(context, ringtoneUri)?.apply {
                            audioAttributes = AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                isLooping = true
                            }
                            play()
                        }
                        Log.i(TAG, "Playing ringtone via Ringtone fallback")
                    } catch (e2: Exception) {
                        Log.e(TAG, "Ringtone fallback also failed", e2)
                    }
                }
            }
        } else {
            Log.i(TAG, "Ringer mode is $ringerMode (not normal), skipping audio ringtone")
        }

        // 2. Vibrate unless device is in silent mode
        if (ringerMode != AudioManager.RINGER_MODE_SILENT) {
            startVibrating()
        } else {
            Log.i(TAG, "Ringer mode is silent, skipping vibration")
        }
    }

    private fun startVibrating() {
        try {
            val vibrator = getVibrator()
            if (vibrator != null && vibrator.hasVibrator()) {
                val pattern = longArrayOf(0, 1000, 1000, 1000)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val effect = VibrationEffect.createWaveform(pattern, 0)
                    val attrs = android.os.VibrationAttributes.Builder()
                        .setUsage(android.os.VibrationAttributes.USAGE_RINGTONE)
                        .build()
                    vibrator.vibrate(effect, attrs)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(pattern, 0) // 0 means loop
                    val attrs = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(effect, attrs)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, 0)
                }
                Log.i(TAG, "Started incoming call vibration")
            } else {
                Log.w(TAG, "No vibrator available on device")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start vibration", e)
        }
    }

    @Synchronized
    fun startOutgoingRinging() {
        if (isRinging) return
        isRinging = true
        Log.i(TAG, "Starting outgoing ringback tone")
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 70).apply {
                startTone(ToneGenerator.TONE_SUP_RINGTONE)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start ToneGenerator for outgoing call", e)
        }
    }

    @Synchronized
    fun stop() {
        if (!isRinging) return
        isRinging = false
        Log.i(TAG, "Stopping call ringing and vibration")

        // Stop ringtone MediaPlayer
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping MediaPlayer", e)
        } finally {
            mediaPlayer = null
        }

        // Stop ringtone Ringtone
        try {
            ringtone?.let {
                if (it.isPlaying) {
                    it.stop()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping Ringtone", e)
        } finally {
            ringtone = null
        }

        // Cancel vibration
        try {
            getVibrator()?.cancel()
        } catch (e: Exception) {
            Log.w(TAG, "Error cancelling vibrator", e)
        }

        // Stop outgoing tone
        try {
            toneGenerator?.let {
                it.stopTone()
                it.release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping ToneGenerator", e)
        } finally {
            toneGenerator = null
        }
    }

    private fun getVibrator(): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    companion object {
        private const val TAG = "CallRinger"
    }
}
