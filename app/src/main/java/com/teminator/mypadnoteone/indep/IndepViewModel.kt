package com.teminator.mypadnoteone.indep


import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class IndepViewModel @Inject constructor(
    private val audioEngine: IndepAudioEngine, // Hilt가 알아서 조립해 준 엔진 주입!
    private val serverUrl: String               // 설정된 서버 URL 주입!
) : ViewModel() {

    fun startPtt() {
        audioEngine.startRecording()
    }

    fun stopPtt() {
        audioEngine.stopRecording()
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release() // ViewModel이 사라질 때 리소스 해제
    }
}