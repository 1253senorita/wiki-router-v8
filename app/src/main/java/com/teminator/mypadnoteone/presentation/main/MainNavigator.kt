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
    // 🔥 [추가] 버튼 2 클릭 믈리언  주입
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
    // 🔥 [추가]   식탁에 식착보를     메니져 아줌마가  갈아주고  그리고  세  식착보를  깔아 서  하이드






    // 🔥 [추가] 상단바의 고객페이지 버튼 클릭 리스너 연결
    fun navigateToClient() {
        hideMainUI()
        val fragment = com.terminator.mypadnoteone.presentation.client.ClientFragment()

        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack("CLIENT")
            .commit()
    }
    // 🔥 [추가]

    /**
     * 메인 대시보드 UI 숨기기 (뚜껑 닫기)
     */
    private fun hideMainUI() {
        binding.fragmentContainer.visibility = View.VISIBLE
        binding.layoutTopBar.visibility = View.GONE         //  이 부분때문에 상단바 전체가 통째로 숨겨집니다!
        binding.layoutCategoryScroll.visibility = View.GONE // 카테고리 탭 바 숨김
        binding.layoutTestMetadata.visibility = View.GONE   // 시스템 상태 패널 숨김
        binding.dividerTop.visibility = View.GONE           // 구분선 숨김
        binding.scrollViewMain.visibility = View.GONE       // 메인 카드 스크롤뷰 숨김
        binding.layoutBottomNav.visibility = View.GONE      // 하단 네비게이션바 숨김
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