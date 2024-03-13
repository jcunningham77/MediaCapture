package mediacapture.io.model

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Media(
    val uri: Uri,
    val thumbnailUri: Bitmap? = null,
    val name: String,
    val duration: Int,
    val size: Int,
    val mediaType: MediaType = MediaType.VIDEO
) : Parcelable