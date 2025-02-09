package com.shaadow.tunes.ui.screens.inbox

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

data class Notification(
    val title: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp(0, 0),
    val read: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeInboxScreen() {
    var notifications by remember { mutableStateOf(listOf<Notification>()) }
    val db = FirebaseFirestore.getInstance()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight(600)) }
            )
        },
        content = { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                NotificationScreen(notifications)
                FloatingActionButton(
                    onClick = {
                        db.collection("notifications")
                            .orderBy("timestamp")
                            .get()
                            .addOnSuccessListener { snapshot ->
                                val notifList = snapshot.documents.map { doc ->
                                    val notification = doc.toObject(Notification::class.java)!!
                                    Log.d("HomeInboxScreen", "Notification: $notification")
                                    notification
                                }
                                notifications = notifList
                            }
                            .addOnFailureListener { e ->
                                Log.w("HomeInboxScreen", "Error getting documents.", e)
                            }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 120.dp, top = 16.dp, end = 16.dp, start = 16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 5.dp, horizontal = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Autorenew,
                            contentDescription = "Search",
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Check for New Notifications",
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun NotificationScreen(notifications: List<Notification>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        items(notifications) { notification ->
            NotificationItem(notification)
        }
    }
}

@Composable
fun NotificationItem(notification: Notification) {
    val backgroundColor = if (notification.read) Color.LightGray else Color.White
    val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        .format(notification.timestamp.toDate())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(backgroundColor)
            .padding(8.dp)
    ) {
        Text(text = notification.title, fontWeight = FontWeight.Bold)
        Text(text = notification.message)
        Text(text = formattedDate, style = MaterialTheme.typography.labelSmall)
    }
}

class HomeInbox : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HomeInboxScreen()
        }
    }
}