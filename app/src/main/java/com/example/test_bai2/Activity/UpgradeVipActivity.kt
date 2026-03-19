package com.example.test_bai2.Activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.*
import com.example.test_bai2.Adapter.PackageAdapter
import com.example.test_bai2.Model.Package
import com.example.test_bai2.R
import com.google.firebase.database.*

class UpgradeVipActivity : AppCompatActivity() {

    private lateinit var billingClient: BillingClient
    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnContinue: Button
    private lateinit var loadProducts: ProgressBar

    private val packageList = ArrayList<Package>()
    private lateinit var adapter: PackageAdapter
    private val productDetailsList = mutableListOf<ProductDetails>()

    private val TAG_BILLING = "BILLING_DEBUG"

    private val userId = "user1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upgrade_vip)

        initViews()
        setupBillingClient()
        loadPackagesFromFirebase()

        btnContinue.setOnClickListener {
            val selected = adapter.getSelectedPackage()
            if (selected != null) {
                val productDetails = productDetailsList.find { it.productId == selected.productId }
                if (productDetails != null) {
                    launchPurchaseFlow(productDetails)
                } else {
                    Log.e(TAG_BILLING, "ProductId chưa sẵn sàng: ${selected.productId}")
                    Toast.makeText(this, "Sản phẩm chưa tải xong từ Store!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Vui lòng chọn 1 gói!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initViews() {
        database = FirebaseDatabase.getInstance().reference
        recyclerView = findViewById(R.id.recyclerPackage)
        btnContinue = findViewById(R.id.btnContinue)
        loadProducts = findViewById(R.id.loadProducts)

        adapter = PackageAdapter(packageList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
                    }
                }
            }
            .enablePendingPurchases()
            .build()

        establishConnection()
    }

    private fun establishConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProducts(BillingClient.ProductType.SUBS)
                    queryProducts(BillingClient.ProductType.INAPP)
                }
            }
            override fun onBillingServiceDisconnected() {
                establishConnection()
            }
        })
    }

    private fun queryProducts(productType: String) {
        val productIds = listOf("basic.test", "com.led.weeklyb", "com.led.monthlyb", "com.led.yearly")

        val productList = productIds.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(productType)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

        billingClient.queryProductDetailsAsync(params) { result, list ->
            runOnUiThread { loadProducts.visibility = View.GONE }
            if (result.responseCode == BillingClient.BillingResponseCode.OK && !list.isNullOrEmpty()) {
                productDetailsList.addAll(list)
                Log.d("TAG", "queryProducts: ${productDetailsList}")
            }
        }
    }

    private fun launchPurchaseFlow(productDetails: ProductDetails) {
        val builder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)

        if (productDetails.productType == BillingClient.ProductType.SUBS) {
            productDetails.subscriptionOfferDetails?.get(0)?.offerToken?.let {
                builder.setOfferToken(it)
            }
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(builder.build()))
            .build()

        billingClient.launchBillingFlow(this, flowParams)
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(acknowledgeParams) { result ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        runOnUiThread {
                            giveUserBenefits(purchase.products[0])
                        }
                    }
                }
            }
        }
    }

    private fun giveUserBenefits(productId: String) {
        database.child("packages").orderByChild("productId").equalTo(productId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (data in snapshot.children) {
                            val coinBonus = data.child("benefits/coin").getValue(Int::class.java) ?: 0
                            val voucherBonus = data.child("benefits/vouchers")

                            updateUserProfile(userId, coinBonus, voucherBonus)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("DATABASE_ERROR", error.message)
                }
            })
    }

    private fun updateUserProfile(
        uId: String,
        coinBonus: Int,
        vouchersSnapshot: DataSnapshot
    ) {
        val userRef = database.child("users").child(uId)

        userRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {

                // ✅ cộng coin
                val currentCoins = currentData.child("coin").getValue(Int::class.java) ?: 0
                currentData.child("coin").value = currentCoins + coinBonus

                // ✅ cộng voucher
                for (voucherSnap in vouchersSnapshot.children) {

                    val id = voucherSnap.child("id").getValue(String::class.java) ?: continue
                    val title = voucherSnap.child("title").getValue(String::class.java) ?: ""
                    val type = voucherSnap.child("type").getValue(String::class.java) ?: ""
                    val minOrder = voucherSnap.child("minOrder").getValue(Long::class.java) ?: 0L
                    val value = voucherSnap.child("value").getValue(Long::class.java) ?: 0L

                    val userVoucherRef = currentData.child("my_vouchers").child(id)

                    val currentQty = userVoucherRef.child("quantity").getValue(Int::class.java) ?: 0

                    userVoucherRef.child("quantity").value = currentQty + 1
                    userVoucherRef.child("title").value = title
                    userVoucherRef.child("type").value = type
                    userVoucherRef.child("minOrder").value = minOrder
                    userVoucherRef.child("value").value = value
                }

                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (committed) {
                    Toast.makeText(this@UpgradeVipActivity, "Thanh toán thành công! Đã cộng xu + voucher", Toast.LENGTH_LONG).show()
                } else {
                    Log.e("TRANSACTION_FAIL", error?.message ?: "Unknown error")
                }
            }
        })
    }

    private fun loadPackagesFromFirebase() {
        database.child("packages").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return

                packageList.clear()
                for (s in snapshot.children) {
                    val name = s.child("name").value?.toString() ?: "Gói không tên"
                    val price = s.child("price").getValue(Int::class.java) ?: 0
                    val productId = s.child("productId").value?.toString() ?: ""

                    val coin = s.child("benefits/coin").getValue(Int::class.java) ?: 0
                    val descriptionBuilder = StringBuilder()

                    if (coin > 0) {
                        descriptionBuilder.append("Tặng $coin xu")
                    }

                    val vouchersSnapshot = s.child("benefits/vouchers")
                    if (vouchersSnapshot.exists()) {
                        for (vDoc in vouchersSnapshot.children) {
                            val title = vDoc.child("title").value?.toString() ?: ""
                            if (title.isNotEmpty()) {
                                if (descriptionBuilder.isNotEmpty()) {
                                    descriptionBuilder.append(" + ")
                                }
                                descriptionBuilder.append(title)
                            }
                        }
                    }

                    val finalDescription = descriptionBuilder.toString().ifEmpty { "Ưu đãi đặc biệt" }
                    packageList.add(Package(name, price, finalDescription, productId))
                }

                runOnUiThread {
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FIREBASE", error.message)
            }
        })
    }
}