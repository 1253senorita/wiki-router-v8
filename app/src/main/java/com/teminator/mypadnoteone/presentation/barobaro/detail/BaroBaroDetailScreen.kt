package com.terminator.mypadnoteone.presentation.barobaro.detail

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teminator.mypadnoteone.domain.model.DispatchOrder

@Composable
fun BaroBaroDetailScreen(
    order: DispatchOrder,
    onAccept: () -> Unit,
    onBack: () -> Unit,
    onForceTestRoomOpen: (String) -> Unit // 💡 강제 세컨드 룸 오픈 콜백 추가
) {
    val context = LocalContext.current

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("화물 상세 정보", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("경로: ${order.route}", fontWeight = FontWeight.Bold)
                Text("정보: ${order.cargoInfo}")
                Text("요금: ${order.price}", color = Color(0xFF2E7D32))
                if (!order.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("요청사항: ${order.description}", fontSize = 14.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 1. 콜 수락 버튼 (정식 수락 로직)
        Button(
            onClick = {
                Toast.makeText(context, "오더가 수락되었습니다!", Toast.LENGTH_SHORT).show()
                onAccept()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("콜 수락 하기")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. 💡 [테스트용] 강제 세컨드 룸 및 통신 파이프 진입 버튼 추가
        OutlinedButton(
            onClick = {
                Toast.makeText(context, "⚠️ [테스트] 가상 세컨드 룸 통신 파이프 개설!", Toast.LENGTH_SHORT).show()
                onForceTestRoomOpen(order.id) // 가상 방 생성 트리거
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F))
        ) {
            Text("강제 세컨드 룸 통신 테스트")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("목록으로 돌아가기")
        }
    }
}