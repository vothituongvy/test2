package com.example.test_bai2.Model

data class CartItem(
    var id: String = "",
    var productId: String = "",
    var name: String = "",
    var price: Long = 0,
    var image: String = "",
    var size: String = "",
    var quantity: Int = 0
) : java.io.Serializable