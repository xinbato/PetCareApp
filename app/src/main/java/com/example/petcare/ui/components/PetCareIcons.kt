package com.example.petcare.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object PetCareIcons {
    // Star filled ⭐
    val StarFilled: ImageVector by lazy {
        ImageVector.Builder("StarFilled", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color(0xFFFFB300))) {
                moveTo(12f, 17.27f)
                lineTo(18.18f, 21f)
                lineToRelative(-1.64f, -7.03f)
                lineTo(22f, 9.24f)
                lineToRelative(-7.19f, -0.61f)
                lineTo(12f, 2f)
                lineTo(9.19f, 8.63f)
                lineTo(2f, 9.24f)
                lineToRelative(5.46f, 4.73f)
                lineTo(5.82f, 21f)
                close()
            }
        }.build()
    }

    // Star outline ☆
    val StarOutline: ImageVector by lazy {
        ImageVector.Builder("StarOutline", 24.dp, 24.dp, 24f, 24f).apply {
            path(stroke = SolidColor(Color(0xFF9E9E9E)), strokeLineWidth = 2f) {
                moveTo(12f, 17.27f)
                lineTo(18.18f, 21f)
                lineToRelative(-1.64f, -7.03f)
                lineTo(22f, 9.24f)
                lineToRelative(-7.19f, -0.61f)
                lineTo(12f, 2f)
                lineTo(9.19f, 8.63f)
                lineTo(2f, 9.24f)
                lineToRelative(5.46f, 4.73f)
                lineTo(5.82f, 21f)
                close()
            }
        }.build()
    }

    // Plus icon (+)
    val Plus: ImageVector by lazy {
        ImageVector.Builder("Plus", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 13f)
                horizontalLineToRelative(-6f)
                verticalLineToRelative(6f)
                horizontalLineToRelative(-2f)
                verticalLineToRelative(-6f)
                horizontalLineTo(5f)
                verticalLineToRelative(-2f)
                horizontalLineToRelative(6f)
                verticalLineTo(5f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(6f)
                horizontalLineToRelative(6f)
                verticalLineToRelative(2f)
                close()
            }
        }.build()
    }

    // Syringe / Injection (Vaccination) 💉
    val Syringe: ImageVector by lazy {
        ImageVector.Builder("Syringe", 24.dp, 24.dp, 24f, 24f).apply {
            path(stroke = SolidColor(Color(0xFF338C63)), strokeLineWidth = 1.8f) {
                moveTo(14.5f, 2.5f)
                lineTo(16.5f, 4.5f)
                lineTo(15.5f, 5.5f)
                lineTo(13.5f, 3.5f)
                close()
                moveTo(13.5f, 5.5f)
                lineTo(18.5f, 10.5f)
                lineTo(9.5f, 19.5f)
                lineTo(4.5f, 14.5f)
                close()
                moveTo(4.5f, 19.5f)
                lineTo(2f, 22f)
                moveTo(10f, 9f)
                lineTo(13f, 12f)
                moveTo(8f, 11f)
                lineTo(11f, 14f)
            }
        }.build()
    }

    val Pill: ImageVector by lazy {
        ImageVector.Builder("Pill", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color(0xFF338C63))) {
                moveTo(6f, 3f)
                curveTo(4.34f, 3f, 3f, 4.34f, 3f, 6f)
                verticalLineToRelative(4f)
                curveToRelative(0f, 1.66f, 1.34f, 3f, 3f, 3f)
                horizontalLineToRelative(12f)
                curveToRelative(1.66f, 0f, 3f, -1.34f, 3f, -3f)
                verticalLineTo(6f)
                curveToRelative(0f, -1.66f, -1.34f, -3f, -3f, -3f)
                horizontalLineTo(6f)
                close()
                moveTo(14f, 11f)
                horizontalLineTo(10f)
                verticalLineTo(5f)
                horizontalLineToRelative(4f)
                verticalLineToRelative(6f)
                close()
            }
        }.build()
    }

    val Calendar: ImageVector by lazy {
        ImageVector.Builder("Calendar", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color(0xFF338C63))) {
                moveTo(19f, 4f)
                horizontalLineToRelative(-1f)
                verticalLineTo(2f)
                horizontalLineToRelative(-2f)
                verticalLineToRelative(2f)
                horizontalLineTo(8f)
                verticalLineTo(2f)
                horizontalLineTo(6f)
                verticalLineToRelative(2f)
                horizontalLineTo(5f)
                curveToRelative(-1.11f, 0f, -1.99f, 0.9f, -1.99f, 2f)
                lineTo(3f, 20f)
                curveToRelative(0f, 1.1f, 0.89f, 2f, 2f, 2f)
                horizontalLineToRelative(14f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                verticalLineTo(6f)
                curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                close()
                moveTo(19f, 20f)
                horizontalLineTo(5f)
                verticalLineTo(10f)
                horizontalLineToRelative(14f)
                verticalLineToRelative(10f)
                close()
            }
        }.build()
    }

    val Camera: ImageVector by lazy {
        ImageVector.Builder("Camera", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color(0xFF338C63))) {
                moveTo(12f, 12f)
                curveToRelative(1.66f, 0f, 3f, 1.34f, 3f, 3f)
                reflectiveCurveToRelative(-1.34f, 3f, -3f, 3f)
                reflectiveCurveToRelative(-3f, -1.34f, -3f, -3f)
                reflectiveCurveToRelative(1.34f, -3f, 3f, -3f)
                close()
                moveTo(9f, 2f)
                lineTo(7.17f, 4f)
                horizontalLineTo(4f)
                curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
                verticalLineToRelative(12f)
                curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
                horizontalLineToRelative(16f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                verticalLineTo(6f)
                curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                horizontalLineToRelative(-3.17f)
                lineTo(15f, 2f)
                horizontalLineTo(9f)
                close()
                moveTo(12f, 17f)
                curveToRelative(-2.76f, 0f, -5f, -2.24f, -5f, -5f)
                reflectiveCurveToRelative(2.24f, -5f, 5f, -5f)
                reflectiveCurveToRelative(5f, 2.24f, 5f, 5f)
                reflectiveCurveToRelative(-2.24f, 5f, -5f, 5f)
                close()
            }
        }.build()
    }

    val Delete: ImageVector by lazy {
        ImageVector.Builder("Delete", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color(0xFFE53935))) {
                moveTo(6f, 19f)
                curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
                horizontalLineToRelative(8f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                verticalLineTo(7f)
                horizontalLineTo(6f)
                verticalLineToRelative(12f)
                close()
                moveTo(19f, 4f)
                horizontalLineToRelative(-3.5f)
                lineToRelative(-1f, -1f)
                horizontalLineToRelative(-5f)
                lineToRelative(-1f, 1f)
                horizontalLineTo(5f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(14f)
                verticalLineTo(4f)
                close()
            }
        }.build()
    }

    val Edit: ImageVector by lazy {
        ImageVector.Builder("Edit", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color(0xFF338C63))) {
                moveTo(3f, 17.25f)
                verticalLineTo(21f)
                horizontalLineToRelative(3.75f)
                lineTo(17.81f, 9.94f)
                lineToRelative(-3.75f, -3.75f)
                lineTo(3f, 17.25f)
                close()
                moveTo(20.71f, 7.04f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0f, -1.41f)
                lineToRelative(-2.34f, -2.34f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0f)
                lineToRelative(-1.83f, 1.83f)
                lineToRelative(3.75f, 3.75f)
                lineToRelative(1.83f, -1.83f)
                close()
            }
        }.build()
    }

    val Check: ImageVector by lazy {
        ImageVector.Builder("Check", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color(0xFF338C63))) {
                moveTo(9f, 16.17f)
                lineTo(4.83f, 12f)
                lineToRelative(-1.42f, 1.41f)
                lineTo(9f, 19f)
                lineTo(21f, 7f)
                lineToRelative(-1.41f, -1.41f)
                close()
            }
        }.build()
    }

    val Activity: ImageVector by lazy {
        ImageVector.Builder("Activity", 24.dp, 24.dp, 24f, 24f).apply {
            path(stroke = SolidColor(Color(0xFF338C63)), strokeLineWidth = 2f) {
                moveTo(22f, 12f)
                horizontalLineToRelative(-4f)
                lineToRelative(-3f, 9f)
                lineTo(9f, 3f)
                lineToRelative(-3f, 9f)
                horizontalLineTo(2f)
            }
        }.build()
    }

    val WeightScale: ImageVector by lazy {
        ImageVector.Builder("WeightScale", 24.dp, 24.dp, 24f, 24f).apply {
            path(stroke = SolidColor(Color(0xFF338C63)), strokeLineWidth = 1.8f) {
                moveTo(12f, 3f)
                verticalLineToRelative(17f)
                moveTo(8f, 21f)
                horizontalLineToRelative(8f)
                moveTo(5f, 7f)
                horizontalLineToRelative(14f)
                moveTo(5f, 7f)
                lineTo(2f, 13f)
                curveToRelative(0f, 1.66f, 1.34f, 3f, 3f, 3f)
                reflectiveCurveToRelative(3f, -1.34f, 3f, -3f)
                lineTo(5f, 7f)
                close()
                moveTo(19f, 7f)
                lineTo(16f, 13f)
                curveToRelative(0f, 1.66f, 1.34f, 3f, 3f, 3f)
                reflectiveCurveToRelative(3f, -1.34f, 3f, -3f)
                lineTo(19f, 7f)
                close()
            }
        }.build()
    }

    val Stethoscope: ImageVector by lazy {
        ImageVector.Builder("Stethoscope", 24.dp, 24.dp, 24f, 24f).apply {
            path(stroke = SolidColor(Color(0xFF338C63)), strokeLineWidth = 1.8f) {
                moveTo(6f, 4f)
                verticalLineToRelative(6f)
                curveToRelative(0f, 3.31f, 2.69f, 6f, 6f, 6f)
                reflectiveCurveToRelative(6f, -2.69f, 6f, -6f)
                verticalLineTo(4f)
                moveTo(6f, 4f)
                horizontalLineTo(4f)
                moveTo(18f, 4f)
                horizontalLineToRelative(2f)
                moveTo(12f, 16f)
                verticalLineToRelative(3f)
                curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
                horizontalLineToRelative(2f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                verticalLineToRelative(-1f)
                moveTo(18f, 18f)
                arcToRelative(2f, 2f, 0f, true, true, 0f, -0.01f)
            }
        }.build()
    }
}

