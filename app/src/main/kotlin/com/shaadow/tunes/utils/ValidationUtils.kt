package com.shaadow.tunes.utils

import android.util.Log
import com.shaadow.tunes.models.BugReport
import com.shaadow.tunes.models.UserFeedback
import java.util.regex.Pattern

object ValidationUtils {
    
    private const val TAG = "ValidationUtils"
    
    // Validation constants
    private const val MIN_TITLE_LENGTH = 3
    private const val MAX_TITLE_LENGTH = 200
    private const val MIN_DESCRIPTION_LENGTH = 10
    private const val MAX_DESCRIPTION_LENGTH = 5000
    private const val MIN_MESSAGE_LENGTH = 5
    private const val MAX_MESSAGE_LENGTH = 2000
    private const val MAX_REPRODUCTION_STEPS = 20
    private const val MAX_REPRODUCTION_STEP_LENGTH = 500
    private const val MAX_ATTACHMENTS = 5
    
    // Rate limiting
    private const val MAX_SUBMISSIONS_PER_HOUR = 10
    private const val MAX_SUBMISSIONS_PER_DAY = 50
    
    // Patterns for validation
    private val HTML_TAG_PATTERN = Pattern.compile("<[^>]+>")
    private val SCRIPT_PATTERN = Pattern.compile("(?i)<script[^>]*>.*?</script>")
    private val JAVASCRIPT_PATTERN = Pattern.compile("(?i)javascript:")
    private val SQL_INJECTION_PATTERN = Pattern.compile("(?i)(union|select|insert|update|delete|drop|create|alter|exec|execute)")
    
    /**
     * Validate a bug report
     */
    fun validateBugReport(bugReport: BugReport): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Validate title
        val titleValidation = validateTitle(bugReport.title)
        if (!titleValidation.isValid) {
            errors.addAll(titleValidation.errors)
        }
        
        // Validate description
        val descriptionValidation = validateDescription(bugReport.description)
        if (!descriptionValidation.isValid) {
            errors.addAll(descriptionValidation.errors)
        }
        
        // Validate reproduction steps
        val stepsValidation = validateReproductionSteps(bugReport.reproductionSteps)
        if (!stepsValidation.isValid) {
            errors.addAll(stepsValidation.errors)
        }
        
        // Validate attachments
        val attachmentsValidation = validateAttachments(bugReport.attachments)
        if (!attachmentsValidation.isValid) {
            errors.addAll(attachmentsValidation.errors)
        }
        
