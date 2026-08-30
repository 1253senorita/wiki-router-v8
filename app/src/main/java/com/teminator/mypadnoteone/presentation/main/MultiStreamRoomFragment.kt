package com.teminator.mypadnoteone.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    // 🔥 여러 개의 세트 방(룸 인스턴스)들을 배치할 대상 ID 리스트 (CH1 ~ CH4)
    val activeSubRooms = listOf("$rootRoomId-CH1", "$rootRoomId-CH2", "$rootRoomId-CH3", "$rootRoomId-CH4")

    // 🔥 전체 화면 스크롤 상태 정의
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
            .verticalScroll(scrollState) // 전체 화면 스크롤 적용
            .padding(bottom = 16.dp)
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

        Spacer(modifier = Modifier.height(8.dp))

        // 2. 하단 다중 세트 2열 배치 영역 (LazyVerticalGrid 대신 Column 내부에 2개씩 짝을 지어 세로 스크롤 레이아웃 구성)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            activeSubRooms.chunked(2).forEach { rowRooms ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowRooms.forEach { subRoomId ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(340.dp)
                        ) {
                            MatchingRoomScreen(
                                roomId = subRoomId,
                                order = null,
                                viewModel = viewModel,
                                onBackClick = onBackClick
                            )
                        }
                    }
                    // 만약 짝수가 안 맞아서 빈 자리가 생길 경우 레이아웃 균형을 맞추기 위한 빈 공간 처리
                    if (rowRooms.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}