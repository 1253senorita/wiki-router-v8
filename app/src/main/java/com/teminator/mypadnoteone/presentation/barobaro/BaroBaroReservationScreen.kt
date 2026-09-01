package com.teminator.mypadnoteone.presentation.barobaro

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teminator.mypadnoteone.domain.model.DispatchOrder
import java.time.LocalDate

@Composable
fun BaroBaroReservationScreen(
    reservedOrderList: List<DispatchOrder>,
    onItemClick: (DispatchOrder) -> Unit,
    viewModel: BaroBaroViewModel
) {
    // 💡 선택된 날짜 관리 (기본값: 오늘 날짜)
    var selectedDate by remember { mutableStateOf(LocalDate.now().toString()) }

    // 해당 날짜에 속하는 예약 오더 필터링 (reservationTime 문자열에 선택된 날짜가 포함되어 있는지 확인)
    val filteredReservedOrders = reservedOrderList.filter { order ->
        order.reservationTime.contains(selectedDate)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "📅 예약 오더 캘린더 관리",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "날짜별로 등록된 예약 오더를 확인하고 스누즈 및 즉시 전환을 관리합니다.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 💡 [확장된 스크롤 가능 날짜 선택 바]
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "선택된 날짜: $selectedDate",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 좌우로 스크롤하여 더 넓은 날짜(어제부터 향후 14일)를 선택할 수 있도록 개선
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val today = LocalDate.now()
                    // 과거 2일 ~ 미래 14일까지 넉넉하게 확장
                    for (i in -2..14) {
                        val targetDate = today.plusDays(i.toLong()).toString()
                        val isSelected = targetDate == selectedDate

                        // 요일 표시를 간소화하거나 MM-DD 형태로 깔끔하게 표현
                        val labelText = if (i == 0) "오늘 (${targetDate.substring(5)})" else targetDate.substring(5)

                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedDate = targetDate },
                            label = { Text(labelText, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(6.dp))

        // 선택된 날짜의 예약 오더 목록 출력
        if (filteredReservedOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "선택한 날짜($selectedDate)에 잡힌 예약 오더가 없습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredReservedOrders) { order ->
                    OrderCardItem(
                        order = order,
                        onItemClick = { onItemClick(order) },
                        onAcceptClick = {
                            val testDriverId = "driver_kim_${System.currentTimeMillis()}"
                            viewModel.acceptOrder(order.id, testDriverId)
                        }
                    )
                }
            }
        }
    }
}