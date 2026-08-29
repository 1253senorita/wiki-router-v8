package com.teminator.mypadnoteone.di

import com.teminator.mypadnoteone.indep.IndepConfig // 💡 IndepConfig 임포트 추가!
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
            // 💡 "http://your-server-url:port" 대신 IndepConfig.SERVER_URL을 사용합니다!
            IO.socket(IndepConfig.SERVER_URL)
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
    }
}