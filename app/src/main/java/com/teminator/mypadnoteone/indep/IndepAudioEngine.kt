package com.teminator.mypadnoteone.indep

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log

class IndepAudioEngine(private val context: Context) {

    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var isRecording = false

    // 스피커 재생을 위한 AudioTrack 선언
    private var audioTrack: AudioTrack? = null

    private val sampleRate = 16000
    private val channelConfigIn = AudioFormat.CHANNEL_IN_MONO
    private val channelConfigOut = AudioFormat.CHANNEL_OUT_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    init {
        initAudioTrack()
    }

    private fun initAudioTrack() {
        try {
            val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfigOut, audioFormat)

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
                .setBufferSizeInBytes(minBufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
            Log.d(IndepConfig.TAG, "IndepAudioEngine: AudioTrack initialized and started")
        } catch (e: Exception) {
            Log.e(IndepConfig.TAG, "IndepAudioEngine: Failed to initialize AudioTrack", e)
        }
    }

    // 🔊 상대방 음성 데이터를 받아 스피커로 재생하는 함수
    fun playAudio(audioData: ByteArray) {
        try {
            if (audioTrack?.state == AudioTrack.STATE_INITIALIZED) {
                audioTrack?.write(audioData, 0, audioData.size)
            }
        } catch (e: Exception) {
            Log.e(IndepConfig.TAG, "IndepAudioEngine: Error playing audio data", e)
        }
    }

    fun startRecording(onAudioDataReady: (ByteArray) -> Unit) {
        if (isRecording) return

        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfigIn, audioFormat)
        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(IndepConfig.TAG, "IndepAudioEngine: Invalid buffer size: $minBufferSize")
            return
        }

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfigIn,
                audioFormat,
                minBufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(IndepConfig.TAG, "IndepAudioEngine: AudioRecord initialization failed")
                return
            }

            audioRecord?.startRecording()
            isRecording = true
            Log.d(IndepConfig.TAG, "IndepAudioEngine: Recording started successfully")

            recordingThread = Thread({
                val buffer = ByteArray(minBufferSize)
                while (isRecording) {
                    val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (readSize > 0) {
                        val dataCopy = buffer.copyOf(readSize)
                        onAudioDataReady(dataCopy)
                    }
                }
            }, "AudioCaptureThread")

            recordingThread?.start()

        } catch (e: SecurityException) {
            Log.e(IndepConfig.TAG, "IndepAudioEngine: Microphone permission denied", e)
            isRecording = false
        } catch (e: Exception) {
            Log.e(IndepConfig.TAG, "IndepAudioEngine: Exception in startRecording", e)
            isRecording = false
        }
    }

    fun stopRecording() {
        if (!isRecording) return
        isRecording = false

        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            Log.e(IndepConfig.TAG, "IndepAudioEngine: Exception while stopping audioRecord", e)
        } finally {
            audioRecord = null
            recordingThread = null
            Log.d(IndepConfig.TAG, "IndepAudioEngine: Recording stopped and released")
        }
    }

    // 엔진 종료 시 AudioTrack도 깔끔하게 해제
    fun release() {
        stopRecording()
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            Log.e(IndepConfig.TAG, "IndepAudioEngine: Exception releasing AudioTrack", e)
        } finally {
            audioTrack = null
        }
    }
}