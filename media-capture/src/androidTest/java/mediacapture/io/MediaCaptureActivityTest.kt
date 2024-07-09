package mediacapture.io

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)

class MediaCaptureActivityTest {
    @get:Rule
    val composeRule = createAndroidComposeRule(MediaCaptureActivity::class.java)

    @Test
    fun flipButtonTest() {
        composeRule.onNodeWithContentDescription("Flip Camera Button").assertIsDisplayed()
    }
}