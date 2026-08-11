package com.teminator.mypadnoteone.presentation.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.teminator.mypadnoteone.R
import com.terminator.mypadnoteone.presentation.barobaro.BaroBaroFragment

class MainNavigator(private val activity: AppCompatActivity, private val binding: com.teminator.mypadnoteone.databinding.ActivityMainBinding) {

    /**
     * 뚜껑을 덮고(메인 대시보드 UI 숨기기), 바로바로 프래그먼트(부품)를 갈아 끼우는 함수입니다.
     * @param isRegisterMode true일 경우 화물 오더 등록 모드, false일 경우 배차 목록 모드로 동작합니다.
     */

    // 뚜껑을 덮고(메인 숨기기), 바로바로 프래그먼트(부품)를 갈아 끼우는 함수
    fun navigateToBaroBaro(isRegisterMode: Boolean) {
        // 1. 메인 대시보드 UI 뚜껑 닫기 (숨기기)
        binding.fragmentContainer.visibility = View.VISIBLE
        binding.layoutTopBar.visibility = View.GONE
        binding.layoutCategoryScroll.visibility = View.GONE
        binding.layoutTestMetadata.visibility = View.GONE
        binding.dividerTop.visibility = View.GONE
        binding.scrollViewMain.visibility = View.GONE
        binding.layoutBottomNav.visibility = View.GONE

        // 2. 모드 값을 담은 프래그먼트 부품 생성
        val fragment = BaroBaroFragment().apply {
            arguments = Bundle().apply {
                putBoolean("IS_REGISTER_MODE", isRegisterMode)
            }
        }

        // 3. 프래그먼트 교체 트랜잭션 실행
        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack("BAROBARO")
            .commit()
    }

    /**
     * 뚜껑을 열고(메인 대시보드 UI 복구하기), 원래의 대시보드 화면들을 다시 보여주는 함수입니다.
     */

    // 뚜껑을 열고(메인 복구하기), 원래 대시보드를 보여주는 함수
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