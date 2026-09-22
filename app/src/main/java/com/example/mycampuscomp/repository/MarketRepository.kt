package com.example.mycampuscomp.repository

import com.example.mycampuscomp.model.MarketplaceItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class MarketplaceRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val marketplaceCollection =
        firestore.collection("marketplaceItems")

    suspend fun getAllItems(): List<MarketplaceItem> {

        val snapshot = marketplaceCollection
            .whereEqualTo("status", "Available")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { document ->

            document.toObject(MarketplaceItem::class.java)?.apply {
                id = document.id
            }
        }
    }

    suspend fun getItemsByCategory(
        category: String
    ): List<MarketplaceItem> {

        val snapshot = marketplaceCollection
            .whereEqualTo("category", category)
            .whereEqualTo("status", "Available")
            .get()
            .await()

        return snapshot.documents.mapNotNull { document ->

            document.toObject(MarketplaceItem::class.java)?.apply {
                id = document.id
            }
        }
    }

    suspend fun getItem(
        itemId: String
    ): MarketplaceItem? {

        val document =
            marketplaceCollection
                .document(itemId)
                .get()
                .await()

        return document.toObject(
            MarketplaceItem::class.java
        )?.apply {
            id = document.id
        }
    }

    suspend fun getMyItems(): List<MarketplaceItem> {

        val uid = auth.currentUser?.uid
            ?: return emptyList()

        val snapshot =
            firestore
                .collection("users")
                .document(uid)
                .collection("marketplaceItems")
                .orderBy(
                    "createdAt",
                    Query.Direction.DESCENDING
                )
                .get()
                .await()

        return snapshot.documents.mapNotNull { document ->

            document.toObject(
                MarketplaceItem::class.java
            )?.apply {
                id = document.id
            }
        }
    }

    suspend fun getSellerItems(
        sellerId: String
    ): List<MarketplaceItem> {

        val snapshot =
            marketplaceCollection
                .whereEqualTo("sellerId", sellerId)
                .whereEqualTo("status", "Available")
                .get()
                .await()

        return snapshot.documents.mapNotNull { document ->

            document.toObject(
                MarketplaceItem::class.java
            )?.apply {
                id = document.id
            }
        }
    }

    suspend fun addItem(
        item: MarketplaceItem
    ): String {

        val user =
            auth.currentUser
                ?: throw Exception("You must be logged in.")

        val userDocument =
            firestore
                .collection("users")
                .document(user.uid)
                .get()
                .await()

        val userName =
            userDocument.getString("name")
                ?: user.displayName
                ?: "Student"

        val finalItem = item.copy(
            sellerId = user.uid,
            sellerName = userName,
            sellerEmail = user.email ?: "",
            status = "Available"
        )

        val document =
            marketplaceCollection
                .document()

        document
            .set(finalItem)
            .await()

        firestore
            .collection("users")
            .document(user.uid)
            .collection("marketplaceItems")
            .document(document.id)
            .set(finalItem.copy(id = document.id))
            .await()

        return document.id
    }

    suspend fun updateItem(
        item: MarketplaceItem
    ) {

        val uid =
            auth.currentUser?.uid
                ?: throw Exception("Not logged in.")

        if (item.sellerId != uid) {
            throw Exception(
                "You can only edit your own listings."
            )
        }

        marketplaceCollection
            .document(item.id)
            .set(item)
            .await()

        firestore
            .collection("users")
            .document(uid)
            .collection("marketplaceItems")
            .document(item.id)
            .set(item)
            .await()
    }

    suspend fun deleteItem(
        itemId: String
    ) {

        val uid =
            auth.currentUser?.uid
                ?: throw Exception("Not logged in.")

        marketplaceCollection
            .document(itemId)
            .delete()
            .await()

        firestore
            .collection("users")
            .document(uid)
            .collection("marketplaceItems")
            .document(itemId)
            .delete()
            .await()
    }

    suspend fun markAsSold(
        itemId: String
    ) {

        val uid =
            auth.currentUser?.uid
                ?: throw Exception("Not logged in.")

        val update = mapOf(
            "status" to "Sold"
        )

        marketplaceCollection
            .document(itemId)
            .update(update)
            .await()

        firestore
            .collection("users")
            .document(uid)
            .collection("marketplaceItems")
            .document(itemId)
            .update(update)
            .await()
    }
}