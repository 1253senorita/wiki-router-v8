package com.terminator.mypadnoteone.indep

import android.content.Context
import com.teminator.mypadnoteone.indep.IndepAudioEngine
import com.teminator.mypadnoteone.indep.IndepConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object IndepModule {

    // 서버 URL 같은 설정값(String)이 필요할 때 Hilt로 공급
    @Provides
    @Singleton
    fun provideServerUrl(): String {
        return IndepConfig.SERVER_URL
    }

    // IndepAudioEngine을 Hilt가 생성해서 관리할 수 있도록 제공하는 함수
    @Provides
    @Singleton
    fun provideIndepAudioEngine(
        @ApplicationContext context: Context
    ): IndepAudioEngine {
        // 엔진 생성 시 필요한 Context를 Hilt가 @ApplicationContext로 자동 주입해 준다.
        // onAudioDataCaptured 콜백은 엔진을 실제로 사용하는 곳(ViewModel 등)에서
        // 메서드나 리스너 형태로 연결해 주면 된다.
        return IndepAudioEngine(context) { audioBytes, length ->
            // 여기에 기본 오디오 데이터 캡처 시 처리할 공통 로직이나 비워둘 수 있는 코드를 넣는다.
        }
    }
}