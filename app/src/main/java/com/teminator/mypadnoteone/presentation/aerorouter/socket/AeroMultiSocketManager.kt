package com.teminator.mypadnoteone.presentation.aerorouter.socket

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException

class AeroMultiSocketManager {

    private var socket: Socket? = null
    private val TAG = "AeroMultiSocket"
    private val SERVER_URL = "http://YOUR_SERVER_IP_OR_DOMAIN:8080" // 서버 주소에 맞게 수정

    /**
     * 서버 연결 초기화
     */
    fun connect(onConnected: () -> Unit, onError: (String) -> Unit) {
        try {
            val options = IO.Options().apply {
                forceNew = true
                reconnection = true
            }
            socket = IO.socket(SERVER_URL, options)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "서버 연결 성공")
                onConnected()
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val err = if (args.isNotEmpty()) args[0].toString() else "Connection Error"
                onError(err)
            }

            socket?.connect()
        } catch (e: URISyntaxException) {
            onError(e.message ?: "URL Error")
        }
    }

    /**
     * 1. 권한 인증 요청 (get_oi) -> 서버 코드의 PASSWORDS 검증과 연동
     */
    fun requestAuth(userId: String, userPw: String, modeId: String, callback: (Boolean, String) -> Unit) {
        val data = JSONObject().apply {
            put("userId", userId)
            put("userPw", userPw)
            put("modeId", modeId) // 'DEV_MASTER', 'GUEST_USER', 'NORMAL_USER'
        }

        socket?.emit("get_oi", arrayOf<Any>(data)) { args ->
            if (args != null && args.isNotEmpty() && args[0] is JSONObject) {
                val res = args[0] as JSONObject
                val success = res.optBoolean("success", false)
                val payload = res.optJSONObject("payload")
                val text = payload?.optString("text") ?: "응답 없음"
                callback(success, text)
            } else {
                callback(false, "서버 응답 오류")
            }
        }
    }

    /**
     * 2. 룸 입장 (join-room)
     */
    fun joinRoom(roomId: String) {
        socket?.emit("join-room", roomId)
        Log.d(TAG, "방 입장 요청: $roomId")
    }

    /**
     * 3. [무전기 / 전화기 모드] 오디오 스트리밍 데이터 전송 (sync-audio-file)
     */
    fun sendAudioData(audioByteArray: ByteArray) {
        val data = JSONObject().apply {
            put("blob", audioByteArray)
        }
        socket?.emit("sync-audio-file", arrayOf<Any>(data))
    }

    /**
     * [무전기 / 전화기 모드] 상대방 오디오 수신 리스너 등록
     */
    fun onReceiveAudio(callback: (ByteArray, String) -> Unit) {
        socket?.on("receive-sync-audio") { args ->
            if (args != null && args.isNotEmpty() && args[0] is JSONObject) {
                val obj = args[0] as JSONObject
                // 서버에서 전달된 blob 및 유저 식별값 처리
                // 실제 구현 시 바이너리 배열 추출 로직 추가
                val senderId = obj.optString("id", "unknown")
                // val blobData = obj.opt("blob") as? ByteArray
                // if (blobData != null) callback(blobData, senderId)
            }
        }
    }

    /**
     * 4. [문자 / 카톡 모드] 텍스트 메시지 송신 이벤트 예시
     */
    fun sendChatMessage(message: String, roomId: String) {
        val data = JSONObject().apply {
            put("room", roomId)
            put("message", message)
            put("time", System.currentTimeMillis())
        }
        socket?.emit("chat-message", arrayOf<Any>(data))
    }

    /**
     * [문자 / 카톡 모드] 텍스트 메시지 수신 리스너 예시
     */
    fun onReceiveChatMessage(callback: (String, String) -> Unit) {
        socket?.on("receive-chat-message") { args ->
            if (args != null && args.isNotEmpty() && args[0] is JSONObject) {
                val obj = args[0] as JSONObject
                val sender = obj.optString("sender", "상대방")
                val msg = obj.optString("message", "")
                callback(sender, msg)
            }
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
    }
}