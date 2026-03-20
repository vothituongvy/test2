package com.example.test_bai2.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test_bai2.Activity.LoginActivity
import com.example.test_bai2.Adapter.CartAdapter
import com.example.test_bai2.Model.CartItem
import com.example.test_bai2.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CartActivity : AppCompatActivity() {

    private lateinit var adapter: CartAdapter
    private val cartList = mutableListOf<CartItem>()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val rvCart = findViewById<RecyclerView>(R.id.rvCart)
        rvCart.layoutManager = LinearLayoutManager(this)

        adapter = CartAdapter(
            cartList,
            onQuantityChange = { id, newQty -> updateQty(id, newQty) },
            onSelectionChange = { updateUI() }
        )
        rvCart.adapter = adapter

        loadCartData()

        findViewById<Button>(R.id.btnCheckout).setOnClickListener {
            val selectedList = cartList.filter {
                adapter.selectedItems[it.id] == true
            }

            if (selectedList.isNotEmpty()) {
                val intent = Intent(this, CheckoutActivity::class.java)
                intent.putExtra("list_cart", ArrayList(selectedList))
                startActivity(intent)
            } else {
                Toast.makeText(this, "Vui lòng tích chọn sản phẩm!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadCartData() {
        val uId = auth.currentUser?.uid ?: return

        FirebaseDatabase.getInstance()
            .getReference("carts")
            .child(uId)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    cartList.clear()

                    for (ds in snapshot.children) {
                        try {
                            val item = ds.getValue(CartItem::class.java)
                            item?.let {
                                it.id = ds.key ?: ""
                                it.productId = ds.child("productId").getValue(String::class.java) ?: ""
                                cartList.add(it)
                            }
                        } catch (e: Exception) {
                            continue
                        }
                    }

                    adapter.notifyDataSetChanged()
                    updateUI()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@CartActivity, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateQty(cartId: String, qty: Int) {
        val uId = auth.currentUser?.uid ?: return
        if (cartId.isEmpty()) return

        val cartRef = FirebaseDatabase.getInstance()
            .getReference("carts")
            .child(uId)
            .child(cartId)

        cartRef.get().addOnSuccessListener { cartSnap ->
            val productId = cartSnap.child("productId").getValue(String::class.java)

            if (productId.isNullOrEmpty()) {
                Toast.makeText(this, "Không tìm thấy sản phẩm!", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            FirebaseDatabase.getInstance()
                .getReference("products")
                .child(productId)
                .child("stock")
                .get()
                .addOnSuccessListener { stockSnap ->

                    val stock = stockSnap.getValue(Int::class.java) ?: 0

                    when {
                        stock == 0 -> {
                            Toast.makeText(this, "Hết hàng!", Toast.LENGTH_SHORT).show()
                            cartRef.child("quantity").setValue(0)
                        }
                        qty > stock -> {
                            Toast.makeText(this, "Chỉ còn $stock sản phẩm!", Toast.LENGTH_SHORT).show()
                            cartRef.child("quantity").setValue(stock)
                        }
                        else -> {
                            cartRef.child("quantity").setValue(qty)
                        }
                    }
                }
        }
    }

    private fun updateUI() {
        val total = adapter.getTotalPrice()
        findViewById<TextView>(R.id.tvTotalPrice).text = "${String.format("%,d", total)}đ"

        val count = adapter.selectedItems.filter { it.value }.size
        findViewById<Button>(R.id.btnCheckout).text = "Thanh toán ($count)"
    }
}