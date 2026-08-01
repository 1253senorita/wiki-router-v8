package com.teminator.mypadnoteone.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teminator.mypadnoteone.domain.usecase.SignInUseCase
import com.teminator.mypadnoteone.domain.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiEvent {
    object NavigateToMain : AuthUiEvent()
    data class ShowToast(val message: String) : AuthUiEvent()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<AuthUiEvent>()
    val uiEvent: SharedFlow<AuthUiEvent> = _uiEvent.asSharedFlow()

    fun signIn(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            emitToast("이메일과 비밀번호를 입력해주세요.")
            return
        }

        viewModelScope.launch {
            signInUseCase(email, password)
                .onSuccess {
                    _uiEvent.emit(AuthUiEvent.NavigateToMain)
                }
                .onFailure { exception ->
                    emitToast("로그인 실패: ${exception.message}")
                }
        }
    }

    fun signUp(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            emitToast("이메일과 비밀번호를 입력해주세요.")
            return
        }

        viewModelScope.launch {
            signUpUseCase(email, password)
                .onSuccess {
                    emitToast("회원가입 성공!")
                    _uiEvent.emit(AuthUiEvent.NavigateToMain)
                }
                .onFailure { exception ->
                    emitToast("회원가입 실패: ${exception.message}")
                }
        }
    }

    private fun emitToast(message: String) {
        viewModelScope.launch {
            _uiEvent.emit(AuthUiEvent.ShowToast(message))
        }
    }
}