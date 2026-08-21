package com.teminator.mypadnoteone.indep

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log

class IndepAudioEngine(private val context: Context) {

    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var isRecording = false

    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    fun startRecording(onAudioDataReady: (ByteArray) -> Unit) {
        if (isRecording) return

        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(IndepConfig.TAG, "IndepAudioEngine: Invalid buffer size: $minBufferSize")
            return
        }

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
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
                        // 오디오 데이터가 읽힐 때마다 콜백으로 즉시 전달
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
}