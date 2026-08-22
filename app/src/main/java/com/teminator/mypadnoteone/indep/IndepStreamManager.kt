package com.teminator.mypadnoteone.indep

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONObject
import java.net.URISyntaxException

class IndepStreamManager {
    companion object {
        private var socket: Socket? = null
    }

    fun connect(callback: (Boolean) -> Unit) {
        try {
            if (socket != null && socket?.connected() == true) {
                callback(true)
                return
            }

            // 로컬 서버 접속 주소 (나중에 클라우드 주소로 변경할 위치)
            socket = IO.socket("http://10.0.2.2:8080")

            socket?.on(Socket.EVENT_CONNECT) {
                callback(true)
            }?.on(Socket.EVENT_CONNECT_ERROR) {
                callback(false)
            }

            socket?.connect()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            callback(false)
        }
    }

    // 💎 서버와 핑퐁 테스트를 수행하는 메서드
    fun sendPing(callback: (Long) -> Unit) {
        val startTime = System.currentTimeMillis()

        socket?.emit("ping", object : io.socket.client.Ack {
            override fun call(vararg args: Any) {
                val latency = System.currentTimeMillis() - startTime
                callback(latency)
            }
        })
    }

    // 💎 방 입장 메서드
    fun joinRoom(roomId: String, callback: (Boolean) -> Unit) {
        socket?.emit("join-room", roomId)
        callback(true)
    }

    // 💎 방 퇴장 메서드
    fun leaveRoom() {
        socket?.emit("leave-room")
    }

    // 💎 채팅 메시지 전송 메서드
    fun sendChatMessage(message: String, senderId: String, callback: (Boolean) -> Unit) {
        try {
            val data = JSONObject().apply {
                put("message", message)
                put("senderId", senderId)
            }
            socket?.emit("chat-message", data)
            callback(true)
        } catch (e: Exception) {
            e.printStackTrace()
            callback(false)
        }
    }

    // 💎 텍스트 메시지 수신 리스너 등록 메서드
    fun onMessageReceived(listener: (String, String) -> Unit) {
        socket?.off("chat-message")
        socket?.on("chat-message") { args ->
            if (args.isNotEmpty()) {
                try {
                    val data = args[0] as? JSONObject
                    if (data != null) {
                        val senderId = data.optString("senderId", "Unknown")
                        val message = data.optString("message", "")
                        listener(senderId, message)
                    }
                } catch (e: Exception) {
                    Log.e("PTT_DATA_CHECK", "❌ [예외 발생] 메시지 수신 실패", e)
                }
            }
        }
    }

    // 💎 서버가 던져주는 오디오 스트림을 받는(Receive) 리스너 위치
    fun onAudioReceived(listener: (ByteArray) -> Unit) {
        socket?.off("receive-sync-audio")
        socket?.on("receive-sync-audio") { args ->
            if (args.isNotEmpty()) {
                try {
                    val data = args[0] as? JSONObject
                    if (data != null) {
                        val blobObj = data.opt("blob")
                        if (blobObj is JSONArray) {
                            val bytes = ByteArray(blobObj.length())
                            for (i in 0 until blobObj.length()) {
                                bytes[i] = blobObj.getInt(i).toByte()
                            }

                            Log.e("PTT_DATA_CHECK", "🎉 [앱 수신 성공!] 서버로부터 오디오 패킷 도착! 크기: ${bytes.size} bytes")
                            listener(bytes)
                        } else {
                            Log.e("PTT_DATA_CHECK", "⚠️ [형식 오류] blob이 JSONArray가 아님")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("PTT_DATA_CHECK", "❌ [예외 발생] 오디오 수신 실패", e)
                }
            }
        }
    }

    // 💎 마이크 오디오 바이트를 서버로 던지는(Send) 함수 위치
    fun sendVoiceData(audioBytes: ByteArray) {
        try {
            val data = JSONObject().apply {
                put("blob", audioBytes)
            }
            socket?.emit("sync-audio-file", data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
    }
}