package com.teminator.mypadnoteone.presentation.barobaro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teminator.mypadnoteone.domain.model.DispatchOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaroBaroRegisterScreen(
    initialOrder: DispatchOrder? = null, // 수정할 때 기존 오더 데이터 전달 (등록일 때는 null)
    // 📞 [수정] 콜백에 예약 여부(Boolean), 예약 일시(String), 알림/스누즈 옵션(String) 추가
    onRegister: (String, String, String, String, String, Boolean, String, String) -> Unit,
    onBack: () -> Unit
) {
    val initialRouteParts = initialOrder?.route?.split(" ➔ ") ?: listOf("", "")
    val initialDeparture = if (initialRouteParts.isNotEmpty()) initialRouteParts[0] else ""
    val initialDestination = if (initialRouteParts.size > 1) initialRouteParts[1] else ""

    var departure by remember { mutableStateOf(initialDeparture) }
    var destination by remember { mutableStateOf(initialDestination) }
    var cargo by remember { mutableStateOf(initialOrder?.cargoInfo ?: "") }

    var priceStr by remember { mutableStateOf(initialOrder?.price ?: "") }
    var description by remember { mutableStateOf(initialOrder?.description ?: "") }

    // 📞 화주 전화번호 상태값
    var shipperPhone by remember { mutableStateOf(initialOrder?.shipperPhone ?: "") }

    // ⏰ [추가] 즉시/예약 오더 상태 및 스누즈 관련 상태 값
    var isReserved by remember { mutableStateOf(initialOrder?.isReserved ?: false) }
    var reservationTime by remember { mutableStateOf(initialOrder?.reservationTime ?: "") }
    var notificationOption by remember { mutableStateOf(initialOrder?.notificationOption ?: "정시 울림") }

    // 알림 옵션 드롭다운 메뉴 제어 상태
    var expandedNotificationMenu by remember { mutableStateOf(false) }
    val notificationOptions = listOf("정시 울림", "10분 전 알림", "30분 전 알림", "1시간 전 알림", "스누즈(10분 간격 반복)")

    val isEditMode = initialOrder != null
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 🍌 [상단 바나나 헤더바 추가!] 뒤로가기 버튼과 타이틀을 최상단에 고정
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onBack,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("⬅ 뒤로", style = MaterialTheme.typography.labelMedium)
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = if (!isEditMode) "신규 오더 등록" else "오더 정보 수정",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))

        // 본문 영역 (스크롤뷰 적용으로 키보드가 올라와도 입력창이 짤리지 않게 방어!)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (!isEditMode) {
                    "(등록 스크린 179) 상위자는 프래그먼트 아래 등록 --함수116--화물 오더 등록"
                } else {
                    "(수정 스크린 179) 상위자는 프래그먼트 아래 등록 --함수116--화물 오더 수정"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(4.dp))

            // ⏰ [추가] 즉시 / 예약 배차 선택 필터 칩
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !isReserved,
                    onClick = { isReserved = false },
                    label = { Text("⚡ 즉시 오더") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = isReserved,
                    onClick = { isReserved = true },
                    label = { Text("⏰ 예약 오더") },
                    modifier = Modifier.weight(1f)
                )
            }

            // ⏰ [조건부 노출] 예약 오더일 때만 표시되는 예약 시간 및 스누즈 설정 카드 영역
            if (isReserved) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "예약 및 스누즈 알람 설정",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // 1. 예약 일시 입력 필드
                        OutlinedTextField(
                            value = reservationTime,
                            onValueChange = { reservationTime = it },
                            label = { Text("예약 일시 (예: 2026-09-03 14:00)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // 2. 알람 스누즈 설정 드롭다운
                        ExposedDropdownMenuBox(
                            expanded = expandedNotificationMenu,
                            onExpandedChange = { expandedNotificationMenu = !expandedNotificationMenu }
                        ) {
                            OutlinedTextField(
                                value = notificationOption,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("알림 상태 (스누즈 설정)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = expandedNotificationMenu,
                                onDismissRequest = { expandedNotificationMenu = false }
                            ) {
                                notificationOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            notificationOption = option
                                            expandedNotificationMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 1. 출발지 입력창
            OutlinedTextField(
                value = departure,
                onValueChange = { departure = it },
                label = { Text("출발지 (예: 포천)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 2. 도착지 입력창
            OutlinedTextField(
                value = destination,
                onValueChange = { destination = it },
                label = { Text("도착지 (예: 대전)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 3. 화물 정보 입력창
            OutlinedTextField(
                value = cargo,
                onValueChange = { cargo = it },
                label = { Text("화물 정보 (예: 5톤 윙바디)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 4. 운임 비용 입력창
            OutlinedTextField(
                value = priceStr,
                onValueChange = { priceStr = it },
                label = { Text("운임 비용 (예: 350,000원)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 5. 화주 전화번호 입력창
            OutlinedTextField(
                value = shipperPhone,
                onValueChange = { shipperPhone = it },
                label = { Text("화주 전화번호 (예: 010-1234-5678)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 6. 상세 기재 사항 입력창
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("상세 기재 사항 (선택)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (departure.isNotBlank() && destination.isNotBlank() && cargo.isNotBlank() && priceStr.isNotBlank() && shipperPhone.isNotBlank()) {
                            // 예약 오더인 경우 예약 일시가 비어있지 않은지 검증
                            if (!isReserved || (isReserved && reservationTime.isNotBlank())) {
                                val combinedRoute = "$departure ➔ $destination"
                                onRegister(
                                    combinedRoute,
                                    cargo,
                                    priceStr,
                                    description,
                                    shipperPhone,
                                    isReserved,
                                    reservationTime,
                                    notificationOption
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text(if (!isEditMode) "등록 완료" else "수정 완료")
                }

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text("취소")
                }
            }

            // 하단 여백 추가 (키보드 올라올 때 스크롤 여유분)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}