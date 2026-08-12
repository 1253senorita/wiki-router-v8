package com.terminator.mypadnoteone.presentation.barobaro.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
//import com.terminator.mypadnoteone.presentation.barobaro.DispatchOrder
import com.teminator.mypadnoteone.domain.model.DispatchOrder


@Composable
fun BaroBaroDetailScreen(
    order: DispatchOrder,
    onAccept: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("화물 상세 정보", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("경로: ${order.route}", fontWeight = FontWeight.Bold)
                Text("정보: ${order.cargoInfo}")
                Text("요금: ${order.price}", color = Color(0xFF2E7D32))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = onAccept, modifier = Modifier.fillMaxWidth()) {
            Text("콜 수락 하기")
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("목록으로 돌아가기")
        }
    }
}

