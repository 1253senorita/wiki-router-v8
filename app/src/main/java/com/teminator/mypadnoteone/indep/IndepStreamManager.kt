package com.teminator.mypadnoteone.indep

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import java.net.URISyntaxException

class IndepStreamManager {

    private var socket: Socket? = null
    private var isConnected = false
    private var currentRoom: String? = null

    /**
     * Socket.io 서버와 연결을 수립합니다.
     */
    fun connect(onResult: (Boolean) -> Unit) {
        try {
            Log.d(IndepConfig.TAG, "IndepStreamManager: Connecting to ${IndepConfig.SERVER_URL}")

            socket = IO.socket(IndepConfig.SERVER_URL)

            socket?.on(Socket.EVENT_CONNECT, Emitter.Listener {
                isConnected = true
                Log.d(IndepConfig.TAG, "IndepStreamManager: Connected successfully!")
                onResult(true)
            })

            socket?.on(Socket.EVENT_CONNECT_ERROR, Emitter.Listener { args ->
                isConnected = false
                val errorMsg = if (!args.isNullOrEmpty() && args[0] != null) args[0].toString() else "Unknown error"
                Log.e(IndepConfig.TAG, "IndepStreamManager: Connection failed: $errorMsg")
                onResult(false)
            })

            socket?.on(Socket.EVENT_DISCONNECT, Emitter.Listener {
                isConnected = false
                Log.d(IndepConfig.TAG, "IndepStreamManager: Disconnected from server")
            })

            // 💡 기존 연결 이벤트들 아래에 추가
            socket?.on("receive-message", Emitter.Listener { args ->
                if (!args.isNullOrEmpty() && args[0] != null) {
                    try {
                        val data = args[0] as org.json.JSONObject // 또는 사용하는 JSON 파싱 방식
                        val message = data.optString("message")
                        val senderId = data.optString("id")
                        Log.d(IndepConfig.TAG, "IndepStreamManager: Received message from [$senderId] -> $message")

                        // TODO: UI나 콜백을 통해 받은 텍스트 메시지 전달 처리
                    } catch (e: Exception) {
                        Log.e(IndepConfig.TAG, "IndepStreamManager: Error parsing received message", e)
                    }
                }
            })

            socket?.on("receive-image", Emitter.Listener { args ->
                if (!args.isNullOrEmpty() && args[0] != null) {
                    try {
                        val data = args[0] as org.json.JSONObject
                        val senderId = data.optString("id")
                        val fileName = data.optString("fileName")
                        Log.d(IndepConfig.TAG, "IndepStreamManager: Received image from [$senderId], file: $fileName")

                        // TODO: 이미지 데이터(Byte 배열 등) 추출 및 후속 처리
                    } catch (e: Exception) {
                        Log.e(IndepConfig.TAG, "IndepStreamManager: Error parsing received image", e)
                    }
                }
            })

            socket?.on("receive-sync-audio", Emitter.Listener { args ->
                if (!args.isNullOrEmpty() && args[0] != null) {
                    try {
                        val data = args[0] as org.json.JSONObject
                        val senderId = data.optString("id")
                        // 🎤 상대방이 보낸 음성 데이터 수신 처리
                        Log.d(IndepConfig.TAG, "IndepStreamManager: Received voice data from [$senderId]")
                    } catch (e: Exception) {
                        Log.e(IndepConfig.TAG, "IndepStreamManager: Error parsing received audio", e)
                    }
                }
            })



            socket?.connect()

        } catch (e: URISyntaxException) {
            Log.e(IndepConfig.TAG, "IndepStreamManager: Invalid Server URL", e)
            onResult(false)
        }
    }

    /**
     * 네트워크 핑(Ping) 테스트 수행
     */
    fun sendPing(onPongReceived: (Long) -> Unit) {
        if (!isConnected || socket == null) {
            Log.w(IndepConfig.TAG, "IndepStreamManager: Not connected.")
            return
        }

        val startTime = System.currentTimeMillis()

        socket?.emit("ping_event", Emitter.Listener {
            val latency = System.currentTimeMillis() - startTime
            onPongReceived(latency)
        })
    }

    /**
     * 특정 PTT 룸(Room) 입장 (서버 규격: join-room)
     */
    fun joinRoom(roomName: String, onResult: (Boolean) -> Unit) {
        if (!isConnected || socket == null) {
            Log.w(IndepConfig.TAG, "IndepStreamManager: Not connected.")
            onResult(false)
            return
        }

        currentRoom = roomName
        socket?.emit("join-room", roomName)
        Log.d(IndepConfig.TAG, "IndepStreamManager: Joined room [$roomName]")
        onResult(true)
    }

    /**
     * 방 나가기
     */
    fun leaveRoom() {
        if (currentRoom != null && socket != null) {
            // 서버에 leave 관련 로직이 있다면 추가 가능
            Log.d(IndepConfig.TAG, "IndepStreamManager: Left room [$currentRoom]")
            currentRoom = null
        }
    }

    /**
     * PTT 음성 스트림 바이너리 데이터 전송 (서버 규격: sync-audio-file)
     */
    /**
     * PTT 음성 스트림 바이너리 데이터 전송 (서버 규격: sync-audio-file)
     */
    /**
     * PTT 음성 스트림 바이너리 데이터 전송
     */
    fun sendVoiceData(data: ByteArray) {
        if (!isConnected || socket == null) {
            Log.w(IndepConfig.TAG, "IndepStreamManager: Cannot send voice, socket is not connected")
            return
        }

        // 💡 데이터가 정상적으로 전송될 때 로그 출력 (너무 빈번하면 버벅일 수 있으니 우선 확인용)
        Log.d(IndepConfig.TAG, "IndepStreamManager: Sending voice data size = ${data.size} bytes")

        val payload = mapOf("blob" to data)
        socket?.emit("sync-audio-file", payload)
    }

    /**
     * 소켓 연결 해제
     */
    fun disconnect() {
        leaveRoom()
        socket?.disconnect()
        socket?.off()
        isConnected = false
        Log.d(IndepConfig.TAG, "IndepStreamManager: Disconnected and cleaned up")
    }


    /**
     * 💬 텍스트 메시지 전송 (서버 규격: send-message)
     */
    fun sendMessage(message: String, userId: String, onResult: (Boolean) -> Unit = {}) {
        if (!isConnected || socket == null) {
            Log.w(IndepConfig.TAG, "IndepStreamManager: Cannot send message, socket is not connected")
            onResult(false)
            return
        }

        val payload = mapOf(
            "message" to message,
            "userId" to userId
        )

        socket?.emit("send-message", payload)
        Log.d(IndepConfig.TAG, "IndepStreamManager: Sent message -> $message")
        onResult(true)
    }

    /**
     * 🖼️ 이미지 전송 (서버 규격: send-image)
     * 이미지를 Base64 문자열 또는 ByteArray 형태로 변환하여 전송합니다.
     */
    fun sendImage(imageByteArray: ByteArray, fileName: String, onResult: (Boolean) -> Unit = {}) {
        if (!isConnected || socket == null) {
            Log.w(IndepConfig.TAG, "IndepStreamManager: Cannot send image, socket is not connected")
            onResult(false)
            return
        }

        val payload = mapOf(
            "image" to imageByteArray,
            "fileName" to fileName
        )

        socket?.emit("send-image", payload)
        Log.d(IndepConfig.TAG, "IndepStreamManager: Sent image size = ${imageByteArray.size} bytes")
        onResult(true)
    }







}