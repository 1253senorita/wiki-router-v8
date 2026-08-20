package com.teminator.mypadnoteone.presentation.aerorouter.socket

import android.util.Log
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

class AeroMultiSocketManager {

    private var socket: Socket? = null
    private val TAG = "AeroMultiSocketManager"

    // 💡 올바른 HTTPS 서버 주소 설정 (오타 수정 완료)
    // 에뮬레이터 테스트용 주소
    private val SERVER_URL = "https://10.0.2.2:8080"

    /**
     * 서버 연결 초기화 (OkHttp를 통한 사설 HTTPS 인증서 우회 적용)
     */
    fun connect(onConnected: () -> Unit, onError: (String) -> Unit) {
        try {
            // 1. 사설 HTTPS 인증서 검증 무시를 위한 TrustManager 설정
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            })

            val sslContext = SSLContext.getInstance("TLS").apply {
                init(null, trustAllCerts, SecureRandom())
            }

            // 2. 사설 인증서와 호스트네임을 신뢰하는 OkHttpClient 생성
            val okHttpClient = OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .build()

            // 3. Socket.io 옵션에 OkHttpClient 주입 (callFactory, webSocketFactory 활용)
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

    /**
     * 방 입장 요청
     */
    fun joinRoom(roomId: String) {
        socket?.emit("join-room", roomId)
        Log.d(TAG, "🏠 방 입장 요청 전송 -> Room ID: [$roomId]")
    }

    /**
     * 오디오 데이터 전송
     */
    fun sendAudioData(audioByteArray: ByteArray) {
        val data = JSONObject().apply {
            put("blob", audioByteArray)
        }
        socket?.emit("sync-audio-file", arrayOf<Any>(data))
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
}