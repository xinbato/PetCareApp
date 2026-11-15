package com.example.petcare.ui.screens

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.petcare.data.model.PetPlaceModel
import com.example.petcare.data.repository.PetRepository
import com.example.petcare.ui.components.PetCareTab
import com.example.petcare.ui.components.PetCareTabBar
import com.example.petcare.ui.theme.BackgroundLight
import com.example.petcare.ui.theme.BorderLight
import com.example.petcare.ui.theme.GreenLight
import com.example.petcare.ui.theme.GreenPrimary
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.theme.TextPrimary
import com.example.petcare.ui.theme.TextSecondary

private val StethoscopeIcon: ImageVector by lazy {
    ImageVector.Builder("StethoscopeIcon", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color(0xFF338C63))) {
            moveTo(19f, 8f)
            verticalLineTo(3f)
            horizontalLineToRelative(-2f)
            verticalLineToRelative(5f)
            curveToRelative(0f, 2.76f, -2.24f, 5f, -5f, 5f)
            curveToRelative(-2.76f, 0f, -5f, -2.24f, -5f, -5f)
            verticalLineTo(3f)
            horizontalLineTo(5f)
            verticalLineToRelative(5f)
            curveToRelative(0f, 3.53f, 2.61f, 6.43f, 6f, 6.92f)
            verticalLineTo(18f)
            curveToRelative(0f, 1.66f, 1.34f, 3f, 3f, 3f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(-2f)
            horizontalLineToRelative(-2f)
            curveToRelative(-0.55f, 0f, -1f, -0.45f, -1f, -1f)
            verticalLineToRelative(-3.08f)
            curveToRelative(3.39f, -0.49f, 6f, -3.39f, 6f, -6.92f)
            close()
        }
    }.build()
}

private val ScissorsIcon: ImageVector by lazy {
    ImageVector.Builder("ScissorsIcon", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color(0xFFD99B26))) {
            moveTo(9.64f, 7.64f)
            curveToRelative(0.23f, -0.5f, 0.36f, -1.05f, 0.36f, -1.64f)
            curveToRelative(0f, -2.21f, -1.79f, -4f, -4f, -4f)
            curveTo(3.79f, 2f, 2f, 3.79f, 2f, 6f)
            curveToRelative(0f, 2.21f, 1.79f, 4f, 4f, 4f)
            curveToRelative(0.59f, 0f, 1.14f, -0.13f, 1.64f, -0.36f)
            lineTo(10f, 12f)
            lineToRelative(-2.36f, 2.36f)
            curveTo(7.14f, 14.13f, 6.59f, 14f, 6f, 14f)
            curveToRelative(-2.21f, 0f, -4f, 1.79f, -4f, 4f)
            curveToRelative(0f, 2.21f, 1.79f, 4f, 4f, 4f)
            curveToRelative(2.21f, 0f, 4f, -1.79f, 4f, -4f)
            curveToRelative(0f, -0.59f, -0.13f, -1.14f, -0.36f, -1.64f)
            lineTo(12f, 14f)
            lineToRelative(7f, 7f)
            horizontalLineToRelative(3f)
            verticalLineToRelative(-1f)
            lineToRelative(-9f, -9f)
            lineToRelative(9f, -9f)
            verticalLineTo(1f)
            horizontalLineToRelative(-3f)
            lineToRelative(-7f, 7f)
            close()
        }
    }.build()
}

private val ListIcon: ImageVector by lazy {
    ImageVector.Builder("ListIcon", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(GreenPrimary)) {
            moveTo(4f, 10.5f)
            curveTo(3.17f, 10.5f, 2.5f, 11.17f, 2.5f, 12f)
            curveTo(2.5f, 12.83f, 3.17f, 13.5f, 4f, 13.5f)
            curveTo(4.83f, 13.5f, 5.5f, 12.83f, 5.5f, 12f)
            curveTo(5.5f, 11.17f, 4.83f, 10.5f, 4f, 10.5f)
            close()
            moveTo(4f, 4.5f)
            curveTo(3.17f, 4.5f, 2.5f, 5.17f, 2.5f, 6f)
            curveTo(2.5f, 6.83f, 3.17f, 7.5f, 4f, 7.5f)
            curveTo(4.83f, 7.5f, 5.5f, 6.83f, 5.5f, 6f)
            curveTo(5.5f, 5.17f, 4.83f, 4.5f, 4f, 4.5f)
            close()
            moveTo(4f, 16.5f)
            curveTo(3.17f, 16.5f, 2.5f, 17.17f, 2.5f, 18f)
            curveTo(2.5f, 18.83f, 3.17f, 19.5f, 4f, 19.5f)
            curveTo(4.83f, 19.5f, 5.5f, 18.83f, 5.5f, 18f)
            curveTo(5.5f, 17.17f, 4.83f, 16.5f, 4f, 16.5f)
            close()
            moveTo(7f, 19f)
            horizontalLineToRelative(14f)
            verticalLineToRelative(-2f)
            horizontalLineTo(7f)
            verticalLineToRelative(2f)
            close()
            moveTo(7f, 13f)
            horizontalLineToRelative(14f)
            verticalLineToRelative(-2f)
            horizontalLineTo(7f)
            verticalLineToRelative(2f)
            close()
            moveTo(7f, 5f)
            verticalLineToRelative(2f)
            horizontalLineToRelative(14f)
            verticalLineTo(5f)
            horizontalLineTo(7f)
            close()
        }
    }.build()
}

