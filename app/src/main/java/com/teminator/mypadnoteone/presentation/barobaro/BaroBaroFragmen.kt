package com.terminator.mypadnoteone.presentation.barobaro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.teminator.mypadnoteone.domain.model.DispatchOrder
import com.terminator.mypadnoteone.presentation.barobaro.BaroBaroRegisterScreen
import com.teminator.mypadnoteone.presentation.barobaro.detail.BaroBaroDetailScreen
import com.teminator.mypadnoteone.presentation.barobaro.room.MatchingRoomScreen
import com.teminator.mypadnoteone.presentation.barobaro.room.MatchingRoomViewModel
import dagger.hilt.android.AndroidEntryPoint
//import com.teminator.mypadnoteone.data.repository.loadOrders



@AndroidEntryPoint
class BaroBaroFragment : Fragment() {

    private val viewModel: BaroBaroViewModel by viewModels()
    private val roomViewModel: MatchingRoomViewModel by viewModels()

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

                            if (activeRoomId != null) {
                                // 💡 [정리 완료] 뷰모델에 있는 selectedOrder(수락된 오더)를 안전하게 전달
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
                                            onRegister = { route: String, cargo: String, price: String, desc: String ->
                                                val targetEdit = editingOrder
                                                if (targetEdit != null) {
                                                    viewModel.updateOrder(targetEdit.id, route, cargo, price, desc)
                                                } else {
                                                    viewModel.addOrder(route, cargo, price, desc)
                                                }

                                                if (viewModel.errorMessage == null) {
                                                    isRegisterMode = false
                                                    editingOrder = null

                                                    // 💡 [핵심 추가] 등록/수정이 성공적으로 끝나면 즉시 목록을 다시 불러오도록 호출!
                                                    viewModel.loadOrders() // 혹은 뷰모델에 목록을 새로고침하는 함수 이름
                                                }
                                            },
                                            onCancel = {
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
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
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

                                            BaroBaroListScreen(
                                                orderList = viewModel.filteredOrderList,
                                                onItemClick = { viewModel.selectOrder(it) },
                                                viewModel = viewModel
                                            )
                                        } else {
                                            BaroBaroDetailScreen(
                                                order = selectedOrder,
                                                onAccept = {
                                                    // 💡 [수정 완료] 테스트용 드라이버 ID나 실제 기사 고유값을 함께 전달합니다!
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