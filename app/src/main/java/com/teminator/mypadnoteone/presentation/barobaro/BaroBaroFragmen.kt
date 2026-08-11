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
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val selectedOrder = viewModel.selectedOrder

                        // 🔥 오더 등록 화면 상태 관리 변수 (필요에 따라 모드 전환 가능)
                        var isRegisterMode by remember { mutableStateOf(false) }

                        Column(modifier = Modifier.fillMaxSize()) {

                            // 상단에 모드 전환용 탭이나 뒤로가기 버튼을 둘 수 있습니다.
                            if (isRegisterMode) {
                                // --- [새 화물 오더 등록 화면 뷰] ---
                                BaroBaroRegisterScreen(
                                    onRegister = { route, cargo, price, desc ->
                                        viewModel.addOrder(route, cargo, price, desc)
                                        isRegisterMode = false // 등록 후 목록 화면으로 복귀
                                    },
                                    onCancel = {
                                        isRegisterMode = false
                                    }
                                )
                            } else {
                                // --- [기존 배차 목록 화면 뷰] ---
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "실시간 화물 콜 대기 목록",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
                                    )
                                    Button(onClick = { isRegisterMode = true }) {
                                        Text("오더 등록하기")
                                    }
                                }

                                if (selectedOrder == null) {
                                    BaroBaroManageScreen(
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

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.restoreMainUI()
    }
}

// 📝 [추가] 오더 직접 입력 폼 컴포저블 화면
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
        Text(text = "새 화물 오더 등록", style = MaterialTheme.typography.titleLarge)
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