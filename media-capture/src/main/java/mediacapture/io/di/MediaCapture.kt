package mediacapture.io.di

import android.content.ContentResolver
import dagger.BindsInstance
import dagger.Subcomponent
import mediacapture.io.MediaCaptureActivity

@Subcomponent
interface MediaCaptureComponent {

    @Subcomponent.Factory
    interface Factory {

        // applicationContext
        fun create(@BindsInstance contentResolver: ContentResolver): MediaCaptureComponent
    }

    fun inject(activity: MediaCaptureActivity)
}
