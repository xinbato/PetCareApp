package com.example.petcare.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Centers content cleanly on tablets and foldables while remaining full width on phones.
 */
@Composable
fun PetCareCenteredContent(
    modifier: Modifier = Modifier,
    maxWidth: Dp = 840.dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .fillMaxWidth(),
        ) {
            content()
        }
    }
}

/**
 * Switches between phone bottom NavigationBar and tablet NavigationRail at >= 600.dp width,
 * and caps wide content width cleanly at 840.dp.
 */
@Composable
fun PetCareAdaptiveTabScaffold(
    currentTab: PetCareTab,
    onTabSelected: (PetCareTab) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.background,
    maxContentWidth: Dp = 840.dp,
    floatingActionButton: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(containerColor),
    ) {
        val useNavRail = maxWidth >= 600.dp
        if (useNavRail) {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    PetCareTab.entries.forEach { tab ->
                        val selected = tab == currentTab
                        NavigationRailItem(
                            selected = selected,
                            onClick = { onTabSelected(tab) },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1,
                                )
                            },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
                Scaffold(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    containerColor = containerColor,
                    floatingActionButton = floatingActionButton,
                    snackbarHost = snackbarHost,
                ) { innerPadding ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = maxContentWidth)
                                .fillMaxSize(),
                        ) {
                            content(innerPadding)
                        }
                    }
                }
            }
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = containerColor,
                bottomBar = {
                    PetCareTabBar(
                        currentTab = currentTab,
                        onTabSelected = onTabSelected,
                    )
                },
                floatingActionButton = floatingActionButton,
                snackbarHost = snackbarHost,
                content = content,
            )
        }
    }
}
