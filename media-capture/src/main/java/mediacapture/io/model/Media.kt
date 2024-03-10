package mediacapture.io.model

import android.graphics.Bitmap
import android.net.Uri

data class Media(
    val uri: Uri,
    val thumbnailUri: Bitmap? = null,
    val name: String,
    val duration: Int,
    val size: Int,
    val mediaType: MediaType = MediaType.VIDEO
)