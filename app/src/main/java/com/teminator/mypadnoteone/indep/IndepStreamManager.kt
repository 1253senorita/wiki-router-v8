package com.teminator.mypadnoteone.indep

import android.util.Log
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONObject
import org.webrtc.VideoTrack
import java.net.URISyntaxException

class IndepStreamManager {
    companion object {
        private var socket: Socket? = null
    }

    var onRemoteStream: ((VideoTrack) -> Unit)? = null
    var onPeerJoined: ((String) -> Unit)? = null
    var onPeerLeft: ((String) -> Unit)? = null
    var onSignalReceived: ((String, JSONObject) -> Unit)? = null

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

            registerSignalingEvents()

            socket?.connect()
        } catch (e: URISyntaxException) {
            Log.e(IndepConfig.TAG, "URI Syntax Exception: ${e.message}")
            onError(e.message ?: "Invalid URL")
        }
    }

    private fun registerSignalingEvents() {
        socket?.off("peer-joined")
        socket?.on("peer-joined") { args ->
            if (args.isNotEmpty()) {
                val peerId = args[0].toString()
                Log.d(IndepConfig.TAG, "👥 상대방 입장: $peerId")
                onPeerJoined?.invoke(peerId)
            }
        }

        socket?.off("peer-left")
        socket?.on("peer-left") { args ->
            if (args.isNotEmpty()) {
                val peerId = args[0].toString()
                Log.d(IndepConfig.TAG, "👋 상대방 퇴장: $peerId")
                onPeerLeft?.invoke(peerId)
            }
        }

        socket?.off("signal")
        socket?.on("signal") { args ->
            if (args.isNotEmpty()) {
                try {
                    val data = args[0] as? JSONObject
                    val senderId = data?.optString("senderId") ?: ""
                    val payload = data?.optJSONObject("payload") ?: JSONObject()
                    onSignalReceived?.invoke(senderId, payload)
                } catch (e: Exception) {
                    Log.e(IndepConfig.TAG, "❌ 시그널 데이터 파싱 실패", e)
                }
            }
        }
    }

    fun sendSignal(targetId: String, payload: JSONObject) {
        try {
            val data = JSONObject().apply {
                put("targetId", targetId)
                put("payload", payload)
            }
            socket?.emit("signal", data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendPing(callback: (Long) -> Unit) {
        val startTime = System.currentTimeMillis()
        socket?.emit("ping", object : Ack {
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