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
import com.terminator.mypadnoteone.presentation.barobaro.detail.BaroBaroDetailScreen
import com.terminator.mypadnoteone.presentation.barobaro.room.MatchingRoomScreen
import com.terminator.mypadnoteone.presentation.barobaro.room.MatchingRoomViewModel
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
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "실시간 화물 --프레그먼트47--대기 목록",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
                                                )
                                                Button(onClick = { isRegisterMode = true }) {
                                                    Text("오더등록--PRg72-")
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
        // 안전한 형변환을 통해 MainActivity 의존성 에러 원천 차단
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

@Composable
fun BaroBaroRegisterScreen(
    onRegister: (String, String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    var route by remember { mutableStateOf("") }
    var cargo by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "프래그먼트 아래 등록 --함수116--화물 오더 등록", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

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
                    if (route.isNotBlank() && cargo.isNotBlank() && price.isNotBlank()) {
                        onRegister(route, cargo, price, description)
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("등록 완료")
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