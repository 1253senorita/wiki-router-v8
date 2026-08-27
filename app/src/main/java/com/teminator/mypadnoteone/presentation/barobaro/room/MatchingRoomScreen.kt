package com.teminator.mypadnoteone.presentation.barobaro.room

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teminator.mypadnoteone.domain.model.DispatchOrder

@Composable
fun MatchingRoomScreen(
    roomId: String,
    order: DispatchOrder?,
    viewModel: MatchingRoomViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    val roomStatus = viewModel.roomStatus
    val logList = viewModel.logList
    val inputMessage = viewModel.inputMessage

    var isCameraOn by remember { mutableStateOf(false) }
    var isVideoCallActive by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

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
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ==========================================
            // 1. 상단 타이틀 영역
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

                    Button(
                        onClick = onBackClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF424242)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("방 나가기", color = Color.White, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "현재 룸 ID: $roomId | 상태: $roomStatus",
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
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5722)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "경로: ${order.route}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "화물: ${order.cargoInfo} | 요금: ${order.price}", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)

                            if (order.shipperPhone.isNotBlank()) {
                                Text(text = "연락처: ${order.shipperPhone}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF81C784))
                            }

                            // 📞 [수정] 오더에 등록된 실제 화주 전화번호 연결
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val targetPhone = order.shipperPhone.takeIf { it.isNotBlank() } ?: "010-0000-0000"
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:$targetPhone")
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "전화 앱을 열 수 없습니다.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                Text("📞 화주에게 바로 전화 걸기", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.DarkGray)
            }

            // ==========================================
            // 2. 중앙 컨테이너 (채팅 및 로그 리스트 영역)
            // ==========================================
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(logList) { logItem ->
                            Surface(
                                color = when (logItem.type) {
                                    RoomLogItem.LogType.MY_MESSAGE -> Color(0xFF37474F)
                                    RoomLogItem.LogType.ACTION -> Color(0xFF4E342E)
                                    RoomLogItem.LogType.IMAGE -> Color(0xFF1B5E20)
                                    else -> Color(0xFF2C2C2C)
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "[${logItem.sender}]",
                                        fontSize = 11.sp,
                                        color = Color(0xFFFF5722),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = logItem.message,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                            }
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF333333), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "사진 첨부",
                            tint = Color(0xFFFF5722)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = inputMessage,
                        onValueChange = { viewModel.onInputChanged(it) },
                        placeholder = { Text("메시지 입력...", color = Color.Gray) },
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
                        onClick = { viewModel.sendCurrentMessage() },
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color(0xFFFF5722), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "전송",
                            tint = Color.White
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            isVideoCallActive = !isVideoCallActive
                            viewModel.updateRoomAction(if (isVideoCallActive) "영상 통화 연결됨" else "영상 통화 종료됨")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isVideoCallActive) Color(0xFF00695C) else Color(0xFF333333)
                        )
                    ) {
                        Text(
                            text = if (isVideoCallActive) "📹 영상 통화 중지" else "📹 영상 통화 시작",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.updateRoomAction("PTT 송신 중") },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xD32F2F))
                    ) {
                        Text("[누르고 말하기] PTT", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ==============================================
        // 4. 우측 상단 PiP 내 카메라 화면 및 켜기 스위치
        // ==============================================
        Card(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 70.dp, end = 4.dp)
                .width(110.dp)
                .height(130.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isCameraOn) "📹 내 카메라 켜짐" else "📷 내 카메라 꺼짐",
                    color = if (isCameraOn) Color(0xFFFF5722) else Color.LightGray,
                    fontSize = 9.sp,
                    maxLines = 1
                )

                Switch(
                    checked = isCameraOn,
                    onCheckedChange = { isCameraOn = it },
                    modifier = Modifier.scale(0.7f)
                )
            }
        }
    }
}