const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

// Send notification to all users
exports.sendNotificationToAll = functions.https.onCall(async (data, context) => {
  // Verify admin access (implement your own auth logic)
  if (!context.auth || !context.auth.token.admin) {
    throw new functions.https.HttpsError('permission-denied', 'Must be an admin to send notifications');
  }

  const { title, body, type, songId, playlistId } = data;

  const message = {
    notification: {
      title: title,
      body: body,
    },
    data: {
      type: type || 'general',
      ...(songId && { songId }),
      ...(playlistId && { playlistId }),
    },
    topic: 'all_users'
  };

  try {
    const response = await admin.messaging().send(message);
    console.log('Successfully sent message:', response);
    return { success: true, messageId: response };
  } catch (error) {
    console.log('Error sending message:', error);
    throw new functions.https.HttpsError('internal', 'Failed to send notification');
  }
});

// Send targeted notification based on user segments
exports.sendTargetedNotification = functions.https.onCall(async (data, context) => {
  if (!context.auth || !context.auth.token.admin) {
    throw new functions.https.HttpsError('permission-denied', 'Must be an admin to send notifications');
  }

  const { title, body, type, segment, songId } = data;

  // Define segments (you can customize these based on your user data)
  const segments = {
    'inactive_users': 'inactive_24h',
    'active_users': 'active_daily',
    'new_users': 'new_users',
    'premium_users': 'premium_users'
  };

  const topic = segments[segment] || 'all_users';

  const message = {
    notification: {
      title: title,
      body: body,
    },
    data: {
      type: type || 'general',
      ...(songId && { songId }),
    },
    topic: topic
  };

  try {
    const response = await admin.messaging().send(message);
    return { success: true, messageId: response };
  } catch (error) {
    console.log('Error sending targeted message:', error);
    throw new functions.https.HttpsError('internal', 'Failed to send notification');
  }
});

// Scheduled function to send daily music suggestions
exports.sendDailySuggestions = functions.pubsub.schedule('0 10 * * *').onRun(async (context) => {
  const suggestions = [
    {
      title: "🎵 Your Daily Music Dose",
      body: "Discover trending tracks that match your vibe today!"
    },
    {
      title: "🎧 Fresh Picks Alert",
      body: "New releases are here! Time to update your playlist"
    },
    {
      title: "🎶 Mood Booster Incoming",
      body: "Perfect songs to make your day 10x better"
    }
  ];

  const randomSuggestion = suggestions[Math.floor(Math.random() * suggestions.length)];

  const message = {
    notification: randomSuggestion,
    data: {
      type: 'music_suggestion',
    },
    topic: 'all_users'
  };

  try {
    await admin.messaging().send(message);
    console.log('Daily suggestion sent successfully');
  } catch (error) {
    console.log('Error sending daily suggestion:', error);
  }
});

// Function to subscribe users to topics based on their activity
exports.updateUserTopics = functions.firestore.document('users/{userId}').onUpdate(async (change, context) => {
  const before = change.before.data();
  const after = change.after.data();
  const userId = context.params.userId;

  // Get user's FCM token
  const fcmToken = after.fcmToken;
  if (!fcmToken) return;

  // Subscribe/unsubscribe based on activity
  const lastActive = after.lastActive?.toDate();
  const now = new Date();
  const hoursSinceActive = (now - lastActive) / (1000 * 60 * 60);

  try {
    if (hoursSinceActive > 24) {
      // Subscribe to inactive users topic
      await admin.messaging().subscribeToTopic([fcmToken], 'inactive_24h');
      await admin.messaging().unsubscribeFromTopic([fcmToken], 'active_daily');
    } else {
      // Subscribe to active users topic
      await admin.messaging().subscribeToTopic([fcmToken], 'active_daily');
      await admin.messaging().unsubscribeFromTopic([fcmToken], 'inactive_24h');
    }

    // Always subscribe to all users topic
    await admin.messaging().subscribeToTopic([fcmToken], 'all_users');
  } catch (error) {
    console.log('Error updating user topics:', error);
  }
});