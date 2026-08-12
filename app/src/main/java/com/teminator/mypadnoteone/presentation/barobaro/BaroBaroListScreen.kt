package com.terminator.mypadnoteone.presentation.barobaro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teminator.mypadnoteone.domain.model.DispatchOrder

@Composable
fun BaroBaroListScreen(
    orderList: List<DispatchOrder>,
    onItemClick: (DispatchOrder) -> Unit,
    viewModel: BaroBaroViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                text = "스크린 리스트 쿨 --ls22--화면",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("운행 구간 또는 화물 검색...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }

        items(viewModel.filteredOrderList) { order ->
            OrderCardItem(
                order = order,
                onItemClick = { onItemClick(order) },
                onAcceptClick = { viewModel.acceptOrder(order.id) }
            )
        }
    }
}

// 💡 요청하신 카드 아이템 내부 텍스트 인자 및 추가 정보 표시 영역을 더욱 탄탄하게 확장했습니다.
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
        onClick = onItemClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 1. 기본 식별 및 구간 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "ID: #${order.id}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                Text(text = "상태: ${order.status}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(4.dp))

            Text(text = "구간--ls36--: ${order.route}", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))

            // 2. 메니저 데이터셋 및 화물 세부 정보 인자 확장
            Divider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant)
            Text(text = "--메니저 데이타셋이 VM 에서 인자받은--: ${order.cargoInfo}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "운임 비용: ${order.price}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.tertiary)

            // 3. 비고 및 추가 확장 인자 텍스트 영역
            if (order.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "비고란--ext99--: ${order.description}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // 4. 상태별 액션 버튼 영역
            if (order.status == "대기중") {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onAcceptClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("오더 수락하기")
                }
            }
        }
    }
}