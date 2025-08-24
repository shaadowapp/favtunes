package com.shaadow.tunes.database

import androidx.room.*
import com.shaadow.tunes.models.PendingBugReportEntity
import com.shaadow.tunes.models.PendingFeedbackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BugReportDao {
    
    // Bug Report operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingBugReport(report: PendingBugReportEntity)
    
    @Query("SELECT * FROM pending_bug_reports ORDER BY createdAt ASC")
    suspend fun getAllPendingBugReports(): List<PendingBugReportEntity>
    
    @Query("SELECT COUNT(*) FROM pending_bug_reports")
    suspend fun getPendingBugReportsCount(): Int
    
    @Query("SELECT COUNT(*) FROM pending_bug_reports")
    fun getPendingBugReportsCountFlow(): Flow<Int>
    
    @Delete
    suspend fun deletePendingBugReport(report: PendingBugReportEntity)
    
    @Query("DELETE FROM pending_bug_reports WHERE localId = :localId")
    suspend fun deletePendingBugReportById(localId: String)
    
    @Query("UPDATE pending_bug_reports SET retryCount = :retryCount WHERE localId = :localId")
    suspend fun updateRetryCount(localId: String, retryCount: Int)
    
    // Feedback operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingFeedback(feedback: PendingFeedbackEntity)
    
    @Query("SELECT * FROM pending_feedback ORDER BY createdAt ASC")
    suspend fun getAllPendingFeedback(): List<PendingFeedbackEntity>
    
    @Query("SELECT COUNT(*) FROM pending_feedback")
    suspend fun getPendingFeedbackCount(): Int
    
    @Query("SELECT COUNT(*) FROM pending_feedback")
    fun getPendingFeedbackCountFlow(): Flow<Int>
    
    @Delete
    suspend fun deletePendingFeedback(feedback: PendingFeedbackEntity)
    
    @Query("DELETE FROM pending_feedback WHERE localId = :localId")
    suspend fun deletePendingFeedbackById(localId: String)
    
    @Query("UPDATE pending_feedback SET retryCount = :retryCount WHERE localId = :localId")
    suspend fun updateFeedbackRetryCount(localId: String, retryCount: Int)
    
    // Cleanup operations
    @Query("DELETE FROM pending_bug_reports WHERE createdAt < :cutoffTime")
    suspend fun deleteOldBugReports(cutoffTime: Long)
    
    @Query("DELETE FROM pending_feedback WHERE createdAt < :cutoffTime")
    suspend fun deleteOldFeedback(cutoffTime: Long)
    
    // Batch operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingBugReports(reports: List<PendingBugReportEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingFeedbacks(feedbacks: List<PendingFeedbackEntity>)
}