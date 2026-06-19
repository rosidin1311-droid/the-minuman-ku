package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Product
import com.example.data.Sale
import com.example.data.StockLog
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// Helper to format currency to IDR (Indonesian Rupiah)
fun Double.toRupiah(): String {
    val localeID = Locale("in", "ID")
    val numberFormat = NumberFormat.getCurrencyInstance(localeID)
    numberFormat.maximumFractionDigits = 0
    return numberFormat.format(this).replace("Rp", "Rp ")
}

// Helper to format timestamps
fun Long.toDateString(pattern: String = "dd MMM yyyy, HH:mm"): String {
    val sdf = SimpleDateFormat(pattern, Locale("id", "ID"))
    return sdf.format(Date(this))
}

@Composable
fun MinumanKuApp(viewModel: MinumanKuViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()

    MyApplicationTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentScreen) {
                Screen.Splash -> SplashScreen(
                    onTimeout = {
                        if (loggedInUser != null) {
                            viewModel.navigateTo(Screen.Dashboard)
                        } else {
                            viewModel.navigateTo(Screen.Login)
                        }
                    }
                )
                Screen.Login -> LoginScreen(viewModel = viewModel)
                else -> AppContainerScaffold(viewModel = viewModel)
            }
        }
    }
}

// 1. SPLASH SCREEN
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val containerColor = MaterialTheme.colorScheme.primaryContainer

    LaunchedEffect(Unit) {
        delay(1800)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(primaryColor, containerColor)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Icon Background
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(BorderStroke(3.dp, GreenTertiary), CircleShape)
                    .testTag("splash_logo_container"),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_logo),
                    contentDescription = "MinumanKu premium logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "MinumanKu",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.testTag("splash_app_title")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Aplikasi Reseller & Kasir Finansial",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.dp,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// 2. LOGIN SCREEN
