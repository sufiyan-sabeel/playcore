package app.playcore

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Navigation and empty-state UI tests. These need a device or emulator with the
 * app installed; they are not part of the CI unit-test job.
 */
@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeShowsWordmarkAndNavigation() {
        composeRule.onNodeWithText("PLAYCORE").assertIsDisplayed()
        composeRule.onNodeWithText("Library").assertIsDisplayed()
        composeRule.onNodeWithText("Dashboard").assertIsDisplayed()
        composeRule.onNodeWithText("Tools").assertIsDisplayed()
        composeRule.onNodeWithText("Settings").assertIsDisplayed()
    }

    @Test
    fun libraryTabOpensLibraryScreen() {
        composeRule.onNodeWithText("Library").performClick()
        composeRule.onNodeWithContentDescription("Add apps").assertIsDisplayed()
        composeRule.onNodeWithText("Search name or package").assertIsDisplayed()
    }

    @Test
    fun settingsTabShowsAppearanceSection() {
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithText("Appearance").assertIsDisplayed()
        composeRule.onNodeWithText("AMOLED black").assertIsDisplayed()
    }
}
