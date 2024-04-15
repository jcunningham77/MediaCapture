package mediacapture.io

import android.app.Activity
import android.content.ContentResolver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ActivityContext

@Module
@InstallIn(ActivityRetainedComponent::class)
class ActivityModule {
    @Provides
    fun provideContentResolver(@ActivityContext activity: Activity):ContentResolver{
        return activity.contentResolver
    }
}