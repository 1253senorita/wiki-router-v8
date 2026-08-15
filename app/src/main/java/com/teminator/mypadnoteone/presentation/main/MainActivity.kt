package com.teminator.mypadnoteone.presentation.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.teminator.mypadnoteone.databinding.ActivityMainBinding
import com.teminator.mypadnoteone.presentation.aerorouter.ui.AeroRouterEntryActivity
import com.teminator.mypadnoteone.presentation.auth.AuthActivity
import com.teminator.mypadnoteone.presentation.wiki.ui.WikiActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * 앱의 메인 화면을 담당하는 Activity입니다.
 * 대시보드 형태의 UI를 제공하며, 각 기능별 화면(Activity/Fragment)으로 이동하는 진입점 역할을 합니다.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    // 🔥 내비게이터(뚜껑 관리자) 선언: 메인 대시보드와 프래그먼트 간의 UI 전환을 관리합니다.
    private lateinit var navigator: MainNavigator

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 내비게이터 초기화 (Activity와 ViewBinding 전달)
        navigator = MainNavigator(this, binding)

        checkAudioPermission()
        setupUI()
        setupObserve()
    }

    /**
     * UI 컴포넌트의 리스너 및 클릭 이벤트를 초기화합니다.
     */
    private fun setupUI() {
        // PTT(무전기) 화면으로 이동
        binding.btnOpenPtt.setOnClickListener {
            val intent = Intent(this, AeroRouterEntryActivity::class.java)
            startActivity(intent)
        }

        // 로그아웃 버튼 클릭 시 ViewModel을 통해 로그아웃 처리 요청
        binding.btnLogout.setOnClickListener {
            viewModel.signOut()
        }

        // WIKI 화면으로 이동
        binding.btnWIKIPtt.setOnClickListener {
            val intent = Intent(this, WikiActivity::class.java)
            startActivity(intent)
        }

        // 파이어베이스 웹 다운로드 페이지 링크 열기
        binding.cardFirebaseDownload.setOnClickListener {
            val url = "https://mypadnoteone-a7ca4.web.app"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "웹페이지를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 🔥 바로바로 배차 목록 화면 띄우기 (뚜껑 덮기: 목록 모드)
        binding.cardBaroBaroBOTT.setOnClickListener {
            navigator.navigateToBaroBaro(isRegisterMode = false)
        }

        // 🔥 새 화물 오더 등록 화면 띄우기 (뚜껑 덮기: 등록 모드)
        binding.cardRegisterOrder.setOnClickListener {
            navigator.navigateToBaroBaro(isRegisterMode = true)
            Toast.makeText(this, "화물 오더 등록 화면으로 진입합니다.", Toast.LENGTH_SHORT).show()
        }

        // 🔥 [수정] 상단바 고객 페이지 버튼 -> 뚜껑 닫고 "CLIENT_PAGE" 모드로 진입
        binding.btnOpenClient.setOnClickListener {
            navigator.navigateToClientUnified("CLIENT_PAGE")
            Toast.makeText(this, "고객(클라이언트) 페이지로 진입합니다.", Toast.LENGTH_SHORT).show()
        }

        // 🔥 [수정] AI 보미 관제 카드 -> 뚜껑 닫고 "AI_BOMI_MONITOR" 모드로 진입
        binding.cardWikiRouterClient.setOnClickListener {
            navigator.navigateToClientAI("AI_BOMI_MONITOR")
            Toast.makeText(this, "AI 보미 관제 페이지로 진입합니다.", Toast.LENGTH_SHORT).show()
        }

        setupOnBackPressed()
    }

    /**
     * 뚜껑을 열고(메인 대시보드 UI 복구), 원래 대시보드 화면을 다시 보여줍니다.
     */
    fun restoreMainUI() {
        navigator.restoreMainUI()
    }

    /**
     * ViewModel의 UI 이벤트(상태 변화)를 감지하고 처리합니다.
     */
    private fun setupObserve() {
        lifecycleScope.launch {
            viewModel.uiEvent
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { event ->
                    when (event) {
                        is MainUiEvent.NavigateToAuth -> {
                            Toast.makeText(this@MainActivity, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@MainActivity, AuthActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        else -> {}
                    }
                }
        }
    }

    /**
     * 무전기 기능 사용을 위한 마이크(오디오 녹음) 권한을 확인합니다.
     */
    private fun checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
        }
    }

    /**
     * 권한 요청 결과에 따른 처리를 수행합니다.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "마이크 권한이 승인되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "무전기 기능을 사용하려면 마이크 권한이 필요합니다.", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * 뒤로가기 버튼(HW Back Button) 동작을 정의합니다.
     * 프래그먼트가 백스택에 쌓여있다면 프래그먼트를 닫고 메인 UI를 복구합니다.
     */
    private fun setupOnBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                    restoreMainUI()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }
}