package mediacapture.io.utils

import android.content.Context

fun Int.dpToPx(context: Context): Int {

    val scale = context.resources.displayMetrics.density
    return this * scale.toInt()

}