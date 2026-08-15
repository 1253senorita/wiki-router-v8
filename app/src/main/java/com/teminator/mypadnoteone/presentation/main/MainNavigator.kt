package com.teminator.mypadnoteone.presentation.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.teminator.mypadnoteone.R
import com.terminator.mypadnoteone.presentation.barobaro.BaroBaroFragment
import com.terminator.mypadnoteone.presentation.client.AiFragment
import com.terminator.mypadnoteone.presentation.client.ClientFragment

class MainNavigator(
    private val activity: AppCompatActivity,
    private val binding: com.teminator.mypadnoteone.databinding.ActivityMainBinding
) {

    /**
     * 바로바로 화면으로 이동 (목록 모드 또는 등록 모드 인자 전달)
     */
    fun navigateToBaroBaro(isRegisterMode: Boolean) {
        hideMainUI()

        val fragment = BaroBaroFragment().apply {
            arguments = Bundle().apply {
                putBoolean("IS_REGISTER_MODE", isRegisterMode)
            }
        }

        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack("BAROBARO")
            .commit()
    }








    /**
     * 🔥 [통합] 고객 및 AI 보미 관제 페이지로 이동 (진입 모드 타입 인자 전달)
     */
    fun navigateToClientUnified(viewType: String) {
        hideMainUI()

        val fragment = AiFragment().apply {
            arguments = Bundle().apply {
                putString("VIEW_TYPE", viewType)
            }
        }

        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack("CLIENT_UNIFIED")
            .commit()
    }



    /**
     * 🔥 [통합] 고객 및 AI 보미 관제 페이지로 이동 (진입 모드 타입 인자 전달)
     */
    fun navigateToClientAI(viewType: String) {
        hideMainUI()

        val fragment = ClientFragment().apply {
            arguments = Bundle().apply {
                putString("VIEW_TYPE", viewType)
            }
        }

        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack("CLIENT_UNIFIED")
            .commit()
    }



    /**
     * 메인 대시보드 UI 숨기기 (뚜껑 닫기)
     */
    private fun hideMainUI() {
        binding.fragmentContainer.visibility = View.VISIBLE
        binding.layoutTopBar.visibility = View.GONE
        binding.layoutCategoryScroll.visibility = View.GONE
        binding.layoutTestMetadata.visibility = View.GONE
        binding.dividerTop.visibility = View.GONE
        binding.scrollViewMain.visibility = View.GONE
        binding.layoutBottomNav.visibility = View.GONE
    }

    /**
     * 메인 대시보드 UI 복구하기 (뚜껑 열기)
     */
    fun restoreMainUI() {
        binding.fragmentContainer.visibility = View.GONE
        binding.layoutTopBar.visibility = View.VISIBLE
        binding.layoutCategoryScroll.visibility = View.VISIBLE
        binding.layoutTestMetadata.visibility = View.VISIBLE
        binding.dividerTop.visibility = View.VISIBLE
        binding.scrollViewMain.visibility = View.VISIBLE
        binding.layoutBottomNav.visibility = View.VISIBLE
    }
}