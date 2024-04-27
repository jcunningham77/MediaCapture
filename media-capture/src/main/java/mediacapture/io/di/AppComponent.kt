package mediacapture.io.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import javax.inject.Named

@Component(modules = [MediaCaptureComponent::class])
interface AppComponent {

    @Component.Factory
    interface Factory {

        // applicationContext
        fun create(@BindsInstance @Named("applicationContext") context: Context): AppComponent
    }
}