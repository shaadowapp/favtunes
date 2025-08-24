package com.shaadow.tunes.utils

import android.content.Context
import android.util.Log
import com.shaadow.tunes.models.*
import com.shaadow.tunes.repository.RepositoryProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class FirebaseSampleDataSeeder(private val context: Context) {
    
    private val repository = RepositoryProvider.getBugReportRepository(context)
    // DeviceInfoCollector is an object, not a class
    
    companion object {
        private const val TAG = "FirebaseSampleDataSeeder"
        private const val PREFS_NAME = "sample_data_seeder"
        private const val KEY_DATA_SEEDED = "data_seeded"
    }
    
    /**
     * Seeds sample data to Firebase if not already done
     */
    fun seedSampleDataIfNeeded() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isDataSeeded = prefs.getBoolean(KEY_DATA_SEEDED, false)
        
        if (!isDataSeeded) {
            Log.d(TAG, "Seeding sample data to Firebase...")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Log.d(TAG, "Starting seedSampleData()...")
                    seedSampleData()
                    prefs.edit().putBoolean(KEY_DATA_SEEDED, true).apply()
                    Log.d(TAG, "Sample data seeded successfully!")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to seed sample data: ${e.message}", e)
                    e.printStackTrace()
                }
            }
        } else {
            Log.d(TAG, "Sample data already seeded, skipping...")
        }
    }
    
    /**
     * Force seed sample data (for testing purposes)
     */
    fun forceSeedSampleData() {
        Log.d(TAG, "Force seeding sample data...")
        // Reset the flag so it will seed again
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DATA_SEEDED, false).apply()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                seedSampleData()
                prefs.edit().putBoolean(KEY_DATA_SEEDED, true).apply()
                Log.d(TAG, "Sample data force seeded successfully!")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to force seed sample data", e)
            }
        }
    }
    
    /**
     * Reset seeding flag for testing
     */
    fun resetSeedingFlag() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DATA_SEEDED, false).apply()
        Log.d(TAG, "Seeding flag reset - will seed on next app start")
    }
    
    private suspend fun seedSampleData() {
        Log.d(TAG, "Starting seedSampleData - collecting device info...")
        val deviceInfo = BugReportDeviceInfoCollector(context).collectDeviceInfo(getAppVersion())
        Log.d(TAG, "Device info collected: ${deviceInfo.deviceModel}")
        
        // Seed bug reports
        Log.d(TAG, "Seeding bug reports...")
        seedBugReports(deviceInfo)
        Log.d(TAG, "Bug reports seeded")
        
        // Add delay between operations
        delay(1000)
        
        // Seed user feedback
        Log.d(TAG, "Seeding user feedback...")
        seedUserFeedback(deviceInfo)
        Log.d(TAG, "User feedback seeded")
        
        // Add delay before admin data
        delay(1000)
        
        // Seed admin users (directly to Firestore)
        Log.d(TAG, "Seeding admin users...")
        seedAdminUsers()
        Log.d(TAG, "Admin users seeded")
    }
    
    private suspend fun seedBugReports(deviceInfo: DeviceInfo) {
        val sampleBugReports = listOf(
            BugReport(
                title = "App crashes when playing music",
                description = "The app crashes randomly when I try to play music from my playlist. This happens about 50% of the time when I select a song. I've tried restarting the app but the issue persists.",
                severity = BugSeverity.HIGH,
                category = BugCategory.PLAYBACK,
                deviceInfo = deviceInfo,
                appVersion = getAppVersion(),
                reproductionSteps = listOf(
                    "Open the app",
                    "Go to playlists",
                    "Select any playlist",
                    "Tap on a song to play",
                    "App crashes immediately"
                )
            ),
            BugReport(
                title = "Search function not working properly",
                description = "When I search for songs, the results are not accurate. Sometimes it shows no results even for popular songs that I know are available.",
                severity = BugSeverity.MEDIUM,
                category = BugCategory.UI,
                deviceInfo = deviceInfo,
                appVersion = getAppVersion(),
                reproductionSteps = listOf(
                    "Open search tab",
                    "Type 'popular song name'",
                    "No results shown or wrong results displayed"
                )
            ),
            BugReport(
                title = "Sync issues with offline downloads",
                description = "Downloaded songs for offline listening are not syncing properly. Some songs show as downloaded but won't play when offline.",
                severity = BugSeverity.MEDIUM,
                category = BugCategory.SYNC,
                deviceInfo = deviceInfo,
                appVersion = getAppVersion(),
                reproductionSteps = listOf(
                    "Download songs for offline",
                    "Turn off internet connection",
                    "Try to play downloaded songs",
                    "Some songs fail to play"
                )
            ),
            BugReport(
                title = "UI elements overlapping on small screens",
                description = "On smaller screen devices, some UI elements overlap making it difficult to navigate. The bottom navigation bar sometimes covers content.",
                severity = BugSeverity.LOW,
                category = BugCategory.UI,
                deviceInfo = deviceInfo,
                appVersion = getAppVersion(),
                reproductionSteps = listOf(
                    "Use app on small screen device",
                    "Navigate through different screens",
                    "Notice overlapping elements"
                )
            ),
            BugReport(
                title = "Performance issues during peak hours",
                description = "The app becomes very slow and unresponsive during peak usage hours (evening 7-10 PM). Loading times increase significantly.",
                severity = BugSeverity.CRITICAL,
                category = BugCategory.PERFORMANCE,
                deviceInfo = deviceInfo,
                appVersion = getAppVersion(),
                reproductionSteps = listOf(
                    "Use app during evening hours (7-10 PM)",
                    "Try to load playlists or search",
                    "Notice significant delays and lag"
                )
            )
        )
        
        sampleBugReports.forEach { bugReport ->
            try {
                val result = repository.submitBugReport(bugReport)
                if (result.isSuccess) {
                    Log.d(TAG, "Seeded bug report: ${bugReport.title}")
                } else {
                    Log.e(TAG, "Failed to seed bug report: ${bugReport.title}", result.exceptionOrNull())
                }
                delay(500) // Small delay between submissions
            } catch (e: Exception) {
                Log.e(TAG, "Error seeding bug report: ${bugReport.title}", e)
            }
        }
    }
    
    private suspend fun seedUserFeedback(deviceInfo: DeviceInfo) {
        val sampleFeedback = listOf(
            UserFeedback(
                rating = 5,
                category = FeedbackCategory.GENERAL,
                message = "Absolutely love this app! The music quality is excellent and the interface is very user-friendly. Keep up the great work!",
                deviceInfo = deviceInfo,
                appVersion = getAppVersion()
            ),
            UserFeedback(
                rating = 4,
                category = FeedbackCategory.FEATURE_REQUEST,
                message = "Great app overall! Would love to see a dark mode option and the ability to create collaborative playlists with friends.",
                email = "user@example.com",
                deviceInfo = deviceInfo,
                appVersion = getAppVersion()
            ),
            UserFeedback(
                rating = 3,
                category = FeedbackCategory.UI_UX,
                message = "The app works well but the user interface could be more intuitive. Sometimes it's hard to find certain features.",
                deviceInfo = deviceInfo,
                appVersion = getAppVersion()
            ),
            UserFeedback(
                rating = 5,
                category = FeedbackCategory.PERFORMANCE,
                message = "Amazing performance! Songs load quickly and the app rarely crashes. Very impressed with the stability.",
                email = "happy.user@email.com",
                deviceInfo = deviceInfo,
                appVersion = getAppVersion()
            ),
            UserFeedback(
                rating = 2,
                category = FeedbackCategory.OTHER,
                message = "The app has potential but needs work. Battery drain is quite high and some features are confusing to use.",
                deviceInfo = deviceInfo,
                appVersion = getAppVersion()
            ),
            UserFeedback(
                rating = 4,
                category = FeedbackCategory.FEATURE_REQUEST,
                message = "Love the offline feature! Could you add support for podcasts and audiobooks as well? That would make this app perfect.",
                email = "podcast.lover@mail.com",
                deviceInfo = deviceInfo,
                appVersion = getAppVersion()
            ),
            UserFeedback(
                rating = 5,
                category = FeedbackCategory.GENERAL,
                message = "Best music app I've used! The recommendation algorithm is spot on and I've discovered so many new artists.",
                deviceInfo = deviceInfo,
                appVersion = getAppVersion()
            )
        )
        
        sampleFeedback.forEach { feedback ->
            try {
                val result = repository.submitFeedback(feedback)
                if (result.isSuccess) {
                    Log.d(TAG, "Seeded feedback: ${feedback.rating} stars - ${feedback.category}")
                } else {
                    Log.e(TAG, "Failed to seed feedback", result.exceptionOrNull())
                }
                delay(500) // Small delay between submissions
            } catch (e: Exception) {
                Log.e(TAG, "Error seeding feedback", e)
            }
        }
    }
    
    private suspend fun seedAdminUsers() {
        try {
            // Create sample admin users directly in Firestore
            val adminUsers = listOf(
                mapOf(
                    "role" to "admin",
                    "email" to "admin@favtunes.com",
                    "name" to "System Administrator",
                    "createdAt" to com.google.firebase.Timestamp.now(),
                    "permissions" to listOf("read_all_reports", "read_all_feedback", "manage_users")
                ),
                mapOf(
                    "role" to "moderator",
                    "email" to "moderator@favtunes.com", 
                    "name" to "Content Moderator",
                    "createdAt" to com.google.firebase.Timestamp.now(),
                    "permissions" to listOf("read_all_reports", "read_all_feedback")
                )
            )
            
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            
            adminUsers.forEachIndexed { index, adminData ->
                val documentId = "admin_user_${index + 1}"
                firestore.collection("admin_users")
                    .document(documentId)
                    .set(adminData)
                    .addOnSuccessListener {
                        Log.d(TAG, "Seeded admin user: ${adminData["email"]}")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to seed admin user: ${adminData["email"]}", e)
                    }
                delay(500)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding admin users", e)
        }
    }
    
    /**
     * Clear all seeded data (for testing purposes)
     */
    fun clearSeededDataFlag() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DATA_SEEDED, false).apply()
        Log.d(TAG, "Cleared seeded data flag - data will be seeded on next app start")
    }
    
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get app version", e)
            "1.0.0"
        }
    }
}