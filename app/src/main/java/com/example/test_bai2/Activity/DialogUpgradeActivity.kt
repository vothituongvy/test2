package com.example.test_bai2.Activity

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.test_bai2.R
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DialogUpgradeActivity : AppCompatActivity() {
    private var rewardedAd: RewardedAd? = null
    private val TAG = "DialogUpgrade"
    private lateinit var database: DatabaseReference
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (auth.currentUser == null) {
            finish()
            return
        }

        database = FirebaseDatabase.getInstance().reference
        MobileAds.initialize(this)

        setupBackHandler()

        showPopup()
    }

    private fun setupBackHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backToMain()
            }
        })
    }

    private fun backToMain() {
        val intent = Intent(this, MainActivity::class.java)
        // Dùng cờ này để quay lại trang MainActivity, ko mở chồng nhiều trang
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    private fun showPopup() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_upgrade)

        dialog.setOnCancelListener {
            backToMain()
        }

        val btnClose = dialog.findViewById<ImageView>(R.id.btnClose)
        val btnPremium = dialog.findViewById<Button>(R.id.btnPremium)
        val btnAds = dialog.findViewById<Button>(R.id.btnContinue)

        btnClose.setOnClickListener {
            dialog.dismiss()
            backToMain()
        }

        btnPremium.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, UpgradeVipActivity::class.java))
        }

        btnAds.setOnClickListener {
            Toast.makeText(this, "Đang tải quảng cáo...", Toast.LENGTH_SHORT).show()
            loadRewardAd()
        }

        dialog.show()
    }

    private fun addCoin() {
        val uId = auth.currentUser?.uid ?: return
        val userCoinRef = database.child("users").child(uId).child("coin")

        userCoinRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val currentCoins = currentData.getValue(Long::class.java) ?: 0L
                currentData.value = currentCoins + 1
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (committed) {
                    runOnUiThread {
                        Toast.makeText(this@DialogUpgradeActivity, "Bạn đã nhận được 1 Xu!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(TAG, "Transaction failed: ${error?.message}")
                }
            }
        })
    }

    private fun loadRewardAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            this,
            "ca-app-pub-3940256099942544/5224354917", // Test ID
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            rewardedAd = null
                        }
                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            rewardedAd = null
                        }
                    }
                    showRewardAd()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    Toast.makeText(this@DialogUpgradeActivity, "Quảng cáo chưa sẵn sàng!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun showRewardAd() {
        rewardedAd?.let { ad ->
            ad.show(this) { rewardItem ->
                addCoin()
            }
        }
    }
}