@Composable
fun LoginScreen(viewModel: MinumanKuViewModel) {
    val username by viewModel.loginUsername.collectAsStateWithLifecycle()
    val password by viewModel.loginPassword.collectAsStateWithLifecycle()
    val loginError by viewModel.loginError.collectAsStateWithLifecycle()

    var showPassword by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(BorderStroke(2.dp, GreenTertiary), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_logo),
                        contentDescription = "MinumanKu premium logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Selamat Datang",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Log in untuk mengelola bisnis minumanmu",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Input fields
                OutlinedTextField(
                    value = username,
                    onValueChange = { viewModel.loginUsername.value = it },
                    label = { Text("Username") },
                    placeholder = { Text("Contoh: admin") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("username_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { viewModel.loginPassword.value = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (showPassword) "Hide Password" else "Show Password"
                            )
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Error feedback
                if (loginError != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = loginError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_error_message")
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.performLogin {
                            viewModel.navigateTo(Screen.Dashboard)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("login_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Masuk Aplikasi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Tip: Gunakan username 'admin' & pass 'password123'",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// 3. CONTAINER WITH BOTTOM NAV & QUICK FAB ACTIONS
@Composable
fun AppContainerScaffold(viewModel: MinumanKuViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    var showQuickActionSheet by remember { mutableStateOf(false) }
    var showProductDialog by remember { mutableStateOf(false) }
    var showTransactionDialog by remember { mutableStateOf(false) }
    var showStockDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                tonalElevation = 8.dp
            ) {
                // Dashboard
                NavigationBarItem(
                    selected = currentScreen == Screen.Dashboard,
                    onClick = { viewModel.navigateTo(Screen.Dashboard) },
                    icon = { Icon(Icons.Filled.Dashboard, null) },
                    label = { Text("Dashboard", maxLines = 1, fontSize = 10.sp) }
                )
                // Products
                NavigationBarItem(
                    selected = currentScreen == Screen.Products,
                    onClick = {
                        viewModel.resetProductForm()
                        viewModel.navigateTo(Screen.Products)
                    },
                    icon = { Icon(Icons.Default.Category, null) },
                    label = { Text("Produk", maxLines = 1, fontSize = 10.sp) }
                )
                // Sales
                NavigationBarItem(
                    selected = currentScreen == Screen.Sales,
                    onClick = {
                        viewModel.resetTransactionForm()
                        viewModel.navigateTo(Screen.Sales)
                    },
                    icon = {
                        BadgedBox(badge = {
                            if (notifications.isNotEmpty()) {
                                Badge(containerColor = MaterialTheme.colorScheme.error) {
                                    Text(notifications.size.toString())
                                }
                            }
                        }) {
                            Icon(Icons.Default.ShoppingCart, null)
                        }
                    },
                    label = { Text("Sales", maxLines = 1, fontSize = 10.sp) }
                )
                // Stock In
                NavigationBarItem(
                    selected = currentScreen == Screen.Stock,
                    onClick = {
                        viewModel.resetStockForm()
                        viewModel.navigateTo(Screen.Stock)
                    },
                    icon = { Icon(Icons.Default.AddBox, null) },
                    label = { Text("Stok", maxLines = 1, fontSize = 10.sp) }
                )
                // Reports
                NavigationBarItem(
                    selected = currentScreen == Screen.Reports,
                    onClick = { viewModel.navigateTo(Screen.Reports) },
                    icon = { Icon(Icons.Default.Assessment, null) },
                    label = { Text("Laporan", maxLines = 1, fontSize = 10.sp) }
                )
                // Settings
                NavigationBarItem(
                    selected = currentScreen == Screen.Settings,
                    onClick = { viewModel.navigateTo(Screen.Settings) },
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("Setelan", maxLines = 1, fontSize = 10.sp) }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showQuickActionSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("app_fab")
            ) {
                Icon(Icons.Default.Add, "Quick Actions")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.Dashboard -> DashboardTab(
                    viewModel = viewModel,
                    onNavigateToStock = { viewModel.navigateTo(Screen.Stock) }
                )
                Screen.Products -> ProductsTab(
                    viewModel = viewModel,
                    onAddNewTriggered = {
                        viewModel.resetProductForm(null)
                        showProductDialog = true
                    },
                    onEditTriggered = { product ->
                        viewModel.resetProductForm(product)
                        showProductDialog = true
                    }
                )
                Screen.Sales -> SalesTab(
                    viewModel = viewModel,
                    onSellProductSelected = {
                        showTransactionDialog = true
                    }
                )
                Screen.Stock -> StockTab(
                    viewModel = viewModel,
                    onAddStockProductSelected = {
                        showStockDialog = true
                    }
                )
                Screen.Reports -> ReportsTab(viewModel = viewModel)
                Screen.Settings -> SettingsTab(viewModel = viewModel)
                else -> DashboardTab(
                    viewModel = viewModel,
                    onNavigateToStock = { viewModel.navigateTo(Screen.Stock) }
                )
            }

            // A. Quick Action Dialog
            if (showQuickActionSheet) {
                QuickActionDialog(
                    onDismiss = { showQuickActionSheet = false },
                    onJualSelected = {
                        showQuickActionSheet = false
                        viewModel.resetTransactionForm()
                        viewModel.navigateTo(Screen.Sales)
                    },
                    onTambahProductSelected = {
                        showQuickActionSheet = false
                        viewModel.resetProductForm(null)
                        showProductDialog = true
                    },
                    onCatatStokSelected = {
                        showQuickActionSheet = false
                        viewModel.resetStockForm()
                        viewModel.navigateTo(Screen.Stock)
                    }
                )
            }

            // B. Add/Edit Product Modal Dialog
            if (showProductDialog) {
                ProductFormDialog(
                    viewModel = viewModel,
                    onDismiss = { showProductDialog = false },
                    onComplete = { showProductDialog = false }
                )
            }

            // C. Do Transaction Modal Dialog
            if (showTransactionDialog) {
                TransactionFormDialog(
                    viewModel = viewModel,
                    onDismiss = { showTransactionDialog = false },
                    onComplete = { showTransactionDialog = false }
                )
            }

            // D. Do Stock In Modal Dialog
            if (showStockDialog) {
                AddStockFormDialog(
                    viewModel = viewModel,
                    onDismiss = { showStockDialog = false },
                    onComplete = { showStockDialog = false }
                )
            }
        }
    }
}

// METADATA SCREEN SHARING CARDS (QUICK DIALOG)
@Composable
fun QuickActionDialog(
    onDismiss: () -> Unit,
    onJualSelected: () -> Unit,
    onTambahProductSelected: () -> Unit,
    onCatatStokSelected: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(0.95f),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Aksi Cepat Reseller",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Item 1: Jual
                Button(
                    onClick = onJualSelected,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("quick_action_jual"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Jual Minuman (POS)", fontWeight = FontWeight.Bold)
                }

                // Item 2: Tambah Produk
                Button(
                    onClick = onTambahProductSelected,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("quick_action_tambah_produk"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlaylistAdd, null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Buat Produk Baru", fontWeight = FontWeight.Bold)
                }

                // Item 3: Catat Stok
                Button(
                    onClick = onCatatStokSelected,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("quick_action_catat_stok"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AddBox, null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Catat Stok Masuk", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onDismiss, modifier = Modifier.testTag("quick_action_cancel")) {
                    Text("Batal")
                }
            }
        }
    }
}

// 4. MODULE 2: DASHBOARD TAB
@Composable
fun DashboardTab(
    viewModel: MinumanKuViewModel,
    onNavigateToStock: () -> Unit
) {
    val reportData by viewModel.reportDashboardData.collectAsStateWithLifecycle()
    val lowStockProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    var showLowStockAlertBanner by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming text
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Selamat Pagi,",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate500,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Rosidin 👋",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Styled Avatar Circle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GreenTertiary)
                        .border(BorderStroke(2.dp, Color.White), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "R",
                        color = GreenSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // 1. Dashboard Custom Illustration Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_dashboard_banner),
                        contentDescription = "Dashboard Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    
                    // Elegant dark gradient text container overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.7f)
                                    )
                                )
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Column {
                            Text(
                                text = "Kelola Usaha Minuman Praktis",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Pencatatan cepat & pantau laba real-time",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Low stock warning alerts
        if (notifications.isNotEmpty() && showLowStockAlertBanner) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Peringatan",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ada ${lowStockProducts.size} Produk Sekarat!",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Stok tersisa <= 5 unit. Cek daftar untuk menyuplai ulang.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                        }
                        IconButton(onClick = { showLowStockAlertBanner = false }) {
                            Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        // Card Metrics Grid row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Horizontal 2-column metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Omzet Hari Ini Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp),
                        colors = CardDefaults.cardColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Omzet Hari Ini",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Column {
                                Text(
                                    text = reportData.totalOmzet.toRupiah(),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.testTag("dashboard_omzet_val")
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "+12% vs Kemarin",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Profit Bersih Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp),
                        colors = CardDefaults.cardColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Laba Bersih",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Column {
                                Text(
                                    text = reportData.totalProfit.toRupiah(),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.testTag("dashboard_profit_val")
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Target 80%",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Row span 2 progress metric card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Slate100),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "Produk Terjual",
                                fontSize = 12.sp,
                                color = Slate500,
                                fontWeight = FontWeight.Medium
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${reportData.totalQtySold} ",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.testTag("dashboard_qty_val")
                                )
                                Text(
                                    text = "botol",
                                    fontSize = 12.sp,
                                    color = Slate400,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        // Sleek Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(Slate100)
                        ) {
                            val progress = if (reportData.totalQtySold > 0) {
                                minOf(1f, reportData.totalQtySold.toFloat() / 100f)
                            } else {
                                0.35f // Sleek progress fallback indicator during initial states
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .fillMaxHeight()
                                    .background(GreenPrimary)
                            )
                        }
                    }
                }
            }
        }

        // Low stock list header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status Stok Menipis (<= 5)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(onClick = onNavigateToStock) {
                    Text("Suplai Stok", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Low stock items list
        if (lowStockProducts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Semua Stok Aman!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Tidak ada produk yang memiliki stok kritis di bawah 5.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(lowStockProducts, key = { it.id }) { product ->
                val nameLower = product.name.lowercase(Locale.ROOT)
                val (bgColor, emoji) = when {
                    nameLower.contains("teh") || nameLower.contains("tea") -> {
                        Color(0xFFFFF7ED) to "🥤"
                    }
                    nameLower.contains("air") || nameLower.contains("aqua") || nameLower.contains("mineral") || nameLower.contains("water") -> {
                        Color(0xFFEFF6FF) to "💧"
                    }
                    else -> {
                        Color(0xFFF0FDF4) to "🍵"
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Slate100),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category-specific emoji container
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 24.sp)
                        }

                        // Product main information
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Kategori: ${product.category}",
                                fontSize = 11.sp,
                                color = Slate500,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Danger critical countdown
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "${product.stock} ${product.unit}",
                                color = Color(0xFFDC2626), // text-red-600
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Segera Isi",
                                color = Slate400,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// 5. MODULE 3: PRODUCT MANAGEMENT TAB
@Composable
fun ProductsTab(
    viewModel: MinumanKuViewModel,
    onAddNewTriggered: () -> Unit,
    onEditTriggered: (Product) -> Unit
) {
    val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val searchQuery by viewModel.productSearchQuery.collectAsStateWithLifecycle()
    val filterCategory by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()

    val categories = listOf("Semua", "Teh", "Kopi", "Susu", "Jus", "Air Mineral", "Lainnya")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Upper search and category controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar Minuman",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = onAddNewTriggered,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_tambah_produk")
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Produk Baru", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search text box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.productSearchQuery.value = it },
                placeholder = { Text("Cari nama minuman / kategori...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_product_field"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal scrolling Category Selection Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = filterCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectedCategoryFilter.value = category },
                        label = { Text(category, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Product Listings lazy list
        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = "Nothing found",
                        tint = Color.Gray,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Tidak Ada Produk Minuman",
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "Silakan tambah produk baru menggunakan tombol di atas.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = product.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = product.category,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Column {
                                        Text("Harga Modal", fontSize = 10.sp, color = Color.Gray)
                                        Text(product.costPrice.toRupiah(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                    }
                                    Column {
                                        Text("Harga Jual", fontSize = 10.sp, color = Color.Gray)
                                        Text(product.sellingPrice.toRupiah(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Column {
                                        Text("Stok", fontSize = 10.sp, color = Color.Gray)
                                        Surface(
                                            color = if (product.stock <= 5) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "${product.stock} ${product.unit}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (product.stock <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Actions
                            Row {
                                IconButton(
                                    onClick = { onEditTriggered(product) },
                                    modifier = Modifier.testTag("edit_product_${product.id}")
                                ) {
                                    Icon(Icons.Default.Edit, "Edit entry", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(
                                    onClick = { viewModel.deleteProduct(product.id) },
                                    modifier = Modifier.testTag("delete_product_${product.id}")
                                ) {
                                    Icon(Icons.Default.Delete, "Delete entry", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Dialog Form for Adding or Editing Products
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormDialog(
    viewModel: MinumanKuViewModel,
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    val ep by viewModel.editingProduct.collectAsStateWithLifecycle()
    val name by viewModel.productNameInput.collectAsStateWithLifecycle()
    val category by viewModel.productCategoryInput.collectAsStateWithLifecycle()
    val costPrice by viewModel.productCostInput.collectAsStateWithLifecycle()
    val sellingPrice by viewModel.productSellingInput.collectAsStateWithLifecycle()
    val stock by viewModel.productStockInput.collectAsStateWithLifecycle()
    val unit by viewModel.productUnitInput.collectAsStateWithLifecycle()
    val formError by viewModel.productFormError.collectAsStateWithLifecycle()

    val categories = listOf("Teh", "Kopi", "Susu", "Jus", "Air Mineral", "Lainnya")
    val units = listOf("botol", "pcs", "cup", "pack")

    var categoryExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = if (ep == null) "Tambah Minuman Baru" else "Edit Detail Minuman",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Input Nama
                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.productNameInput.value = it },
                    label = { Text("Nama Minuman") },
                    placeholder = { Text("Contoh: Teh Botol Sosro") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_product_name"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Dropdown Kategori
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = category,
                        onValueChange = {},
                        label = { Text("Kategori") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("edit_product_category"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { selection ->
                            DropdownMenuItem(
                                text = { Text(selection) },
                                onClick = {
                                    viewModel.productCategoryInput.value = selection
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Harga modal input
                OutlinedTextField(
                    value = costPrice,
                    onValueChange = { viewModel.productCostInput.value = it },
                    label = { Text("Harga Modal (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_product_cost"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Harga jual input
                OutlinedTextField(
                    value = sellingPrice,
                    onValueChange = { viewModel.productSellingInput.value = it },
                    label = { Text("Harga Jual (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_product_selling_price"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Stok awal input
                OutlinedTextField(
                    value = stock,
                    onValueChange = { viewModel.productStockInput.value = it },
                    label = { Text("Jumlah Stok") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_product_stock"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Dropdown Satuan unit
                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = !unitExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = unit,
                        onValueChange = {},
                        label = { Text("Satuan") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("edit_product_unit"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        units.forEach { selection ->
                            DropdownMenuItem(
                                text = { Text(selection) },
                                onClick = {
                                    viewModel.productUnitInput.value = selection
                                    unitExpanded = false
                                }
                            )
                        }
                    }
                }

                // Error indicators
                if (formError != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = formError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("product_validation_error")
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.testTag("product_dialog_cancel")) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            viewModel.saveProduct {
                                onComplete()
                            }
                        },
                        modifier = Modifier.testTag("save_product_button")
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

// 6. MODULE 4: SALES TRANSACTION TAB (POS BAR)
@Composable
fun SalesTab(
    viewModel: MinumanKuViewModel,
    onSellProductSelected: () -> Unit
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val recentSales by viewModel.sales.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Text(
                text = "Kasir Penjualan",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Pilih minuman di bawah untuk mencatat transaksi penjualan",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.HourglassEmpty, null, modifier = Modifier.size(50.dp), tint = Color.Gray)
                    Text("Produk kosong. Tambahkan produk di menu Produk dahulu!", color = Color.Gray)
                }
            }
        } else {
            // Horizontal list to choose product
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Daftar Minuman Tersedia",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(products.filter { it.stock > 0 }, key = { it.id }) { product ->
                        Card(
                            modifier = Modifier
                                .width(130.dp)
                                .clickable {
                                    viewModel.selectProductForTransaction(product)
                                    onSellProductSelected()
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = product.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = product.category,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = product.sellingPrice.toRupiah(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Stok: ${product.stock} ${product.unit}",
                                    fontSize = 10.sp,
                                    color = if (product.stock <= 5) Color.Red else Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent cashier transaction logs
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Riwayat Transaksi Terbaru",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (recentSales.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada transaksi terkini.",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            } else {
                val productMap = products.associateBy { it.id }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentSales, key = { it.id }) { sale ->
                        val productName = productMap[sale.productId]?.name ?: "Produk Terhapus"
                        val unit = productMap[sale.productId]?.unit ?: "botol"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = productName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${sale.qty} $unit × ${sale.sellingPrice.toRupiah()}",
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                    Text(
                                        text = sale.createdAt.toDateString(),
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = sale.total.toRupiah(),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 14.sp
                                    )
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "Laba: +${sale.profit.toRupiah()}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Dialog form to complete a sales transaction
@Composable
fun TransactionFormDialog(
    viewModel: MinumanKuViewModel,
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    val product by viewModel.transactionSelectedProduct.collectAsStateWithLifecycle()
    val qtyInput by viewModel.transactionQtyInput.collectAsStateWithLifecycle()
    val overridePriceInput by viewModel.transactionOverridePriceInput.collectAsStateWithLifecycle()
    val transactionError by viewModel.transactionError.collectAsStateWithLifecycle()
    val transactionSuccess by viewModel.transactionSuccess.collectAsStateWithLifecycle()

    val currentProduct = product ?: return

    val qty = qtyInput.toIntOrNull() ?: 0
    val sellPrice = overridePriceInput.toDoubleOrNull() ?: currentProduct.sellingPrice
    val total = qty * sellPrice
    val modalCost = qty * currentProduct.costPrice
    val profit = total - modalCost

    val context = LocalContext.current

    LaunchedEffect(transactionSuccess) {
        if (transactionSuccess != null) {
            delay(1000)
            viewModel.clearTransactionSuccess()
            onComplete()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Konfirmasi Penjualan",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Product details box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = currentProduct.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Stok tersedia:", fontSize = 12.sp)
                            Text("${currentProduct.stock} ${currentProduct.unit}", fontWeight = FontWeight.Bold)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Harga modal:", fontSize = 12.sp)
                            Text(currentProduct.costPrice.toRupiah(), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quantity selector with quick modifiers
                Text("Jumlah Pembelian (Qty)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val cur = qtyInput.toIntOrNull() ?: 1
                            if (cur > 1) {
                                viewModel.transactionQtyInput.value = (cur - 1).toString()
                            }
                        }
                    ) {
                        Icon(Icons.Default.RemoveCircle, null, tint = MaterialTheme.colorScheme.primary)
                    }

                    OutlinedTextField(
                        value = qtyInput,
                        onValueChange = { viewModel.transactionQtyInput.value = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("transaction_qty"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    IconButton(
                        onClick = {
                            val cur = qtyInput.toIntOrNull() ?: 0
                            if (cur < currentProduct.stock) {
                                viewModel.transactionQtyInput.value = (cur + 1).toString()
                            }
                        }
                    ) {
                        Icon(Icons.Default.AddCircle, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Selling price override
                OutlinedTextField(
                    value = overridePriceInput,
                    onValueChange = { viewModel.transactionOverridePriceInput.value = it },
                    label = { Text("Harga Jual Satuan (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_selling_price"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Computation Panel
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Tagihan / Omzet:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(total.toRupiah(), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Estimasi Keuntungan / Profit:", fontSize = 12.sp, color = Color.Gray)
                    Text("+${profit.toRupiah()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary, fontSize = 13.sp)
                }

                // Messages UI
                if (transactionError != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = transactionError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("transaction_validation_error")
                    )
                }

                if (transactionSuccess != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = transactionSuccess ?: "",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transaction_checkout_success"),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.testTag("transaction_dialog_cancel")) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { viewModel.performTransaction { } },
                        modifier = Modifier.testTag("btn_checkout_save")
                    ) {
                        Text("Simpan Transaksi")
                    }
                }
            }
        }
    }
}

// 7. MODULE 5: STOCK MANAGEMENT TAB
@Composable
fun StockTab(
    viewModel: MinumanKuViewModel,
    onAddStockProductSelected: () -> Unit
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val logs by viewModel.stockLogs.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Text(
                text = "Kelola Hambatan Stok",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Tambah stok barang yang baru dibeli dari penyuplai",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        if (products.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Daftar produk masih kosong. Tambah produk di menu Produk dahulu!", color = Color.Gray)
            }
        } else {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Pilih Produk untuk Ditambah Stok",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(products) { product ->
                        Card(
                            modifier = Modifier
                                .width(120.dp)
                                .clickable {
                                    viewModel.selectProductForStock(product)
                                    onAddStockProductSelected()
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = product.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Stok: ${product.stock} ${product.unit}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "+ Masuk",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Logs listing
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Catatan Log Barang Masuk",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (logs.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Belum ada pencatatan stok masuk.", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                val productMap = products.associateBy { it.id }

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(logs, key = { it.id }) { log ->
                        val productName = productMap[log.productId]?.name ?: "Produk Terhapus"
                        val unit = productMap[log.productId]?.unit ?: "botol"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = productName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Supplier: ${log.supplier}",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray
                                    )
                                    Text(
                                        text = log.createdAt.toDateString(),
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "+${log.qtyAdded} $unit",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Dialog entry to log incoming stock arrivals
@Composable
fun AddStockFormDialog(
    viewModel: MinumanKuViewModel,
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    val product by viewModel.stockSelectedProduct.collectAsStateWithLifecycle()
    val qtyAddedInput by viewModel.stockQtyAddedInput.collectAsStateWithLifecycle()
    val supplierInput by viewModel.stockSupplierInput.collectAsStateWithLifecycle()
    val stockError by viewModel.stockError.collectAsStateWithLifecycle()
    val stockSuccess by viewModel.stockSuccess.collectAsStateWithLifecycle()

    val currentProduct = product ?: return

    LaunchedEffect(stockSuccess) {
        if (stockSuccess != null) {
            delay(1000)
            viewModel.clearStockSuccess()
            onComplete()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Suplai / Tambah Stok",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Current info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = currentProduct.name,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Stok Lama:", fontSize = 12.sp)
                            Text("${currentProduct.stock} ${currentProduct.unit}", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Qty input
                OutlinedTextField(
                    value = qtyAddedInput,
                    onValueChange = { viewModel.stockQtyAddedInput.value = it },
                    label = { Text("Jumlah Stok Ditambahkan") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stock_qty_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Supplier input
                OutlinedTextField(
                    value = supplierInput,
                    onValueChange = { viewModel.stockSupplierInput.value = it },
                    label = { Text("Nama Penyuplai / Supplier") },
                    placeholder = { Text("Contoh: PT Sumber Makmur") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stock_supplier_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                // Verification logs
                if (stockError != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stockError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("stock_form_error")
                    )
                }

                if (stockSuccess != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stockSuccess ?: "",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stock_form_success"),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.testTag("stock_dialog_cancel")) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { viewModel.performAddStock {} },
                        modifier = Modifier.testTag("btn_stock_save")
                    ) {
                        Text("Simpan Registrasi")
                    }
                }
            }
        }
    }
}

// 8. MODULE 6: ANALYTICS & REPORTS TAB
@Composable
fun ReportsTab(viewModel: MinumanKuViewModel) {
    val reportFilter by viewModel.selectedReportFilter.collectAsStateWithLifecycle()
    val reportMetrics by viewModel.reportDashboardData.collectAsStateWithLifecycle()

    val filters = listOf(
        MinumanKuViewModel.ReportFilter.HariIni to "Hari ini",
        MinumanKuViewModel.ReportFilter.MingguIni to "Minggu ini",
        MinumanKuViewModel.ReportFilter.BulanIni to "Bulan ini",
        MinumanKuViewModel.ReportFilter.TahunIni to "Tahun ini",
        MinumanKuViewModel.ReportFilter.Semua to "Semua"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Text(
                text = "Laporan Profitabilitas",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Time horizon selector chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filters) { (filterVal, filterLabel) ->
                    val isSelected = reportFilter == filterVal
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectedReportFilter.value = filterVal },
                        label = { Text(filterLabel, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Metrics summary cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Total Pembukuan", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = reportMetrics.totalOmzet.toRupiah(),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("report_total_omzet")
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Total Laba Bersih", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = reportMetrics.totalProfit.toRupiah(),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.testTag("report_total_profit")
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Rata-rata / Hari", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = reportMetrics.averageSalesPerDay.toRupiah(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Volume Terjual", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = "${reportMetrics.totalQtySold} unit",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Interactive Graphics canvas: "Charts: Daily trend, Monthly trend, Top products"
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Aktivitas Penjualan (Laba vs Omzet)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (reportMetrics.salesHistory.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Tidak ada transaksi dalam rentang ini.",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            // Render a Custom Canvas Drawing line graph representing profits and omzet
                            val salesHistory = reportMetrics.salesHistory.sortedBy { it.createdAt }
                            val maxVal = salesHistory.maxOfOrNull { it.total } ?: 10000.0

                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                            ) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                val sizeHistory = salesHistory.size
                                val paddingBottom = 20f

                                // Grid Guidelines
                                drawLine(
                                    color = Color.LightGray.copy(alpha = 0.5f),
                                    start = Offset(0f, canvasHeight / 2f),
                                    end = Offset(canvasWidth, canvasHeight / 2f),
                                    strokeWidth = 2f
                                )

                                drawLine(
                                    color = Color.LightGray.copy(alpha = 0.5f),
                                    start = Offset(0f, canvasHeight - paddingBottom),
                                    end = Offset(canvasWidth, canvasHeight - paddingBottom),
                                    strokeWidth = 2f
                                )

                                if (sizeHistory > 1) {
                                    val stepX = canvasWidth / (sizeHistory - 1)
                                    val pathOmzet = Path()
                                    val pathProfit = Path()

                                    salesHistory.forEachIndexed { idx, s ->
                                        val x = idx * stepX
                                        // Max value scaled down
                                        val ratioO = (s.total / maxVal).toFloat()
                                        val ratioP = (s.profit / maxVal).toFloat()

                                        val yO = canvasHeight - paddingBottom - ratioO * (canvasHeight - paddingBottom - 10f)
                                        val yP = canvasHeight - paddingBottom - ratioP * (canvasHeight - paddingBottom - 10f)

                                        if (idx == 0) {
                                            pathOmzet.moveTo(x, yO)
                                            pathProfit.moveTo(x, yP)
                                        } else {
                                            pathOmzet.lineTo(x, yO)
                                            pathProfit.lineTo(x, yP)
                                        }
                                    }

                                    // Draw line curves
                                    drawPath(
                                        path = pathOmzet,
                                        color = Color(0xFF1B5E20), // Forest Green for Omzet
                                        style = Stroke(width = 6f)
                                    )
                                    drawPath(
                                        path = pathProfit,
                                        color = Color(0xFF0288D1), // Accent Blue for Profit
                                        style = Stroke(width = 4f)
                                    )
                                } else {
                                    // Fallback dot
                                    val ratio = (salesHistory.first().total / maxVal).toFloat()
                                    val y = canvasHeight - paddingBottom - ratio * (canvasHeight - paddingBottom - 10f)
                                    drawCircle(
                                        color = Color(0xFF1B5E20),
                                        radius = 12f,
                                        center = Offset(canvasWidth / 2f, y)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            // Small labels
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFF1B5E20), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Omzet", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.width(16.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFF0288D1), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Laba Bersih", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            // Top Beverages Sold Ranking Table
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Produk Terlaris (Top Beverages)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (reportMetrics.topProducts.isEmpty()) {
                            Text(
                                "Belum ada produk yang terjual.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        } else {
                            // Simple responsive table list
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Nama Produk", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
                                Text("Jumlah Terjual (Qty)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
                            }
                            HorizontalDivider()

                            reportMetrics.topProducts.take(5).forEachIndexed { index, metric ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .background(
                                                    if (index == 0) MaterialTheme.colorScheme.primaryContainer
                                                    else MaterialTheme.colorScheme.secondaryContainer,
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${index + 1}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(metric.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                            Text(metric.category, fontSize = 10.sp, color = Color.Gray)
                                        }
                                    }

                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "${metric.qtySold} unit",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 9. MODULE 8: SETTINGS TAB
@Composable
fun SettingsTab(viewModel: MinumanKuViewModel) {
    val user by viewModel.loggedInUser.collectAsStateWithLifecycle()
    val changePasswordToast by viewModel.changePasswordToast.collectAsStateWithLifecycle()

    val oldPassword by viewModel.oldPasswordState.collectAsStateWithLifecycle()
    val newPassword by viewModel.newPasswordState.collectAsStateWithLifecycle()
    val confirmPassword by viewModel.confirmNewPasswordState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(changePasswordToast) {
        if (changePasswordToast != null) {
            delay(2000)
            viewModel.clearChangePasswordToast()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile placeholder",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Rosidin (Owner)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Role: Administrator Reseller",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Change Password Form card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Ganti Password",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { viewModel.oldPasswordState.value = it },
                        label = { Text("Password Lama") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("old_password_field"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { viewModel.newPasswordState.value = it },
                        label = { Text("Password Baru (Min 6 Karakter)") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_password_field"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { viewModel.confirmNewPasswordState.value = it },
                        label = { Text("Konfirmasi Password Baru") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("confirm_password_field"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Error or Success toasts
                    if (changePasswordToast != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = changePasswordToast ?: "",
                            fontWeight = FontWeight.Bold,
                            color = if (changePasswordToast!!.contains("berhasil")) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            modifier = Modifier.testTag("password_toast_message")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.performChangePassword() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_change_password"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Simpan Sandi Baru", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Backup simulated menu
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Backup & Pemulihan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = "Simpan basis data lokal secara berkala agar pencatatan pembukuan tetap terlindungi.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                // Simulate Backup triggers
                                val rand = (100000..999999).random()
                                ToastHelper.showToast(context, "Selesai! File: backup_minumanku_$rand.db tersimpan.")
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_backup")
                        ) {
                            Icon(Icons.Default.CloudUpload, null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Backup Data", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                ToastHelper.showToast(context, "Data berhasil divalidasi dan dipulihkan!")
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_restore")
                        ) {
                            Icon(Icons.Default.Refresh, null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restore Data", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Logout block
        item {
            Button(
                onClick = { viewModel.performLogout() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_logout"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Logout, null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout Akun", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Simple context Toast helper for UI
object ToastHelper {
    fun showToast(context: android.content.Context, message: String) {
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}
