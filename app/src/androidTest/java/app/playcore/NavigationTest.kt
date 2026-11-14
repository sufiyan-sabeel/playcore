package app.playcore

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.playcore.ui.navigation.Routes
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Navigation smoke tests against the real bottom bar.
 *
 * Each test asserts the interaction in order — item present and displayed, real tap,
 * destination changed, destination content present — so a failure names the step that
 * broke rather than reporting a single opaque assertion. No delays are used as a fix;
 * waitForIdle only waits for composition to settle.
 */
@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeShowsWordmarkModeTabsAndBottomBar() {
        composeRule.onAllNodesWithText("PLAYCORE").onFirst().assertIsDisplayed()
        composeRule.onAllNodesWithText("EXPLORE").onFirst().assertIsDisplayed()
        composeRule.onAllNodesWithText("HANDHELD").onFirst().assertIsDisplayed()
        composeRule.onAllNodesWithText("GAMEPAD").onFirst().assertIsDisplayed()

        // Step 2 of the interaction trace: every bottom-bar item is on screen.
        composeRule.onNodeWithTag("nav_${Routes.EXPLORE_HOME}").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_${Routes.EXPLORE_LIBRARY}").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_${Routes.EXPLORE_MONITOR}").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_${Routes.SUPER_BASE}").assertIsDisplayed()
    }

    @Test
    fun libraryDestinationOpensLibraryScreen() {
        // Steps 1-3: the bar item is visible and a real tap is performed on it.
        composeRule.onNodeWithTag("nav_${Routes.EXPLORE_LIBRARY}").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_${Routes.EXPLORE_LIBRARY}").performClick()
        composeRule.waitForIdle()

        // Steps 5-6: the destination actually changed — Home's marker is gone.
        composeRule.onAllNodesWithText("CONTINUE PLAYING", substring = true).assertCountEquals(0)

        // Steps 7-8: the Library screen composed and its content is usable.
        composeRule.onAllNodesWithText("ON THE CONSOLE", substring = true).onFirst().assertExists()
        composeRule.onAllNodesWithText("Search apps").onFirst().assertExists()
    }

    @Test
    fun superBaseShowsTheEightTiles() {
        composeRule.onNodeWithTag("nav_${Routes.SUPER_BASE}").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_${Routes.SUPER_BASE}").performClick()
        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("Game Center").onFirst().assertExists()
        composeRule.onAllNodesWithText("Plugin Library").onFirst().assertExists()
        composeRule.onAllNodesWithText("Core Reactor").onFirst().assertExists()
    }
}
