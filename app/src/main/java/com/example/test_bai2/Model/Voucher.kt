package com.example.test_bai2.Model


import java.io.Serializable

data class Voucher(
    var id: String = "",
    val code: String = "",
    val type: String = "",
    val discountAmount: Long = 0L,
    val minOrder: Long = 0L,
    val quantity: Int = 0,
    var isSelected: Boolean = false
) : Serializable
