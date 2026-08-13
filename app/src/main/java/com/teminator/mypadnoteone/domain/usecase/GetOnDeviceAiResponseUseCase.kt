package com.terminator.mypadnoteone.domain.usecase

import com.terminator.mypadnoteone.data.ai.OnDeviceAiManager
import javax.inject.Inject

class GetOnDeviceAiResponseUseCase @Inject constructor(
    private val onDeviceAiManager: OnDeviceAiManager
) {
    suspend operator fun invoke(prompt: String): String {
        return onDeviceAiManager.generateLocalAiResponse(prompt)
    }
}


//이렇게 유즈케이스를 만들어 두면, 나중에 AIClientViewModel이 이 유즈케이스를 호출해서
// 깔끔하게 온디바이스 AI 응답을 가져올 수 있어  --온 디바이스 메니져에  호출 -  ! 🚀💙