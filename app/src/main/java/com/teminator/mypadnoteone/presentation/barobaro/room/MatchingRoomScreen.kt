package com.teminator.mypadnoteone.presentation.barobaro.room

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teminator.mypadnoteone.domain.model.DispatchOrder
import kotlinx.coroutines.delay

@Composable
fun MatchingRoomScreen(
    roomId: String,
    order: DispatchOrder?,
    viewModel: MatchingRoomViewModel,
    isMasterUser: Boolean = false, // 👑 방장(마스터) 여부 플래그 추가
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    val roomStatus = viewModel.roomStatus
    val logList = viewModel.logList
    val inputMessage = viewModel.inputMessage

    var isVideoCallActive by remember { mutableStateOf(false) }
    var isCommunicationReady by remember { mutableStateOf(false) }
    var isPttTransmitting by remember { mutableStateOf(false) }

    // 통화 모드 토글 상태 (false: PTT 무전 모드 / true: CALL 다이얼 통화 모드)
    var isCallMode by remember { mutableStateOf(false) }

    // 회원 등급 및 1분 통화 제한을 위한 잔여 크레딧 / 타이머 상태 필터
    var userMembershipTier by remember { mutableStateOf("GENERAL") } // GENERAL 또는 VIP
    var remainingCallSeconds by remember { mutableStateOf(10) }      // 기본 통화 시간 1분 (초)
    var isCallTimerRunning by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // 1분 통화 타이머 관리 (CALL 모드 전용 필터)
    LaunchedEffect(isCallTimerRunning) {
        if (isCallTimerRunning) {
            remainingCallSeconds = 60
            while (remainingCallSeconds > 0 && isCallTimerRunning) {
                delay(1000L)
                remainingCallSeconds--
            }
            if (remainingCallSeconds <= 0) {
                isCallTimerRunning = false
                isPttTransmitting = false
                viewModel.updateRoomAction("📞 [1분 기본 통화 시간 만료: 자동 종료 및 크레딧 재확인 필요]")
                Toast.makeText(context, "⏰ 1분 통화 시간이 종료되었습니다. 연장을 원하시면 크레딧을 확인하세요.", Toast.LENGTH_LONG).show()
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.sendImageMessage(it.toString())
        }
    }

    LaunchedEffect(logList.size) {
        if (logList.isNotEmpty()) {
            listState.animateScrollToItem(logList.size - 1)
        }
    }

    LaunchedEffect(roomId) {
        viewModel.joinMatchingRoom(roomId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0288D1))
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ==========================================
            // 1. 상단 타이틀 및 표준 뒤로가기 헤더 영역
            // ==========================================
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onBackClick,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("⬅ 뒤로", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Matching Room CALL",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = onBackClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF01579B)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("방 나가기", color = Color.White, fontSize = 10.sp)
                    }
                }

                // 🛠️ [방장 전용 관제 제어 패널] 마스터 권한이 있는 경우에만 전체 제어 스테퍼 노출
                if (isMasterUser) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "👑 [MASTER BROADCAST & ROUTER CONTROL]",
                                color = Color(0xFFFF5722),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "현재 룸 ID: $roomId | 상태: $roomStatus",
                                color = Color(0xFFE1F5FE),
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        val current = viewModel.currentMatrixStep
                                        if (current > 1) viewModel.updateMatrixStep(current - 1)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(30.dp),
                                    contentPadding = PaddingValues(2.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD))
                                ) {
                                    Text("◀ 단계 내림", fontSize = 10.sp, color = Color.White)
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Text(
                                    text = "⚡ ${viewModel.currentMatrixStep}단계",
                                    color = Color(0xFFFFCC80),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                Button(
                                    onClick = {
                                        val current = viewModel.currentMatrixStep
                                        if (current < 10) viewModel.updateMatrixStep(current + 1)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(30.dp),
                                    contentPadding = PaddingValues(2.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD))
                                ) {
                                    Text("단계 올림 ▶", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF01579B))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🛡️ 이모티콘 엔티티 & 비밀키 설정",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFCC80)
                            )

                            OutlinedButton(
                                onClick = {
                                    val emojis = listOf("🐱", "🐶", "🦊", "🐼", "🦄", "🐰", "🦁", "🐧", "🚀", "⭐")
                                    val randomEmoji = emojis.random()
                                    val secretKey = (100..999).random()
                                    val wikiGeneratedId = "$randomEmoji WIKI-$secretKey-SEC"

                                    viewModel.updateVirtualNumber(wikiGeneratedId)
                                    Toast.makeText(context, "귀여운 이모티콘 ID 생성됨: $wikiGeneratedId", Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(36.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF81D4FA))
                            ) {
                                Text("✨ 이모티콘 키 생성", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = viewModel.virtualNumber,
                            onValueChange = { viewModel.updateVirtualNumber(it) },
                            label = { Text("이모티콘 ID 또는 커스텀 번호 입력", fontSize = 10.sp, color = Color(0xFF81D4FA)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF81D4FA),
                                unfocusedBorderColor = Color(0xFF0288D1),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF014361),
                                unfocusedContainerColor = Color(0xFF014361)
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFF0288D1), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎯 상대방 통신 ID / 타겟 번호",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF81D4FA)
                            )

                            Surface(
                                color = if (userMembershipTier == "VIP") Color(0xFFFF8F00) else Color(0xFF0277BD),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.clickable {
                                    userMembershipTier = if (userMembershipTier == "GENERAL") "VIP" else "GENERAL"
                                    Toast.makeText(context, "회원 등급 필터 변경: $userMembershipTier", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text(
                                    text = " 🎫 등급: $userMembershipTier (터치변경) ",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = viewModel.targetPeerId,
                            onValueChange = { viewModel.updateTargetPeerId(it) },
                            label = { Text("상대방 이모티콘 ID 입력", fontSize = 10.sp, color = Color(0xFFB3E5FC)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF81D4FA),
                                unfocusedBorderColor = Color(0xFF0288D1),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF014361),
                                unfocusedContainerColor = Color(0xFF014361)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // ==========================================
                        // [개별 통신 버튼 영역] PTT 및 통화 모드 (참가자 자율 조작)
                        // ==========================================
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    isVideoCallActive = !isVideoCallActive
                                    viewModel.updateRoomAction(if (isVideoCallActive) "영상 통화 연결됨" else "영상 통화 종료됨")
                                },
                                modifier = Modifier
                                    .weight(0.8f)
                                    .height(72.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isVideoCallActive) Color(0xFF00695C) else Color(0xFF01579B)
                                )
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("📹", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isVideoCallActive) "중지" else "영상",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(2.2f)
                                    .height(72.dp)
                                    .background(
                                        color = if (!isCommunicationReady) Color(0xFF546E7A)
                                        else if (isPttTransmitting) (if (isCallMode) Color(0xFF1565C0) else Color(0xFF2E7D32))
                                        else Color(0xFFD32F2F),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .pointerInput(isCommunicationReady, isCallMode) {
                                        detectTapGestures(
                                            onPress = {
                                                if (!isCommunicationReady) return@detectTapGestures

                                                if (!isCallMode) {
                                                    isPttTransmitting = true
                                                    viewModel.updateRoomAction("🐰 📢 [무전 송신 중...]")
                                                    try {
                                                        tryAwaitRelease()
                                                    } finally {
                                                        isPttTransmitting = false
                                                        viewModel.updateRoomAction("🐰 [무전 대기 중]")
                                                    }
                                                } else {
                                                    if (!isPttTransmitting) {
                                                        isPttTransmitting = true
                                                        isCallTimerRunning = true
                                                        viewModel.updateRoomAction("📞 [1분 통화 연결 시작... (잔여: 60초)]")
                                                        Toast.makeText(context, "📞 다이얼 통화가 연결되었습니다. (기본 1분)", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        isPttTransmitting = false
                                                        isCallTimerRunning = false
                                                        viewModel.updateRoomAction("📞 [통화 수동 종료됨]")
                                                    }
                                                }
                                            }
                                        )
                                    }
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!isCommunicationReady) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(text = "🐢", fontSize = 44.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(verticalArrangement = Arrangement.Center) {
                                            Text(
                                                text = "대기 중",
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Text(
                                                text = "터치 불가",
                                                color = Color(0xFFFFCC80),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = if (isCallMode) "📞" else "🐰",
                                            fontSize = 44.sp,
                                            lineHeight = 44.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(verticalArrangement = Arrangement.Center) {
                                            Text(
                                                text = if (isPttTransmitting) {
                                                    if (isCallMode) "통화중 (${remainingCallSeconds}초)" else "송신 중..."
                                                } else {
                                                    if (isCallMode) "📞 다이얼 대기" else "💬 무전 대기"
                                                },
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                maxLines = 1
                                            )
                                            Spacer(modifier = Modifier.height(1.dp))
                                            Text(
                                                text = if (isCallMode) "⏳ 1분 제한 필터 적용" else "👆 누르고 말하기",
                                                color = Color(0xFFFFCC80),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.connectWebCommunication()
                                    isCommunicationReady = true
                                    viewModel.updateRoomUserCount(2)
                                    viewModel.updateRoomAction("웹 파이프 및 무전기 바이너리 채널 연동 완료")
                                    Toast.makeText(context, "🎙️ [${viewModel.targetPeerId}]와 연결 완료!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(0.8f)
                                    .height(72.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCommunicationReady) Color(0xFF00695C) else Color(0xFF0277BD)
                                )
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("🌐", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isCommunicationReady) "완료" else "연결",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                if (order != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF01579B))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "📦 매칭된 화물 정보 (#${order.id})",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFCC80)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "경로: ${order.route}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "화물: ${order.cargoInfo} | 요금: ${order.price}", style = MaterialTheme.typography.labelSmall, color = Color(0xFFB3E5FC))

                            if (order.shipperPhone.isNotBlank()) {
                                Text(text = "등록 연락처: ${order.shipperPhone}", style = MaterialTheme.typography.labelSmall, color = Color(0xFFC8E6C9))
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFF01579B))
            }

            // ==========================================
            // 2. 중앙 컨테이너 (채팅 및 로그 리스트 영역)
            // ==========================================
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF01579B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(logList) { logItem ->
                            Surface(
                                color = when (logItem.type) {
                                    RoomLogItem.LogType.MY_MESSAGE -> Color(0xFF0288D1)
                                    RoomLogItem.LogType.ACTION -> Color(0xFF014361)
                                    RoomLogItem.LogType.IMAGE -> Color(0xFF1B5E20)
                                    RoomLogItem.LogType.CONTROL_MATRIX -> Color(0xFF4A148C)
                                    else -> Color(0xFF0277BD)
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (logItem.sender.isNotBlank() && logItem.sender != "시스템" && logItem.sender != "시스템(공구박스)") {
                                            viewModel.updateTargetPeerId(logItem.sender)
                                            viewModel.connectWebCommunication()
                                            isCommunicationReady = true
                                            viewModel.updateRoomUserCount(2)
                                            Toast.makeText(context, "🎯 [${logItem.sender}]님과 1:1 회선 및 무전기 연동", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = "[${logItem.sender}]" + if (logItem.sender.contains("시스템")) "" else " (터치하여 1:1 연결)",
                                        fontSize = 10.sp,
                                        color = Color(0xFFFFCC80),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = logItem.message,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}