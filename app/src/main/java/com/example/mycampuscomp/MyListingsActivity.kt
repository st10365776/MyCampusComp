package com.example.mycampuscomp

import android.content.Intent
import android.os.Bundle
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

class MyListingsActivity : AppCompatActivity() {

    private val repository =
        MarketplaceRepository()

    private lateinit var adapter:
            MarketplaceAdapter

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_my_listings
        )

        val recycler =
            findViewById<RecyclerView>(
                R.id.recyclerMyListings
            )

        adapter =
            MarketplaceAdapter(
                mutableListOf()
            ) { item ->

                val intent =
                    Intent(
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

        loadListings()
    }

    private fun loadListings() {

        CoroutineScope(
            Dispatchers.IO
        ).launch {

            try {

                val listings =
                    repository.getMyItems()

                withContext(
                    Dispatchers.Main
                ) {

                    adapter.updateItems(
                        listings
                    )
                }

            } catch (e: Exception) {

                withContext(
                    Dispatchers.Main
                ) {

                    Toast.makeText(
                        this@MyListingsActivity,
                        "Unable to load your listings.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (::adapter.isInitialized) {
            loadListings()
        }
    }
}