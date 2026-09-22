package com.example.mycampuscomp

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mycampuscomp.adapter.MarketplaceAdapter
import com.example.mycampuscomp.repository.MarketplaceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SellerProfileActivity : AppCompatActivity() {

    private val repository =
        MarketplaceRepository()

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_seller_profile
        )

        val sellerId =
            intent.getStringExtra(
                "SELLER_ID"
            ) ?: ""

        val sellerName =
            intent.getStringExtra(
                "SELLER_NAME"
            ) ?: "Student"

        findViewById<TextView>(
            R.id.tvSellerProfileName
        ).text = sellerName

        val recycler =
            findViewById<RecyclerView>(
                R.id.recyclerSellerItems
            )

        val adapter =
            MarketplaceAdapter(
                mutableListOf()
            ) { item ->

                val intent =
                    android.content.Intent(
                        this,
                        ProductDetailsActivity::class.java
                    )

                intent.putExtra(
                    "ITEM_ID",
                    item.id
                )

                startActivity(intent)
            }

        recycler.layoutManager =
            GridLayoutManager(
                this,
                2
            )

        recycler.adapter =
            adapter

        CoroutineScope(
            Dispatchers.IO
        ).launch {

            try {

                val items =
                    repository.getSellerItems(
                        sellerId
                    )

                withContext(
                    Dispatchers.Main
                ) {

                    adapter.updateItems(
                        items
                    )
                }

            } catch (e: Exception) {

                withContext(
                    Dispatchers.Main
                ) {

                    Toast.makeText(
                        this@SellerProfileActivity,
                        "Unable to load seller.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}