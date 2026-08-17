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

    private val viewModel: MatchingRoomViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val roomId = arguments?.getString("ROOM_ID") ?: "ROOM_TEST_DEFAULT"

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // 💡 단독 프래그먼트로 열릴 때는 order가 없으므로 null로 안전하게 전달
                        MatchingRoomScreen(
                            roomId = roomId,
                            order = null,
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