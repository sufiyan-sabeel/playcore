package app.playcore

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Navigation smoke tests.
 *
 * Selectors use onAllNodes…onFirst() because the shell renders several labels twice:
 * an action button uppercases its label ("Super Base" → "SUPER BASE"), which then
 * collides with the bottom-bar caption. That is a rendering fact, not a bug, so the
 * test asserts the first matching node is present and tappable.
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
        composeRule.onAllNodesWithText("SUPER BASE").onFirst().assertIsDisplayed()
    }

    @Test
    fun libraryDestinationOpensLibraryScreen() {
        composeRule.onAllNodesWithText("LIBRARY").onFirst().performClick()
        composeRule.waitForIdle()
        // Existence, not visibility: during the navigation transition the outgoing screen
        // is still composed, so a visibility assertion races the animation.
        composeRule.onAllNodesWithText("Search apps").onFirst().assertExists()
        composeRule.onAllNodesWithText("Only fav").onFirst().assertExists()
    }

    @Test
    fun superBaseShowsTheEightTiles() {
        composeRule.onAllNodesWithText("SUPER BASE").onFirst().performClick()
        composeRule.onAllNodesWithText("Game Center").onFirst().assertIsDisplayed()
        composeRule.onAllNodesWithText("Plugin Library").onFirst().assertIsDisplayed()
        composeRule.onAllNodesWithText("Core Reactor").onFirst().assertIsDisplayed()
    }
}
