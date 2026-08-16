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
import com.teminator.mypadnoteone.presentation.barobaro.BaroBaroRegisterScreen
import com.teminator.mypadnoteone.presentation.barobaro.detail.BaroBaroDetailScreen
import com.teminator.mypadnoteone.presentation.barobaro.room.MatchingRoomScreen
import com.teminator.mypadnoteone.presentation.barobaro.room.MatchingRoomViewModel
import dagger.hilt.android.AndroidEntryPoint

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

                            if (activeRoomId != null) {
                                MatchingRoomScreen(
                                    roomId = activeRoomId,
                                    viewModel = roomViewModel,
                                    onBackClick = {
                                        viewModel.clearMockRoomId()
                                    }
                                )
                            } else {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    if (isRegisterMode) {
                                        BaroBaroRegisterScreen(
                                            onRegister = { route: String, cargo: String, price: String, desc: String ->
                                                viewModel.addOrder(route, cargo, price, desc)
                                                if (viewModel.errorMessage == null) {
                                                    isRegisterMode = false
                                                }
                                            },
                                            onCancel = {
                                                if (arguments?.containsKey("IS_REGISTER_MODE") == true) {
                                                    requireActivity().onBackPressedDispatcher.onBackPressed()
                                                } else {
                                                    isRegisterMode = false
                                                }
                                            }
                                        )
                                    } else {
                                        if (selectedOrder == null) {
                                            // 상단 영역: 타이틀, 검색창, 오더등록 버튼을 가로(Row)로 배치
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                            ) {
                                                // 1. 검색창을 Row 안에서 빈 공간을 채우도록 배치
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

                                                // 2. 오더등록 버튼
                                                Button(
                                                    onClick = { isRegisterMode = true },
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                    modifier = Modifier.height(40.dp)
                                                ) {
                                                    Text("오더등록", style = MaterialTheme.typography.labelMedium)
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
                                                    viewModel.acceptOrder(selectedOrder.id)
                                                    viewModel.forceCreateTestMatchRoom(selectedOrder.id)
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