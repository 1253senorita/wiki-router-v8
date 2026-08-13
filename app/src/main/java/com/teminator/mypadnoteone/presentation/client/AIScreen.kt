package com.terminator.mypadnoteone.presentation.client

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teminator.mypadnoteone.presentation.client.AIClientViewModel

@Composable
fun AIScreen(
    viewModel: AIClientViewModel,
    onBack: () -> Unit
) {
    // 사용자가 입력 중인 텍스트 상태 관리
    var userInputText by remember { mutableStateOf("") }

    // 🔥 1. 채팅창 자동 스크롤을 위한 리스트 상태 선언
    val listState = rememberLazyListState()

    // 🔥 2. ViewModel의 chatMessages에 새로운 대화가 추가될 때마다 가장 아래(최신 대화)로 자동 스크롤 이동
    LaunchedEffect(viewModel.chatMessages.size) {
        if (viewModel.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.chatMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. 상단 타이틀 ---
        Text(
            text = "🤖 AI 보미 소통 관제 스크린",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- 2. 실시간 가져온 데이터 연동 카드 (현재 상태 표시) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📊 [실시간 데이터 연동 상태]",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                // ViewModel에서 가져오는 실시간 상태 텍스트
                Text(text = viewModel.aiStatusText, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- 3. AI와 소통하는 채팅 / 메시지 로그창 (ViewModel의 chatMessages와 연동) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // 남은 공간을 꽉 채우도록 설정
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            LazyColumn(
                state = listState, // 👈 자동 스크롤 상태 연결 완료!
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 🔥 화면 자체 변수 대신 ViewModel의 chatMessages 리스트를 직접 바라봅니다.
                items(viewModel.chatMessages) { message ->
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(10.dp),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- 4. 하단 소통 입력 바 (말하기 / 텍스트 전송) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userInputText,
                onValueChange = { userInputText = it },
                placeholder = { Text("AI 보미에게 지시사항이나 말을 입력하세요...") },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                maxLines = 1
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (userInputText.isNotBlank()) {
                        // 🔥 뷰모델의 전용 함수 호출로 사용자 메시지 전달 및 보미 응답 처리 일원화
                        viewModel.sendUserMessage(userInputText)

                        userInputText = "" // 입력창 초기화
                    }
                },
                modifier = Modifier.size(56.dp),
                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "전송",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- 5. 네비게이션 및 테스트 버튼들 ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    viewModel.updateAiStatus("🤖 AI 관제 시스템 신규 오더 상태가 갱신되었습니다!")
                    // 필요하다면 뷰모델 상태나 메시지 추가 연동 가능
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("데이터 연동 테스트")
            }

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("메인 화면으로 돌아가기")
            }
        }
    }
}