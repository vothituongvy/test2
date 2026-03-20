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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.test_bai2.Adapter.ProductAdapter
import com.example.test_bai2.Model.Product
import com.example.test_bai2.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var database: DatabaseReference
    private lateinit var userDatabase: DatabaseReference
    private lateinit var productList: ArrayList<Product>
    private lateinit var edtSearch: EditText
    private lateinit var adapter: ProductAdapter
    private lateinit var filteredList: ArrayList<Product>
    private lateinit var imgAvatar: ShapeableImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnxu = findViewById<LinearLayout>(R.id.btnxu)
        val btn_voucher = findViewById<LinearLayout>(R.id.btn_voucher)
        val btngiohang = findViewById<ImageButton>(R.id.btngiohang)
        imgAvatar = findViewById(R.id.imgAvatar)
        listView = findViewById(R.id.listProduct)
        edtSearch = findViewById(R.id.edttimkiem)

        database = FirebaseDatabase.getInstance().getReference("products")
        userDatabase = FirebaseDatabase.getInstance().getReference("users")
        productList = ArrayList()
        filteredList = ArrayList()
        adapter = ProductAdapter(this, filteredList)
        listView.adapter = adapter

        loadProduct()
        loadUserProfileImage()

        btnxu.setOnClickListener {
            startActivity(Intent(this, DialogUpgradeActivity::class.java))
        }

        btn_voucher.setOnClickListener {
            startActivity(Intent(this, ViewVoucherCoinActivity::class.java))
        }

        btngiohang.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        imgAvatar.setOnClickListener {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

            if (currentUserId != null) {
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("USER_ID", currentUserId)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Vui lòng đăng nhập để xem hồ sơ!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }

        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val keyword = s.toString().trim().lowercase()
                filteredList.clear()
                for (product in productList) {
                    if (product.name.lowercase().contains(keyword)) {
                        filteredList.add(product)
                    }
                }
                adapter.notifyDataSetChanged()
            }
            override fun afterTextChanged(s: Editable?) {}
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

    private fun loadUserProfileImage() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            userDatabase.child(user.uid).child("profileImage")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val imageUrl = snapshot.getValue(String::class.java)

                        if (!imageUrl.isNullOrEmpty()) {
                            Glide.with(this@MainActivity)
                                .load(imageUrl)
                                .placeholder(R.drawable.avartar)
                                .error(R.drawable.avartar)
                                .into(imgAvatar)
                        } else {
                            imgAvatar.setImageResource(R.drawable.avartar)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        imgAvatar.setImageResource(R.drawable.avartar)
                    }
                })
        } else {
            imgAvatar.setImageResource(R.drawable.avartar)
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
                        size.getValue(String::class.java)?.let { sizeList.add(it) }
                    }

                    productList.add(Product(id, name, price, image, rating, description, expiryDate, stock, usage, sizeList))
                }
                filteredList.addAll(productList)
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Lỗi tải sản phẩm: ${error.message}")
            }
        })
    }
}