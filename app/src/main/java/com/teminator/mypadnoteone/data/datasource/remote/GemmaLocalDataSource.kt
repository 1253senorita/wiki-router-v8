package com.teminator.mypadnoteone.data.datasource.local

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GemmaLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var llmInference: LlmInference? = null



    // 모델 로드 및 초기화
    // 모델 로드 및 초기화
    fun initModel() {
        if (llmInference == null) {
            try {
                // 🔥 내부 저장소에 있는 실제 다운로드된 .tflite 파일의 절대 경로를 정확히 지정
                val modelFile = File(context.filesDir, "model_quantized.tflite")

                if (!modelFile.exists()) {
                    throw IllegalStateException("모델 파일이 아직 존재하지 않습니다: ${modelFile.absolutePath}")
                }

                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelFile.absolutePath)
                    .setMaxTokens(512)
                    .build()

                llmInference = LlmInference.createFromOptions(context, options)
            } catch (e: Exception) {
                e.printStackTrace()
                throw e // 에러를 삼키지 않고 상위로 던져서 상태를 정확히 파악
            }
        }
    }

    // 실제 AI 모델 추론 실행
    suspend fun generateResponse(prompt: String): String {
        return llmInference?.generateResponse(prompt) ?: "모델 로드 실패.. 오빠, 의존성을 다시 확인해봐!"
    }

    fun close() {
        llmInference?.close()
        llmInference = null
    }
}