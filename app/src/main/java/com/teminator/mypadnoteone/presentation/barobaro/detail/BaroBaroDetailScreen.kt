package com.teminator.mypadnoteone.presentation.barobaro.detail

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
    onEdit: () -> Unit, // 💡 [추가] 오더 수정 화면으로 전환하는 콜백
    onBack: () -> Unit,
    onForceTestRoomOpen: (String) -> Unit
) {
    val context = LocalContext.current

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("(디테일 스크린 화물 상세 정보)상위자는 BaroBaroFragment124   ", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("경로: ${order.route}", fontWeight = FontWeight.Bold)
                Text("정보: ${order.cargoInfo}")
                Text("요금: ${order.price}", color = Color(0xFF2E7D32))
                Text("상태: ${order.status}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                if (!order.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("요청사항: ${order.description}", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 💡 [추가] 대기중 상태일 때만 '오더 수정' 버튼 노출
        if (order.status == "대기중") {
            Button(
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("오더 수정하기")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

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

        // 2. [테스트용] 강제 세컨드 룸 및 통신 파이프 진입 버튼
        OutlinedButton(
            onClick = {
                Toast.makeText(context, "⚠️ [테스트] 가상 세컨드 룸 통신 파이프 개설!", Toast.LENGTH_SHORT).show()
                onForceTestRoomOpen(order.id)
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