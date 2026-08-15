package com.terminator.mypadnoteone.presentation.barobaro.room

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MatchingRoomScreen(
    roomId: String,
    viewModel: MatchingRoomViewModel,
    onBackClick: () -> Unit
) {
    // 뷰모델의 상태들을 관찰 (State 연동)
    val roomStatus = viewModel.roomStatus
    val lastLogMessage = viewModel.lastLogMessage

    // 화면이 처음 켜질 때 세컨드 룸 통신 파이프 연결 시도
    LaunchedEffect(roomId) {
        viewModel.joinMatchingRoom(roomId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. 상단 타이틀 및 방 정보 영역
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "실시간 매칭 세컨드 룸",
                    style = MaterialTheme.typography.titleLarge
                )
                Button(onClick = onBackClick) {
                    Text("방 나가기")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "현재 룸 ID: $roomId",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Divider(modifier = Modifier.padding(vertical = 12.dp))
        }

        // 2. 중앙 통신 파이프 및 상태 로그 표시 영역
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "상태: $roomStatus",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!lastLogMessage.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = "로그: $lastLogMessage",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Text(
                    text = "기사와 화주 간의 통신 파이프 대기 중...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        // 3. 하단 액션 버튼 영역 (상태 업데이트 및 통신 확인)
        Button(
            onClick = {
                viewModel.updateRoomAction("운행 진행 중 상태 전송")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("운행 상태 업데이트 / 신호 보내기")
        }
    }
}