package com.teminator.mypadnoteone.presentation.dronecore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teminator.mypadnoteone.presentation.dronecore.data.DronePayload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DroneCoreViewModel : ViewModel() {

    private val _payloadState = MutableStateFlow<DronePayload?>(null)
    val payloadState: StateFlow<DronePayload?> = _payloadState.asStateFlow()

    fun processIncomingData(payload: DronePayload) {
        viewModelScope.launch {
            _payloadState.value = payload
        }
    }
}