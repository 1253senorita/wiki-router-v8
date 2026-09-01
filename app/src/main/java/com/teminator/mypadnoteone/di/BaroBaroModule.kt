package com.teminator.mypadnoteone.di

import com.teminator.mypadnoteone.data.repository.BaroBaroHybridRepositoryImpl
import com.teminator.mypadnoteone.data.repository.WikiRouterRepositoryImpl
import com.teminator.mypadnoteone.domain.repository.BaroBaroRepository
import com.teminator.mypadnoteone.domain.repository.WikiRouterRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BaroBaroModule {

    @Binds
    @Singleton
    abstract fun bindBaroBaroRepository(
        impl: BaroBaroHybridRepositoryImpl
    ): BaroBaroRepository

    @Binds
    @Singleton
    abstract fun bindWikiRouterRepository(
        impl: WikiRouterRepositoryImpl
    ): WikiRouterRepository
}