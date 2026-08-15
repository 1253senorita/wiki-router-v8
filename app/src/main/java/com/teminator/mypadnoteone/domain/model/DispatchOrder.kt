package com.teminator.mypadnoteone.domain.model

data class DispatchOrder(
    val id: String,
    val route: String,
    val cargoInfo: String,
    val price: String,
    val status: String,
    val description: String = ""
)