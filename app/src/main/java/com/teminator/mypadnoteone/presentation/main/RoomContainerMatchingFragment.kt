package com.teminator.mypadnoteone.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.teminator.mypadnoteone.presentation.barobaro.room.MatchingRoomScreen
import com.teminator.mypadnoteone.presentation.barobaro.room.MatchingRoomViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RoomContainerMatchingFragment : Fragment() {

    private val viewModel: MatchingRoomViewModel by viewModels()

    companion object {
        fun newInstance(roomId: String): RoomContainerMatchingFragment {
            return RoomContainerMatchingFragment().apply {
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
        val roomId = arguments?.getString("ROOM_ID") ?: "ROOM_TEST_GLOBAL"

        return ComposeView(requireContext()).apply {
            setContent {
                // 💡 [수정] 함수 선언이 아니라 함수를 정상적으로 호출하도록 변경했습니다!
                MatchingRoomScreen(
                    roomId = roomId,
                    order = null, // 단독 컨테이너 진입 시 오더 정보는 기본 null 처리
                    viewModel = viewModel,
                    onBackClick = {
                        // 뒤로가기 누를 때 프래그먼트를 닫고 메인 대시보드 UI 복구
                        requireActivity().supportFragmentManager.popBackStack()
                        (requireActivity() as? MainActivity)?.restoreMainUI()
                    }
                )
            }
        }
    }
}