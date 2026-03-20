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
// email app : vttvy10122004@gmail.com
// password: Vovy1012!
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
        binding.txtForgot.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
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
        binding.btnLogin.isEnabled = false

        auth.signInWithEmailAndPassword(email, pass).addOnSuccessListener { authResult ->
            val uid = authResult.user?.uid
            if (uid != null) {
                db.child(uid).get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                            val updates = mapOf(
                                "fcmToken" to token,
                                "lastLogin" to System.currentTimeMillis(),
                                "password" to HashHelper.hashPassword(pass)
                            )

                            snapshot.ref.updateChildren(updates).addOnSuccessListener {
                                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                        }
                    }
                }
            }
        }.addOnFailureListener { e ->
            binding.btnLogin.isEnabled = true
            Toast.makeText(this, "Lỗi đăng nhập: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}