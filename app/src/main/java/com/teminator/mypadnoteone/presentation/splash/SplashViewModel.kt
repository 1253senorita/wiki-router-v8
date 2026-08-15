package com.teminator.mypadnoteone.presentation.splash

import androidx.lifecycle.ViewModel
import com.teminator.mypadnoteone.domain.usecase.IsUserLoggedInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase
) : ViewModel() {

    // 임시 테스트용 우회 플래그 (필요시 false로 변경 가능)
    private val isBypassAuth = true

    // 유저가 로그인되어 있거나 우회 모드일 때 true 반환
    fun shouldNavigateToMain(): Boolean {
        return isBypassAuth || isUserLoggedInUseCase()
    }
}