private val FilterIcon: ImageVector by lazy {
    ImageVector.Builder("FilterIcon", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(GreenPrimary)) {
            moveTo(10f, 18f)
            horizontalLineToRelative(4f)
            verticalLineToRelative(-2f)
            horizontalLineToRelative(-4f)
            verticalLineToRelative(2f)
            close()
            moveTo(3f, 6f)
            verticalLineToRelative(2f)
            horizontalLineToRelative(18f)
            verticalLineTo(6f)
            horizontalLineTo(3f)
            close()
            moveTo(6f, 13f)
            horizontalLineToRelative(12f)
            verticalLineToRelative(-2f)
            horizontalLineTo(6f)
            verticalLineToRelative(2f)
            close()
        }
    }.build()
}

private val ShoppingBagIcon: ImageVector by lazy {
    ImageVector.Builder("ShoppingBagIcon", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color(0xFF8E44AD))) {
            moveTo(18f, 6f)
            horizontalLineToRelative(-2f)
            curveToRelative(0f, -2.21f, -1.79f, -4f, -4f, -4f)
            curveTo(9.79f, 2f, 8f, 3.79f, 8f, 6f)
            horizontalLineTo(6f)
            curveTo(4.9f, 6f, 4f, 6.9f, 4f, 8f)
            verticalLineToRelative(12f)
            curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
            horizontalLineToRelative(12f)
            curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
            verticalLineTo(8f)
            curveTo(20f, 6.9f, 19.1f, 6f, 18f, 6f)
            close()
            moveTo(12f, 4f)
            curveToRelative(1.1f, 0f, 2f, 0.9f, 2f, 2f)
            horizontalLineToRelative(-4f)
            curveTo(10f, 4.9f, 10.9f, 4f, 12f, 4f)
            close()
            moveTo(18f, 20f)
            horizontalLineTo(6f)
            verticalLineTo(8f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(2f)
            horizontalLineToRelative(2f)
            verticalLineTo(8f)
            horizontalLineToRelative(4f)
            verticalLineToRelative(2f)
            horizontalLineToRelative(2f)
            verticalLineTo(8f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(12f)
            close()
        }
    }.build()
}

