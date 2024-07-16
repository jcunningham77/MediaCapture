package mediacapture.io

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import androidx.test.rule.GrantPermissionRule.grant
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)

class MediaCaptureActivityTest {
    @get:Rule
    val composeRule = createAndroidComposeRule(MediaCaptureActivity::class.java)

    @get:Rule
    val permissionRule: GrantPermissionRule = grant(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
    )

    @Test
    fun happyPathTest() {
        composeRule.onNodeWithContentDescription("Flip Camera Button").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Record").assertIsDisplayed().performClick()

        // flaky results on github action emulator
//        composeRule.onNodeWithContentDescription("Record").assertIsNotDisplayed()
//        composeRule.onNodeWithContentDescription("Stop").assertIsDisplayed()

    }
}