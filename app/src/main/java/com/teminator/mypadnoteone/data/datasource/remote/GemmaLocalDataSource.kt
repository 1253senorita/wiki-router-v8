package com.teminator.mypadnoteone.data.datasource.local

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GemmaLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var llmInference: LlmInference? = null

    // 모델 로드 및 초기화
    fun initModel() {
        if (llmInference == null) {
            try {
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath("gemma-2b-Q4_K_M.gguf") // assets 폴더 내 파일명
                    .setMaxTokens(512)
                    .build()
                llmInference = LlmInference.createFromOptions(context, options)
            } catch (e: Exception) {
                e.printStackTrace()
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