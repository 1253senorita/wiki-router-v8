package com.teminator.mypadnoteone.presentation.client




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

@Composable
fun AIScreen(
    viewModel: AIClientViewModel,
    onBack: () -> Unit
) {
    var userInputText by remember { mutableStateOf("") }
    val chatMessages = viewModel.chatMessages

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

        // --- 2. 실시간 가져온 데이터 연동 카드 ---
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
                Text(text = viewModel.aiStatusText, fontSize = 14.sp)

                // 🔥 다운로드 진행 중일 때 노출되는 프로그레스 바 및 안내 텍스트
                if (viewModel.isDownloading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = viewModel.downloadProgressText,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- 3. AI와 소통하는 채팅 / 메시지 로그창 ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
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

        // --- 4. 하단 소통 입력 바 ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userInputText,
                onValueChange = { userInputText = it },
                placeholder = { Text("AI 보미에게 온디바이스 지시사항 입력...") },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                maxLines = 1
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (userInputText.isNotBlank()) {
                        viewModel.sendUserMessage(userInputText)
                        userInputText = ""
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

        // --- 5. 네비게이션 및 조건부 다운로드 버튼 영역 ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 🔥 [핵심] 모델 파일이 아직 다운로드되지 않았을 때만 다운로드 버튼을 노출합니다!
            if (!viewModel.isModelDownloaded) {
                Button(
                    onClick = {
                        viewModel.downloadAiModelFiles()
                    },
                    enabled = !viewModel.isDownloading,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text(if (viewModel.isDownloading) "다운로드 중..." else "AI 모델 다운로드")
                }
            }

            // 메인으로 돌아가기 버튼 (이미 다운로드 완료되어 왼쪽 버튼이 숨겨지면 이 버튼이 화면 전체 폭을 예쁘게 채웁니다)
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("메인으로 돌아가기")
            }
        }
    }
}