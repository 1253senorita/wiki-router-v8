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

    fun connect(onConnected: () -> Unit, onError: (String) -> Unit) {
        try {
            if (socket != null && socket?.connected() == true) {
                onConnected()
                return
            }

            val options = IO.Options().apply {
                forceNew = true
                reconnection = true
                reconnectionAttempts = 5
                reconnectionDelay = 1000
            }

            socket = IO.socket(IndepConfig.SERVER_URL, options)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(IndepConfig.TAG, "Socket Connected Successfully!")
                onConnected()
            }?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val errorMsg = if (args.isNotEmpty()) args[0].toString() else "Unknown Connection Error"
                Log.e(IndepConfig.TAG, "Connection Error: $errorMsg")
                onError(errorMsg)
            }

            socket?.connect()
        } catch (e: URISyntaxException) {
            Log.e(IndepConfig.TAG, "URI Syntax Exception: ${e.message}")
            onError(e.message ?: "Invalid URL")
        }
    }

    fun sendPing(callback: (Long) -> Unit) {
        val startTime = System.currentTimeMillis()
        socket?.emit("ping", object : io.socket.client.Ack {
            override fun call(vararg args: Any) {
                val latency = System.currentTimeMillis() - startTime
                callback(latency)
            }
        })
    }

    fun joinRoom(roomId: String) {
        socket?.emit("join-room", roomId)
        Log.d(IndepConfig.TAG, "🏠 방 입장 요청 전송 -> Room ID: [$roomId]")
    }

    fun leaveRoom() {
        socket?.emit("leave-room")
    }

    // 💬 카톡 스타일 텍스트 메시지 전송
    fun sendChatMessage(message: String, senderId: String, callback: (Boolean) -> Unit) {
        try {
            val data = JSONObject().apply {
                put("message", message)
                put("senderId", senderId)
                put("timestamp", System.currentTimeMillis())
            }
            socket?.emit("chat-message", data)
            callback(true)
        } catch (e: Exception) {
            e.printStackTrace()
            callback(false)
        }
    }

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
                    Log.e(IndepConfig.TAG, "❌ 메시지 수신 실패", e)
                }
            }
        }
    }

    // 🖼️ 이미지 + 캡션(텍스트) 전송
    fun sendImageMessage(imageBytes: ByteArray, senderId: String, caption: String = "", callback: (Boolean) -> Unit) {
        try {
            val data = JSONObject().apply {
                put("image", imageBytes)
                put("senderId", senderId)
                put("message", caption)
                put("timestamp", System.currentTimeMillis())
            }
            socket?.emit("chat-image", data)
            callback(true)
        } catch (e: Exception) {
            e.printStackTrace()
            callback(false)
        }
    }

    fun onImageReceived(listener: (String, ByteArray, String) -> Unit) {
        socket?.off("chat-image")
        socket?.on("chat-image") { args ->
            if (args.isNotEmpty()) {
                try {
                    val data = args[0] as? JSONObject
                    val blobObj = data?.opt("image")
                    val senderId = data?.optString("senderId", "Unknown") ?: "Unknown"
                    val caption = data?.optString("message", "") ?: ""

                    if (blobObj is JSONArray) {
                        val bytes = ByteArray(blobObj.length())
                        for (i in 0 until blobObj.length()) {
                            bytes[i] = blobObj.getInt(i).toByte()
                        }
                        listener(senderId, bytes, caption)
                    }
                } catch (e: Exception) {
                    Log.e(IndepConfig.TAG, "❌ 이미지 수신 실패", e)
                }
            }
        }
    }

    // 🎙️ 오디오 스트림 데이터 전송 (Aero 스타일 적용)
    fun sendVoiceData(audioByteArray: ByteArray) {
        try {
            val data = JSONObject().apply {
                put("blob", audioByteArray)
            }
            socket?.emit("sync-audio-file", arrayOf<Any>(data))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onAudioReceived(listener: (ByteArray) -> Unit) {
        socket?.off("receive-sync-audio")
        socket?.on("receive-sync-audio") { args ->
            if (args.isNotEmpty()) {
                try {
                    val data = args[0] as? JSONObject
                    val blobObj = data?.opt("blob")
                    if (blobObj is JSONArray) {
                        val bytes = ByteArray(blobObj.length())
                        for (i in 0 until blobObj.length()) {
                            bytes[i] = blobObj.getInt(i).toByte()
                        }
                        listener(bytes)
                    }
                } catch (e: Exception) {
                    Log.e(IndepConfig.TAG, "❌ 오디오 수신 실패", e)
                }
            }
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        Log.d(IndepConfig.TAG, "Socket Disconnected.")
    }
}