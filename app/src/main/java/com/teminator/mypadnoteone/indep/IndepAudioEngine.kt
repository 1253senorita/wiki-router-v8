package com.teminator.mypadnoteone.indep

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class IndepAudioEngine(
    private val context: Context,
    private val onAudioDataCaptured: (ByteArray, Int) -> Unit
) {
    private val sampleRate = 16000
    private val channelConfigIn = AudioFormat.CHANNEL_IN_MONO
    private val channelConfigOut = AudioFormat.CHANNEL_OUT_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var isRecording = false

    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfigIn, audioFormat)

    @SuppressLint("MissingPermission")
    fun startRecording() {
        if (isRecording) return

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfigIn,
                audioFormat,
                maxOf(minBufferSize, 2048)
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(IndepConfig.TAG, "AudioRecord initialization failed!")
                return
            }

            audioRecord?.startRecording()
            isRecording = true
            Log.d(IndepConfig.TAG, "PTT Recording Started...")

            recordingJob = scope.launch {
                val buffer = ByteArray(2048)
                while (isRecording && isActive) {
                    val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (readSize > 0) {
                        onAudioDataCaptured(buffer, readSize)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(IndepConfig.TAG, "Error starting recording: ${e.message}")
        }
    }

    fun stopRecording() {
        if (!isRecording) return
        isRecording = false
        recordingJob?.cancel()

        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            Log.d(IndepConfig.TAG, "PTT Recording Stopped.")
        } catch (e: Exception) {
            Log.e(IndepConfig.TAG, "Error stopping recording: ${e.message}")
        }
    }

    fun playAudio(audioData: ByteArray, length: Int = audioData.size) {
        try {
            if (audioTrack == null) {
                val trackMinBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfigOut, audioFormat)
                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(audioFormat)
                            .setSampleRate(sampleRate)
                            .setChannelMask(channelConfigOut)
                            .build()
                    )
                    .setBufferSizeInBytes(maxOf(trackMinBufferSize, length))
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack?.play()
            }
            audioTrack?.write(audioData, 0, length)
        } catch (e: Exception) {
            Log.e(IndepConfig.TAG, "Error playing audio data", e)
        }
    }

    fun release() {
        stopRecording()
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            Log.e(IndepConfig.TAG, "Exception releasing AudioTrack", e)
        } finally {
            audioTrack = null
        }
    }
}