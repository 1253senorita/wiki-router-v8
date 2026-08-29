package com.teminator.mypadnoteone.presentation.barobaro.detail

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teminator.mypadnoteone.domain.model.DispatchOrder

@Composable
fun BaroBaroDetailScreen(
    dispporderins: DispatchOrder,
    onAccept: () -> Unit,
    onEdit: () -> Unit,
    onBack: () -> Unit, // 👈 표준 onBack 행동 변수 연결
    onForceTestRoomOpen: (String) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 🍌 [상단 헤더바 추가] ⬅ 뒤로 버튼을 최상단에 고정하여 백스택 완성!
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onBack, // 👉 누르면 viewModel.selectOrder(null)이 실행되어 목록으로 복귀!
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("⬅ 뒤로", style = MaterialTheme.typography.labelMedium)
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "오더 상세 정보",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))

        // 본문 영역 (스크롤 적용)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "(디테일 스크린 화물 상세 정보)상위자는 BaroBaroFragment124",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(4.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("경로: ${dispporderins.route}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("정보: ${dispporderins.cargoInfo}", fontSize = 14.sp)
                    Text("요금: ${dispporderins.price}", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("화주 연락처: ${dispporderins.shipperPhone}", fontSize = 14.sp)
                    Text("상태: ${dispporderins.status}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    if (!dispporderins.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("요청사항: ${dispporderins.description}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (dispporderins.status == "대기중") {
                Button(
                    onClick = onEdit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("오더 수정하기")
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Button(
                onClick = {
                    Toast.makeText(context, "오더가 수락되었습니다!", Toast.LENGTH_SHORT).show()
                    onAccept()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("콜 수락 하기")
            }

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedButton(
                onClick = {
                    Toast.makeText(context, "⚠️ [테스트] 가상 세컨드 룸 통신 파이프 개설!", Toast.LENGTH_SHORT).show()
                    onForceTestRoomOpen(dispporderins.id)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F))
            ) {
                Text("강제 세컨드 룸 통신 테스트")
            }

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("목록으로 돌아가기")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}