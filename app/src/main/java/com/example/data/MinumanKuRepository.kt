package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

class MinumanKuRepository(private val database: AppDatabase) {
    private val userDao = database.userDao()
    private val productDao = database.productDao()
    private val saleDao = database.saleDao()
    private val stockLogDao = database.stockLogDao()

    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allSales: Flow<List<Sale>> = saleDao.getAllSales()
    val allStockLogs: Flow<List<StockLog>> = stockLogDao.getAllStockLogs()

    // Setup default admin account if count is 0
    suspend fun checkAndSeedAdmin() = withContext(Dispatchers.IO) {
        if (userDao.getUserCount() == 0) {
            val hashedPassword = hashPassword("password123")
            userDao.insertUser(
                User(
                    username = "admin",
                    passwordHash = hashedPassword
                )
            )
        }
    }

    suspend fun login(username: String, pword: String): User? = withContext(Dispatchers.IO) {
        val user = userDao.getUserByUsername(username) ?: return@withContext null
        val hashedInput = hashPassword(pword)
        if (user.passwordHash == hashedInput) {
            user
        } else {
            null
        }
    }

    suspend fun changePassword(username: String, newPword: String): Boolean = withContext(Dispatchers.IO) {
        val user = userDao.getUserByUsername(username) ?: return@withContext false
        val updatedUser = user.copy(passwordHash = hashPassword(newPword))
        userDao.updateUser(updatedUser)
        true
    }

    // Product CRUD
    suspend fun addProduct(name: String, category: String, costPrice: Double, sellingPrice: Double, stock: Int, unit: String) = withContext(Dispatchers.IO) {
        val product = Product(
            name = name,
            category = category,
            costPrice = costPrice,
            sellingPrice = sellingPrice,
            stock = stock,
            unit = unit
        )
        productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: Product) = withContext(Dispatchers.IO) {
        productDao.updateProduct(product)
    }

    suspend fun softDeleteProduct(productId: String) = withContext(Dispatchers.IO) {
        val product = productDao.getProductById(productId)
        if (product != null) {
            productDao.updateProduct(product.copy(isDeleted = true))
        }
    }

    // Sales Transaction (Business Rules flow)
    suspend fun recordSale(productId: String, qty: Int, overrideSellingPrice: Double? = null) = withContext(Dispatchers.IO) {
        val product = productDao.getProductById(productId) ?: throw Exception("Produk tidak ditemukan")
        if (product.stock < qty) {
            throw Exception("Stok tidak cukup! (Stok: ${product.stock} ${product.unit})")
        }

        val finalSellingPrice = overrideSellingPrice ?: product.sellingPrice
        val total = qty * finalSellingPrice
        val costTotal = qty * product.costPrice
        val profit = total - costTotal

        // Update Stock
        val updatedProduct = product.copy(stock = product.stock - qty)
        productDao.updateProduct(updatedProduct)

        // Store Sale
        val sale = Sale(
            productId = productId,
            qty = qty,
            sellingPrice = finalSellingPrice,
            total = total,
            profit = profit
        )
        saleDao.insertSale(sale)
    }

    // Stock Management (Goods Inflow)
    suspend fun addStock(productId: String, qtyAdded: Int, supplier: String) = withContext(Dispatchers.IO) {
        val product = productDao.getProductById(productId) ?: throw Exception("Produk tidak ditemukan")
        
        // Update product stock: stok_baru = stok_lama + qty_added
        val updatedProduct = product.copy(stock = product.stock + qtyAdded)
        productDao.updateProduct(updatedProduct)

        // Create log entry
        val log = StockLog(
            productId = productId,
            qtyAdded = qtyAdded,
            supplier = supplier
        )
        stockLogDao.insertStockLog(log)
    }

    // Helper: SHA-256 Hashing for PRD requirements
    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}
