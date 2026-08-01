package com.teminator.mypadnoteone.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teminator.mypadnoteone.domain.usecase.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI에 전달할 단발성 이벤트
sealed class MainUiEvent {
    object NavigateToAuth : MainUiEvent()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<MainUiEvent>()
    val uiEvent: SharedFlow<MainUiEvent> = _uiEvent.asSharedFlow()

    // 로그아웃 수행
    fun signOut() {
        viewModelScope.launch {
            signOutUseCase() // Domain 계층의 UseCase 호출
            _uiEvent.emit(MainUiEvent.NavigateToAuth)
        }
    }
}