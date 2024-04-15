package demoapp.io

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DemoApplication : Application() {
    private val TAG = this.javaClass.simpleName
    init {
        Log.i(TAG, "JEFFREYCUNNINGHAM: : ")
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "JEFFREYCUNNINGHAM: onCreate: ")
    }
}