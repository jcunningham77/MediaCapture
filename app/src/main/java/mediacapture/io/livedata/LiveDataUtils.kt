package mediacapture.io.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.toLiveData
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable


fun <T> Observable<T>.observe(lifecycleOwner: LifecycleOwner, block: (T) -> Unit) {
    toFlowable(BackpressureStrategy.BUFFER).toLiveData()
        .observe(lifecycleOwner) { block.invoke(it!!) }

}

