package com.example.test_bai2.Activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.test_bai2.Activity.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirebaseDatabase.getInstance().getReference("users").child(user.uid).get()
                .addOnSuccessListener { snapshot ->
                    val lastLogin = snapshot.child("lastLogin").getValue(Long::class.java) ?: 0L
                    val sixtyDays = 60L * 24 * 60 * 60 * 1000

                    if (System.currentTimeMillis() - lastLogin > sixtyDays) {
                        FirebaseAuth.getInstance().signOut()
                        startActivity(Intent(this, LoginActivity::class.java))
                    } else {
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                    finish()
                }
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}