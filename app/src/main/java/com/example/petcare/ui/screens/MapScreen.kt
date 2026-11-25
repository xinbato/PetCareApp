package com.example.petcare.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.style.TextOverflow
import com.example.petcare.data.model.PetPlaceModel
import com.example.petcare.data.model.resolvePetIconRes
import com.example.petcare.ui.components.NearbySkeleton
import com.example.petcare.ui.components.PetCareAdaptiveTabScaffold
import com.example.petcare.ui.components.PetCareTab
import com.example.petcare.ui.components.PetCareTabBar
import com.example.petcare.ui.components.SmoothSkeletonWrapper
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.viewmodel.MapViewModel
import com.example.petcare.ui.viewmodel.SettingsViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    onTabSelected: (PetCareTab) -> Unit = {},
) {
    var isMapView by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var areaSearchInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tất cả") }
    var selectedPlaceId by remember { mutableStateOf<String?>(null) }
    var showLocationDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by settingsViewModel.settings.collectAsState()
    val places = uiState.places
    val currentLocationText = uiState.locationLabel
    val fusedClient = remember(context) { LocationServices.getFusedLocationProviderClient(context) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ) == PackageManager.PERMISSION_GRANTED,
        )
    }

    fun fallbackSystemLocation(): Location? {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        if (!fineGranted && !coarseGranted) return null
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val candidates = listOfNotNull(
            if (fineGranted) runCatching { lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) }.getOrNull() else null,
            runCatching { lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) }.getOrNull(),
        )
        return candidates.maxByOrNull { it.time }
    }

    fun fetchCurrentGpsAndReload(radius: Int = settings.defaultSearchRadiusMeters) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        if (!fineGranted && !coarseGranted) return

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (lm != null && !LocationManagerCompat.isLocationEnabled(lm)) {
            viewModel.reportLocationError("Vui lòng bật Vị trí (GPS) trên điện thoại hoặc tìm theo tên khu vực.")
            return
        }

        val tokenSource = CancellationTokenSource()
        val priority = if (fineGranted) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY

        fun tryFallbackOrReport() {
            fusedClient.lastLocation
                .addOnSuccessListener { last ->
                    val resolved = last ?: fallbackSystemLocation()
                    if (resolved != null) {
                        viewModel.loadNearbyFromCoordinates(
                            lat = resolved.latitude,
                            lon = resolved.longitude,
                            label = "Vị trí gần nhất (GPS)",
                            radiusMeters = radius,
                        )
                    } else {
                        viewModel.reportLocationError("Không thể lấy tọa độ GPS. Hãy kiểm tra tín hiệu GPS hoặc tìm theo tên khu vực.")
                    }
                }
                .addOnFailureListener {
                    val resolved = fallbackSystemLocation()
                    if (resolved != null) {
                        viewModel.loadNearbyFromCoordinates(
                            lat = resolved.latitude,
                            lon = resolved.longitude,
                            label = "Vị trí gần nhất (GPS)",
                            radiusMeters = radius,
                        )
                    } else {
                        viewModel.reportLocationError("Không thể lấy tọa độ GPS. Hãy tìm theo tên khu vực.")
                    }
                }
        }

        runCatching {
            fusedClient.getCurrentLocation(priority, tokenSource.token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.loadNearbyFromCoordinates(
                            lat = location.latitude,
                            lon = location.longitude,
                            label = "Vị trí hiện tại (GPS)",
                            radiusMeters = radius,
                        )
                    } else {
                        tryFallbackOrReport()
                    }
                }
                .addOnFailureListener {
                    tryFallbackOrReport()
                }
        }.onFailure {
            tryFallbackOrReport()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
        if (granted) {
            fetchCurrentGpsAndReload(settings.defaultSearchRadiusMeters)
        }
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission) {
            fetchCurrentGpsAndReload(settings.defaultSearchRadiusMeters)
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    LaunchedEffect(settings.defaultSearchRadiusMeters) {
        if (uiState.selectedRadiusMeters != settings.defaultSearchRadiusMeters) {
            viewModel.changeRadius(settings.defaultSearchRadiusMeters)
        }
    }

    val filteredPlaces = remember(searchQuery, selectedCategory, places) {
        places.filter { place ->
            val matchesCategory = selectedCategory == "Tất cả" || place.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                place.name.lowercase().contains(searchQuery.lowercase()) ||
                place.subtitle.lowercase().contains(searchQuery.lowercase())
            matchesCategory && matchesSearch
        }
    }

    PetCareAdaptiveTabScaffold(
        currentTab = PetCareTab.MAP,
        onTabSelected = onTabSelected,
        modifier = modifier,
    ) { innerPadding ->
        SmoothSkeletonWrapper(
            isLoading = uiState.isLoading && places.isEmpty(),
            skeleton = {
                NearbySkeleton(
                    modifier = Modifier
                        .padding(innerPadding)
                        .statusBarsPadding(),
                )
            },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                ) {
                    // Main Top Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bản đồ",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${filteredPlaces.size} cơ sở gần $currentLocationText (${uiState.selectedRadiusMeters / 1000} km)",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                    .clickable {
                                        if (hasLocationPermission) {
                                            fetchCurrentGpsAndReload(uiState.selectedRadiusMeters)
                                        } else {
                                            permissionLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                                ),
                                            )
                                        }
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "Vị trí GPS",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp),
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                    .clickable { isMapView = !isMapView },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = if (isMapView) Icons.AutoMirrored.Filled.FormatListBulleted else Icons.Default.LocationOn,
                                    contentDescription = if (isMapView) "Xem danh sách" else "Xem bản đồ",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }
                    }

                    uiState.errorMessage?.let { err ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = err,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f),
                            )
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Đóng",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { viewModel.clearError() },
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
                        .verticalScroll(rememberScrollState()),
                ) {
                    // Search Bar
                    PaddingBox {
                        MapSearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category Filter Badges (Thú y, Spa, Cửa hàng)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        CategoryCircleButton(
                            title = "Thú y",
                            icon = Icons.Default.MedicalServices,
                            bgColor = MaterialTheme.colorScheme.secondaryContainer,
                            isSelected = selectedCategory == "Thú y",
                            onClick = { selectedCategory = if (selectedCategory == "Thú y") "Tất cả" else "Thú y" },
                        )

                        CategoryCircleButton(
                            title = "Spa",
                            icon = Icons.Default.ContentCut,
                            bgColor = MaterialTheme.colorScheme.tertiaryContainer,
                            isSelected = selectedCategory == "Spa",
                            onClick = { selectedCategory = if (selectedCategory == "Spa") "Tất cả" else "Spa" },
                        )

                        CategoryCircleButton(
                            title = "Cửa hàng",
                            icon = Icons.Default.ShoppingBag,
                            bgColor = MaterialTheme.colorScheme.surfaceVariant,
                            isSelected = selectedCategory == "Cửa hàng",
                            onClick = { selectedCategory = if (selectedCategory == "Cửa hàng") "Tất cả" else "Cửa hàng" },
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Section 1: Gợi ý cho bạn
                    PaddingBox {
                        Text(
                            text = "Gợi ý cho bạn",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
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
                                },
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
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        filteredPlaces.forEach { place ->
                            NearbyPlaceCard(
                                place = place,
                                onSaveToggle = { viewModel.toggleSavePlace(place.id) },
                                onClick = {
                                    selectedPlaceId = place.id
                                    isMapView = true
                                },
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
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Vị trí",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Đang hiển thị dịch vụ quanh $currentLocationText",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium,
                                )
                            }

                            Text(
                                text = "Đổi",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clickable { showLocationDialog = true }
                                    .padding(start = 8.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            } else {
                // ================= MODE 2: NATIVE GOOGLE MAPS SDK VIEW =================
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                ) {
                    NativeGoogleMapView(
                        places = filteredPlaces,
                        selectedPlaceId = selectedPlaceId,
                        hasLocationPermission = hasLocationPermission,
                        onSelectMarker = { placeId ->
                            selectedPlaceId = placeId
                        },
                    )

                    // Floating Search bar on top of map
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        MapSearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                        )
                    }

                    val selectedPlace = filteredPlaces.find { it.id == selectedPlaceId }
                    selectedPlace?.let { place ->
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
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
                                    viewModel.toggleSavePlace(place.id)
                                },
                            )
                        }
                    }
                }
            }

                } // close Column inside Box
            }

            // Location Change & Search Dialog
            if (showLocationDialog) {
                AlertDialog(
                    onDismissRequest = { showLocationDialog = false },
                    title = { Text("Chọn hoặc tìm khu vực", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                OutlinedTextField(
                                    value = areaSearchInput,
                                    onValueChange = { areaSearchInput = it },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Nhập quận, thành phố...") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                )
                                Button(
                                    onClick = {
                                        if (areaSearchInput.isNotBlank()) {
                                            viewModel.searchByAreaName(areaSearchInput)
                                            showLocationDialog = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    Text("Tìm", fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                listOf(3000 to "3 km", 5000 to "5 km", 10000 to "10 km").forEach { (r, label) ->
                                    FilterChip(
                                        selected = uiState.selectedRadiusMeters == r,
                                        onClick = { viewModel.changeRadius(r) },
                                        label = { Text(label, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        ),
                                    )
                                }
                            }

                            listOf("Quận 1, TP.HCM", "Quận 3, TP.HCM", "Hà Nội", "Đà Nẵng", "Thủ Đức, TP.HCM").forEach { loc ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (currentLocationText == loc) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface)
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                        .clickable {
                                            viewModel.searchByAreaName(loc)
                                            showLocationDialog = false
                                        }
                                        .padding(12.dp),
                                ) {
                                    Text(
                                        text = loc,
                                        fontSize = 14.sp,
                                        fontWeight = if (currentLocationText == loc) FontWeight.Bold else FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface,
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
                    },
                )
            }
        }
    }
}

@Composable
private fun NativeGoogleMapView(
    places: List<PetPlaceModel>,
    selectedPlaceId: String?,
    hasLocationPermission: Boolean,
    onSelectMarker: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedPlace = places.find { it.id == selectedPlaceId } ?: places.firstOrNull()
    val centerLatLng = LatLng(selectedPlace?.lat ?: 10.7769, selectedPlace?.lng ?: 106.7009)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(centerLatLng, 14f)
    }

    val uiSettings = remember(hasLocationPermission) {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = hasLocationPermission,
            compassEnabled = true,
        )
    }

    val mapProperties = remember(hasLocationPermission) {
        MapProperties(
            isMyLocationEnabled = hasLocationPermission,
        )
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = uiSettings,
        properties = mapProperties,
    ) {
        places.forEach { place ->
            val markerState = remember(place.id) {
                MarkerState(position = LatLng(place.lat, place.lng))
            }
            Marker(
                state = markerState,
                title = place.name,
                snippet = "${place.category} • ${place.address}",
                onClick = {
                    onSelectMarker(place.id)
                    false
                },
            )
        }
    }
}

@Composable
private fun SingleMapClinicPreviewCard(
    place: PetPlaceModel,
    onClose: () -> Unit,
    onDirectionsClick: () -> Unit,
    onCallClick: () -> Unit,
    onShareClick: () -> Unit,
    onSaveToggle: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = place.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${place.rating}  (${place.reviewCount})",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row {
                    repeat(5) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(15.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = place.statusText,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Pill Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MapActionButton(
                    title = "Directions",
                    icon = Icons.Default.Navigation,
                    onClick = onDirectionsClick,
                    modifier = Modifier.weight(1.2f),
                )

                MapActionButton(
                    title = "Call",
                    icon = Icons.Default.Call,
                    onClick = onCallClick,
                    modifier = Modifier.weight(0.9f),
                )

                MapActionButton(
                    title = "Share",
                    icon = Icons.Default.Share,
                    onClick = onShareClick,
                    modifier = Modifier.weight(1f),
                )

                MapActionButton(
                    title = "Save",
                    icon = if (place.isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    iconTint = if (place.isSaved) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    onClick = onSaveToggle,
                    modifier = Modifier.weight(0.9f),
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
    onQueryChange: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(26.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                    inner()
                },
            )

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Bộ lọc",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
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
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() },
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(if (isSelected) MaterialTheme.colorScheme.primary else bgColor)
                .border(if (isSelected) 2.dp else 0.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun FeaturedPlaceCard(
    place: PetPlaceModel,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
    ) {
        Image(
            painter = painterResource(id = resolvePetIconRes(place.imageRes)),
            contentDescription = place.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f)),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.25f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "Được tin chọn",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            Column {
                Text(
                    text = place.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = place.subtitle,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${place.rating} (${place.reviewCount})   ${place.distance}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
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
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            .clickable { onClick() },
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
            ) {
                Image(
                    painter = painterResource(id = resolvePetIconRes(place.imageRes)),
                    contentDescription = place.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onSaveToggle() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (place.isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Yêu thích",
                        tint = if (place.isSaved) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = place.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = place.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = place.subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = place.rating,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    Text(
                        text = place.distance,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    iconTint: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
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
