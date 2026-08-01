package com.teminator.mypadnoteone.di

import com.google.firebase.auth.FirebaseAuth
import com.teminator.mypadnoteone.data.repository.AuthRepositoryImpl
import com.teminator.mypadnoteone.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        // FirebaseAuth.getInstance()로 직접 호출
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth
    ): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth)
    }
}