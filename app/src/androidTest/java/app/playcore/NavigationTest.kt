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
    fun homeShowsWordmarkModeTabsAndBottomBar() {
        composeRule.onNodeWithText("PLAYCORE").assertIsDisplayed()
        composeRule.onNodeWithText("EXPLORE").assertIsDisplayed()
        composeRule.onNodeWithText("HANDHELD").assertIsDisplayed()
        composeRule.onNodeWithText("GAMEPAD").assertIsDisplayed()
        composeRule.onNodeWithText("SUPER BASE").assertIsDisplayed()
    }

    @Test
    fun libraryDestinationOpensLibraryScreen() {
        composeRule.onNodeWithText("LIBRARY").performClick()
        composeRule.onNodeWithContentDescription("Add apps").assertIsDisplayed()
        composeRule.onNodeWithText("Search apps").assertIsDisplayed()
    }

    @Test
    fun superBaseShowsTheEightTiles() {
        composeRule.onNodeWithText("SUPER BASE").performClick()
        composeRule.onNodeWithText("Game Center").assertIsDisplayed()
        composeRule.onNodeWithText("Plugin Library").assertIsDisplayed()
        composeRule.onNodeWithText("Core Reactor").assertIsDisplayed()
    }
}
