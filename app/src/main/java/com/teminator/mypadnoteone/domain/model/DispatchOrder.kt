package com.teminator.mypadnoteone.domain.model

data class DispatchOrder(
    val id: String = "",
    val route: String = "",
    val cargoInfo: String = "",
    val price: String = "",
    val status: String = "대기중",
    val description: String = ""
)