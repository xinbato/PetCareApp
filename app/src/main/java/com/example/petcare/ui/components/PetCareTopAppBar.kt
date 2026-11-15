package com.example.petcare.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petcare.R
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.theme.TextPrimary
import com.example.petcare.ui.theme.TextSecondary

val KeyboardArrowLeftIcon: ImageVector by lazy {
    ImageVector.Builder("KeyboardArrowLeft", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(TextPrimary)) {
            moveTo(15.41f, 16.59f)
            lineTo(10.83f, 12f)
            lineTo(15.41f, 7.41f)
            lineTo(14f, 6f)
            lineToRelative(-6f, 6f)
            lineToRelative(6f, 6f)
            lineTo(15.41f, 16.59f)
            close()
        }
    }.build()
}

@Composable
fun PetCareTopAppBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(start = 0.dp),
        ) {
            Icon(
                imageVector = KeyboardArrowLeftIcon,
                contentDescription = stringResource(R.string.cd_back_button),
                tint = TextPrimary,
                modifier = Modifier.size(36.dp),
            )
        }

        if (title != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
        }

        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PetCareTopAppBarPreview() {
    PetCareTheme {
        PetCareTopAppBar(
            title = "Đăng nhập",
            subtitle = "Theo dõi sức khỏe và lịch chăm sóc",
            onBackClick = {},
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PetCareTopAppBarDarkPreview() {
    PetCareTheme(darkTheme = true) {
        PetCareTopAppBar(
            title = "Đăng nhập",
            subtitle = "Theo dõi sức khỏe và lịch chăm sóc",
            onBackClick = {},
        )
    }
}
