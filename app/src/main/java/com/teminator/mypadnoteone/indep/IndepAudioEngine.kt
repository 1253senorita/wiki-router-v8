package com.teminator.mypadnoteone.indep

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log

class IndepAudioEngine(private val context: Context) {

    private var isRecording = false
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    fun startRecording(onAudioDataReady: (ByteArray) -> Unit) {
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        try {
            val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                minBufferSize
            )

            isRecording = true
            audioRecord.startRecording()

            Thread {
                val buffer = ByteArray(minBufferSize)
                while (isRecording) {
                    val readSize = audioRecord.read(buffer, 0, buffer.size)
                    if (readSize > 0) {
                        // 마이크에서 읽은 오디오 바이트 데이터를 콜백으로 전달
                        onAudioDataReady(buffer.copyOf(readSize))
                    }
                }
                audioRecord.stop()
                audioRecord.release()
            }.start()

            Log.d(IndepConfig.TAG, "IndepAudioEngine: Recording started")
        } catch (e: SecurityException) {
            Log.e(IndepConfig.TAG, "Microphone permission denied", e)
        }
    }

    fun stopRecording() {
        isRecording = false
        Log.d(IndepConfig.TAG, "IndepAudioEngine: Recording stopped")
    }
}