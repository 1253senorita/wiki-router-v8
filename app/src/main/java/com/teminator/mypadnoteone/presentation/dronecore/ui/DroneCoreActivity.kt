package com.teminator.mypadnoteone.presentation.dronecore.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.teminator.mypadnoteone.databinding.ActivityDroneCoreBinding
import com.teminator.mypadnoteone.presentation.dronecore.data.DronePayload
import com.teminator.mypadnoteone.presentation.dronecore.viewmodel.DroneCoreViewModel
import kotlinx.coroutines.launch

class DroneCoreActivity : AppCompatActivity() {

    private val tagLog = "DroneCoreActivity"
    private val viewModel: DroneCoreViewModel by viewModels()
    private lateinit var binding: ActivityDroneCoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDroneCoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        launchEngineAndData()
        observeViewModel()
    }

    private fun launchEngineAndData() {
        Log.d(tagLog, "DroneCore Engine launched successfully.")

        viewModel.processIncomingData(
            DronePayload(
                targetId = "DRONE-V8-NODE-01",
                coordinateX = 127.23f,
                coordinateY = 37.45f,
                confidence = 0.98f
            )
        )
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
}