package com.teminator.mypadnoteone.indep

import android.util.Log

class IndepStreamManager {

    fun connect() {
        Log.d(IndepConfig.TAG, "IndepStreamManager: Connecting to ${IndepConfig.SERVER_URL}")
        // TODO: Socket.io 또는 WebRTC 연결 구현
    }

    fun sendVoiceData(data: ByteArray) {
        // TODO: 서버로 오디오 바이트 전송
    }

    fun disconnect() {
        Log.d(IndepConfig.TAG, "IndepStreamManager: Disconnected")
    }
}