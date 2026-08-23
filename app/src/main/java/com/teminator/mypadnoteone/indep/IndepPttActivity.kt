package com.teminator.mypadnoteone.presentation.barobaro.room

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teminator.mypadnoteone.domain.model.DispatchOrder
import com.teminator.mypadnoteone.indep.IndepAudioEngine
import com.teminator.mypadnoteone.indep.IndepStreamManager
import java.io.ByteArrayOutputStream

@Composable
fun MatchingRoomScreen(
    roomId: String,
    order: DispatchOrder?, // 수락한 화물 오더 정보
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    // 💡 1. 독립 통신 매니저 및 오디오 엔진 직접 생성 (remember를 통해 화면 생명주기 동안 유지)
    val streamManager = remember { IndepStreamManager() }

    // 오디오 캡처 시 소켓을 통해 실시간 음성(PCM 데이터) 송신
    val audioEngine = remember {
        IndepAudioEngine(context) { buffer, length ->
            val actualData = if (length == buffer.size) buffer else buffer.copyOfRange(0, length)
            streamManager.sendVoiceData(actualData)
        }
    }

    // 💡 2. 화면 내부 상태 관리 변수들
    var roomStatus by remember { mutableStateOf("연결 대기 중") }
    var lastLogMessage by remember { mutableStateOf<String?>(null) }
    var inputMessage by remember { mutableStateOf("") }
    var isVideoMode by remember { mutableStateOf(false) }
    var isConnected by remember { mutableStateOf(false) }
    var isPttActive by remember { mutableStateOf(false) } // PTT 무전 송출 상태

    // 💡 3. 화면 최초 진입 시 소켓 연결 및 리스너 등록
    LaunchedEffect(roomId) {
        roomStatus = "소켓 통신 파이프 연결 시도 중... ($roomId)"

        streamManager.connect(
            onConnected = {
                isConnected = true
                roomStatus = "통신 연결 완료 (매칭 방 활성화)"
                streamManager.joinRoom(roomId)
                lastLogMessage = "시스템: [$roomId] 방에 성공적으로 입장했습니다."
            },
            onError = { err ->
                isConnected = false
                roomStatus = "연결 실패"
                lastLogMessage = "에러: $err"
            }
        )

        // 상대방 메시지 수신 리스너
        streamManager.onMessageReceived { senderId, message ->
            lastLogMessage = "상대방($senderId): $message"
        }

        // 상대방 음성(PTT) 데이터 수신 리스너 -> 오디오 트랙으로 즉시 재생
        streamManager.onAudioReceived { audioBytes ->
            if (audioBytes.isNotEmpty()) {
                audioEngine.playAudio(audioBytes)
            }
        }
    }

    // 💡 4. 화면이 완전히 종료될 때 소켓 및 오디오 리소스 안전 해제
    DisposableEffect(Unit) {
        onDispose {
            audioEngine.release()
            streamManager.disconnect()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ==========================================
            // 1. 상단 타이틀 및 모드 전환 영역
            // ==========================================
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WIKI-ROUTER HYBRID CALL",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { isVideoMode = !isVideoMode },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = if (isVideoMode) "📹 영상 모드" else "🎙️ 음성(PTT) 모드",
                                color = Color(0xFFFF5722),
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = {
                                streamManager.leaveRoom()
                                onBackClick()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF424242)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("방 나가기", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "룸 ID: $roomId",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB0BEC5)
                )

                if (order != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "📦 매칭된 화물 정보 (#${order.id})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = Color(0xFFFF5722)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "경로: ${order.route}", style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = Color.White)
                            Text(text = "화물: ${order.cargoInfo} | 요금: ${order.price}", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                            if (!order.description.isNullOrBlank()) {
                                Text(text = "요청사항: ${order.description}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.DarkGray)
            }

            // ==========================================
            // 2. 중앙 컨테이너 (상태 로그 및 대화 화면)
            // ==========================================
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                if (!isVideoMode) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "상태: $roomStatus",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFFFF5722),
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (!lastLogMessage.isNullOrBlank()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                            ) {
                                Text(
                                    text = "로그: $lastLogMessage",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                            }
                        } else {
                            Text(
                                text = "기사와 화주 간의 통신 파이프 대기 중...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "상대방 영상 스트림 송수신 중...",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 3. 하단 액션 및 실시간 대화 입력 바 영역
            // ==========================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 실시간 텍스트 채팅 입력 및 전송
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        placeholder = { Text("상대방에게 전달할 메시지 입력...", color = Color.Gray) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        maxLines = 1,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF5722),
                            unfocusedBorderColor = Color.DarkGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputMessage.isNotBlank()) {
                                // 💡 StreamManager를 통한 텍스트 채팅 송신
                                streamManager.sendChatMessage(inputMessage, "OppaUser") { success ->
                                    if (success) {
                                        lastLogMessage = "나: $inputMessage"
                                        inputMessage = ""
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color(0xFFFF5722), RoundedCornerShape(8.dp)),
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFFF5722))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "전송",
                            tint = Color.White
                        )
                    }
                }

                // 하단 버튼들 (상태 업데이트 + PTT 무전 송신 제어)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            // 💡 Ping 테스트 혹은 운행 상태 신호 전송으로 활용
                            streamManager.sendPing { latency ->
                                lastLogMessage = "네트워크 Ping: ${latency}ms"
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
                    ) {
                        Text("통신 핑(Ping) 체크", color = Color.White, fontSize = 12.sp)
                    }

                    // 💡 PTT 무전기 토글 버튼 (누르면 마이크 녹음 시작/중지)
                    Button(
                        onClick = {
                            if (!isConnected) {
                                lastLogMessage = "경고: 소켓이 연결되지 않았습니다."
                                return@Button
                            }

                            isPttActive = !isPttActive
                            if (isPttActive) {
                                audioEngine.startRecording()
                                lastLogMessage = "🎙️ PTT 마이크 송출 시작..."
                            } else {
                                audioEngine.stopRecording()
                                lastLogMessage = "⏹️ PTT 마이크 송출 중지"
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPttActive) Color.Green else Color(0xFFD32F2F)
                        )
                    ) {
                        Text(
                            text = if (isPttActive) "🔴 PTT 송출 중지" else "[누르고 말하기] PTT",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                }
            }
        }

        // ==============================================
        // 4. 우측 상단 PiP 내 카메라 화면 카드
        // ==========================================
        Card(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 70.dp, end = 4.dp)
                .width(80.dp)
                .height(110.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "내 화면",
                    color = Color.LightGray,
                    fontSize = 10.sp
                )
            }
        }
    }
}