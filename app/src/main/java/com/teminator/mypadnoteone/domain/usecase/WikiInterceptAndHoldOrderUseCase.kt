package com.terminator.mypadnoteone.domain.usecase

import com.terminator.mypadnoteone.domain.repository.WikiRouterRepository
import javax.inject.Inject

class WikiInterceptAndHoldOrderUseCase @Inject constructor(
    private val wikiRouterRepository: WikiRouterRepository
) {
    // AI 보미의 인터셉트 및 홀드 로직을 실행하는 오퍼레이터 함수
    operator fun invoke(onResult: (Boolean, String) -> Unit) {
        wikiRouterRepository.observeIncomingMessages { isAllowed, statusMessage ->
            // 필요하다면 여기서 추가적인 비즈니스 가공을 거친 뒤 UI로 토스합니다.
            onResult(isAllowed, statusMessage)
        }
    }
}