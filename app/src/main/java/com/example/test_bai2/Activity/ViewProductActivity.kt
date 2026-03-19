package com.example.test_bai2.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.bumptech.glide.Glide
import com.example.test_bai2.Model.CartItem
import com.example.test_bai2.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.database.*

class ViewProductActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    private var productId = ""
    private var pName = ""
    private var pPrice = 0L
    private var pImage = ""
    private var pSizeList = ArrayList<String>()
    private var pStock = 0
    private var pDescription = ""
    private var pUsage = ""
    private var pRating = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_product)

        val txtName = findViewById<TextView>(R.id.txttenSP)
        val txtGia = findViewById<TextView>(R.id.txtGia)
        val imgProduct = findViewById<ImageView>(R.id.imgProduct)
        val btnAddToCart = findViewById<AppCompatButton>(R.id.btnAddToCart)
        val btnBuyNow = findViewById<AppCompatButton>(R.id.btnBuyNow)

        productId = intent.getStringExtra("productId") ?: ""
        database = FirebaseDatabase.getInstance().getReference("products")

        loadProductFromFirebase(txtName, txtGia, imgProduct)

        btnAddToCart.setOnClickListener { showBottomSheet("cart") }
        btnBuyNow.setOnClickListener { showBottomSheet("buy") }
    }

    private fun loadProductFromFirebase(
        txtName: TextView,
        txtGia: TextView,
        imgProduct: ImageView
    ) {
        val txtThongSo = findViewById<TextView>(R.id.txt_thongso)
        val txtMoTa = findViewById<TextView>(R.id.txt_khac)
        val ratingBar = findViewById<RatingBar>(R.id.ratingProduct)

        if (productId.isEmpty()) return

        database.child(productId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) return

                    pName = snapshot.child("name").getValue(String::class.java) ?: ""
                    pPrice = snapshot.child("price").getValue(Long::class.java) ?: 0L
                    pImage = snapshot.child("image").getValue(String::class.java) ?: ""
                    pStock = snapshot.child("stock").getValue(Int::class.java) ?: 0
                    pDescription = snapshot.child("description").getValue(String::class.java) ?: ""
                    pUsage = snapshot.child("usage").getValue(String::class.java) ?: ""
                    pRating = snapshot.child("rating").getValue(Double::class.java)?.toFloat() ?: 0f

                    pSizeList.clear()
                    snapshot.child("size").children.forEach {
                        it.getValue(String::class.java)?.let { size ->
                            pSizeList.add(size)
                        }
                    }

                    txtName.text = pName
                    txtGia.text = "${String.format("%,d", pPrice)}đ"
                    txtThongSo.text = "Số lượng: $pStock\nSize: ${pSizeList.joinToString(", ")}"
                    txtMoTa.text = "$pDescription\nCách dùng: $pUsage"
                    ratingBar.rating = pRating

                    Glide.with(this@ViewProductActivity)
                        .load(pImage)
                        .placeholder(R.drawable.bg_search)
                        .into(imgProduct)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun showBottomSheet(type: String) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_bottom_sheet_cart, null)

        val imgSheet = view.findViewById<ImageView>(R.id.imgSheetProduct)
        val txtPrice = view.findViewById<TextView>(R.id.txtSheetPrice)
        val txtStock = view.findViewById<TextView>(R.id.txtSheetStock)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupSize)
        val txtQty = view.findViewById<TextView>(R.id.txtSheetQty)
        val btnConfirm = view.findViewById<Button>(R.id.btnConfirmAdd)

        Glide.with(this).load(pImage).into(imgSheet)
        txtPrice.text = "${String.format("%,d", pPrice)}đ"
        txtStock.text = "Kho: $pStock"

        btnConfirm.text = if (type == "buy") "Thanh toán ngay" else "Thêm vào giỏ hàng"

        var currentQty = 1
        txtQty.text = currentQty.toString()

        chipGroup.removeAllViews()
        pSizeList.forEach { size ->
            val chip = Chip(this)

            chip.text = size
            chip.isCheckable = true
            chip.isCheckedIconVisible = false

            chip.chipCornerRadius = 20f
            chip.chipStrokeWidth = 3f

            val states = arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf()
            )

            val textColors = intArrayOf(
                android.graphics.Color.parseColor("#FF5722"),
                android.graphics.Color.BLACK
            )
            chip.setTextColor(android.content.res.ColorStateList(states, textColors))

            val strokeColors = intArrayOf(
                android.graphics.Color.parseColor("#FF5722"),
                android.graphics.Color.LTGRAY
            )
            chip.chipStrokeColor =
                android.content.res.ColorStateList(states, strokeColors)


            val bgColors = intArrayOf(
                android.graphics.Color.parseColor("#FFF3E0"),
                android.graphics.Color.WHITE
            )
            chip.chipBackgroundColor =
                android.content.res.ColorStateList(states, bgColors)

            chipGroup.addView(chip)
        }


        view.findViewById<ImageButton>(R.id.btnPlus).setOnClickListener {
            if (currentQty < pStock) {
                currentQty++
                txtQty.text = currentQty.toString()
            } else {
                Toast.makeText(this, "Đã đạt tối đa tồn kho", Toast.LENGTH_SHORT).show()
            }
        }


        view.findViewById<ImageButton>(R.id.btnMinus).setOnClickListener {
            if (currentQty > 1) {
                currentQty--
                txtQty.text = currentQty.toString()
            }
        }

        btnConfirm.setOnClickListener {
            val selectedId = chipGroup.checkedChipId

            if (selectedId == View.NO_ID) {
                Toast.makeText(this, "Vui lòng chọn size", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val size = view.findViewById<Chip>(selectedId).text.toString()

            if (type == "cart") {
                addToCart(size, currentQty)
            } else {
                val intent = Intent(this, CheckoutActivity::class.java)
                intent.putExtra("productId", productId)
                intent.putExtra("name", pName)
                intent.putExtra("price", pPrice)
                intent.putExtra("image", pImage)
                intent.putExtra("size", size)
                intent.putExtra("qty", currentQty)
                startActivity(intent)
            }

            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun addToCart(size: String, qty: Int) {
        val userId = "user1"
        val cartRef = FirebaseDatabase.getInstance()
            .getReference("carts")
            .child(userId)

        cartRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                var foundKey: String? = null
                var existingQty = 0

                for (child in snapshot.children) {
                    val pId = child.child("productId").getValue(String::class.java)
                    val s = child.child("size").getValue(String::class.java)

                    if (pId == productId && s == size) {
                        foundKey = child.key
                        existingQty = child.child("quantity").getValue(Int::class.java) ?: 0
                        break
                    }
                }

                if (foundKey != null) {
                    val newQty = existingQty + qty

                    if (newQty > pStock) {
                        Toast.makeText(
                            this@ViewProductActivity,
                            "Chỉ còn $pStock sản phẩm",
                            Toast.LENGTH_SHORT
                        ).show()

                        cartRef.child(foundKey).child("quantity").setValue(pStock)
                    } else {
                        cartRef.child(foundKey).child("quantity").setValue(newQty)
                    }

                } else {

                    if (qty > pStock) {
                        Toast.makeText(
                            this@ViewProductActivity,
                            "Chỉ còn $pStock sản phẩm",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    val newRef = cartRef.push()

                    val item = CartItem(
                        id = newRef.key ?: "",
                        productId = productId,
                        name = pName,
                        price = pPrice,
                        image = pImage,
                        size = size,
                        quantity = qty
                    )

                    newRef.setValue(item)
                }

                Toast.makeText(this@ViewProductActivity, "Đã thêm vào giỏ", Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}