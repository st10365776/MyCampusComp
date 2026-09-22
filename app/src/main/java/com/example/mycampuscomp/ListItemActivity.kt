package com.example.mycampuscomp

import android.os.Bundle
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mycampuscomp.model.MarketplaceItem
import com.example.mycampuscomp.repository.MarketplaceRepository
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListItemActivity : AppCompatActivity() {

    private val repository = MarketplaceRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_list_item)

        val title = findViewById<EditText>(
            R.id.etItemTitle
        )

        val description = findViewById<EditText>(
            R.id.etItemDescription
        )

        val price = findViewById<EditText>(
            R.id.etItemPrice
        )

        val imageUrl = findViewById<EditText>(
            R.id.etItemImage
        )

        val location = findViewById<EditText>(
            R.id.etItemLocation
        )

        val category = findViewById<Spinner>(
            R.id.spinnerCategory
        )

        val condition = findViewById<Spinner>(
            R.id.spinnerCondition
        )

        val publishButton = findViewById<MaterialButton>(
            R.id.btnPublishItem
        )

        publishButton.setOnClickListener {

            val titleText =
                title.text.toString().trim()

            val descriptionText =
                description.text.toString().trim()

            val priceText =
                price.text.toString().trim()

            val imageText =
                imageUrl.text.toString().trim()

            val locationText =
                location.text.toString().trim()

            if (
                titleText.isEmpty() ||
                descriptionText.isEmpty() ||
                priceText.isEmpty()
            ) {

                Toast.makeText(
                    this,
                    "Please complete the required fields.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val priceValue =
                priceText.toDoubleOrNull()

            if (priceValue == null) {

                Toast.makeText(
                    this,
                    "Please enter a valid price.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val item = MarketplaceItem(

                title = titleText,

                description = descriptionText,

                price = priceValue,

                category =
                    category.selectedItem.toString(),

                condition =
                    condition.selectedItem.toString(),

                imageUrl = imageText,

                location = locationText,

                status = "Available"
            )

            publishItem(item)
        }
    }

    private fun publishItem(
        item: MarketplaceItem
    ) {

        CoroutineScope(
            Dispatchers.IO
        ).launch {

            try {

                repository.addItem(item)

                withContext(
                    Dispatchers.Main
                ) {

                    Toast.makeText(
                        this@ListItemActivity,
                        "Your item has been listed!",
                        Toast.LENGTH_LONG
                    ).show()

                    finish()
                }

            } catch (e: Exception) {

                withContext(
                    Dispatchers.Main
                ) {

                    Toast.makeText(
                        this@ListItemActivity,
                        e.message
                            ?: "Unable to publish item.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}