package mediacapture.io

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MediaCaptureActivityTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MediaCaptureActivity::class.java)


}