package com.example.test_bai2.Activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.test_bai2.Adapter.ProductAdapter
import com.example.test_bai2.Model.Product
import com.example.test_bai2.R
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var database: DatabaseReference
    private lateinit var productList: ArrayList<Product>
    private lateinit var edtSearch: EditText
    private lateinit var adapter: ProductAdapter
    private lateinit var filteredList: ArrayList<Product>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btnxu = findViewById<LinearLayout>(R.id.btnxu)
        val btn_voucher=findViewById<LinearLayout>(R.id.btn_voucher)
        val btngiohang=findViewById<ImageButton>(R.id.btngiohang)
        btnxu.setOnClickListener{
            val intent = Intent( this,DialogUpgradeActivity::class.java)
            startActivity(intent)
        }
        btn_voucher.setOnClickListener{
            val intent=Intent(this,ViewVoucherCoinActivity::class.java)
            startActivity(intent)
        }
        btngiohang.setOnClickListener{
            val intent=Intent(this,CartActivity::class.java)
            startActivity(intent)
        }
        listView = findViewById(R.id.listProduct)

        productList = ArrayList()
        database = FirebaseDatabase.getInstance().getReference("products")
        edtSearch = findViewById(R.id.edttimkiem)
        productList = ArrayList()
        filteredList = ArrayList()
        adapter = ProductAdapter(this, filteredList)
        listView.adapter = adapter
        loadProduct()
        edtSearch.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                val keyword = s.toString().trim().lowercase()

                filteredList.clear()

                for (product in productList) {

                    val productName = product.name.lowercase()

                    if (productName.contains(keyword)) {
                        filteredList.add(product)
                    }
                }

                adapter.notifyDataSetChanged()
            }
        })
        listView.setOnItemClickListener { _, _, position, _ ->

            val product = filteredList[position]

            val intent = Intent(this, ViewProductActivity::class.java)
            intent.putExtra("productId", product.id)
            intent.putExtra("name", product.name)
            intent.putExtra("price", product.price)
            intent.putExtra("image", product.image)
            intent.putExtra("rating", product.rating)
            intent.putExtra("description", product.description)
            intent.putExtra("expiryDate", product.expiryDate)
            intent.putExtra("stock", product.stock)
            intent.putExtra("usage", product.usage)
            intent.putStringArrayListExtra("size", ArrayList(product.size))
            startActivity(intent)
        }

    }

    private fun loadProduct() {

        database.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                productList.clear()
                filteredList.clear()

                for (data in snapshot.children) {
                    val id = data.key ?: ""
                    val name = data.child("name").getValue(String::class.java) ?: ""
                    val price = data.child("price").getValue(Long::class.java)?.toString() ?: ""
                    val image = data.child("image").getValue(String::class.java) ?: ""
                    val rating = data.child("rating").getValue(Double::class.java)?.toFloat() ?: 0f
                    val description = data.child("description").getValue(String::class.java) ?: ""
                    val expiryDate = data.child("expiryDate").getValue(String::class.java) ?: ""
                    val stock = data.child("stock").getValue(Int::class.java) ?: 0
                    val usage = data.child("usage").getValue(String::class.java) ?: ""
                    val sizeList = ArrayList<String>()

                    for (size in data.child("size").children) {
                        val s = size.getValue(String::class.java) ?: ""
                        sizeList.add(s)
                    }
                    val product = Product(
                        id,
                        name,
                        price,
                        image,
                        rating,
                        description,
                        expiryDate,
                        stock,
                        usage,
                        sizeList
                    )

                    productList.add(product)
                }
                filteredList.addAll(productList)
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }
}