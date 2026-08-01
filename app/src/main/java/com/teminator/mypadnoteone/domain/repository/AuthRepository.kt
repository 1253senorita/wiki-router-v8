package com.teminator.mypadnoteone.domain.repository

interface AuthRepository {
    fun signOut()
    fun isUserLoggedIn(): Boolean
}