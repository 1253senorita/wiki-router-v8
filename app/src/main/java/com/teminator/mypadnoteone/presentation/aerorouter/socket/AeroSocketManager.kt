package com.teminator.mypadnoteone.presentation.aerorouter.socket

import android.util.Log
import com.teminator.mypadnoteone.data.datasource.remote.WikiRouterSocketDataSource
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.net.URISyntaxException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class AeroSocketManager {

    private var socket: Socket? = null
    private val TAG = "AeroSocketManager"

    // 서버가 HTTPS로 열려있으므로 10.0.2.2 HTTPS 주소 지정
    private val SERVER_URL = "https://10.0.2.2:8080"

    /**
     * Socket.io 서버 연결 초기화 (HTTPS 사설 인증서 우회 적용)
     */
    fun connect(onConnected: () -> Unit, onError: (String) -> Unit) {
        try {
            // 사설 HTTPS 인증서 검증 무시 설정
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            })

            val sslContext = SSLContext.getInstance("TLS").apply {
                init(null, trustAllCerts, SecureRandom())
            }

            val okHttpClient = OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .build()

            val options = IO.Options().apply {
                callFactory = okHttpClient
                webSocketFactory = okHttpClient
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
                callback(success, text)
            } else {
                callback(false, "서버 응답 오류")
            }
        }
    }

    fun joinRoom(roomId: String) {
        socket?.emit("join-room", roomId)
        Log.d(TAG, "🏠 방 입장 요청 전송 -> Room ID: [$roomId]")
    }

    fun sendAudioData(audioByteArray: ByteArray) {
        val data = JSONObject().apply {
            put("blob", audioByteArray)
        }
        socket?.emit("sync-audio-file", arrayOf<Any>(data))
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        Log.d(TAG, "Socket Disconnected.")
    }
}