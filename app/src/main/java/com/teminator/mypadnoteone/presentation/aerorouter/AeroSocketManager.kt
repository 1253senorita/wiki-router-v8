package com.teminator.mypadnoteone.presentation.aerorouter

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException

class AeroSocketManager {

    private var socket: Socket? = null
    private val TAG = "AeroSocketManager"

    // 💡 에뮬레이터 테스트 시 http://10.0.2.2:8080 (실기기는 PC IP 사용)
    private val SERVER_URL = "http://10.0.2.2:8080"

    /**
     * Socket.io 서버 연결 초기화
     */
    fun connect(onConnected: () -> Unit, onError: (String) -> Unit) {
        try {
            val options = IO.Options().apply {
                forceNew = true
                reconnection = true
                reconnectionAttempts = 5
                reconnectionDelay = 1000
            }

            socket = IO.socket(SERVER_URL, options)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Socket Connected Successfully!")
                onConnected()
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val errorMsg = if (args.isNotEmpty()) args[0].toString() else "Unknown Connection Error"
                Log.e(TAG, "Connection Error: $errorMsg")
                onError(errorMsg)
            }

            socket?.connect()

        } catch (e: URISyntaxException) {
            Log.e(TAG, "URI Syntax Exception: ${e.message}")
            onError(e.message ?: "Invalid URL")
        }
    }

    /**
     * 1. [권한 및 자동 룸 분기 인증] get_oi 이벤트
     * 서버 측에서 인증 성공 시 해당 modeId 룸으로 자동 socket.join() 수행됨
     * modeId 종류: 'DEV_MASTER' (PW: 1234), 'GUEST_USER' (PW: 0000), 'NORMAL_USER' (PW: 1111)
     */
    fun requestWikiAuth(userId: String, userPw: String, modeId: String, callback: (Boolean, String) -> Unit) {
        val data = JSONObject().apply {
            put("userId", userId)
            put("userPw", userPw)
            put("modeId", modeId)
        }

        socket?.emit("get_oi", arrayOf<Any>(data)) { args ->
            if (args != null && args.isNotEmpty() && args[0] is JSONObject) {
                val res = args[0] as JSONObject
                val success = res.optBoolean("success", false)
                val payload = res.optJSONObject("payload")
                val text = payload?.optString("text") ?: "응답 없음"

                Log.d(TAG, "Auth Result -> Success: $success, Message: $text")
                callback(success, text)
            } else {
                callback(false, "서버 응답 오류")
            }
        }
    }

    /**
     * 2. [수동 룸 입장 분기] join-room 이벤트
     * 특정 방 번호(roomId)를 인자로 보내어 서버 룸에 가입
     */
    fun joinRoom(roomId: String) {
        socket?.emit("join-room", roomId)
        Log.d(TAG, "🏠 방 입장 요청 전송 -> Room ID: [$roomId]")
    }

    /**
     * 3. [무전기 오디오 전송] sync-audio-file 이벤트
     */
    fun sendAudioData(audioByteArray: ByteArray) {
        val data = JSONObject().apply {
            put("blob", audioByteArray)
        }
        socket?.emit("sync-audio-file", arrayOf<Any>(data))
    }

    /**
     * 4. [무전기 오디오 수신 리스너] receive-sync-audio 이벤트
     */
    fun onReceiveAudio(callback: (ByteArray, String) -> Unit) {
        socket?.on("receive-sync-audio") { args ->
            if (args != null && args.isNotEmpty() && args[0] is JSONObject) {
                val obj = args[0] as JSONObject
                val senderId = obj.optString("id", "unknown")
                // 추후 바이너리 블롭 데이터 핸들링 위치
                Log.d(TAG, "🎤 오디오 수신됨 from: $senderId")
            }
        }
    }

    /**
     * 연결 해제
     */
    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        Log.d(TAG, "Socket Disconnected.")
    }
    fun sendAudio(buffer: ByteArray, length: Int) {
        // Socket.io 또는 WebSocket을 통해 바이트 배열 스트리밍 전송 로직
        // 예: socket.emit("audio_stream", buffer)
    }


}