private val NavigationIcon: ImageVector by lazy {
    ImageVector.Builder("NavigationIcon", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color(0xFF2563EB))) {
            moveTo(12f, 2f)
            lineTo(4.5f, 20.29f)
            lineToRelative(0.71f, 0.71f)
            lineTo(12f, 18f)
            lineToRelative(6.79f, 3f)
            lineToRelative(0.71f, -0.71f)
            close()
        }
    }.build()
}

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    onTabSelected: (PetCareTab) -> Unit = {},
) {
    var isMapView by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tất cả") }
    var selectedPlaceId by remember { mutableStateOf<String?>(null) }
    var currentLocationText by remember { mutableStateOf("Quận 3, TP.HCM") }
    var showLocationDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val places = PetRepository.petPlaces

    val filteredPlaces = remember(searchQuery, selectedCategory, places.size) {
        places.filter { place ->
            val matchesCategory = selectedCategory == "Tất cả" || place.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    place.name.lowercase().contains(searchQuery.lowercase()) ||
                    place.subtitle.lowercase().contains(searchQuery.lowercase())
            matchesCategory && matchesSearch
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Main Top Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Bản đồ",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tất cả cơ sở thú y gần nhất",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }

                // Toggle Mode Button (List mode <-> Map mode)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, BorderLight, CircleShape)
                        .clickable { isMapView = !isMapView },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isMapView) ListIcon else Icons.Default.LocationOn,
                        contentDescription = if (isMapView) "Xem danh sách" else "Xem bản đồ",
                        tint = GreenPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Body Content depending on mode
            if (!isMapView) {
                // ================= MODE 1: LIST VIEW =================
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Search Bar
                    PaddingBox {
                        MapSearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category Filter Badges (Thú y, Spa, Cửa hàng)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        CategoryCircleButton(
                            title = "Thú y",
                            icon = StethoscopeIcon,
                            bgColor = Color(0xFFE8F5E9),
                            isSelected = selectedCategory == "Thú y",
                            onClick = { selectedCategory = if (selectedCategory == "Thú y") "Tất cả" else "Thú y" }
                        )

                        CategoryCircleButton(
                            title = "Spa",
                            icon = ScissorsIcon,
                            bgColor = Color(0xFFFFF8E1),
                            isSelected = selectedCategory == "Spa",
                            onClick = { selectedCategory = if (selectedCategory == "Spa") "Tất cả" else "Spa" }
                        )

                        CategoryCircleButton(
                            title = "Cửa hàng",
                            icon = ShoppingBagIcon,
                            bgColor = Color(0xFFF3E5F5),
                            isSelected = selectedCategory == "Cửa hàng",
                            onClick = { selectedCategory = if (selectedCategory == "Cửa hàng") "Tất cả" else "Cửa hàng" }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Section 1: Gợi ý cho bạn
                    PaddingBox {
                        Text(
                            text = "Gợi ý cho bạn",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val featuredPlace = places.find { it.isFeatured } ?: places.firstOrNull()
                    featuredPlace?.let { place ->
                        PaddingBox {
                            FeaturedPlaceCard(
                                place = place,
                                onClick = {
                                    selectedPlaceId = place.id
                                    isMapView = true
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Section 2: Gần bạn
                    PaddingBox {
                        Text(
                            text = "Gần bạn",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Horizontal list of nearby places
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        filteredPlaces.forEach { place ->
                            NearbyPlaceCard(
                                place = place,
                                onSaveToggle = { PetRepository.toggleSavePlace(place.id) },
                                onClick = {
                                    selectedPlaceId = place.id
                                    isMapView = true
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Location Info Banner Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFFF7EC))
                            .border(1.dp, Color(0xFFFFE0B2), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Vị trí",
                                    tint = Color(0xFFE65100),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Đang hiển thị dịch vụ quanh $currentLocationText",
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Text(
                                text = "Đổi",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary,
                                modifier = Modifier
                                    .clickable { showLocationDialog = true }
                                    .padding(start = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            } else {
                // ================= MODE 2: REAL GOOGLE MAPS EMBEDDED VIEW (FULLSCREEN MAP 100%) =================
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    // REAL Google Maps Live Embed Render (OpenStreetMap / Google Maps Live Embed)
                    RealGoogleMapView(
                        places = filteredPlaces,
                        selectedPlaceId = selectedPlaceId,
                        onSelectMarker = { placeId ->
                            selectedPlaceId = placeId
                        }
                    )

                    // Floating Search bar on top of map
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        MapSearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it }
                        )
                    }

                    // Small Single Preview Tooltip Popup when a marker pin is selected
                    val selectedPlace = filteredPlaces.find { it.id == selectedPlaceId }
                    selectedPlace?.let { place ->
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            SingleMapClinicPreviewCard(
                                place = place,
                                onClose = { selectedPlaceId = null },
                                onDirectionsClick = {
                                    val gmmIntentUri = Uri.parse("geo:${place.lat},${place.lng}?q=${Uri.encode(place.name + " " + place.address)}")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                        setPackage("com.google.android.apps.maps")
                                    }
                                    if (mapIntent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(mapIntent)
                                    } else {
                                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=${Uri.encode(place.address)}"))
                                        context.startActivity(webIntent)
                                    }
                                },
                                onCallClick = {
                                    val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${place.phone}"))
                                    context.startActivity(callIntent)
                                },
                                onShareClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, "Gợi ý phòng khám thú cưng: ${place.name} - ${place.address} (SĐT: ${place.phone})")
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ cơ sở thú cưng"))
                                },
                                onSaveToggle = {
                                    PetRepository.toggleSavePlace(place.id)
                                }
                            )
                        }
                    }
                }
            }

            // Bottom Navigation Bar
            PetCareTabBar(
                currentTab = PetCareTab.MAP,
                onTabSelected = onTabSelected
            )
        }

        // Location Change Dialog
        if (showLocationDialog) {
            AlertDialog(
                onDismissRequest = { showLocationDialog = false },
                title = { Text("Chọn khu vực", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("Quận 1, TP.HCM", "Quận 3, TP.HCM", "Quận 10, TP.HCM", "Bình Thạnh, TP.HCM", "Thủ Đức, TP.HCM").forEach { loc ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (currentLocationText == loc) GreenLight else Color.White)
                                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                                    .clickable {
                                        currentLocationText = loc
                                        showLocationDialog = false
                                    }
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = loc,
                                    fontSize = 14.sp,
                                    fontWeight = if (currentLocationText == loc) FontWeight.Bold else FontWeight.Normal,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    OutlinedButton(onClick = { showLocationDialog = false }) {
                        Text("Đóng")
                    }
                }
            )
        }
    }
}

@Composable
private fun RealGoogleMapView(
    places: List<PetPlaceModel>,
    selectedPlaceId: String?,
    onSelectMarker: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedPlace = places.find { it.id == selectedPlaceId } ?: places.firstOrNull()
    val lat = selectedPlace?.lat ?: 10.7769
    val lng = selectedPlace?.lng ?: 106.7009
    val queryName = selectedPlace?.name ?: "Phòng khám thú y TP.HCM"

    // Real Google Maps Live Web Embed URL (Displays real Google Maps satellite & roadmap tiles)
    val googleMapsUrl = remember(lat, lng, queryName) {
        "https://maps.google.com/maps?q=${Uri.encode(queryName + " " + (selectedPlace?.address ?: ""))}&ll=$lat,$lng&z=15&output=embed"
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    webViewClient = WebViewClient()
                    loadUrl(googleMapsUrl)
                }
            },
            update = { webView ->
                webView.loadUrl(googleMapsUrl)
            }
        )
    }
}

@Composable
private fun SingleMapClinicPreviewCard(
    place: PetPlaceModel,
    onClose: () -> Unit,
    onDirectionsClick: () -> Unit,
    onCallClick: () -> Unit,
    onShareClick: () -> Unit,
    onSaveToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.5.dp, GreenPrimary, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = place.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${place.rating}  (${place.reviewCount})",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row {
                    repeat(5) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = place.statusText,
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Pill Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MapActionButton(
                    title = "Directions",
                    icon = NavigationIcon,
                    onClick = onDirectionsClick,
                    modifier = Modifier.weight(1.2f)
                )

                MapActionButton(
                    title = "Call",
                    icon = Icons.Default.Call,
                    onClick = onCallClick,
                    modifier = Modifier.weight(0.9f)
                )

                MapActionButton(
                    title = "Share",
                    icon = Icons.Default.Share,
                    onClick = onShareClick,
                    modifier = Modifier.weight(1f)
                )

                MapActionButton(
                    title = "Save",
                    icon = if (place.isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    iconTint = if (place.isSaved) Color(0xFFE53935) else Color(0xFF2563EB),
                    onClick = onSaveToggle,
                    modifier = Modifier.weight(0.9f)
                )
            }
        }
    }
}

