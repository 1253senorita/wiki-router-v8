package com.teminator.mypadnoteone.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.teminator.mypadnoteone.presentation.barobaro.room.MatchingRoomScreen
import com.teminator.mypadnoteone.presentation.barobaro.room.MatchingRoomViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MultiStreamRoomFragment : Fragment() {

    private val viewModel: MatchingRoomViewModel by viewModels()

    companion object {
        fun newInstance(roomId: String): MultiStreamRoomFragment {
            return MultiStreamRoomFragment().apply {
                arguments = Bundle().apply {
                    putString("ROOM_ID", roomId)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootRoomId = arguments?.getString("ROOM_ID") ?: "MULTI_STREAM_GLOBAL_ROOM"

        return ComposeView(requireContext()).apply {
            setContent {
                MultiStreamMatrixLayout(
                    rootRoomId = rootRoomId,
                    viewModel = viewModel,
                    onBackClick = {
                        requireActivity().supportFragmentManager.popBackStack()
                        (requireActivity() as? MainActivity)?.restoreMainUI()
                    }
                )
            }
        }
    }
}

@Composable
fun MultiStreamMatrixLayout(
    rootRoomId: String,
    viewModel: MatchingRoomViewModel,
    onBackClick: () -> Unit
) {
    // 🔥 여러 개의 세트 방(룸 인스턴스)들을 그리드 틀 안에서 한 세트씩 묶어 배치할 대상 ID 리스트
    val activeSubRooms = listOf("$rootRoomId-CH1", "$rootRoomId-CH2", "$rootRoomId-CH3", "$rootRoomId-CH4")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        // 1. 상단 마스터 관제/메인 송출 뷰포트 영역 (BJ 메인 화면)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color(0xFF1E1E1E)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🔴 [MASTER BROADCAST & ROUTER CONTROL]",
                    color = Color(0xFFFF5722),
                    fontSize = 14.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "통합 관제 루트 ID: $rootRoomId",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }
        }

        // 2. 하단 다중 세트 그리드 틀 배치 영역 (MatchingRoomScreen들이 가구 세트처럼 각각의 격자 셀에 쏙쏙 박히는 구조)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // 2열 그리드 구조
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(activeSubRooms) { subRoomId ->
                // 📦 각각의 셀(틀)마다 한 세트의 MatchingRoomScreen 독립 컴포넌트 배치
                Box(
                    modifier = Modifier
                        .height(340.dp)
                        .fillMaxWidth()
                ) {
                    MatchingRoomScreen(
                        roomId = subRoomId,
                        order = null,
                        viewModel = viewModel,
                        onBackClick = onBackClick
                    )
                }
            }
        }
    }
}