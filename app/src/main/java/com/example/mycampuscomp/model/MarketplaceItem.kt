package com.example.mycampuscomp.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class MarketplaceItem(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var price: Double = 0.0,
    var category: String = "",
    var condition: String = "",
    var imageUrl: String = "",
    var location: String = "",
    var sellerId: String = "",
    var sellerName: String = "",
    var sellerEmail: String = "",
    var status: String = "Available",

    @ServerTimestamp
    var createdAt: Date? = null
)