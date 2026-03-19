package com.example.test_bai2.Model

data class UserProfile(
    val coin: Int = 0,
    val vouchers: Map<String, Voucher> = emptyMap()
)