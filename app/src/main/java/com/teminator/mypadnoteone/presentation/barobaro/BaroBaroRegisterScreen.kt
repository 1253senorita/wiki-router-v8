package com.terminator.mypadnoteone.presentation.barobaro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teminator.mypadnoteone.domain.model.DispatchOrder

@Composable
fun BaroBaroRegisterScreen(
    initialOrder: DispatchOrder? = null, // 💡 수정할 때 기존 오더 데이터 전달 (등록일 때는 null)
    onRegister: (String, String, String, String) -> Unit, // 등록 또는 수정 완료 콜백
    onCancel: () -> Unit
) {
    // 💡 기존 데이터가 있으면 출발지/도착지를 '➔' 기준으로 쪼개서 초기값으로 세팅하고, 없으면 빈 칸("")으로 시작
    val initialRouteParts = initialOrder?.route?.split(" ➔ ") ?: listOf("", "")
    val initialDeparture = if (initialRouteParts.isNotEmpty()) initialRouteParts[0] else ""
    val initialDestination = if (initialRouteParts.size > 1) initialRouteParts[1] else ""

    var departure by remember { mutableStateOf(initialDeparture) }
    var destination by remember { mutableStateOf(initialDestination) }
    var cargo by remember { mutableStateOf(initialOrder?.cargoInfo ?: "") }
    var price by remember { mutableStateOf(initialOrder?.price ?: "") }
    var description by remember { mutableStateOf(initialOrder?.description ?: "") }

    val isEditMode = initialOrder != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (!isEditMode) {
                "(등록 스크린 179) 상위자는 프래그먼트 아래 등록 --함수116--화물 오더 등록"
            } else {
                "(수정 스크린 179) 상위자는 프래그먼트 아래 등록 --함수116--화물 오더 수정"
            },
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 1. 출발지 입력창
        OutlinedTextField(
            value = departure,
            onValueChange = { departure = it },
            label = { Text("출발지 (예: 포천)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 2. 도착지 입력창
        OutlinedTextField(
            value = destination,
            onValueChange = { destination = it },
            label = { Text("도착지 (예: 대전)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 3. 화물 정보 입력창
        OutlinedTextField(
            value = cargo,
            onValueChange = { cargo = it },
            label = { Text("화물 정보 (예: 5톤 윙바디)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 4. 운임 비용 입력창
        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("운임 비용 (예: 350,000원)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 5. 상세 기재 사항 입력창
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("상세 기재 사항 (선택)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (departure.isNotBlank() && destination.isNotBlank() && cargo.isNotBlank() && price.isNotBlank()) {
                        val combinedRoute = "$departure ➔ $destination"
                        onRegister(combinedRoute, cargo, price, description)
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                // 모드에 따라 버튼 문구 동적 변경
                Text(if (!isEditMode) "등록 완료" else "수정 완료")
            }

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("취소")
            }
        }
    }
}