package com.teminator.mypadnoteone.di

import com.teminator.mypadnoteone.data.datasource.remote.WikiRouterSocketDataSource
import com.teminator.mypadnoteone.data.repository.BaroBaroRepositoryImpl
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
        impl: BaroBaroRepositoryImpl
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
            // 기존에 오빠가 쓰던 object 형태 또는 기본 생성자 방식 그대로 반환합니다.
            return WikiRouterSocketDataSource
        }
    }
}