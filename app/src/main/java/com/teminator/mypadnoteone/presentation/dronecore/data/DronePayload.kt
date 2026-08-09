package com.teminator.mypadnoteone.presentation.dronecore.data

data class DronePayload(
    val targetId: String,
    val coordinateX: Float,
    val coordinateY: Float,
    val confidence: Float
)