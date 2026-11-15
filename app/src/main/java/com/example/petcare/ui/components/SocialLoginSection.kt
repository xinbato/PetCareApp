package com.example.petcare.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petcare.R
import com.example.petcare.ui.theme.BorderLight
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.theme.TextPrimary
import com.example.petcare.ui.theme.TextSecondary

@Composable
fun SocialLoginSection(
    modifier: Modifier = Modifier,
    enabled: Boolean = false,
    onGoogleClick: () -> Unit = {},
    onYahooClick: () -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.or_divider),
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedButton(
                onClick = onGoogleClick,
                enabled = enabled,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderLight),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    disabledContainerColor = Color.White.copy(alpha = 0.6f),
                    contentColor = TextPrimary,
                    disabledContentColor = TextPrimary.copy(alpha = 0.5f),
                ),
            ) {
                Text(
                    text = stringResource(R.string.btn_google),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            OutlinedButton(
                onClick = onYahooClick,
                enabled = enabled,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderLight),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    disabledContainerColor = Color.White.copy(alpha = 0.6f),
                    contentColor = TextPrimary,
                    disabledContentColor = TextPrimary.copy(alpha = 0.5f),
                ),
            ) {
                Text(
                    text = stringResource(R.string.btn_yahoo),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SocialLoginSectionPreview() {
    PetCareTheme {
        SocialLoginSection()
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SocialLoginSectionDarkPreview() {
    PetCareTheme(darkTheme = true) {
        SocialLoginSection()
    }
}
