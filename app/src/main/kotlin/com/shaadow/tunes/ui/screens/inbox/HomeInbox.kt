package com.shaadow.tunes.ui.screens.inbox

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

data class Notification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp(0, 0),
    val read: Boolean = false,
    val type: String = "info",
    val priority: String = "normal",
    val actionText: String? = null,
    val actionUrl: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeInboxScreen() {
    var notifications by remember { mutableStateOf(listOf<Notification>()) }
    var isLoading by remember { mutableStateOf(false) }
    val db = FirebaseFirestore.getInstance()

    fun loadNotifications() {
        isLoading = true
        db.collection("notifications")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val notifList = if (snapshot.isEmpty) {
                    listOf(
                        Notification(
                            id = "1",
                            title = "Update Available",
                            message = "FavTunes v2.3.0 is now available with new features and bug fixes. Update now to get the latest improvements.",
                            timestamp = Timestamp.now(),
                            read = false,
                            type = "update",
                            priority = "high",
                            actionText = "Update Now",
                            actionUrl = "https://play.google.com/store/apps/details?id=com.shaadow.tunes"
                        ),
                        Notification(
                            id = "2",
                            title = "New Features Added",
                            message = "Discover new playlist management features and improved audio quality settings in the latest version.",
                            timestamp = Timestamp(System.currentTimeMillis() / 1000 - 3600, 0),
                            read = true,
                            type = "feature",
                            priority = "normal"
                        ),
                        Notification(
                            id = "3",
                            title = "Maintenance Notice",
                            message = "Scheduled maintenance will occur tonight from 2:00 AM to 4:00 AM. Some features may be temporarily unavailable.",
                            timestamp = Timestamp(System.currentTimeMillis() / 1000 - 7200, 0),
                            read = false,
                            type = "maintenance",
                            priority = "medium"
                        )
                    )
                } else {
                    snapshot.documents.map { doc ->
                        doc.toObject(Notification::class.java)!!.copy(id = doc.id)
                    }
                }
                notifications = notifList
                isLoading = false
            }
            .addOnFailureListener { e ->
                Log.w("HomeInboxScreen", "Error getting documents.", e)
                isLoading = false
            }
    }

    LaunchedEffect(Unit) {
        loadNotifications()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Notifications",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(onClick = { loadNotifications() }) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { loadNotifications() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                }
            }
        }
    ) { paddingValues ->
        NotificationScreen(
            notifications = notifications,
            isLoading = isLoading,
            onRefresh = { loadNotifications() },
            onDelete = { notification ->
                // Delete from Firebase
                if (notification.id.isNotEmpty()) {
                    db.collection("notifications").document(notification.id)
                        .delete()
                        .addOnSuccessListener {
                            notifications = notifications.filter { it != notification }
                        }
                        .addOnFailureListener { e ->
                            Log.w("HomeInboxScreen", "Error deleting document", e)
                        }
                } else {
                    // For sample data
                    notifications = notifications.filter { it != notification }
                }
            },
            onMarkAsRead = { notification ->
                // Update in Firebase
                if (notification.id.isNotEmpty()) {
                    db.collection("notifications").document(notification.id)
                        .update("read", true)
                        .addOnSuccessListener {
                            notifications = notifications.map { 
                                if (it == notification) it.copy(read = true) else it 
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.w("HomeInboxScreen", "Error updating document", e)
                        }
                } else {
                    // For sample data
                    notifications = notifications.map { 
                        if (it == notification) it.copy(read = true) else it 
                    }
                }
            },
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun NotificationScreen(
    notifications: List<Notification>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onDelete: (Notification) -> Unit,
    onMarkAsRead: (Notification) -> Unit,
    modifier: Modifier = Modifier
) {
    if (notifications.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No notifications yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Tap refresh to check for notifications",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRefresh) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Refresh")
                }
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notifications) { notification ->
                SwipeableNotificationItem(
                    notification = notification,
                    onDelete = { onDelete(notification) },
                    onMarkAsRead = { onMarkAsRead(notification) }
                )
            }
        }
    }
}

@Composable
fun SwipeableNotificationItem(
    notification: Notification,
    onDelete: () -> Unit,
    onMarkAsRead: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val maxSwipeDistance = 150f
    val actionThreshold = 100f
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Background actions
        if (offsetX > 10f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF4CAF50),
                                Color(0xFF66BB6A)
                            )
                        )
                    ),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.padding(start = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                Icons.Default.MarkEmailRead,
                                contentDescription = "Mark as read",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (offsetX > actionThreshold) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Mark as Read",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        } else if (offsetX < -10f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFEF5350),
                                Color(0xFFF44336)
                            )
                        )
                    ),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    modifier = Modifier.padding(end = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (offsetX < -actionThreshold) {
                        Text(
                            "Delete",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // Notification item
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .pointerInput(notification.id) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            isDragging = true
                        },
                        onDragEnd = {
                            isDragging = false
                            when {
                                offsetX > actionThreshold -> {
                                    onMarkAsRead()
                                }
                                offsetX < -actionThreshold -> {
                                    onDelete()
                                }
                            }
                            offsetX = 0f
                        }
                    ) { _, dragAmount ->
                        val newOffset = offsetX + dragAmount * 0.8f // Reduce sensitivity
                        offsetX = newOffset.coerceIn(-maxSwipeDistance, maxSwipeDistance)
                    }
                }
        ) {
            NotificationItem(notification)
        }
    }
}

@Composable
fun NotificationItem(notification: Notification) {
    val formattedDate = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        .format(notification.timestamp.toDate())
    
    val (icon, iconColor) = when (notification.type) {
        "update" -> Icons.Default.SystemUpdate to MaterialTheme.colorScheme.primary
        "feature" -> Icons.Default.NewReleases to MaterialTheme.colorScheme.tertiary
        "maintenance" -> Icons.Default.Build to MaterialTheme.colorScheme.secondary
        else -> Icons.Default.Info to MaterialTheme.colorScheme.outline
    }
    
    val priorityColor = when (notification.priority) {
        "high" -> MaterialTheme.colorScheme.error
        "medium" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle notification click */ },
        colors = CardDefaults.cardColors(
            containerColor = if (notification.read)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (notification.read) 2.dp else 6.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = iconColor.copy(alpha = 0.1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (notification.read) FontWeight.Medium else FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (notification.priority == "high") {
                                Surface(
                                    modifier = Modifier.size(6.dp),
                                    shape = RoundedCornerShape(3.dp),
                                    color = priorityColor
                                ) {}
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            
                            if (!notification.read) {
                                Surface(
                                    modifier = Modifier.size(8.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {}
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        
                        notification.actionText?.let { actionText ->
                            Button(
                                onClick = { /* Handle action */ },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = iconColor,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = actionText,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Badge indicator for unread notifications
@Composable
fun InboxTabWithBadge(
    hasUnreadNotifications: Boolean = false,
    content: @Composable () -> Unit
) {
    Box {
        content()
        
        if (hasUnreadNotifications) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        MaterialTheme.colorScheme.error,
                        RoundedCornerShape(4.dp)
                    )
                    .offset(x = 12.dp, y = 8.dp)
            )
        }
    }
}

class HomeInbox : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            Log.d("HomeInbox", "HomeInbox activity created successfully")
            setContent {
                HomeInboxScreen()
            }
        } catch (e: Exception) {
            Log.e("HomeInbox", "Error creating HomeInbox activity", e)
            throw e
        }
    }
}