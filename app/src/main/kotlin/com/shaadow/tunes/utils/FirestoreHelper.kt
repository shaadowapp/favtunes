package com.shaadow.tunes.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.shaadow.tunes.models.BugReport
import com.shaadow.tunes.models.UserFeedback
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object FirestoreHelper {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private const val TAG = "FirestoreHelper"
    
    // Collections
    private const val BUG_REPORTS_COLLECTION = "bug_reports"
    private const val USER_FEEDBACK_COLLECTION = "user_feedback"
    private const val LEGACY_FEEDBACK_COLLECTION = "feedbacks"

    // Legacy method - keeping for backward compatibility
    fun submitFeedback(
        publicKey: String,
        privateKey: String,
        username: String,
        message: String,
        deviceModel: String,
        callback: (Boolean) -> Unit
    ) {
        val feedback = hashMapOf(
            "publicKey" to publicKey,
            "privateKey" to privateKey,
            "username" to username,
            "message" to message,
            "deviceModel" to deviceModel,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection(LEGACY_FEEDBACK_COLLECTION).add(feedback)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }
    
    /**
     * Submit a bug report to Firestore
     * @param bugReport The bug report to submit
     * @return Result containing the document ID on success or error on failure
     */
    suspend fun submitBugReport(bugReport: BugReport): Result<String> {
        return try {
            if (!bugReport.isValid()) {
                return Result.failure(IllegalArgumentException("Invalid bug report data"))
            }
            
            val documentRef = db.collection(BUG_REPORTS_COLLECTION)
                .add(bugReport.toFirestoreMap())
                .await()
            
            Log.d(TAG, "Bug report submitted successfully with ID: ${documentRef.id}")
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to submit bug report", e)
            Result.failure(e)
        }
    }
    
    /**
     * Submit user feedback to Firestore
     * @param feedback The user feedback to submit
     * @return Result containing the document ID on success or error on failure
     */
    suspend fun submitUserFeedback(feedback: UserFeedback): Result<String> {
        return try {
            if (!feedback.isValid()) {
                return Result.failure(IllegalArgumentException("Invalid feedback data"))
            }
            
            val documentRef = db.collection(USER_FEEDBACK_COLLECTION)
                .add(feedback.toFirestoreMap())
                .await()
            
            Log.d(TAG, "User feedback submitted successfully with ID: ${documentRef.id}")
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to submit user feedback", e)
            Result.failure(e)
        }
    }
    
    /**
     * Submit multiple reports in a batch operation
     * @param reports List of bug reports and/or user feedback to submit
     * @return Result containing list of document IDs on success or error on failure
     */
    suspend fun batchSubmitReports(reports: List<Any>): Result<List<String>> {
        return try {
            if (reports.isEmpty()) {
                return Result.success(emptyList())
            }
            
            val batch = db.batch()
            val documentIds = mutableListOf<String>()
            
            reports.forEach { report ->
                when (report) {
                    is BugReport -> {
                        if (report.isValid()) {
                            val docRef = db.collection(BUG_REPORTS_COLLECTION).document()
                            batch.set(docRef, report.toFirestoreMap())
                            documentIds.add(docRef.id)
                        } else {
                            Log.w(TAG, "Skipping invalid bug report: ${report.id}")
                        }
                    }
                    is UserFeedback -> {
                        if (report.isValid()) {
                            val docRef = db.collection(USER_FEEDBACK_COLLECTION).document()
                            batch.set(docRef, report.toFirestoreMap())
                            documentIds.add(docRef.id)
                        } else {
                            Log.w(TAG, "Skipping invalid feedback: ${report.id}")
                        }
                    }
                    else -> {
                        Log.w(TAG, "Unknown report type: ${report::class.simpleName}")
                    }
                }
            }
            
            if (documentIds.isNotEmpty()) {
                batch.commit().await()
                Log.d(TAG, "Batch submitted ${documentIds.size} reports successfully")
            }
            
            Result.success(documentIds)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to submit batch reports", e)
            Result.failure(e)
        }
    }
    
    /**
     * Check if Firestore is available and accessible
     * @return true if Firestore is accessible, false otherwise
     */
    suspend fun isFirestoreAvailable(): Boolean {
        return try {
            // Try to access Firestore settings to verify connectivity
            // This doesn't require reading from any collection
            db.firestoreSettings
            true
        } catch (e: Exception) {
            Log.w(TAG, "Firestore connectivity check failed", e)
            false
        }
    }
    
    /**
     * Get error message from Firebase exception
     * @param exception The Firebase exception
     * @return User-friendly error message
     */
    fun getErrorMessage(exception: Exception): String {
        return when {
            exception.message?.contains("PERMISSION_DENIED") == true -> 
                "Permission denied. Please check your authentication."
            exception.message?.contains("UNAVAILABLE") == true -> 
                "Service temporarily unavailable. Please try again later."
            exception.message?.contains("DEADLINE_EXCEEDED") == true -> 
                "Request timeout. Please check your internet connection."
            exception.message?.contains("UNAUTHENTICATED") == true -> 
                "Authentication required. Please sign in."
            else -> "An error occurred while submitting your report. Please try again."
        }
    }
}
