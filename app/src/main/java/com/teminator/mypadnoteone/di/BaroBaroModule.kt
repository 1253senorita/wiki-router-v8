package com.teminator.mypadnoteone.di

import com.teminator.mypadnoteone.data.repository.BaroBaroRepositoryImpl
import com.teminator.mypadnoteone.domain.repository.BaroBaroRepository
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
        impl: BaroBaroRepositoryImpl
    ): BaroBaroRepository
}