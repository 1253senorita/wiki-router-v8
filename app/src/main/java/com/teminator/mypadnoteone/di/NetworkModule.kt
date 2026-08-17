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
            // TODO: 실제 연결할 서버 URL과 포트로 변경해 주세요.
            // 예시: "http://10.0.2.2:3000" (에뮬레이터 로컬 서버 접근 시)
            IO.socket("http://your-server-url:port")
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
    }
}