@Composable
private fun PaddingBox(content: @Composable () -> Unit) {
    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
        content()
    }
}

@Composable
private fun MapSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White)
            .border(1.dp, BorderLight, RoundedCornerShape(26.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = TextSecondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Tìm phòng khám, spa thú cưng...",
                            fontSize = 14.sp,
                            color = TextSecondary.copy(alpha = 0.7f)
                        )
                    }
                    inner()
                }
            )

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = FilterIcon,
                    contentDescription = "Bộ lọc",
                    tint = GreenPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoryCircleButton(
    title: String,
    icon: ImageVector,
    bgColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(if (isSelected) GreenPrimary else bgColor)
                .border(if (isSelected) 2.dp else 0.dp, GreenPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) Color.White else GreenPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

@Composable
private fun FeaturedPlaceCard(
    place: PetPlaceModel,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(id = place.imageRes),
            contentDescription = place.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Tag: Được tin chọn
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.25f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Được tin chọn",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Column {
                Text(
                    text = place.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = place.subtitle,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${place.rating} (${place.reviewCount})   ${place.distance}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun NearbyPlaceCard(
    place: PetPlaceModel,
    onSaveToggle: () -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, BorderLight, RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                Image(
                    painter = painterResource(id = place.imageRes),
                    contentDescription = place.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Favorite Heart Button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSaveToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (place.isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Yêu thích",
                        tint = if (place.isSaved) Color(0xFFE53935) else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = place.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = place.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = place.subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = place.rating,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = place.distance,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun MapActionButton(
    title: String,
    icon: ImageVector,
    iconTint: Color = Color(0xFF2563EB),
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFDCE8FF))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2563EB)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MapScreenPreview() {
    PetCareTheme {
        MapScreen()
    }
}
