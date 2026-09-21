package com.example.mycampuscomp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mycampuscomp.adapter.MarketplaceAdapter
import com.example.mycampuscomp.model.MarketplaceItem
import com.example.mycampuscomp.repository.MarketplaceRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MarketplaceActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MarketplaceAdapter
    private lateinit var searchInput: EditText

    private val repository =
        MarketplaceRepository()

    private var allItems =
        mutableListOf<MarketplaceItem>()

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_marketplace
        )

        recyclerView =
            findViewById(R.id.recyclerMarketplace)

        searchInput =
            findViewById(R.id.etSearchMarketplace)

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

        recyclerView.layoutManager =
            GridLayoutManager(this, 2)

        recyclerView.adapter = adapter

        findViewById<MaterialButton>(
            R.id.btnListItem
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    ListItemActivity::class.java
                )
            )
        }

        findViewById<MaterialButton>(
            R.id.btnMyListings
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    MyListingsActivity::class.java
                )
            )
        }

        searchInput.setOnEditorActionListener {
                _, _, _ ->

            searchItems(
                searchInput.text.toString()
            )

            false
        }

        setupBottomNavigation()

        loadItems()
    }

    private fun loadItems() {

        CoroutineScope(
            Dispatchers.IO
        ).launch {

            try {

                val items =
                    repository.getAllItems()

                withContext(
                    Dispatchers.Main
                ) {

                    allItems.clear()
                    allItems.addAll(items)

                    adapter.updateItems(
                        allItems
                    )
                }

            } catch (e: Exception) {

                withContext(
                    Dispatchers.Main
                ) {

                    Toast.makeText(
                        this@MarketplaceActivity,
                        "Unable to load marketplace.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun searchItems(
        query: String
    ) {

        val filtered =
            if (query.isBlank()) {

                allItems

            } else {

                allItems.filter {

                    it.title.contains(
                        query,
                        ignoreCase = true
                    ) ||
                            it.category.contains(
                                query,
                                ignoreCase = true
                            ) ||
                            it.description.contains(
                                query,
                                ignoreCase = true
                            )
                }
            }

        adapter.updateItems(
            filtered
        )
    }

    private fun setupBottomNavigation() {

        val navigation =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigation
            )

        navigation.selectedItemId =
            R.id.nav_marketplace

        navigation.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_home -> {

                    startActivity(
                        Intent(
                            this,
                            DashboardActivity::class.java
                        )
                    )

                    finish()

                    true
                }

                R.id.nav_timetable -> {

                    startActivity(
                        Intent(
                            this,
                            TimetableActivity::class.java
                        )
                    )

                    finish()

                    true
                }

                R.id.nav_map -> {

                    startActivity(
                        Intent(
                            this,
                            MainActivity::class.java
                        )
                    )

                    finish()

                    true
                }

                R.id.nav_assignments -> {

                    startActivity(
                        Intent(
                            this,
                            AssignmentsActivity::class.java
                        )
                    )

                    finish()

                    true
                }

                R.id.nav_marketplace -> true

                R.id.nav_ai -> {

                    startActivity(
                        Intent(
                            this,
                            AIStudyAssistantActivity::class.java
                        )
                    )

                    true
                }

                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (::adapter.isInitialized) {
            loadItems()
        }
    }
}