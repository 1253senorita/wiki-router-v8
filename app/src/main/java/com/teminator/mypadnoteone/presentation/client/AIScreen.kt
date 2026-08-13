package com.terminator.mypadnoteone.presentation.client

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    // 사용자가 입력 중인 텍스트 상태 관리 (채팅/명령어 입력용)
    var userInputText by remember { mutableStateOf("") }

    // 예시 대화 내역 리스트 (나중에 ViewModel의 실제 대화 StateFlow와 연동하면 돼!)
    val chatMessages = remember {
        mutableStateListOf(
            "🤖 [AI 보미]: 안녕하세요 오빠! AI 관제 시스템 대기 중입니다.",
            "📡 [시스템]: 위키 라우터 연결 완료. 실시간 오더 모니터링 중..."
        )
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

        // --- 3. AI와 소통하는 채팅 / 메시지 로그창 (말하고 띄우는 공간) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // 남은 공간을 꽉 채우도록 설정
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chatMessages) { message ->
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
                        // 사용자가 입력한 메시지를 대화창에 추가
                        chatMessages.add("👤 [오빠]: $userInputText")

                        // AI 보미가 대답하는 시뮬레이션 (나중에 ViewModel 연동)
                        chatMessages.add("🤖 [AI 보미]: \"$userInputText\" 지시를 접수했어요! 처리 중입니다.")

                        // 뷰모델 상태도 함께 갱신
                        viewModel.updateAiStatus("진행 중인 작업: $userInputText")

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
                    chatMessages.add("📡 [시스템]: 새로운 화물 오더 데이터가 연동되었습니다.")
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("데이터 연동 테스트")
            }

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("메인으로 돌아가기")
            }
        }
    }
}