package com.terminator.mypadnoteone.presentation.client

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.teminator.mypadnoteone.data.datasource.local.GemmaLocalDataSource
import com.terminator.mypadnoteone.domain.usecase.WikiInterceptAndHoldOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class AIClientViewModel @Inject constructor(
    application: Application,
    private val interceptAndHoldOrderUseCase: WikiInterceptAndHoldOrderUseCase,
    private val gemmaDataSource: GemmaLocalDataSource
) : AndroidViewModel(application) {

    companion object {
        const val MODEL_DOWNLOAD_URL = "https://huggingface.co/lunaF/Qwen3.5-0.8B-LiteRT/resolve/main/model_quantized.tflite"
        const val TOKENIZER_DOWNLOAD_URL = "https://huggingface.co/lunaF/Qwen3.5-0.8B-LiteRT/resolve/main/tokenizer.json"
    }

    var aiStatusText by mutableStateOf("🤖 [AI 보미 관제] 시스템 실시간 모니터링 대기 중...")
        private set

    var isDownloading by mutableStateOf(false)
        private set

    var downloadProgressText by mutableStateOf("")
        private set

    // 🔥 [추가] 모델 다운로드 완료 여부 상태 변수 (UI에서 버튼 숨김 처리에 사용)
    var isModelDownloaded by mutableStateOf(false)
        private set

    val chatMessages = mutableStateListOf(
        "🤖 [AI 보미]: 안녕하세요 오빠! 온디바이스 AI 관제 화면에 진입했어요.",
        "📡 [시스템]: 위키 라우터 연결 완료. 실시간 오더 모니터링 중..."
    )

    init {
        checkAndInitModel()
        startListeningToBomiAgent()
    }

    /**
     * 🔥 파일이 있는지 안전하게 체크한 뒤 모델을 초기화하여 Crash 방지
     */
    private fun checkAndInitModel() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val filesDir = getApplication<Application>().filesDir
                val modelFile = File(filesDir, "model_quantized.tflite")
                val tokenizerFile = File(filesDir, "tokenizer.json")

                if (modelFile.exists() && tokenizerFile.exists()) {
                    withContext(Dispatchers.Main) {
                        aiStatusText = "🤖 [AI 보미 관제] 모델 파일 확인 중..."
                    }
                    gemmaDataSource.initModel()
                    withContext(Dispatchers.Main) {
                        aiStatusText = "🤖 [AI 보미 관제] 온디바이스 AI 모델 로드 완료!"
                        isModelDownloaded = true // 🔥 파일이 이미 있으므로 다운로드 완료 상태로 처리
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        aiStatusText = "📥 [안내]: 아직 AI 모델 파일이 없습니다. 아래 버튼을 눌러 다운로드를 진행해주세요!"
                        chatMessages.add("🤖 [AI 보미]: 오빠, 대화를 시작하려면 먼저 'AI 모델 다운로드' 버튼을 눌러줘요!")
                        isModelDownloaded = false // 🔥 파일이 없으므로 버튼 노출
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    aiStatusText = "⚠️ 모델 대기 중: ${e.localizedMessage}"
                }
            }
        }
    }

    private fun startListeningToBomiAgent() {
        viewModelScope.launch {
            interceptAndHoldOrderUseCase { isAllowed, statusMessage ->
                val formattedMessage = if (isAllowed) {
                    "[AI 하이패스 통과] $statusMessage"
                } else {
                    "[AI 보미 홀드 중] $statusMessage"
                }
                aiStatusText = formattedMessage
                chatMessages.add("📡 [데이터 연동]: $formattedMessage")
            }
        }
    }

    fun updateAiStatus(newText: String) {
        aiStatusText = newText
    }

    /**
     * 🔥 [후 처리 보완] 기기 여유 공간 체크 및 임시 파일(.tmp) 기반 안전 다운로드 수행
     */
    fun downloadAiModelFiles() {
        if (isDownloading) return

        viewModelScope.launch(Dispatchers.IO) {
            isDownloading = true
            try {
                val context = getApplication<Application>()
                val filesDir = context.filesDir

                // 1. 기기 여유 공간 체크 (예: 최소 1.5GB 이상 여유 공간 필요)
                val freeSpace = filesDir.freeSpace
                val requiredSpace = 1500L * 1024L * 1024L // 약 1.5GB
                if (freeSpace < requiredSpace) {
                    withContext(Dispatchers.Main) {
                        isDownloading = false
                        downloadProgressText = ""
                        aiStatusText = "❌ 다운로드 실패: 기기 저장공간이 부족합니다."
                        chatMessages.add("🤖 [AI 보미]: 오빠, 폰에 남은 용량이 부족해요! 공간을 좀 비워주세요.")
                    }
                    return@launch
                }

                // 2. 토크나이저 다운로드 (.tmp 임시 파일 적용)
                withContext(Dispatchers.Main) { downloadProgressText = "토크나이저 다운로드 중 (20MB)..." }
                val tokenizerFile = File(filesDir, "tokenizer.json")
                val tempTokenizerFile = File(filesDir, "tokenizer.json.tmp")
                downloadFileSafely(TOKENIZER_DOWNLOAD_URL, tempTokenizerFile, tokenizerFile)

                // 3. AI 모델 다운로드 (.tmp 임시 파일 적용)
                withContext(Dispatchers.Main) { downloadProgressText = "AI 모델 다운로드 중 (792MB)..." }
                val modelFile = File(filesDir, "model_quantized.tflite")
                val tempModelFile = File(filesDir, "model_quantized.tflite.tmp")
                downloadFileSafely(MODEL_DOWNLOAD_URL, tempModelFile, modelFile)

                // 4. 다운로드 완료 직후 모델 초기화 시도
                withContext(Dispatchers.Main) { downloadProgressText = "모델 엔진 초기화 중..." }
                gemmaDataSource.initModel()

                withContext(Dispatchers.Main) {
                    isDownloading = false
                    downloadProgressText = ""
                    aiStatusText = "🤖 [AI 보미 관제] 다운로드 및 모델 로드 완료!"
                    isModelDownloaded = true // 🔥 다운로드가 끝났으므로 버튼 숨김 처리 활성화!
                    chatMessages.add("🤖 [AI 보미]: 모델 파일 다운로드와 초기화가 완벽하게 끝났어요 오빠!")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isDownloading = false
                    downloadProgressText = ""
                    aiStatusText = "❌ 다운로드 실패: ${e.localizedMessage}"
                    chatMessages.add("🤖 [AI 보미]: 파일 다운로드 중 오류가 발생했어요 (${e.localizedMessage})")
                }
            }
        }
    }

    /**
     * 🔥 [안전한 다운로드 함수] 임시 파일(.tmp)로 먼저 받은 뒤, 100% 성공 시에만 정식 파일명으로 교체
     * 중간에 끊기거나 실패하면 찌꺼기 임시 파일은 스스로 청소합니다.
     */
    private fun downloadFileSafely(urlString: String, tempFile: File, targetFile: File) {
        try {
            val url = URL(urlString)
            val connection = url.openConnection()
            connection.connect()

            connection.getInputStream().use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }

            // 다운로드가 정상 완료되면 기존 정식 파일을 삭제하고 임시 파일을 정식 파일명으로 변경
            if (targetFile.exists()) {
                targetFile.delete()
            }
            if (!tempFile.renameTo(targetFile)) {
                throw IOException("임시 파일을 정식 파일로 전환하는 데 실패했습니다.")
            }
        } catch (e: Exception) {
            // 실패 시 남은 임시 찌꺼기 파일 삭제
            if (tempFile.exists()) {
                tempFile.delete()
            }
            throw e
        }
    }

    fun sendUserMessage(message: String) {
        if (message.isBlank()) return

        chatMessages.add("👤 [오빠]: $message")

        viewModelScope.launch {
            chatMessages.add("🤖 [AI 보미]: 생각 중...")

            try {
                val aiResponse = gemmaDataSource.generateResponse(message)

                if (chatMessages.isNotEmpty()) {
                    chatMessages.removeAt(chatMessages.lastIndex)
                }
                chatMessages.add("🤖 [AI 보미]: $aiResponse")
            } catch (e: Exception) {
                if (chatMessages.isNotEmpty()) {
                    chatMessages.removeAt(chatMessages.lastIndex)
                }
                chatMessages.add("🤖 [AI 보미]: 모델이 아직 준비되지 않았거나 오류가 발생했어요 (${e.localizedMessage})")
            }
        }
    }
}