        // Validate app version
        if (bugReport.appVersion.isBlank()) {
            errors.add("App version is required")
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    /**
     * Validate user feedback
     */
    fun validateUserFeedback(feedback: UserFeedback): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Validate rating
        if (feedback.rating !in 1..5) {
            errors.add("Rating must be between 1 and 5")
        }
        
        // Validate message
        val messageValidation = validateMessage(feedback.message)
        if (!messageValidation.isValid) {
            errors.addAll(messageValidation.errors)
        }
        
        // Validate app version
        if (feedback.appVersion.isBlank()) {
            errors.add("App version is required")
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    /**
     * Validate title field
     */
    fun validateTitle(title: String): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (title.isBlank()) {
            errors.add("Title is required")
        } else {
            if (title.length < MIN_TITLE_LENGTH) {
                errors.add("Title must be at least $MIN_TITLE_LENGTH characters")
            }
            if (title.length > MAX_TITLE_LENGTH) {
                errors.add("Title must not exceed $MAX_TITLE_LENGTH characters")
            }
            
            // Check for malicious content
            if (containsMaliciousContent(title)) {
                errors.add("Title contains invalid content")
            }
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    /**
     * Validate description field
     */
    fun validateDescription(description: String): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (description.isBlank()) {
            errors.add("Description is required")
        } else {
            if (description.length < MIN_DESCRIPTION_LENGTH) {
                errors.add("Description must be at least $MIN_DESCRIPTION_LENGTH characters")
            }
            if (description.length > MAX_DESCRIPTION_LENGTH) {
                errors.add("Description must not exceed $MAX_DESCRIPTION_LENGTH characters")
            }
            
            // Check for malicious content
            if (containsMaliciousContent(description)) {
                errors.add("Description contains invalid content")
            }
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    /**
     * Validate message field (for feedback)
     */
    fun validateMessage(message: String): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (message.isBlank()) {
            errors.add("Message is required")
        } else {
            if (message.length < MIN_MESSAGE_LENGTH) {
                errors.add("Message must be at least $MIN_MESSAGE_LENGTH characters")
            }
            if (message.length > MAX_MESSAGE_LENGTH) {
                errors.add("Message must not exceed $MAX_MESSAGE_LENGTH characters")
            }
            
            // Check for malicious content
            if (containsMaliciousContent(message)) {
                errors.add("Message contains invalid content")
            }
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    /**
     * Validate reproduction steps
     */
    fun validateReproductionSteps(steps: List<String>): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (steps.size > MAX_REPRODUCTION_STEPS) {
            errors.add("Too many reproduction steps (maximum $MAX_REPRODUCTION_STEPS)")
        }
        
        steps.forEachIndexed { index, step ->
            if (step.isBlank()) {
                errors.add("Reproduction step ${index + 1} cannot be empty")
            } else if (step.length > MAX_REPRODUCTION_STEP_LENGTH) {
                errors.add("Reproduction step ${index + 1} is too long (maximum $MAX_REPRODUCTION_STEP_LENGTH characters)")
            } else if (containsMaliciousContent(step)) {
                errors.add("Reproduction step ${index + 1} contains invalid content")
            }
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    /**
     * Validate attachments
     */
    fun validateAttachments(attachments: List<String>): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (attachments.size > MAX_ATTACHMENTS) {
            errors.add("Too many attachments (maximum $MAX_ATTACHMENTS)")
        }
        
        attachments.forEach { attachment ->
            if (attachment.isBlank()) {
                errors.add("Attachment path cannot be empty")
            }
            // Additional validation for file paths, URLs, etc. can be added here
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    /**
     * Check if text contains malicious content
     */
    private fun containsMaliciousContent(text: String): Boolean {
        return try {
            SCRIPT_PATTERN.matcher(text).find() ||
            JAVASCRIPT_PATTERN.matcher(text).find() ||
            SQL_INJECTION_PATTERN.matcher(text).find()
        } catch (e: Exception) {
            Log.w(TAG, "Error checking for malicious content", e)
            false
        }
    }
    
    /**
     * Sanitize text input by removing potentially harmful content
     */
    fun sanitizeText(text: String): String {
        return try {
            var sanitized = text
            
            // Remove HTML tags
            sanitized = HTML_TAG_PATTERN.matcher(sanitized).replaceAll("")
            
            // Remove script tags
            sanitized = SCRIPT_PATTERN.matcher(sanitized).replaceAll("")
            
            // Remove javascript: protocols
            sanitized = JAVASCRIPT_PATTERN.matcher(sanitized).replaceAll("")
            
            // Trim whitespace
            sanitized = sanitized.trim()
            
            sanitized
        } catch (e: Exception) {
            Log.e(TAG, "Error sanitizing text", e)
            text.trim() // Return original text trimmed if sanitization fails
        }
    }
    
    /**
     * Sanitize a bug report
     */
    fun sanitizeBugReport(bugReport: BugReport): BugReport {
        return bugReport.copy(
            title = sanitizeText(bugReport.title),
            description = sanitizeText(bugReport.description),
            reproductionSteps = bugReport.reproductionSteps.map { sanitizeText(it) }
        )
    }
    
    /**
     * Sanitize user feedback
     */
    fun sanitizeUserFeedback(feedback: UserFeedback): UserFeedback {
        return feedback.copy(
            message = sanitizeText(feedback.message)
        )
    }
}

/**
 * Data class representing validation results
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String>
) {
    val errorMessage: String
        get() = errors.joinToString("; ")
}