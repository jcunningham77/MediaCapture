package mediacapture.io.di

import android.content.Context

class DaggerInjector {

    companion object {

        private var appComponentInstance: AppComponent? = null

        fun appComponent(context: Context): AppComponent {
            appComponentInstance?.let {
                return it
            } ?:run {
                appComponentInstance = DaggerAppComponent.factory().create(context)
                return appComponentInstance as AppComponent
            }
        }

    }
}