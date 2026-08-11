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
                            BaroBaroManageScreen(
                                orderList = viewModel.orderList,
                                onItemClick = { viewModel.selectOrder(it) },
                                viewModel = viewModel
                            )
                        } else {
                            // 상세 화면 호출 부분 (기존 상세 화면 코드가 있다면 연결)
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