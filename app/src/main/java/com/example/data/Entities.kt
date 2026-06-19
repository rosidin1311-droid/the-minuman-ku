package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val username: String,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val costPrice: Double,
    val sellingPrice: Double,
    val stock: Int,
    val unit: String, // "botol", "pcs", "cup", "pack"
    val createdAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val productId: String,
    val qty: Int,
    val sellingPrice: Double,
    val total: Double,
    val profit: Double,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "stock_logs")
data class StockLog(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val productId: String,
    val qtyAdded: Int,
    val supplier: String,
    val createdAt: Long = System.currentTimeMillis()
)
