package com.example.petcare.ui.components

import android.content.Context
import android.util.TypedValue
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.petcare.R
import org.xmlpull.v1.XmlPullParser
import java.io.File

/**
 * Resolves a drawable resource ID guaranteed to be supported by Compose [painterResource]
 * (vector, animated-vector, or raster PNG/JPG/WEBP), preventing crashes from stale persisted IDs.
 */
internal fun resolveRenderableDrawableRes(
    context: Context,
    candidateRes: Int,
    defaultRes: Int = R.drawable.dog,
): Int {
    if (candidateRes == 0) return defaultRes
    return if (isSupportedComposeDrawable(context, candidateRes)) candidateRes else defaultRes
}

private fun isSupportedComposeDrawable(context: Context, resId: Int): Boolean {
    return runCatching {
        val value = TypedValue()
        context.resources.getValue(resId, value, true)
        val path = value.string?.toString().orEmpty().lowercase()
        when {
            path.endsWith(".png") ||
                path.endsWith(".jpg") ||
                path.endsWith(".jpeg") ||
                path.endsWith(".webp") -> true
            path.endsWith(".xml") -> {
                val parser = context.resources.getXml(resId)
                try {
                    var event = parser.eventType
                    while (event != XmlPullParser.START_TAG && event != XmlPullParser.END_DOCUMENT) {
                        event = parser.next()
                    }
                    event == XmlPullParser.START_TAG &&
                        (parser.name == "vector" || parser.name == "animated-vector")
                } finally {
                    parser.close()
                }
            }
            else -> false
        }
    }.getOrDefault(false)
}

/**
 * Renders a pet's stored photo (file path, file://, content://, or https://)
 * with graceful fallback to a local bundled drawable resource.
 */
@Composable
fun PetAvatar(
    photoUri: String,
    fallbackIconRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context = LocalContext.current
    val safeFallback = remember(context, fallbackIconRes) {
        resolveRenderableDrawableRes(context, fallbackIconRes, R.drawable.dog)
    }
    val trimmed = photoUri.trim()
    if (trimmed.isBlank()) {
        Image(
            painter = painterResource(id = safeFallback),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
        )
        return
    }

    val modelData: Any = remember(trimmed) {
        if (trimmed.startsWith("/")) File(trimmed) else trimmed
    }
    val fallbackPainter = painterResource(id = safeFallback)
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(modelData)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        placeholder = fallbackPainter,
        error = fallbackPainter,
        fallback = fallbackPainter,
        modifier = modifier,
        contentScale = contentScale,
    )
}
