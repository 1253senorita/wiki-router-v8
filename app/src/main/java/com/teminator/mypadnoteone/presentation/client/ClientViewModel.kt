package com.terminator.mypadnoteone.presentation.client

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ClientViewModel @Inject constructor() : ViewModel() {

    // 고객이 요청한 내역이나 상태를 관리할 수 있는 변수 예시
    var clientStatusText by mutableStateOf("고객님, 환영합니다. 화물을 의뢰해 주세요.")
        private set

    fun updateStatus(newText: String) {
        clientStatusText = newText
    }
}