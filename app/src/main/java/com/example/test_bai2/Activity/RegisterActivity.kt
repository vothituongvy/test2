package com.example.test_bai2.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.test_bai2.Utils.HashHelper
import com.example.test_bai2.databinding.RegisterActivityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: RegisterActivityBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().getReference("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegisterActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            if (validateData()) {
                performRegister()
            }
        }

    }

    private fun validateData(): Boolean {
        val email = binding.edtEmail.text.toString().trim()
        val pass = binding.edtPassword.text.toString().trim()
        val confirmPass = binding.edtConfirm.text.toString().trim()

        var isValid = true

        if (email.isEmpty()) {
            binding.edtEmail.error = "Email không được để trống"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edtEmail.error = "Định dạng Email không hợp lệ"
            isValid = false
        }

        if (pass.isEmpty()) {
            binding.edtPassword.error = "Mật khẩu không được để trống"
            isValid = false
        } else if (pass.length < 6) {
            binding.edtPassword.error = "Mật khẩu phải từ 6 ký tự trở lên"
            isValid = false
        }

        if (confirmPass.isEmpty()) {
            binding.edtConfirm.error = "Vui lòng xác nhận lại mật khẩu"
            isValid = false
        } else if (pass != confirmPass) {
            binding.edtConfirm.error = "Mật khẩu nhập lại không khớp"
            isValid = false
        }

        return isValid
    }

    private fun performRegister() {
        val email = binding.edtEmail.text.toString().trim()
        val pass = binding.edtPassword.text.toString().trim()

        binding.btnRegister.isEnabled = false

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { res ->
                val uid = res.user?.uid ?: ""
                val hashedPass = HashHelper.hashPassword(pass)
                val userMap = mapOf(
                    "uid" to uid,
                    "email" to email,
                    "password" to hashedPass,
                    "coin" to 0,
                    "lastLogin" to System.currentTimeMillis()
                )

                db.child(uid).setValue(userMap)
                    .addOnSuccessListener {
                        auth.signOut()

                        Toast.makeText(
                            this,
                            "Đăng ký thành công! Hãy đăng nhập.",
                            Toast.LENGTH_LONG
                        ).show()

                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        binding.btnRegister.isEnabled = true
                        Toast.makeText(this, "Lỗi lưu dữ liệu: ${e.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
            }
            .addOnFailureListener { e ->
                binding.btnRegister.isEnabled = true
                if (e.message?.contains("already in use") == true) {
                    binding.edtEmail.error = "Email này đã được sử dụng!"
                } else {
                    Toast.makeText(this, "Đăng ký thất bại: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}