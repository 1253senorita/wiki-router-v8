package com.teminator.mypadnoteone.presentation.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.teminator.mypadnoteone.R
import com.terminator.mypadnoteone.presentation.barobaro.BaroBaroFragment

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


        /**
         *   제일 중요  뚜껑작업
         */

        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack("BAROBARO")
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