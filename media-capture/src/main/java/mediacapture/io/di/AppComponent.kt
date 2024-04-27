package mediacapture.io.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import javax.inject.Named

@Component(modules = [AppSubComponents::class])
interface AppComponent {

    @Component.Factory
    interface Factory {

        fun create(@BindsInstance @Named("applicationContext") context: Context): AppComponent
    }

    fun mediaCaptureComponent(): MediaCaptureComponent.Factory
}