package com.example.test_bai2.Activity


import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.test_bai2.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var edtEmailForgot: EditText
    private lateinit var btnSendRequest: MaterialButton
    private lateinit var btnBackToLogin: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        auth = FirebaseAuth.getInstance()

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        edtEmailForgot = findViewById(R.id.edtEmailForgot)
        btnSendRequest = findViewById(R.id.btnSendRequest)
        btnBackToLogin = findViewById(R.id.btnBackToLogin)
    }

    private fun setupClickListeners() {
        btnSendRequest.setOnClickListener {
            val email = edtEmailForgot.text.toString().trim()

            if (validateEmail(email)) {
                sendResetPasswordEmail(email)
            }
        }

        btnBackToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun validateEmail(email: String): Boolean {
        if (email.isEmpty()) {
            edtEmailForgot.error = "Vui lòng nhập email"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmailForgot.error = "Định dạng email không hợp lệ"
            return false
        }
        return true
    }

    private fun sendResetPasswordEmail(email: String) {
        btnSendRequest.isEnabled = false

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                btnSendRequest.isEnabled = true
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Link đặt lại mật khẩu đã được gửi vào Email của bạn!",
                        Toast.LENGTH_LONG
                    ).show()


                    edtEmailForgot.postDelayed({
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }, 2000)

                } else {
                    Toast.makeText(
                        this,
                        "Lỗi: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}