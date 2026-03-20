package com.example.test_bai2.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.test_bai2.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var edtName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPhone: EditText
    private lateinit var edtAddress: EditText
    private lateinit var btnSave: Button
    private lateinit var btnLogout: TextView

    private lateinit var database: DatabaseReference
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initViews()

        userId = intent.getStringExtra("USER_ID") ?: FirebaseAuth.getInstance().currentUser?.uid

        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        database = FirebaseDatabase.getInstance().getReference("users").child(userId!!)

        loadUserData()

        btnSave.setOnClickListener {
            validateAndSave()
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("USER_ID", userId)
            // FLAG_ACTIVITY_CLEAR_TASK là đóng tất cả các trang đang mở trước đó và biến trang mới thành trang duy nhất đang chạy.
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            Toast.makeText(this, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews() {
        edtName = findViewById(R.id.edtNameProfile)
        edtEmail = findViewById(R.id.edtEmailProfile)
        edtPhone = findViewById(R.id.edtPhoneProfile)
        edtAddress = findViewById(R.id.edtAddressProfile)
        btnSave = findViewById(R.id.btnSaveProfile)
        btnLogout = findViewById(R.id.btnLogout)
        edtEmail.isEnabled = false
    }

    private fun loadUserData() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java) ?: ""
                    val email = snapshot.child("email").getValue(String::class.java) ?: ""
                    val phone = snapshot.child("phone").getValue(String::class.java) ?: ""
                    val address = snapshot.child("address").getValue(String::class.java) ?: ""

                    edtName.setText(name)
                    edtEmail.setText(email)
                    edtPhone.setText(phone)
                    edtAddress.setText(address)

                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun validateAndSave() {
        val name = edtName.text.toString().trim()
        val phone = edtPhone.text.toString().trim()
        val address = edtAddress.text.toString().trim()

        if (name.isEmpty()) {
            edtName.error = "Họ tên không được để trống"
            return
        }

        if (phone.length < 10 || !phone.all { it.isDigit() }) {
            edtPhone.error = "Số điện thoại không hợp lệ"
            return
        }

        if (address.isEmpty()) {
            edtAddress.error = "Địa chỉ không được để trống"
            return
        }

        val updates = mapOf(
            "name" to name,
            "phone" to phone,
            "address" to address
        )

        updateFirebase(updates)
    }

    private fun updateFirebase(updates: Map<String, Any>) {
        database.updateChildren(updates).addOnSuccessListener {
            Toast.makeText(this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Lỗi: Không thể cập nhật dữ liệu", Toast.LENGTH_SHORT).show()
        }
    }
}