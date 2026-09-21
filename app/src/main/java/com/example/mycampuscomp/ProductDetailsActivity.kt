package com.example.mycampuscomp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mycampuscomp.repository.MarketplaceRepository
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductDetailsActivity : AppCompatActivity() {

    private val repository =
        MarketplaceRepository()

    private var itemId = ""

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_product_details
        )

        itemId =
            intent.getStringExtra(
                "ITEM_ID"
            ) ?: ""

        if (itemId.isEmpty()) {
            finish()
            return
        }

        loadItem()
    }

    private fun loadItem() {

        CoroutineScope(
            Dispatchers.IO
        ).launch {

            try {

                val item =
                    repository.getItem(itemId)

                withContext(
                    Dispatchers.Main
                ) {

                    if (item == null) {

                        Toast.makeText(
                            this@ProductDetailsActivity,
                            "Item no longer exists.",
                            Toast.LENGTH_LONG
                        ).show()

                        finish()

                        return@withContext
                    }

                    findViewById<TextView>(
                        R.id.tvProductTitle
                    ).text = item.title

                    findViewById<TextView>(
                        R.id.tvProductPrice
                    ).text =
                        "R%.2f".format(item.price)

                    findViewById<TextView>(
                        R.id.tvProductDescription
                    ).text =
                        item.description

                    findViewById<TextView>(
                        R.id.tvProductCategory
                    ).text =
                        item.category

                    findViewById<TextView>(
                        R.id.tvProductCondition
                    ).text =
                        item.condition

                    findViewById<TextView>(
                        R.id.tvProductLocation
                    ).text =
                        item.location

                    findViewById<TextView>(
                        R.id.tvSellerName
                    ).text =
                        item.sellerName

                    findViewById<TextView>(
                        R.id.tvSellerEmail
                    ).text =
                        item.sellerEmail

                    findViewById<MaterialButton>(
                        R.id.btnViewSeller
                    ).setOnClickListener {

                        val intent =
                            Intent(
                                this@ProductDetailsActivity,
                                SellerProfileActivity::class.java
                            )

                        intent.putExtra(
                            "SELLER_ID",
                            item.sellerId
                        )

                        intent.putExtra(
                            "SELLER_NAME",
                            item.sellerName
                        )

                        startActivity(intent)
                    }
                }

            } catch (e: Exception) {

                withContext(
                    Dispatchers.Main
                ) {

                    Toast.makeText(
                        this@ProductDetailsActivity,
                        "Unable to load item.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}