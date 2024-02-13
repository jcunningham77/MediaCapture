package mediacapture.io

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

class LifecycleLogger: LifecycleEventObserver {
    fun registerLifecycle(lifecycle: Lifecycle){
        Log.i(TAG, "JEFFREYCUNNINGHAM: registerLifecycle: lifecyclehash: ${lifecycle.hashCode()}")
        lifecycle.addObserver(this)
    }

    private val TAG = this.javaClass.simpleName
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        Log.d(TAG, "JEFFREYCUNNINGHAM onStateChanged: source: $source, event: $event")
    }


}