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
import com.teminator.mypadnoteone.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BaroBaroFragment : Fragment() {

    private val viewModel: BaroBaroViewModel by viewModels()

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

                    // ViewModel의 에러 메시지 감지 및 스낵바 출력
                    LaunchedEffect(viewModel.errorMessage) {
                        viewModel.errorMessage?.let { error ->
                            snackbarHostState.showSnackbar(error)
                            viewModel.clearError()
                        }
                    }

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

                            Column(modifier = Modifier.fillMaxSize()) {
                                if (isRegisterMode) {
                                    BaroBaroRegisterScreen(
                                        // 💡 파라미터에 명시적 타입(String) 추가하여 타입 추론 에러 해결
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
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
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

                                    if (selectedOrder == null) {
                                        BaroBaroListScreen(
                                            orderList = viewModel.orderList,
                                            onItemClick = { viewModel.selectOrder(it) },
                                            viewModel = viewModel
                                        )
                                    } else {
                                        // 상세 화면 호출 부분
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
        (activity as? MainActivity)?.restoreMainUI()
    }
}

// 오더 직접 입력 폼 컴포저블 화면 (중복 선언 방지용 단일 정의)
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