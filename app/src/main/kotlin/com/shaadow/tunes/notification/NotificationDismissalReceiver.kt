package com.shaadow.tunes.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver to track notification dismissal behavior
 * Integrates with BehaviorTrackingService for learning user patterns
 */
class NotificationDismissalReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val contentType = intent.getStringExtra("content_type") ?: return
        val deliveryTime = intent.getLongExtra("delivery_time", 0L)
        
        if (deliveryTime == 0L) return
        
        // Track dismissal behavior
        val behaviorTracker = BehaviorTrackingService(
            context,
            IntelligentNotificationScheduler(context)
        )
        
        CoroutineScope(Dispatchers.Default).launch {
            behaviorTracker.recordNotificationDismissal(contentType, deliveryTime)
        }
    }
}