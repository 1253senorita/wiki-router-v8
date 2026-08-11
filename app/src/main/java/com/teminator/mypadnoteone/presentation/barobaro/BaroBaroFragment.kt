package com.terminator.mypadnoteone.presentation.barobaro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.terminator.mypadnoteone.presentation.barobaro.detail.BaroBaroDetailScreen
import com.terminator.mypadnoteone.presentation.barobaro.list.BaroBaroListScreen
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

                        if (selectedOrder == null) {
                            BaroBaroListScreen(
                                orderList = viewModel.orderList,
                                onItemClick = { viewModel.selectOrder(it) }
                            )
                        } else {
                            BaroBaroDetailScreen(
                                order = selectedOrder,
                                onAccept = { viewModel.acceptOrder(selectedOrder.id) },
                                onBack = {
                                    // 상세 화면에서 뒤로 갈 때 주문 선택 해제
                                    viewModel.selectOrder(null)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 프래그먼트가 파괴될 때 액티비티의 메인 UI 복구 호출
        (activity as? MainActivity)?.restoreMainUI()
    }
}