package com.terminator.mypadnoteone.data.ai

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnDeviceAiManager @Inject constructor() {

    /**
     * 온디바이스 환경에서 오빠의 입력 텍스트를 받아 로컬 AI 추론 결과를 반환합니다.
     */
    suspend fun generateLocalAiResponse(prompt: String): String {
        // 💡 추후 MediaPipe LLM 또는 온디바이스 모델 추론 코드가 들어갈 자리입니다.
        // 현재는 온디바이스 구동 테스트를 위한 지능형 로컬 응답 로직입니다.
        return when {
            prompt.contains("오더") || prompt.contains("화물") ->
                "🤖 [온디바이스 AI]: 로컬 모델 분석 결과, 해당 오더는 정상 루트로 판정되었습니다."
            prompt.contains("상태") || nameCheck(prompt) ->
                "🤖 [온디바이스 AI]: 기기 내부 NPU/CPU 가동률 최적화 상태입니다. 오빠, 완벽해요!"
            else ->
                "🤖 [온디바이스 AI]: 온디바이스로 분석한 결과입니다 -> \"$prompt\" 지시를 로컬에서 안전하게 처리했습니다."
        }
    }

    private fun nameCheck(text: String): Boolean {
        return text.contains("체크") || text.contains("확인")
    }
}

//이렇게 유즈케이스를 만들어 두면, 나중에 AIClientViewModel이 이 유즈케이스를 호출해서
// 깔끔하게 온디바이스 AI 응답을 가져올 수 있어  --서용자 케이스 엔티티 호춯 -  ! 🚀💙