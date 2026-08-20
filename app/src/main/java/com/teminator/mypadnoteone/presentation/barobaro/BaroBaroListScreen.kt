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
            .padding(horizontal = 9.dp),
        contentPadding = PaddingValues(vertical = 10.dp)
    ) {
        item {
            Text(
                text = " 프레그먼트와 컨태이너가  쩍  이고  그  위에  인포트로  가져온  컴포져믈 상테  한수에  추가  하는  갓  컴포저블(Composable 함수)일 뿐입니다 그래서  컴포져블이 샅태를  버군  다  라는  계념 으로바뀐 ui 를  보여준다  ! .BaroBaroFragment  setContent { ... }selectedOrder ",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }





        items(viewModel.filteredOrderList) { order ->
            OrderCardItem(
                order = order,
                onItemClick = { onItemClick(order) },
                onAcceptClick = {
                    // 💡 [수정 완료] 리스트 화면에서도 수락 시 드라이버 ID를 함께 전달합니다!
                    val testDriverId = "driver_kim_${System.currentTimeMillis()}"
                    viewModel.acceptOrder(order.id, testDriverId)
                }
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
    val routeParts = order.route.split("➔").map { it.trim() }
    val departure = routeParts.getOrNull(0) ?: order.route
    val destination = routeParts.getOrNull(1) ?: ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp), // 카드 간격 최소화
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onItemClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp) // 내부 상하 패딩 최소화
        ) {
            // 1. 기본 식별 및 구간 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ID: #${order.id}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "상태: ${order.status}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // 2. 출발지 / 도착지 구간 정보 (Spacer 제거 및 표면 내부 여백 최소화)
            Spacer(modifier = Modifier.height(1.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = "출발: $departure",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "➔",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )

                Surface(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = "도착: ${if (destination.isNotBlank()) destination else "미정"}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // 3. 화물 정보 및 운임 비용
            Spacer(modifier = Modifier.height(1.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "--기타--: ${order.cargoInfo}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                Text(
                    text = order.price,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            // 4. 비고란
            if (order.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "비고: ${order.description}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    color = Color.DarkGray,
                    maxLines = 1
                )
            }

            // 5. 상태별 액션 버튼
            if (order.status == "대기중") {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onAcceptClick,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp) // 버튼 높이도 최대한 슬림하게 압축
                    ) {
                        Text("오더 수락", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}