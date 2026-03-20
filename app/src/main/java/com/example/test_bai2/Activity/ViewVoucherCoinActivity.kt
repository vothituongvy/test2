package com.example.test_bai2.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test_bai2.Activity.LoginActivity
import com.example.test_bai2.Adapter.VoucherAdapter
import com.example.test_bai2.Model.Voucher
import com.example.test_bai2.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ViewVoucherCoinActivity : AppCompatActivity() {

    private lateinit var db: DatabaseReference
    private lateinit var tvUserCoin: TextView
    private lateinit var rvVouchers: RecyclerView
    private lateinit var adapter: VoucherAdapter

    private val voucherList = mutableListOf<Voucher>()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_voucher_coin)

        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        tvUserCoin = findViewById(R.id.tvUserCoin)
        rvVouchers = findViewById(R.id.rvVouchers)
        rvVouchers.layoutManager = LinearLayoutManager(this)

        adapter = VoucherAdapter(voucherList) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
        rvVouchers.adapter = adapter

        db = FirebaseDatabase.getInstance().getReference("users").child(currentUser.uid)

        getData()
    }

    private fun getData() {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return

                val coin = snapshot.child("coin").getValue(Long::class.java) ?: 0L
                tvUserCoin.text = String.format("%,d", coin)

                voucherList.clear()
                val vouchersSnapshot = snapshot.child("my_vouchers")

                for (child in vouchersSnapshot.children) {
                    val vId = child.key ?: continue
                    val qty = child.child("quantity").getValue(Int::class.java) ?: 0

                    if (qty <= 0) continue

                    val type = child.child("type").getValue(String::class.java) ?: ""
                    val title = child.child("title").getValue(String::class.java) ?: ""
                    val minOrder = child.child("minOrder").getValue(Long::class.java) ?: 0L
                    val firebaseValue = child.child("value").getValue(Long::class.java) ?: 0L
                    val voucher = Voucher(
                        id = vId,
                        code = title,
                        type = type,
                        discountAmount = if (type == "freeship") 30000L else firebaseValue,
                        minOrder = minOrder,
                        quantity = qty
                    )

                    voucherList.add(voucher)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ViewVoucherCoinActivity, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}