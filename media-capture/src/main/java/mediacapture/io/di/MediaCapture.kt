package mediacapture.io.di

import dagger.Subcomponent
import dagger.Module
import mediacapture.io.MediaCaptureActivity

@Subcomponent(modules = [MediaCaptureModule::class])
interface MediaCaptureComponent{

    fun inject(activity: MediaCaptureActivity)
}

@Module
abstract class MediaCaptureModule {


}