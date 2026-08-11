package com.teminator.mypadnoteone.presentation.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.teminator.mypadnoteone.R
import com.teminator.mypadnoteone.databinding.ActivityMainBinding
import com.teminator.mypadnoteone.presentation.auth.AuthActivity
import com.teminator.mypadnoteone.presentation.aerorouter.ui.AeroRouterEntryActivity
import com.teminator.mypadnoteone.presentation.wiki.ui.WikiActivity
import com.terminator.mypadnoteone.presentation.barobaro.BaroBaroFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAudioPermission()
        setupUI()
        setupObserve()
    }

    private fun setupUI() {
        // 1. 무전기 진입 화면으로 이동
        binding.btnOpenPtt.setOnClickListener {
            val intent = Intent(this, AeroRouterEntryActivity::class.java)
            startActivity(intent)
        }

        // 2. 로그아웃
        binding.btnLogout.setOnClickListener {
            viewModel.signOut()
        }

        // 3. Wiki 라우터 화면으로 이동
        binding.btnWIKIPtt.setOnClickListener {
            val intent = Intent(this, WikiActivity::class.java)
            startActivity(intent)
        }

        // 파이어베이스 다운로드 호스팅 주소로 이동
        binding.cardFirebaseDownload.setOnClickListener {
            val url = "https://mypadnoteone-a7ca4.web.app"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "웹페이지를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 4. 바로바로 화물 배차 프래그먼트로 교체하여 진입
        binding.cardBaroBaroBOTT.setOnClickListener {
            showBaroBaroFragment()
        }

        setupOnBackPressed()
    }

    private fun showBaroBaroFragment() {
        // 메인 대시보드 UI 숨기기
        binding.fragmentContainer.visibility = View.VISIBLE
        binding.layoutTopBar.visibility = View.GONE
        binding.layoutCategoryScroll.visibility = View.GONE
        binding.layoutTestMetadata.visibility = View.GONE
        binding.dividerTop.visibility = View.GONE
        binding.scrollViewMain.visibility = View.GONE
        binding.layoutBottomNav.visibility = View.GONE

        // 프래그먼트 추가
        val fragment = BaroBaroFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack("BAROBARO")
            .commit()
    }

    fun restoreMainUI() {
        // 프래그먼트 영역 숨기기 및 메인 대시보드 복구
        binding.fragmentContainer.visibility = View.GONE
        binding.layoutTopBar.visibility = View.VISIBLE
        binding.layoutCategoryScroll.visibility = View.VISIBLE
        binding.layoutTestMetadata.visibility = View.VISIBLE
        binding.dividerTop.visibility = View.VISIBLE
        binding.scrollViewMain.visibility = View.VISIBLE
        binding.layoutBottomNav.visibility = View.VISIBLE
    }

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