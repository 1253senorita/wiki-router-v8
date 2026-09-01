package com.teminator.mypadnoteone.data.datasource.remote

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WikiRouterSocketDataSource @Inject constructor() {

    private val TAG = "WikiRouterSocket"
    private var socket: Socket? = null
    private var isConnected: Boolean = false

    // 1. 실제 Node.js Socket.io 서버 연결 초기화
    fun connectRouter(roomId: String, userId: String, peerId: String) {
        if (socket == null) {
            try {
                // 에뮬레이터 기준 로컬 서버 주소 (실제 기기 테스트 시 컴퓨터 IP로 변경)
                socket = IO.socket("http://10.0.2.2:3000")

                // 서버 연결 성공 이벤트
                socket?.on(Socket.EVENT_CONNECT) {
                    isConnected = true
                    Log.d(TAG, "WIKI-ROUTER 서버 연결 성공!")
                    // 연결 직후 방 입장(join-room) 신호 전송
                    joinRoom(roomId, userId, peerId)
                }

                // 서버 연결 끊김 이벤트
                socket?.on(Socket.EVENT_DISCONNECT) {
                    isConnected = false
                    Log.d(TAG, "WIKI-ROUTER 연결 해제")
                }
            } catch (e: Exception) {
                Log.e(TAG, "소켓 초기화 오류: ${e.message}")
            }
        }
        socket?.connect()
    }

    // 2. 방 입장 요청 (Node.js 서버의 'join-room' 이벤트와 매칭)
    private fun joinRoom(roomId: String, userId: String, peerId: String) {
        val data = JSONObject().apply {
            put("roomId", roomId)
            put("userId", userId)
            put("peerId", peerId)
        }
        socket?.emit("join-room", data)
        Log.d(TAG, "프라이빗 룸 [$roomId] 입장 요청 전송 (User: $userId)")
    }

    // 🎯 AI '보미'의 핵심 후킹(Hooking) 메서드 (기존 로직 유지)
    fun interceptAndFilterMessage(rawMessage: String, onBomiFiltered: (Boolean, String) -> Unit) {
        Log.d(TAG, "AI 보미 인터셉트 작동: $rawMessage")

        // 1차 필터링 로직 (예: 스팸 콜, 불필요한 단가 필터링, 단골 여부 체크)
        val isTrustedCustomer = rawMessage.contains("VIP") || rawMessage.contains("단골")

        if (isTrustedCustomer) {
            // 단골이거나 검증된 콜인 경우: 하이패스 통과 (즉시 연결)
            onBomiFiltered(true, "[보미 비서] 단골 고객 order입니다. 즉시 연결합니다!")
        } else {
            // 일반 콜인 경우: AI 보미가 1차 상담 및 임시 홀드 처리
            onBomiFiltered(false, "[보미 비서] 1차 상담 진행 중: 임시 홀드(Tentative Hold)를 겁니다.")
        }
    }

    // 3. PTT 음성 파일 동기화 전송 (서버의 'sync-audio-file' 이벤트와 매칭)
    fun sendAudioSync(roomId: String, senderId: String, blob: Any) {
        val data = JSONObject().apply {
            put("roomId", roomId)
            put("senderId", senderId)
            put("blob", blob)
        }
        socket?.emit("sync-audio-file", data)
    }

    // 4. 소켓 연결 해제
    fun disconnectRouter() {
        socket?.disconnect()
        socket = null
        isConnected = false
        Log.d(TAG, "WIKI-ROUTER 강제 연결 해제 완료")
    }
}