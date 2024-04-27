package mediacapture.io.di

import android.content.Context
import android.util.Log

class DaggerInjector {
    companion object {
        private val TAG = this.javaClass.simpleName

        private var appComponentInstance: AppComponent? = null

        fun appComponent(context: Context): AppComponent {
            appComponentInstance?.let {
                Log.i(
                    TAG,
                    "JEFFREYCUNNINGHAM: appComponent: returning already created appComponentInstance: ${it.hashCode()}"
                )
                return it
            } ?: run {

                appComponentInstance = DaggerAppComponent.factory().create(context)
                Log.i(
                    TAG,
                    "JEFFREYCUNNINGHAM: appComponent: created new appComponentInstance: ${appComponentInstance.hashCode()}"
                )
                return appComponentInstance as AppComponent
            }
        }

    }
}