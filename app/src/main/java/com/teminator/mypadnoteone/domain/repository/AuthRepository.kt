package com.teminator.mypadnoteone.domain.repository

interface AuthRepository {
    fun signOut()
    fun isUserLoggedIn(): Boolean
    suspend fun signInWithEmail(email: String, password: String): Result<Unit>
    suspend fun signUpWithEmail(email: String, password: String): Result<Unit>
}