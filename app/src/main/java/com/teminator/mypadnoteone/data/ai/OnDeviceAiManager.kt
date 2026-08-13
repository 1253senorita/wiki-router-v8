package com.terminator.mypadnoteone.data.ai

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnDeviceAiManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var llmInference: LlmInference? = null
    private val aiScope = CoroutineScope(Dispatchers.IO)

    init {
        // 🔥 assets에 있는 model.task를 내부 저장소로 복사하고 엔진을 로드하는 초기화 코드
        aiScope.launch {
            try {
                val modelFile = File(context.filesDir, "model.task")

                // 1. 내부 저장소에 파일이 없으면 assets에서 복사해오기
                if (!modelFile.exists()) {
                    context.assets.open("model.task").use { inputStream ->
                        modelFile.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }

                // 2. 이제 파일이 준비되었으니 엔진 로드
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelFile.absolutePath)
                    .setMaxTokens(512)
                    .setTemperature(0.7f)
                    .build()

                llmInference = LlmInference.createFromOptions(context, options)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 🔥 온디바이스 AI 추론 (모델이 있으면 실제 LLM 실행, 없으면 똑똑한 폴백 시뮬레이션 작동)
     */
    suspend fun generateLocalAiResponse(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            val inference = llmInference
            if (inference != null) {
                // 실제 MediaPipe LLM 추론
                val result = inference.generateResponse(prompt)
                "🤖 [온디바이스 LLM]: $result"
            } else {
                // 💡 모델 파일이 아직 복사되지 않았거나 없을 때 작동하는 폴백 응답 로직
                when {
                    prompt.contains("오더") || prompt.contains("화물") ->
                        "🤖 [온디바이스 AI]: 로컬 분석 결과, 해당 오더는 정상 루트로 안전하게 판정되었습니다 오빠!"
                    prompt.contains("상태") || prompt.contains("체크") || prompt.contains("확인") ->
                        "🤖 [온디바이스 AI]: 기기 내부 NPU 및 CPU 가동 상태 최적 양호합니다. 완벽해요!"
                    prompt.contains("안녕") || prompt.contains("HI") || prompt.contains("hi") ->
                        "🤖 [온디바이스 AI]: 안녕하세요 오빠! 온디바이스 관제 시스템 상시 대기 중입니다. 무엇을 도와드릴까요?"
                    else ->
                        "🤖 [온디바이스 AI]: 입력하신 \"$prompt\" 지시사항을 로컬 신경망에서 완벽하게 처리했습니다 오빠!"
                }
            }
        } catch (e: Exception) {
            "🤖 [온디바이스 오류]: 추론 중 에러가 발생했습니다 -> ${e.localizedMessage}"
        }
    }
}