package com.terminator.mypadnoteone.presentation.barobaro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.teminator.mypadnoteone.domain.model.DispatchOrder
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


@Composable
fun BaroBaroListScreen(
    orderList: List<DispatchOrder>,
    onItemClick: (DispatchOrder) -> Unit,
    viewModel: BaroBaroViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp),
        contentPadding = PaddingValues(vertical = 10.dp)
    ) {
        item {
            Text(
                text = "스크린 리스트 쿨 --ls22--화면",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
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



@Composable
fun OrderCardItem(
    order: DispatchOrder,
    onItemClick: () -> Unit,
    onAcceptClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), // 카드 간의 상하 간격을 좁힘 (기존 8dp -> 4dp)
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onItemClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp) // 내부 여백을 컴팩트하게 축소 (기존 16dp -> 상하 8dp)
        ) {
            // 1. 기본 식별 및 구간 정보 (한 줄 컴팩트 배치)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "ID: #${order.id}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                Text(text = "상태: ${order.status}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "구간--ls36--: ${order.route}",
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(3.dp))

            // 2. 화물 정보 및 운임 비용 (한 줄로 묶어서 높이 절약)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "--데이타셋--: ${order.cargoInfo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                Text(
                    text = order.price,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            // 3. 비고란 (있을 때만 아주 얇게 표시)
            if (order.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "비고: ${order.description}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    maxLines = 1
                )
            }

            // 4. 상태별 액션 버튼 (대기중일 때 우측에 콤팩트하게 배치)
            if (order.status == "대기중") {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onAcceptClick,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 3.dp),
                        modifier = Modifier.height(30.dp) // 버튼 높이를 강제로 낮춤
                    ) {
                        Text("오더 수락", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}