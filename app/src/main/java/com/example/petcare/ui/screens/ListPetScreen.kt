package com.example.petcare.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petcare.data.model.PetModel
import com.example.petcare.ui.components.KeyboardArrowLeftIcon
import com.example.petcare.ui.components.PetCareIcons
import com.example.petcare.ui.components.PetAvatar
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextOverflow
import com.example.petcare.ui.components.PetCareAdaptiveTabScaffold
import com.example.petcare.ui.components.PetCareTab
import com.example.petcare.ui.components.PetsSkeleton
import com.example.petcare.ui.components.SmoothSkeletonWrapper
import com.example.petcare.ui.viewmodel.SettingsViewModel
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.viewmodel.PetManagementViewModel

@Composable
fun ListPetScreen(
    modifier: Modifier = Modifier,
    currentTab: PetCareTab = PetCareTab.HOME,
    viewModel: PetManagementViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onPetClick: (PetModel) -> Unit = {},
    onEditPetClick: () -> Unit = {},
    onAddPetClick: () -> Unit = {},
    onTabSelected: (PetCareTab) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pets = uiState.pets
    var searchQuery by remember { mutableStateOf("") }
    var selectedSpeciesFilter by remember { mutableStateOf("Tất cả") }
    val colorScheme = MaterialTheme.colorScheme

    val filteredPets = remember(pets, searchQuery, selectedSpeciesFilter) {
        val q = searchQuery.trim()
        pets.filter { pet ->
            val matchesQuery = q.isEmpty() ||
                pet.name.contains(q, ignoreCase = true) ||
                pet.breed.contains(q, ignoreCase = true) ||
                pet.species.contains(q, ignoreCase = true)
            val matchesSpecies = when (selectedSpeciesFilter) {
                "Tất cả" -> true
                "Chó" -> pet.species.contains("Chó", ignoreCase = true)
                "Mèo" -> pet.species.contains("Mèo", ignoreCase = true)
                else -> !pet.species.contains("Chó", ignoreCase = true) &&
                    !pet.species.contains("Mèo", ignoreCase = true)
            }
            matchesQuery && matchesSpecies
        }
    }

    PetCareAdaptiveTabScaffold(
        currentTab = currentTab,
        onTabSelected = onTabSelected,
        modifier = modifier,
    ) { innerPadding ->
        SmoothSkeletonWrapper(
            isLoading = uiState.isLoading && pets.isEmpty(),
            skeleton = {
                PetsSkeleton(
                    modifier = Modifier
                        .padding(innerPadding)
                        .statusBarsPadding(),
                )
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding(),
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                imageVector = KeyboardArrowLeftIcon,
                                contentDescription = "Quay lại",
                                tint = colorScheme.onBackground,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Danh sách thú cưng",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onBackground,
                            )
                            Text(
                                text = "Danh sách thú cưng của bạn",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    // Edit pill button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(colorScheme.primary)
                            .clickable {
                                onEditPetClick()
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Chỉnh sửa",
                            color = colorScheme.onPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                // Search & Species Filter
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Tìm theo tên, giống hoặc loài...") },
                        leadingIcon = {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Search,
                                contentDescription = "Tìm kiếm",
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf("Tất cả", "Chó", "Mèo", "Khác").forEach { sp ->
                            val selected = selectedSpeciesFilter == sp
                            FilterChip(
                                selected = selected,
                                onClick = { selectedSpeciesFilter = sp },
                                label = { Text(sp, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colorScheme.primaryContainer,
                                    selectedLabelColor = colorScheme.onPrimaryContainer,
                                ),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Pet list
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(filteredPets, key = { it.id }) { pet ->
                        PetListItemCard(
                            pet = pet,
                            onClick = {
                                viewModel.selectPet(pet.id)
                                onPetClick(pet)
                            },
                            onToggleFavorite = {
                                viewModel.toggleFavorite(pet.id)
                            },
                        )
                    }

                    // Add pet card button
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(84.dp)
                                .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                                .clickable(onClick = onAddPetClick),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    imageVector = PetCareIcons.Plus,
                                    contentDescription = "Thêm thú cưng",
                                    tint = colorScheme.onSurface,
                                    modifier = Modifier.size(28.dp),
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Thêm thú cưng",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorScheme.primary,
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PetListItemCard(
    pet: PetModel,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    PetAvatar(
                        photoUri = pet.photoUri,
                        fallbackIconRes = pet.safeIconRes,
                        contentDescription = pet.name,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = pet.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = pet.statusText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (pet.statusText == "Khỏe mạnh") colorScheme.primary else colorScheme.error,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = pet.statusDetail.ifEmpty { "Cân nặng ${pet.weight}" },
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Star favorite toggle
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (pet.isFavorite) PetCareIcons.StarFilled else PetCareIcons.StarOutline,
                    contentDescription = "Yêu thích",
                    tint = if (pet.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListPetScreenPreview() {
    PetCareTheme {
        ListPetScreen()
    }
}
