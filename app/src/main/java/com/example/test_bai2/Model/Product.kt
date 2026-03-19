package com.example.test_bai2.Model

data class Product(
    var id:String="",
    var name: String = "",
    var price: String = "",
    var image: String = "",
    var rating: Float=0f,
    var description: String="",
    var expiryDate: String="",
    var stock: Int=0,
    var usage: String="",
    var size: List<String> = listOf()
)