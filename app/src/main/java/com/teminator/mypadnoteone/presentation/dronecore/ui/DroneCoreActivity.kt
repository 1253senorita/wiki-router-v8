package com.teminator.mypadnoteone.presentation.dronecore.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.teminator.mypadnoteone.databinding.ActivityDroneCoreBinding
import com.teminator.mypadnoteone.presentation.dronecore.data.DronePayload
import com.teminator.mypadnoteone.presentation.dronecore.viewmodel.DroneCoreViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DroneCoreActivity : AppCompatActivity() {

    private val tagLog = "DroneCoreActivity"
    private val viewModel: DroneCoreViewModel by viewModels()
    private lateinit var binding: ActivityDroneCoreBinding

    // 카메라 프레임 분석 전용 백그라운드 스레드 풀
    private lateinit var cameraExecutor: ExecutorService

    // 카메라 권한 요청 런처
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "카메라 권한이 거부되어 실시간 분석을 시작할 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDroneCoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 백그라운드 스레드 초기화
        cameraExecutor = Executors.newSingleThreadExecutor()

        // 카메라 권한 확인 및 시작
        checkCameraPermissionAndStart()

        // 뷰모델 상태 관찰
        observeViewModel()
    }

    private fun checkCameraPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // 1. 프리뷰 설정 (XML의 viewFinder와 서피스 프로바이더 연결)
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            // 2. 이미지 분석기 설정 (실시간 텐서 파이프라인 진입점)
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        try {
                            Log.d(tagLog, "Frame captured: width=${imageProxy.width}, height=${imageProxy.height}")
                        } catch (e: Exception) {
                            Log.e(tagLog, "Image analysis failed", e)
                        } finally {
                            // 다음 프레임을 받기 위해 반드시 close() 호출
                            imageProxy.close()
                        }
                    }
                }

            // 3. 후면 카메라 선택
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
                Log.d(tagLog, "CameraX bound successfully.")
            } catch (exc: Exception) {
                Log.e(tagLog, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.payloadState.collect { payload ->
                    payload?.let {
                        Log.d(tagLog, "UI Updated -> Target: ${it.targetId}")

                        binding.tvTargetId.text = "Target: ${it.targetId}"
                        binding.tvCoordinates.text = "Coordinates: X: ${it.coordinateX}, Y: ${it.coordinateY}"
                        binding.tvConfidence.text = "Confidence: ${(it.confidence * 100).toInt()}%"
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}