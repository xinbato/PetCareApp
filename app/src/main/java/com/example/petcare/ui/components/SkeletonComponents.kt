package com.example.petcare.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Provides an adaptive shimmer brush synchronized across placeholders on screen.
 */
@Composable
fun rememberShimmerBrush(): Brush {
    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = MaterialTheme.colorScheme.surface
    val transition = rememberInfiniteTransition(label = "petcare_shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "petcare_shimmer_progress",
    )
    return Brush.linearGradient(
        colors = listOf(
            base.copy(alpha = 0.85f),
            highlight.copy(alpha = 0.9f),
            base.copy(alpha = 0.85f),
        ),
        start = Offset(progress - 320f, 0f),
        end = Offset(progress, 320f),
    )
}

@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    brush: Brush = rememberShimmerBrush(),
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(brush),
    )
}

@Composable
fun SmoothSkeletonWrapper(
    isLoading: Boolean,
    skeleton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    AnimatedContent(
        targetState = isLoading,
        transitionSpec = {
            fadeIn(tween(260)) togetherWith fadeOut(tween(200))
        },
        modifier = modifier,
        label = "skeleton_transition",
    ) { loading ->
        if (loading) skeleton() else content()
    }
}

@Composable
fun HomeSkeleton(modifier: Modifier = Modifier) {
    val brush = rememberShimmerBrush()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonBox(Modifier.width(120.dp).height(14.dp), brush = brush)
                SkeletonBox(Modifier.width(200.dp).height(24.dp), brush = brush)
            }
            SkeletonBox(Modifier.size(44.dp), shape = CircleShape, brush = brush)
        }
        SkeletonBox(Modifier.fillMaxWidth().height(152.dp), shape = RoundedCornerShape(20.dp), brush = brush)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(3) {
                SkeletonBox(
                    Modifier.weight(1f).height(88.dp),
                    shape = RoundedCornerShape(16.dp),
                    brush = brush,
                )
            }
        }
        SkeletonBox(Modifier.width(170.dp).height(20.dp), brush = brush)
        repeat(3) {
            SkeletonBox(
                Modifier.fillMaxWidth().height(84.dp),
                shape = RoundedCornerShape(16.dp),
                brush = brush,
            )
        }
    }
}

@Composable
fun PetsSkeleton(modifier: Modifier = Modifier) {
    val brush = rememberShimmerBrush()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SkeletonBox(Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(24.dp), brush = brush)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(4) {
                SkeletonBox(Modifier.width(72.dp).height(34.dp), shape = CircleShape, brush = brush)
            }
        }
        repeat(4) {
            SkeletonBox(
                Modifier.fillMaxWidth().height(98.dp),
                shape = RoundedCornerShape(18.dp),
                brush = brush,
            )
        }
    }
}

@Composable
fun HealthSkeleton(modifier: Modifier = Modifier) {
    val brush = rememberShimmerBrush()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SkeletonBox(Modifier.fillMaxWidth().height(110.dp), shape = RoundedCornerShape(18.dp), brush = brush)
        repeat(4) {
            SkeletonBox(
                Modifier.fillMaxWidth().height(76.dp),
                shape = RoundedCornerShape(16.dp),
                brush = brush,
            )
        }
    }
}

@Composable
fun ScheduleSkeleton(modifier: Modifier = Modifier) {
    val brush = rememberShimmerBrush()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SkeletonBox(Modifier.fillMaxWidth().height(72.dp), shape = RoundedCornerShape(16.dp), brush = brush)
        repeat(4) {
            SkeletonBox(
                Modifier.fillMaxWidth().height(84.dp),
                shape = RoundedCornerShape(16.dp),
                brush = brush,
            )
        }
    }
}

@Composable
fun NearbySkeleton(modifier: Modifier = Modifier) {
    val brush = rememberShimmerBrush()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SkeletonBox(Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(14.dp), brush = brush)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) {
                SkeletonBox(Modifier.width(96.dp).height(36.dp), shape = CircleShape, brush = brush)
            }
        }
        repeat(4) {
            SkeletonBox(
                Modifier.fillMaxWidth().height(126.dp),
                shape = RoundedCornerShape(18.dp),
                brush = brush,
            )
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
fun ProfileSkeleton(modifier: Modifier = Modifier) {
    val brush = rememberShimmerBrush()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SkeletonBox(Modifier.size(88.dp), shape = CircleShape, brush = brush)
        SkeletonBox(Modifier.width(180.dp).height(22.dp), brush = brush)
        SkeletonBox(Modifier.width(140.dp).height(14.dp), brush = brush)
        repeat(5) {
            SkeletonBox(
                Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(16.dp),
                brush = brush,
            )
        }
    }
}
