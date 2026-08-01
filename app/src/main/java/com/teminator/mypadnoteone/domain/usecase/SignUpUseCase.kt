package com.teminator.mypadnoteone.domain.usecase

import com.teminator.mypadnoteone.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        return authRepository.signUpWithEmail(email, password)
    }
}