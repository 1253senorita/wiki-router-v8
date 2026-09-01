package com.teminator.mypadnoteone.presentation.barobaro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.teminator.mypadnoteone.domain.model.DispatchOrder
import com.teminator.mypadnoteone.presentation.barobaro.detail.BaroBaroDetailScreen
import com.teminator.mypadnoteone.presentation.barobaro.room.MatchingRoomScreen
import com.teminator.mypadnoteone.presentation.barobaro.room.MatchingRoomViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BaroBaroFragment : Fragment() {

    private val viewModel: BaroBaroViewModel by viewModels()
    private val roomViewModel: MatchingRoomViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 💡 [시스템 백스택 연동] 하드웨어 뒤로가기 버튼이나 제스처백을 눌렀을 때의 흐름 제어
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            when {
                viewModel.mockMatchingRoomId != null -> {
                    viewModel.clearMockRoomId()
                }
                viewModel.selectedOrder != null -> {
                    viewModel.selectOrder(null)
                }
                else -> {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val initialRegisterMode = arguments?.getBoolean("IS_REGISTER_MODE", false) ?: false

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    val snackbarHostState = remember { SnackbarHostState() }

                    LaunchedEffect(viewModel.errorMessage) {
                        viewModel.errorMessage?.let { error ->
                            snackbarHostState.showSnackbar(error)
                            viewModel.clearError()
                        }
                    }

                    val activeRoomId = viewModel.mockMatchingRoomId

                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            val selectedOrder = viewModel.selectedOrder
                            var isRegisterMode by remember { mutableStateOf(initialRegisterMode) }
                            var editingOrder by remember { mutableStateOf<DispatchOrder?>(null) }

                            // 💡 [추가] 상단 탭 상태 관리 (0: 실시간 오더, 1: 예약 오더 & 캘린더)
                            var selectedTab by remember { mutableStateOf(0) }

                            if (activeRoomId != null) {
                                MatchingRoomScreen(
                                    roomId = activeRoomId,
                                    order = selectedOrder,
                                    viewModel = roomViewModel,
                                    onBackClick = {
                                        viewModel.clearMockRoomId()
                                    }
                                )
                            } else {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    if (isRegisterMode || editingOrder != null) {
                                        BaroBaroRegisterScreen(
                                            initialOrder = editingOrder,
                                            onRegister = { route: String, cargo: String, price: String, desc: String, shipperPhone: String, isReserved: Boolean, reservationTime: String, notificationOption: String ->
                                                val targetEdit = editingOrder
                                                if (targetEdit != null) {
                                                    viewModel.updateOrder(
                                                        targetEdit.id, route, cargo, price, desc, shipperPhone,
                                                        isReserved, reservationTime, notificationOption
                                                    )
                                                } else {
                                                    viewModel.addOrder(
                                                        route, cargo, price, desc, shipperPhone,
                                                        isReserved, reservationTime, notificationOption
                                                    )
                                                }

                                                if (viewModel.errorMessage == null) {
                                                    isRegisterMode = false
                                                    editingOrder = null
                                                    viewModel.loadOrders()
                                                }
                                            },
                                            onBack = {
                                                if (editingOrder != null) {
                                                    editingOrder = null
                                                } else if (arguments?.containsKey("IS_REGISTER_MODE") == true) {
                                                    requireActivity().onBackPressedDispatcher.onBackPressed()
                                                } else {
                                                    isRegisterMode = false
                                                }
                                            }
                                        )
                                    } else {
                                        if (selectedOrder == null) {
                                            // 💡 [상단 탭 레이아웃 추가] 실시간 오더와 예약 오더 영역 분리
                                            TabRow(selectedTabIndex = selectedTab) {
                                                Tab(
                                                    selected = selectedTab == 0,
                                                    onClick = { selectedTab = 0 },
                                                    text = { Text("⚡ 실시간 오더", fontWeight = FontWeight.Bold) }
                                                )
                                                Tab(
                                                    selected = selectedTab == 1,
                                                    onClick = { selectedTab = 1 },
                                                    text = { Text("📅 예약 오더 & 캘린더", fontWeight = FontWeight.Bold) }
                                                )
                                            }

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                OutlinedTextField(
                                                    value = viewModel.searchQuery,
                                                    onValueChange = { viewModel.updateSearchQuery(it) },
                                                    label = { Text("BaroBaroFr 100구간 또는 화물 검색...") },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(50.dp),
                                                    singleLine = true,
                                                    textStyle = MaterialTheme.typography.bodySmall
                                                )

                                                Button(
                                                    onClick = { isRegisterMode = true },
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                    modifier = Modifier.height(40.dp)
                                                ) {
                                                    Text("오더등록----", style = MaterialTheme.typography.labelMedium)
                                                }
                                            }

                                            // 💡 [탭 분기 처리] 선택된 탭에 따라 실시간 리스트 혹은 예약 캘린더 화면 출력
                                            when (selectedTab) {
                                                0 -> {
                                                    // 실시간 오더만 필터링 (isReserved == false)
                                                    val realTimeOrders = viewModel.filteredOrderList.filter { !it.isReserved }
                                                    BaroBaroListScreen(
                                                        orderList = realTimeOrders,
                                                        onItemClick = { viewModel.selectOrder(it) },
                                                        viewModel = viewModel
                                                    )
                                                }
                                                1 -> {
                                                    // 예약 오더만 필터링 (isReserved == true)
                                                    val reservedOrders = viewModel.filteredOrderList.filter { it.isReserved }
                                                    BaroBaroReservationScreen(
                                                        reservedOrderList = reservedOrders,
                                                        onItemClick = { viewModel.selectOrder(it) },
                                                        viewModel = viewModel
                                                    )
                                                }
                                            }
                                        } else {
                                            BaroBaroDetailScreen(
                                                dispporderins = selectedOrder,
                                                onAccept = {
                                                    val testDriverId = "driver_kim_${System.currentTimeMillis()}"
                                                    viewModel.acceptOrder(selectedOrder.id, testDriverId)
                                                    viewModel.forceCreateTestMatchRoom(selectedOrder.id)
                                                },
                                                onEdit = {
                                                    editingOrder = selectedOrder
                                                },
                                                onBack = {
                                                    viewModel.selectOrder(null)
                                                },
                                                onForceTestRoomOpen = { orderId ->
                                                    viewModel.forceCreateTestMatchRoom(orderId)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            val mainActivityClass = Class.forName("com.terminator.mypadnoteone.presentation.main.MainActivity")
            if (mainActivityClass.isInstance(activity)) {
                val method = mainActivityClass.getMethod("restoreMainUI")
                method.invoke(activity)
            }
        } catch (_: Exception) {
        }
    }
}