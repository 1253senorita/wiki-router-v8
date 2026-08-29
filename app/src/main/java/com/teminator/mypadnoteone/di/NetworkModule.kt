package com.teminator.mypadnoteone.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.socket.client.IO
import io.socket.client.Socket
import javax.inject.Singleton
import java.net.URISyntaxException

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideSocket(): Socket {
        return try {
            // 에뮬레이터에서 호스트 PC의 로컬 서버(포트 3000)로 직접 접근
            IO.socket("http://10.0.2.2:3000")
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
    }
}