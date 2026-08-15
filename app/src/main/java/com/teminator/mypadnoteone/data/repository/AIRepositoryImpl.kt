package com.teminator.mypadnoteone.data.repository

import com.teminator.mypadnoteone.data.datasource.local.GemmaLocalDataSource
import javax.inject.Inject

class AIRepositoryImpl @Inject constructor(
    private val gemmaLocalDataSource: GemmaLocalDataSource
) {

    // 1. 모델 초기화 및 파일 다운로드 수행 (예외 처리를 통해 성공 여부인 Boolean 반환 보장)
    suspend fun initializeAIModel(): Boolean {
        return try {
            gemmaLocalDataSource.initModel()
            true
        } catch (e: Exception) {
            false
        }
    }

    // 2. 프롬프트를 받아 AI 응답 생성
    suspend fun getAIResponse(prompt: String): String {
        return gemmaLocalDataSource.generateResponse(prompt)
    }
}