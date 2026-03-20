package com.example.test_bai2.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.test_bai2.Utils.HashHelper
import com.example.test_bai2.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().getReference("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            if (validateInput()) {
                loginUser()
            }
        }

        binding.txtRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInput(): Boolean {
        val email = binding.edtEmail.text.toString().trim()
        val pass = binding.edtPassword.text.toString().trim()
        var isValid = true

        binding.edtEmail.error = null
        binding.tilPassword.error = null

        if (email.isEmpty()) {
            binding.edtEmail.error = "Vui lòng nhập Email"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edtEmail.error = "Định dạng Email không hợp lệ"
            isValid = false
        }

        if (pass.isEmpty()) {
            binding.tilPassword.error = "Vui lòng nhập mật khẩu"
            isValid = false
        } else if (pass.length < 6) {
            binding.tilPassword.error = "Mật khẩu phải từ 6 ký tự"
            isValid = false
        }

        return isValid
    }

    private fun loginUser() {
        val email = binding.edtEmail.text.toString().trim()
        val pass = binding.edtPassword.text.toString().trim()

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }


        db.orderByChild("email").equalTo(email).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val userSnap = snapshot.children.first()
                val hashedPass = userSnap.child("password").value.toString()

                if (HashHelper.checkPassword(pass, hashedPass)) {
                    auth.signInWithEmailAndPassword(email, pass).addOnSuccessListener {
                        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                            val updates = mapOf(
                                "fcmToken" to token,
                                "lastLogin" to System.currentTimeMillis()
                            )
                            userSnap.ref.updateChildren(updates).addOnSuccessListener {
                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                        }
                    }.addOnFailureListener { e ->
                        Toast.makeText(this, "Lỗi xác thực: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Sai mật khẩu!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Tài khoản không tồn tại!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Lỗi kết nối database!", Toast.LENGTH_SHORT).show()
        }
    }
}