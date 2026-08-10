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
import androidx.fragment.app.viewModels // 🔥 이 임포트가 핵심입니다!
import com.terminator.mypadnoteone.presentation.barobaro.detail.BaroBaroDetailScreen
import com.terminator.mypadnoteone.presentation.barobaro.list.BaroBaroListScreen
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
                                onBack = { viewModel.selectOrder(null) }
                            )
                        }
                    }
                }
            }
        }
    }
}