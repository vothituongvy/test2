package com.example.test_bai2.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test_bai2.Adapter.CartAdapter
import com.example.test_bai2.Model.CartItem
import com.example.test_bai2.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CartActivity : AppCompatActivity() {
    private lateinit var adapter: CartAdapter
    private val cartList = mutableListOf<CartItem>()
    private val userId = "user1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        val rvCart = findViewById<RecyclerView>(R.id.rvCart)
        rvCart.layoutManager = LinearLayoutManager(this)

        adapter = CartAdapter(cartList,
            onQuantityChange = { id, newQty -> updateQty(id, newQty) },
            onSelectionChange = { updateUI() }
        )
        rvCart.adapter = adapter

        loadCartData()

        findViewById<Button>(R.id.btnCheckout).setOnClickListener {
            val selectedList = cartList.filter { item ->
                adapter.selectedItems[item.id] == true
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
        FirebaseDatabase.getInstance().getReference("carts").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    cartList.clear()
                    if (snapshot.exists()) {
                        for (ds in snapshot.children) {
                            try {
                                val item = ds.getValue(CartItem::class.java)
                                item?.let {
                                    it.id = ds.key ?: ""
                                    cartList.add(it)
                                }
                            } catch (e: Exception) {
                                continue
                            }
                        }
                    }
                    adapter.notifyDataSetChanged()
                    updateUI()
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@CartActivity, "Lỗi tải: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateQty(id: String, qty: Int) {
        if (id.isEmpty()) return
        FirebaseDatabase.getInstance().getReference("carts").child(userId).child(id)
            .child("quantity").setValue(qty)
    }

    private fun updateUI() {
        val total = adapter.getTotalPrice()
        findViewById<TextView>(R.id.tvTotalPrice).text = "${String.format("%,d", total)}đ"

        val count = adapter.selectedItems.filter { it.value }.size
        findViewById<Button>(R.id.btnCheckout).text = "Thanh toán ($count)"
    }
}