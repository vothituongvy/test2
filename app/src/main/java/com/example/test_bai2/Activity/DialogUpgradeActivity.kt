package com.example.test_bai2.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.test_bai2.R
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DialogUpgradeActivity : AppCompatActivity() {
    private var rewardedAd: RewardedAd? = null
    private val TAG = "Dialog"
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseDatabase.getInstance().reference
        showPopup()
    }
    private fun showPopup() {

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_upgrade)

        dialog.setCancelable(true)
        MobileAds.initialize(this)
        val btnClose = dialog.findViewById<ImageView>(R.id.btnClose)
        val btnPremium = dialog.findViewById<Button>(R.id.btnPremium)
        val btnAds = dialog.findViewById<Button>(R.id.btnContinue)

        btnClose.setOnClickListener {
            val intent = Intent( this,MainActivity::class.java)
            startActivity(intent)
        }

        btnPremium.setOnClickListener {
            val intent = Intent( this, UpgradeVipActivity::class.java)
            startActivity(intent)
        }

        btnAds.setOnClickListener {
            loadRewardAd()
        }

        dialog.show()
    }
    private fun addCoin() {

        val userId = "user1"

        database.child("users").child(userId).child("coin")
            .get().addOnSuccessListener {

                var coin = it.value.toString().toInt()
                coin++
                database.child("users").child(userId).child("coin").setValue(coin)
            }
    }
    private fun loadRewardAd() {

        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            this,
            "ca-app-pub-3940256099942544/5224354917",
            adRequest,
            object : RewardedAdLoadCallback() {

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Ad loaded")
                    rewardedAd = ad

                    rewardedAd?.fullScreenContentCallback =
                        object : FullScreenContentCallback() {

                            override fun onAdDismissedFullScreenContent() {
                                Log.d(TAG, "Ad dismissed")
                                rewardedAd = null
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                Log.d(TAG, "Ad failed to show")
                                rewardedAd = null
                            }

                            override fun onAdShowedFullScreenContent() {
                                Log.d(TAG, "Ad showed fullscreen")
                            }

                            override fun onAdClicked() {
                                Log.d(TAG, "Ad clicked")
                            }

                            override fun onAdImpression() {
                                Log.d(TAG, "Ad impression recorded")
                            }
                        }

                    showRewardAd()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.d(TAG, "Ad failed to load: ${loadAdError.message}")
                    rewardedAd = null
                }
            }
        )
    }
    private fun showRewardAd() {
        if (rewardedAd != null) {
            rewardedAd?.show(
                this,
                OnUserEarnedRewardListener { rewardItem: RewardItem ->
                    addCoin()

                }
            )

        } else {
            Log.d(TAG, "Ad not ready")
        }
    }
}