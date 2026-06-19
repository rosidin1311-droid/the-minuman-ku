package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class Screen {
    Splash,
    Login,
    Dashboard,
    Products,
    Sales,
    Stock,
    Reports,
    Settings
}

class MinumanKuViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MinumanKuRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MinumanKuRepository(database)
        
        // Seed default admin and initial drinks if database is brand new
        viewModelScope.launch {
            repository.checkAndSeedAdmin()
            seedInitialProductsIfEmpty()
        }
    }

    // Auth state
    private val _loggedInUser = MutableStateFlow<User?>(null)
    val loggedInUser: StateFlow<User?> = _loggedInUser.asStateFlow()

    private val _currentScreen = MutableStateFlow(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Navigation history list for standard hardware back / UI back management
    private val _navHistory = MutableStateFlow<List<Screen>>(emptyList())
    val navHistory: StateFlow<List<Screen>> = _navHistory.asStateFlow()

    fun navigateTo(screen: Screen) {
        if (screen == Screen.Splash) {
            _navHistory.value = emptyList()
        } else {
            val current = _currentScreen.value
            if (current != Screen.Splash && current != Screen.Login) {
                _navHistory.value = _navHistory.value + current
            }
        }
        _currentScreen.value = screen
    }

    fun navigateBack() {
        val currentHistory = _navHistory.value
        if (currentHistory.isNotEmpty()) {
            val lastScreen = currentHistory.last()
            _navHistory.value = currentHistory.dropLast(1)
            _currentScreen.value = lastScreen
        } else {
            _currentScreen.value = Screen.Dashboard
        }
    }

    // Flows from database
    val products = repository.allProducts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val sales = repository.allSales.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val stockLogs = repository.allStockLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Dynamic Low-Stock Products (stok <= 5)
    val lowStockProducts = products.map { list ->
        list.filter { it.stock <= 5 }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Low stock notifications
    val notifications = lowStockProducts.map { list ->
        list.map { "Stok ${it.name} tinggal ${it.stock} ${it.unit}" }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Login Form State
    val loginUsername = MutableStateFlow("")
    val loginPassword = MutableStateFlow("")
    private val _loginError = MutableStateFlow<String?>(null)
    val loginError = _loginError.asStateFlow()

    fun performLogin(onSuccess: () -> Unit) {
        val username = loginUsername.value.trim()
        val password = loginPassword.value

        if (username.isEmpty() || password.isEmpty()) {
            _loginError.value = "Username dan password wajib diisi"
            return
        }
        if (password.length < 6) {
            _loginError.value = "Password minimal 6 karakter"
            return
        }

        viewModelScope.launch {
            val user = repository.login(username, password)
            if (user != null) {
                _loggedInUser.value = user
                _loginError.value = null
                onSuccess()
            } else {
                _loginError.value = "Username atau password salah"
            }
        }
    }

    fun performLogout() {
        _loggedInUser.value = null
        loginUsername.value = ""
        loginPassword.value = ""
        _navHistory.value = emptyList()
        _currentScreen.value = Screen.Login
    }

    // Change Password Form State
    val oldPasswordState = MutableStateFlow("")
    val newPasswordState = MutableStateFlow("")
    val confirmNewPasswordState = MutableStateFlow("")
    private val _changePasswordToast = MutableStateFlow<String?>(null)
    val changePasswordToast = _changePasswordToast.asStateFlow()

    fun performChangePassword() {
        val curUser = _loggedInUser.value ?: return
        val oldP = oldPasswordState.value
        val newP = newPasswordState.value
        val confP = confirmNewPasswordState.value

        if (newP.length < 6) {
            _changePasswordToast.value = "Password baru minimal 6 karakter"
            return
        }
        if (newP != confP) {
            _changePasswordToast.value = "Konfirmasi password tidak cocok"
            return
        }

        viewModelScope.launch {
            val checkUser = repository.login(curUser.username, oldP)
            if (checkUser == null) {
                _changePasswordToast.value = "Password lama salah"
                return@launch
            }

            val success = repository.changePassword(curUser.username, newP)
            if (success) {
                _changePasswordToast.value = "Password berhasil diubah!"
                oldPasswordState.value = ""
                newPasswordState.value = ""
                confirmNewPasswordState.value = ""
            } else {
                _changePasswordToast.value = "Gagal mengubah password"
            }
        }
    }

    fun clearChangePasswordToast() {
        _changePasswordToast.value = null
    }

    // Product Search and Category Selection State
    val productSearchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow<String?>("Semua") // "Semua" or specific category

    val filteredProducts = combine(
        products,
        productSearchQuery,
        selectedCategoryFilter
    ) { prodList, query, filterCategory ->
        prodList.filter { product ->
            val matchesQuery = product.name.contains(query, ignoreCase = true) || 
                               product.category.contains(query, ignoreCase = true)
            val matchesCategory = filterCategory == null || filterCategory == "Semua" || product.category == filterCategory
            matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Product Add/Edit Form State
    val editingProduct = MutableStateFlow<Product?>(null)
    val productNameInput = MutableStateFlow("")
    val productCategoryInput = MutableStateFlow("Teh")
    val productCostInput = MutableStateFlow("")
    val productSellingInput = MutableStateFlow("")
    val productStockInput = MutableStateFlow("")
    val productUnitInput = MutableStateFlow("botol")

    private val _productFormError = MutableStateFlow<String?>(null)
    val productFormError = _productFormError.asStateFlow()

    fun resetProductForm(product: Product? = null) {
        editingProduct.value = product
        if (product != null) {
            productNameInput.value = product.name
            productCategoryInput.value = product.category
            productCostInput.value = product.costPrice.toInt().toString()
            productSellingInput.value = product.sellingPrice.toInt().toString()
            productStockInput.value = product.stock.toString()
            productUnitInput.value = product.unit
        } else {
            productNameInput.value = ""
            productCategoryInput.value = "Teh"
            productCostInput.value = ""
            productSellingInput.value = ""
            productStockInput.value = ""
            productUnitInput.value = "botol"
        }
        _productFormError.value = null
    }

    fun saveProduct(onSuccess: () -> Unit) {
        val name = productNameInput.value.trim()
        val category = productCategoryInput.value
        val cost = productCostInput.value.toDoubleOrNull()
        val selling = productSellingInput.value.toDoubleOrNull()
        val stock = productStockInput.value.toIntOrNull()
        val unit = productUnitInput.value

        if (name.isEmpty()) {
            _productFormError.value = "Nama produk wajib diisi"
            return
        }
        if (cost == null || cost < 0) {
            _productFormError.value = "Harga modal tidak valid"
            return
        }
        if (selling == null || selling < 0) {
            _productFormError.value = "Harga jual tidak valid"
            return
        }
        if (stock == null || stock < 0) {
            _productFormError.value = "Stok tidak valid"
            return
        }

        viewModelScope.launch {
            val ep = editingProduct.value
            if (ep != null) {
                val updated = ep.copy(
                    name = name,
                    category = category,
                    costPrice = cost,
                    sellingPrice = selling,
                    stock = stock,
                    unit = unit
                )
                repository.updateProduct(updated)
            } else {
                repository.addProduct(
                    name = name,
                    category = category,
                    costPrice = cost,
                    sellingPrice = selling,
                    stock = stock,
                    unit = unit
                )
            }
            _productFormError.value = null
            onSuccess()
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repository.softDeleteProduct(productId)
        }
    }

    // Sales Transaction State
    val transactionSelectedProduct = MutableStateFlow<Product?>(null)
    val transactionQtyInput = MutableStateFlow("1")
    val transactionOverridePriceInput = MutableStateFlow("")
    private val _transactionError = MutableStateFlow<String?>(null)
    val transactionError = _transactionError.asStateFlow()
    private val _transactionSuccess = MutableStateFlow<String?>(null)
    val transactionSuccess = _transactionSuccess.asStateFlow()

    fun resetTransactionForm() {
        transactionSelectedProduct.value = null
        transactionQtyInput.value = "1"
        transactionOverridePriceInput.value = ""
        _transactionError.value = null
        _transactionSuccess.value = null
    }

    fun selectProductForTransaction(product: Product) {
        transactionSelectedProduct.value = product
        transactionQtyInput.value = "1"
        transactionOverridePriceInput.value = product.sellingPrice.toInt().toString()
        _transactionError.value = null
        _transactionSuccess.value = null
    }

    fun performTransaction(onSuccess: () -> Unit) {
        val product = transactionSelectedProduct.value
        if (product == null) {
            _transactionError.value = "Silakan pilih produk terlebih dahulu"
            return
        }

        val qty = transactionQtyInput.value.toIntOrNull()
        if (qty == null || qty <= 0) {
            _transactionError.value = "Jumlah minimal pembelian adalah 1"
            return
        }

        if (qty > product.stock) {
            _transactionError.value = "Stok tidak cukup! (Stok saat ini: ${product.stock} ${product.unit})"
            return
        }

        val overridePrice = transactionOverridePriceInput.value.toDoubleOrNull()
        if (overridePrice != null && overridePrice < 0) {
            _transactionError.value = "Harga jual tidak boleh kurang dari 0"
            return
        }

        viewModelScope.launch {
            try {
                repository.recordSale(
                    productId = product.id,
                    qty = qty,
                    overrideSellingPrice = overridePrice
                )
                _transactionError.value = null
                _transactionSuccess.value = "Transaksi berhasil dicatat"
                transactionSelectedProduct.value = null
                onSuccess()
            } catch (e: Exception) {
                _transactionError.value = e.message ?: "Gagal mencatat transaksi"
            }
        }
    }

    fun clearTransactionSuccess() {
        _transactionSuccess.value = null
    }

    // Stock Management State (Barang Masuk)
    val stockSelectedProduct = MutableStateFlow<Product?>(null)
    val stockQtyAddedInput = MutableStateFlow("")
    val stockSupplierInput = MutableStateFlow("")
    private val _stockError = MutableStateFlow<String?>(null)
    val stockError = _stockError.asStateFlow()
    private val _stockSuccess = MutableStateFlow<String?>(null)
    val stockSuccess = _stockSuccess.asStateFlow()

    fun resetStockForm() {
        stockSelectedProduct.value = null
        stockQtyAddedInput.value = ""
        stockSupplierInput.value = ""
        _stockError.value = null
        _stockSuccess.value = null
    }

    fun selectProductForStock(product: Product) {
        stockSelectedProduct.value = product
        stockQtyAddedInput.value = ""
        stockSupplierInput.value = ""
        _stockError.value = null
        _stockSuccess.value = null
    }

    fun performAddStock(onSuccess: () -> Unit) {
        val product = stockSelectedProduct.value
        if (product == null) {
            _stockError.value = "Silakan pilih produk"
            return
        }

        val qtyAdded = stockQtyAddedInput.value.toIntOrNull()
        if (qtyAdded == null || qtyAdded <= 0) {
            _stockError.value = "Jumlah stok harus lebih besar dari 0"
            return
        }

        val supplier = stockSupplierInput.value.trim()
        if (supplier.isEmpty()) {
            _stockError.value = "Nama penyuplai / supplier wajib diisi"
            return
        }

        viewModelScope.launch {
            try {
                repository.addStock(
                    productId = product.id,
                    qtyAdded = qtyAdded,
                    supplier = supplier
                )
                _stockError.value = null
                _stockSuccess.value = "Stok berhasil ditambahkan"
                stockSelectedProduct.value = null
                onSuccess()
            } catch (e: Exception) {
                _stockError.value = e.message ?: "Gagal menambahkan stok"
            }
        }
    }

    fun clearStockSuccess() {
        _stockSuccess.value = null
    }

    // Dynamic Report Calculations
    enum class ReportFilter {
        HariIni,
        MingguIni,
        BulanIni,
        TahunIni,
        Semua
    }

    val selectedReportFilter = MutableStateFlow(ReportFilter.HariIni)

    val reportDashboardData = combine(
        sales,
        products,
        selectedReportFilter
    ) { salesList, productList, filter ->
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val oneWeekMs = 7 * oneDayMs
        val oneMonthMs = 30 * oneDayMs
        val oneYearMs = 365 * oneDayMs

        val filteredSales = salesList.filter { sale ->
            val diff = now - sale.createdAt
            when (filter) {
                ReportFilter.HariIni -> diff <= oneDayMs
                ReportFilter.MingguIni -> diff <= oneWeekMs
                ReportFilter.BulanIni -> diff <= oneMonthMs
                ReportFilter.TahunIni -> diff <= oneYearMs
                ReportFilter.Semua -> true
            }
        }

        val totalOmzet = filteredSales.sumOf { it.total }
        val totalProfit = filteredSales.sumOf { it.profit }
        val totalQtySold = filteredSales.sumOf { it.qty }

        // Find Top Products
        val salesByProduct = filteredSales.groupBy { it.productId }
        val productQuantities = salesByProduct.mapValues { entry ->
            entry.value.sumOf { it.qty }
        }

        val productMap = productList.associateBy { it.id }
        val topProducts = productQuantities.map { (prodId, totalQty) ->
            val prodName = productMap[prodId]?.name ?: "Produk Terhapus"
            val category = productMap[prodId]?.category ?: "Bahan"
            TopProductMetric(prodId, prodName, category, totalQty)
        }.sortedByDescending { it.qtySold }

        val activeDaysCount = when (filter) {
            ReportFilter.HariIni -> 1.0
            ReportFilter.MingguIni -> 7.0
            ReportFilter.BulanIni -> 30.0
            ReportFilter.TahunIni -> 365.0
            ReportFilter.Semua -> {
                if (filteredSales.isEmpty()) 1.0
                else {
                    val oldest = filteredSales.minOf { it.createdAt }
                    val days = ((now - oldest) / oneDayMs).toDouble()
                    if (days < 1.0) 1.0 else days
                }
            }
        }
        val averageSalesPerDay = totalOmzet / activeDaysCount

        ReportMetrics(
            totalOmzet = totalOmzet,
            totalProfit = totalProfit,
            totalQtySold = totalQtySold,
            topProducts = topProducts,
            averageSalesPerDay = averageSalesPerDay,
            salesHistory = filteredSales
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportMetrics()
    )

    // Seed helpers
    private suspend fun seedInitialProductsIfEmpty() {
        val count = repository.allProducts.first().size
        if (count == 0) {
            repository.addProduct("Teh Pucuk Harum", "Teh", 2500.0, 4000.0, 12, "botol")
            repository.addProduct("Kopi Kapal Api", "Kopi", 3000.0, 5000.0, 3, "cup")
            repository.addProduct("Susu Ultra Chocolate", "Susu", 4500.0, 6500.0, 20, "pcs")
            repository.addProduct("Le Minerale 600ml", "Air Mineral", 1500.0, 3000.0, 5, "botol")
            repository.addProduct("Cup Es Jeruk", "Jus", 4000.0, 7000.0, 15, "cup")
            
            // Seed sample database records
            val updatedProducts = repository.allProducts.first()
            if (updatedProducts.isNotEmpty()) {
                val teh = updatedProducts.find { it.name.contains("Teh") }
                if (teh != null) {
                    repository.recordSale(teh.id, 2)
                    repository.addStock(teh.id, 10, "Grosir Sejahtera")
                }
                val kopi = updatedProducts.find { it.name.contains("Kopi") }
                if (kopi != null) {
                    repository.recordSale(kopi.id, 1)
                }
            }
        }
    }
}

// Data structures for representations
data class TopProductMetric(
    val id: String,
    val name: String,
    val category: String,
    val qtySold: Int
)

data class ReportMetrics(
    val totalOmzet: Double = 0.0,
    val totalProfit: Double = 0.0,
    val totalQtySold: Int = 0,
    val topProducts: List<TopProductMetric> = emptyList(),
    val averageSalesPerDay: Double = 0.0,
    val salesHistory: List<Sale> = emptyList()
)
