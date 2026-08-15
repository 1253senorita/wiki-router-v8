package com.teminator.mypadnoteone.data.repository

import com.teminator.mypadnoteone.data.datasource.remote.FirebaseAuthDataSource
import com.teminator.mypadnoteone.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuthDataSource: FirebaseAuthDataSource
) : AuthRepository {

    override fun signOut() {
        firebaseAuthDataSource.signOut()
    }

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuthDataSource.isUserLoggedIn()
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> = runCatching {
        firebaseAuthDataSource.signInWithEmail(email, password)
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<Unit> = runCatching {
        firebaseAuthDataSource.signUpWithEmail(email, password)
    }
}