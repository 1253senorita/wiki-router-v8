package com.terminator.mypadnoteone.presentation.barobaro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Fragment와 호환되도록 orderList와 onItemClick을 받도록 수정
@Composable
fun BaroBaroManageScreen(
    orderList: List<DispatchOrder>,
    onItemClick: (DispatchOrder) -> Unit,
    viewModel: BaroBaroViewModel // ViewModel도 함께 받거나 내부 주입 사용
) {
    // 화면에 보여질 입력 상태값들 (TextField와 연결됨)
    var route by remember { mutableStateOf("") }
    var cargo by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // --- [파트 1] 상단: 오더 입력 섹션 ---
        Text(text = "새 화물 오더 등록", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = route,
            onValueChange = { route = it },
            label = { Text("운행 구간 (예: 인천 ➔ 대구)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = cargo,
            onValueChange = { cargo = it },
            label = { Text("화물 정보 (예: 5톤 윙바디)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("운임 비용 (예: 350,000원)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("상세 기재 사항 (예: 상차지 연락처)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
        Spacer(modifier = Modifier.height(16.dp))

        // [등록하기] 버튼 -> ViewModel의 addOrder 호출
        Button(
            onClick = {
                if (route.isNotBlank() && cargo.isNotBlank()) {
                    viewModel.addOrder(route, cargo, price, description)
                    route = ""
                    cargo = ""
                    price = ""
                    description = ""
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("화물 오더 등록하기")
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // --- [파트 2] 하단: 등록된 오더 리스트 ---
        Text(text = "현재 대기 중인 오더 목록", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(orderList) { order ->
                OrderCardItem(
                    order = order,
                    onItemClick = { onItemClick(order) },
                    onAcceptClick = { viewModel.acceptOrder(order.id) }
                )
            }
        }
    }
}

// 리스트 아이템 카드 컴포저블
@Composable
fun OrderCardItem(
    order: DispatchOrder,
    onItemClick: () -> Unit,
    onAcceptClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onItemClick // 카드 클릭 시 상세 화면으로 이동
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "구간: ${order.route}", style = MaterialTheme.typography.titleSmall)
            Text(text = "화물: ${order.cargoInfo}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "운임: ${order.price}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "상태: ${order.status}", color = MaterialTheme.colorScheme.primary)

            if (order.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "비고: ${order.description}", style = MaterialTheme.typography.bodySmall)
            }

            if (order.status == "대기중") {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onAcceptClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("수락")
                }
            }
        }
    }
}