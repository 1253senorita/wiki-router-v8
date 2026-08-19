package com.teminator.mypadnoteone.di

import com.teminator.mypadnoteone.data.datasource.remote.WikiRouterSocketDataSource
import com.teminator.mypadnoteone.data.repository.BaroBaroHybridRepositoryImpl // 👈 하이브리드 구현체 임포트
import com.teminator.mypadnoteone.data.repository.WikiRouterRepositoryImpl
import com.teminator.mypadnoteone.domain.repository.BaroBaroRepository
import com.terminator.mypadnoteone.domain.repository.WikiRouterRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BaroBaroModule {

    @Binds
    @Singleton
    abstract fun bindBaroBaroRepository(
        impl: BaroBaroHybridRepositoryImpl // 👈 주입할 구현체를 하이브리드 버전으로 변경
    ): BaroBaroRepository

    @Binds
    @Singleton
    abstract fun bindWikiRouterRepository(
        impl: WikiRouterRepositoryImpl
    ): WikiRouterRepository

    companion object {
        @Provides
        @Singleton
        fun provideWikiRouterSocketDataSource(): WikiRouterSocketDataSource {
            return WikiRouterSocketDataSource
        }
    }
}