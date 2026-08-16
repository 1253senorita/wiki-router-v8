package com.teminator.mypadnoteone.presentation.barobaro.room

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MatchingRoomFragment : Fragment() {

    // 💡 Hilt를 통해 MatchingRoomViewModel 정식 주입!
    private val viewModel: MatchingRoomViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 전달받은 룸 ID가 없으면 기본 테스트 룸 ID 지정
        val roomId = arguments?.getString("ROOM_ID") ?: "ROOM_TEST_DEFAULT"

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // 💡 아까 준비된 진짜 MatchingRoomScreen과 뷰모델을 연결합니다.
                        MatchingRoomScreen(
                            roomId = roomId,
                            viewModel = viewModel,
                            onBackClick = {
                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            }
                        )
                    }
                }
            }
        }
    }
}