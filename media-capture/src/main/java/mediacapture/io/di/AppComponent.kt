package mediacapture.io.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component

@Component
interface AppComponent {

    @Component.Factory
    interface Factory {

        // applicationContext
        fun create(@BindsInstance context: Context): AppComponent
    }



}