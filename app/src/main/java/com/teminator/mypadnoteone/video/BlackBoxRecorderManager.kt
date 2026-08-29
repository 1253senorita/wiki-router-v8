package com.teminator.mypadnoteone.video

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BlackBoxRecorderManager(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var currentFilePath: String? = null
    var isRecording = false
        private set

    enum class RecordMode {
        VIDEO_AND_AUDIO, // 영상 + 음성 동시 녹화
        AUDIO_ONLY       // 음성만 녹화 (블랙박스 음성 모드)
    }

    fun startRecording(mode: RecordMode): String? {
        try {
            if (isRecording) stopRecording()

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val dir = context.getExternalFilesDir(null) ?: context.cacheDir
            val fileName = if (mode == RecordMode.VIDEO_AND_AUDIO) {
                "BlackBox_VID_$timeStamp.mp4"
            } else {
                "BlackBox_AUD_$timeStamp.m4a"
            }
            val file = File(dir, fileName)
            currentFilePath = file.absolutePath

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                if (mode == RecordMode.VIDEO_AND_AUDIO) {
                    // 💡 비디오+음성 모드 (주의: 카메라 하드웨어 프리뷰 Surface가 연결되어야 정상 동작)
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setVideoSource(MediaRecorder.VideoSource.CAMERA)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                    setVideoSize(1280, 720)
                    setVideoFrameRate(30)
                    setVideoEncodingBitRate(3000000)
                } else {
                    // 🎙️ 음성 전용 모드
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                }

                setOutputFile(currentFilePath)
                prepare()
                start()
            }

            isRecording = true
            Log.d("BlackBox", "녹화 시작 성공: $currentFilePath")
            return currentFilePath
        } catch (e: Exception) {
            Log.e("BlackBox", "녹화 시작 실패", e)
            isRecording = false
            return null
        }
    }

    fun stopRecording(): String? {
        try {
            if (isRecording && mediaRecorder != null) {
                mediaRecorder?.stop()
                mediaRecorder?.reset()
                mediaRecorder?.release()
                mediaRecorder = null
                isRecording = false
                Log.d("BlackBox", "녹화 종료 및 파일 저장 완료: $currentFilePath")
                return currentFilePath
            }
        } catch (e: Exception) {
            Log.e("BlackBox", "녹화 종료 중 예외 발생", e)
            isRecording = false
        }
        mediaRecorder = null
        return null
    }
}