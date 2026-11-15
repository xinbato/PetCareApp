package com.example.petcare.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.petcare.ui.theme.PetCareTheme

enum class PetCareTab(val title: String, val icon: ImageVector) {
    HOME("Trang chủ", Icons.Default.Home),
    HEALTH("Sức khỏe", Icons.Default.Favorite),
    CALENDAR("Lịch", Icons.Default.DateRange),
    MAP("Bản đồ", Icons.Default.Place),
    PROFILE("Tôi", Icons.Default.Person)
}

/**
 * Standard Material 3 Navigation Bar for PetCareApp.
 * Built with native NavigationBar and NavigationBarItem.
 */
@Composable
fun PetCareTabBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onAddPetClick: (() -> Unit)? = null,
) {
    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        PetCareTab.entries.forEachIndexed { index, tab ->
            val isSelected = selectedTab == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun PetCareTabBar(
    currentTab: PetCareTab,
    onTabSelected: (PetCareTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    PetCareTabBar(
        selectedTab = currentTab.ordinal,
        onTabSelected = { index -> onTabSelected(PetCareTab.entries.getOrElse(index) { PetCareTab.HOME }) },
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun PetCareTabBarPreview() {
    PetCareTheme {
        PetCareTabBar(
            selectedTab = 0,
            onTabSelected = {}
        )
    }
}
