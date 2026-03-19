package com.example.test_bai2.Activity

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test_bai2.Adapter.CheckoutAdapter
import com.example.test_bai2.Adapter.VoucherAdapter
import com.example.test_bai2.Model.CartItem
import com.example.test_bai2.Model.Voucher
import com.example.test_bai2.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.*

class CheckoutActivity : AppCompatActivity() {

    private lateinit var rvItems: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var btnOrder: Button
    private lateinit var swUseCoins: SwitchCompat
    private lateinit var tvCoinLabel: TextView
    private lateinit var edtVoucher: EditText

    private val checkoutList = mutableListOf<CartItem>()
    private val availableVouchers = mutableListOf<Voucher>()

    private var userCoins = 0L
    private var originalTotal = 0L
    private var shippingFee = 30000L
    private var coinDiscountUsed = 0L

    private val userId = "user1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        initViews()
        loadDataFromIntent()

        originalTotal = checkoutList.sumOf { it.price * it.quantity }

        fetchUserDataFromFirebase()

        swUseCoins.setOnCheckedChangeListener { _, _ ->
            calculateFinalTotal()
        }

        edtVoucher.isFocusable = false
        edtVoucher.hint = "Chọn mã giảm giá"
        edtVoucher.setOnClickListener { showVoucherBottomSheet() }

        btnOrder.setOnClickListener { placeOrder() }

        calculateFinalTotal()
    }

    private fun initViews() {
        rvItems = findViewById(R.id.rvCheckoutItems)
        tvTotal = findViewById(R.id.tvTotalCheckout)
        btnOrder = findViewById(R.id.btnPlaceOrder)
        swUseCoins = findViewById(R.id.swUseCoins)
        tvCoinLabel = findViewById(R.id.tvCoinLabel)
        edtVoucher = findViewById(R.id.edtVoucher)

        rvItems.layoutManager = LinearLayoutManager(this)
    }

    private fun loadDataFromIntent() {
        val fromCart = intent.getSerializableExtra("list_cart") as? ArrayList<CartItem>

        if (fromCart != null) {
            checkoutList.addAll(fromCart)
        } else {
            val pId = intent.getStringExtra("productId") ?: ""
            val pName = intent.getStringExtra("name")
            val pPrice = intent.getLongExtra("price", 0L)
            val pImage = intent.getStringExtra("image")
            val pSize = intent.getStringExtra("size")
            val pQty = intent.getIntExtra("qty", 1)

            if (pName != null) {
                checkoutList.add(
                    CartItem(
                        id = "",
                        productId = pId,
                        name = pName,
                        price = pPrice,
                        image = pImage ?: "",
                        size = pSize ?: "",
                        quantity = pQty
                    )
                )
            }
        }

        rvItems.adapter = CheckoutAdapter(checkoutList)
    }

    private fun fetchUserDataFromFirebase() {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return

                userCoins = snapshot.child("coin").getValue(Long::class.java) ?: 0L
                tvCoinLabel.text = "Dùng ${String.format("%,d", userCoins)} Xu"

                availableVouchers.clear()
                snapshot.child("my_vouchers").children.forEach { vSnap ->
                    val id = vSnap.key ?: return@forEach
                    val qty = vSnap.child("quantity").getValue(Int::class.java) ?: 0
                    val type = vSnap.child("type").getValue(String::class.java) ?: ""
                    val value = vSnap.child("value").getValue(Long::class.java) ?: 0L
                    val min = vSnap.child("minOrder").getValue(Long::class.java) ?: 0L

                    if (qty > 0) {
                        val displayName = if (type.lowercase() == "freeship") "Miễn phí vận chuyển"
                        else "Giảm ${String.format("%,d", value)}đ"

                        availableVouchers.add(Voucher(id, displayName, type.lowercase(), value, min, qty))
                    }
                }
                calculateFinalTotal()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun calculateFinalTotal() {
        var voucherDiscount = 0L
        var currentShippingFee = shippingFee

        availableVouchers.filter { it.isSelected }.forEach {
            if (it.type == "freeship") {
                currentShippingFee = 0L
            } else {
                voucherDiscount += it.discountAmount
            }
        }

        val totalBeforeCoins = originalTotal + currentShippingFee - voucherDiscount

        coinDiscountUsed = if (swUseCoins.isChecked) {
            minOf(userCoins, maxOf(0L, totalBeforeCoins))
        } else {
            0L
        }

        val finalTotal = totalBeforeCoins - coinDiscountUsed
        tvTotal.text = "${String.format("%,d", maxOf(0L, finalTotal))}đ"
    }

    private fun showVoucherBottomSheet() {
        val validVouchers = availableVouchers.filter { originalTotal >= it.minOrder }
        if (validVouchers.isEmpty()) {
            Toast.makeText(this, "Không có voucher phù hợp", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_voucher_bottom_sheet, null)
        val rvVouchers = view.findViewById<RecyclerView>(R.id.rvVoucherList)

        rvVouchers.layoutManager = LinearLayoutManager(this)
        rvVouchers.adapter = VoucherAdapter(validVouchers) { selectedVoucher ->
            applyVoucherLogic(selectedVoucher, dialog)
        }
        dialog.setContentView(view)
        dialog.show()
    }

    private fun applyVoucherLogic(voucher: Voucher, dialog: BottomSheetDialog) {
        if (voucher.isSelected) {
            voucher.isSelected = false
        } else {
            availableVouchers.filter { it.type == voucher.type }.forEach { it.isSelected = false }
            voucher.isSelected = true
        }

        edtVoucher.setText(availableVouchers.filter { it.isSelected }.joinToString(", ") { it.code })
        calculateFinalTotal()
        dialog.dismiss()
    }

    private fun placeOrder() {
        val db = FirebaseDatabase.getInstance()
        val userRef = db.getReference("users").child(userId)
        val productRef = db.getReference("products")
        val cartRef = db.getReference("carts").child(userId)

        if (coinDiscountUsed > 0) {
            userRef.child("coin").runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val currentCoins = currentData.getValue(Long::class.java) ?: 0L
                    currentData.value = maxOf(0L, currentCoins - coinDiscountUsed)
                    return Transaction.success(currentData)
                }
                override fun onComplete(e: DatabaseError?, c: Boolean, s: DataSnapshot?) {}
            })
        }

        availableVouchers.filter { it.isSelected }.forEach { voucher ->
            userRef.child("my_vouchers").child(voucher.id).runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val qty = currentData.child("quantity").getValue(Int::class.java) ?: 0
                    if (qty <= 1) {
                        currentData.value = null
                    } else {
                        currentData.child("quantity").value = qty - 1
                    }
                    return Transaction.success(currentData)
                }
                override fun onComplete(e: DatabaseError?, c: Boolean, s: DataSnapshot?) {}
            })
        }


        checkoutList.forEach { item ->
            productRef.child(item.productId).child("stock")
                .runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        val stock = currentData.getValue(Int::class.java) ?: 0
                        if (stock >= item.quantity) {
                            currentData.value = stock - item.quantity
                        }
                        return Transaction.success(currentData)
                    }
                    override fun onComplete(e: DatabaseError?, c: Boolean, s: DataSnapshot?) {}
                })

            if (item.id.isNotEmpty()) {
                cartRef.child(item.id).removeValue()
            }
        }

        Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show()
        finish()
    }
}