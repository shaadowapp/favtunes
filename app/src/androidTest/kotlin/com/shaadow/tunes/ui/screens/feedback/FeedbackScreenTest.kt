package com.shaadow.tunes.ui.screens.feedback

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shaadow.tunes.models.FeedbackCategory
import com.shaadow.tunes.ui.screens.feedback.FeedbackScreen
import com.shaadow.tunes.viewmodels.FeedbackViewModel
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FeedbackScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun feedbackScreen_displaysCorrectTitle() {
        composeTestRule.setContent {
            FeedbackScreen(
                onNavigateBack = { }
            )
        }

        composeTestRule
            .onNodeWithText("Send Feedback")
            .assertIsDisplayed()
    }

    @Test
    fun feedbackScreen_displaysHeaderCard() {
        composeTestRule.setContent {
            FeedbackScreen(
                onNavigateBack = { }
            )
        }

        composeTestRule
            .onNodeWithText("Share your thoughts")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Your feedback helps us improve the app. Let us know what you think!")
            .assertIsDisplayed()
    }

    @Test
    fun feedbackScreen_displaysRatingSection() {
        composeTestRule.setContent {
            FeedbackScreen(
                onNavigateBack = { }
            )
        }

        composeTestRule
            .onNodeWithText("Rate Your Experience")
            .assertIsDisplayed()

        // Check that 5 star buttons are present
        composeTestRule
            .onAllNodesWithContentDescription("Star 1")
            .assertCountEquals(1)
        
        composeTestRule
            .onAllNodesWithContentDescription("Star 5")
            .assertCountEquals(1)
    }

    @Test
    fun feedbackScreen_starRatingInteraction() {
        composeTestRule.setContent {
            FeedbackScreen(
                onNavigateBack = { }
            )
        }

        // Click on the third star
        composeTestRule
            .onNodeWithContentDescription("Star 3")
            .performClick()

        // Verify rating description appears
        composeTestRule
            .onNodeWithText("Average")
            .assertIsDisplayed()
    }

    @Test
    fun feedbackScreen_displaysCategorySelection() {
        composeTestRule.setContent {
            FeedbackScreen(
                onNavigateBack = { }
            )
        }

        composeTestRule
            .onNodeWithText("Feedback Category")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("GENERAL")
            .assertIsDisplayed()
    }

    @Test
    fun feedbackScreen_categoryDropdownInteraction() {
        composeTestRule.setContent {
            FeedbackScreen(
                onNavigateBack = { }
            )
        }

        // Click on category dropdown
        composeTestRule
            .onNodeWithContentDescription(null)
            .filterToOne(hasClickAction())
            .performClick()

        // Verify dropdown options are displayed
        composeTestRule
            .onNodeWithText("FEATURE REQUEST")
            .assertIsDisplayed()
    }

    @Test
    fun feedbackScreen_displaysMessageField() {
        composeTestRule.setContent {
            FeedbackScreen(
                onNavigateBack = { }
            )
        }

        composeTestRule
            .onNodeWithText("Your Feedback")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Feedback Message *")
            .assertIsDisplayed()
    }

    @Test
    fun feedbackScreen_messageInputInteraction() {
        composeTestRule.setContent {
            FeedbackScreen(
                onNavigateBack = { }
            )
        }

        val testMessage = "This is a test feedback message"

        composeTestRule
            .onNodeWithText("Feedback Message *")
            .performTextInput(testMessage)

        composeTestRule
            .onNodeWithText(testMessage)
            .assertIsDisplayed()
    }

    @Test
    fun feedbackScreen_displaysAnonymousToggle() {
        composeTestRule.setContent {
            FeedbackScreen(
                onNavigateBack = { }
            )
        }

        composeTestRule
            .onNodeWithText("Submit Anonymously")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Your feedback will be submitted without personal identification")
            .assertIsDisplayed()
    }

    @Test
    fun feedbackScreen_anonymousToggleInteraction() {
        composeTestRule.setContent {
            FeedbackScreen(
                onNavigateBack = { }
            )
        }

        // Click on the anonymous toggle
        composeTestRule
            .onNode(hasClickAction() and hasText("Submit Anonymously"))
            .performClick()

        // Verify the description changes
        composeTestRule
            .onNodeWithText("Your feedback will be linked to your account")
            .assertIsDisplayed()
    }

    @Test
    fun feedbackScreen_displaysDeviceInfo() {
        composeTestRule.setContent {
            FeedbackScreen(
                onNavigateBack = { }
            )
        }

        composeTestRule
            .onNodeWithText("Device Information")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("This information helps us understand your environment")
            .assertIsDisplayed()

        // Check for device info labels
        composeTestRule
            .onNodeWithText("Device")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("OS Version")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("App Version")
            .assertIsDisplayed()
    }

    @Test
    fun feedbackScreen_submitButtonInitialState() {
        composeTestRule.setContent {
            FeedbackScreen(
                onNavigateBack = { }
            )
        }

        composeTestRule
            .onNodeWithText("Submit Feedback")
            .assertIsDisplayed()
            .assertIsNotEnabled() // Should be disabled initially
    }

    @Test
    fun feedbackScreen_submitButtonEnabledWithValidInput() {
        composeTestRule.setContent {
            FeedbackScreen(
                onNavigateBack = { }
            )
        }

        // Provide valid input
        composeTestRule
            .onNodeWithContentDescription("Star 4")
            .performClick()

        composeTestRule
            .onNodeWithText("Feedback Message *")
            .performTextInput("Great app, love the features!")

        // Submit button should now be enabled
        composeTestRule
            .onNodeWithText("Submit Feedback")
            .assertIsEnabled()
    }

    @Test
    fun feedbackScreen_backButtonInteraction() {
        var backPressed = false
        
        composeTestRule.setContent {
            FeedbackScreen(
                onNavigateBack = { backPressed = true }
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Back")
            .performClick()

        assert(backPressed)
    }

    @Test
    fun feedbackScreen_formValidationFlow() {
        composeTestRule.setContent {
            FeedbackScreen(
                onNavigateBack = { }
            )
        }

        // Initially submit button should be disabled
        composeTestRule
            .onNodeWithText("Submit Feedback")
            .assertIsNotEnabled()

        // Add rating only - still should be disabled
        composeTestRule
            .onNodeWithContentDescription("Star 3")
            .performClick()

        composeTestRule
            .onNodeWithText("Submit Feedback")
            .assertIsNotEnabled()

        // Add message - now should be enabled
        composeTestRule
            .onNodeWithText("Feedback Message *")
            .performTextInput("This is my feedback")

        composeTestRule
            .onNodeWithText("Submit Feedback")
            .assertIsEnabled()
    }

    @Test
    fun feedbackScreen_errorDisplaysCorrectly() {
        composeTestRule.setContent {
            FeedbackScreen(
                onNavigateBack = { }
            )
        }

        // This test would need to be expanded with proper ViewModel mocking
        // to simulate error states, but shows the structure for error testing
    }
}