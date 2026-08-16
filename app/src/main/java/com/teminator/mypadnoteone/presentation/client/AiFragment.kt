package com.teminator.mypadnoteone.presentation.client



import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.teminator.mypadnoteone.presentation.main.MainActivity
//import com.terminator.mypadnoteone.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import com.teminator.mypadnoteone.presentation.client.AIScreen

@AndroidEntryPoint
class AiFragment : Fragment() {

    private val viewModel: AIClientViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    Surface {
                        AIScreen(
                            viewModel = viewModel,
                            onBack = {
                                // 뒤로가기 시 메인 대시보드로 복구
                                (activity as? MainActivity)?.restoreMainUI()
                                parentFragmentManager.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}