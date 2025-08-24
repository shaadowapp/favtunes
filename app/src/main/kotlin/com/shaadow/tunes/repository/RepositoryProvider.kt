package com.shaadow.tunes.repository

import android.content.Context

object RepositoryProvider {
    
    @Volatile
    private var bugReportRepository: BugReportRepository? = null
    
    fun getBugReportRepository(context: Context): BugReportRepository {
        return bugReportRepository ?: synchronized(this) {
            bugReportRepository ?: BugReportRepositoryImpl(context.applicationContext).also {
                bugReportRepository = it
            }
        }
    }
}