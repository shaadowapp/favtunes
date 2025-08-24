# 🔥 Complete Firebase Setup Guide for Bug Reports & Feedback

## 📋 Overview

This guide will help you set up Firebase Firestore to store bug reports and user feedback from your Android app.

## 🚀 Step 1: Firebase Project Setup

### 1.1 Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Create a project" or "Add project"
3. Enter your project name (e.g., "favtunes-app")
4. Enable Google Analytics (recommended)
5. Choose or create a Google Analytics account
6. Click "Create project"

### 1.2 Add Android App to Firebase

1. In your Firebase project, click "Add app" → Android icon
2. Enter your Android package name: `com.shaadow.tunes`
3. Enter app nickname: "FavTunes"
4. Enter SHA-1 certificate fingerprint (optional but recommended for auth)
5. Click "Register app"
6. Download `google-services.json`
7. Place it in your `app/` directory

### 1.3 Configure Firebase SDK (Already Done)

Your app already has Firebase configured, but verify these files exist:

- `app/google-services.json` ✅
- Firebase dependencies in `app/build.gradle` ✅

## 🗄️ Step 2: Create Firestore Database

### 2.1 Enable Firestore

1. In Firebase Console, go to "Firestore Database"
2. Click "Create database"
3. Choose "Start in test mode" (we'll add security rules later)
4. Select a location (choose closest to your users)
5. Click "Done"

### 2.2 Create Collections

**Important**: Firestore collections are created automatically when you first write data to them. However, you can create them manually:

#### Create `bug_reports` Collection:

1. In Firestore Console, click "Start collection"
2. Collection ID: `bug_reports`
3. Add a sample document:
   - Document ID: `sample_bug_report`
   - Fields:
     ```
     title: "Sample Bug Report"
     description: "This is a sample bug report"
     severity: "MEDIUM"
     category: "UI"
     deviceInfo: {
       deviceModel: "Sample Device",
       osVersion: "Android 13",
       appVersion: "1.0.0"
     }
     appVersion: "1.0.0"
     timestamp: [Current timestamp]
     userId: "sample_user"
     ```
4. Click "Save"

#### Create `user_feedback` Collection:

1. Click "Start collection"
2. Collection ID: `user_feedback`
3. Add a sample document:
   - Document ID: `sample_feedback`
   - Fields:
     ```
     rating: 5
     category: "GENERAL"
     message: "Great app!"
     deviceInfo: {
       deviceModel: "Sample Device",
       osVersion: "Android 13",
       appVersion: "1.0.0"
     }
     appVersion: "1.0.0"
     timestamp: [Current timestamp]
     isAnonymous: true
     userId: "sample_user"
     ```
4. Click "Save"

## 🔒 Step 3: Deploy Security Rules

### 3.1 Install Firebase CLI

```bash
npm install -g firebase-tools
```

### 3.2 Login to Firebase

```bash
firebase login
```

### 3.3 Initialize Firebase in Your Project

```bash
cd /path/to/your/project
firebase init firestore
```

- Select your Firebase project
- Keep default Firestore rules file: `firestore.rules`
- Keep default Firestore indexes file: `firestore.indexes.json`

### 3.4 Deploy Security Rules

The `firestore.rules` file is already created in your project. Deploy it:

```bash
firebase deploy --only firestore:rules
```

### 3.5 Verify Rules Deployment

1. Go to Firebase Console → Firestore Database → Rules
2. You should see your custom rules deployed
3. Test the rules using the Rules Playground

## 📊 Step 4: Set Up Indexes (Optional but Recommended)

### 4.1 Create Composite Indexes

Add these to your `firestore.indexes.json`:

```json
{
  "indexes": [
    {
      "collectionGroup": "bug_reports",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "userId", "order": "ASCENDING" },
        { "fieldPath": "timestamp", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "bug_reports",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "severity", "order": "ASCENDING" },
        { "fieldPath": "timestamp", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "user_feedback",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "userId", "order": "ASCENDING" },
        { "fieldPath": "timestamp", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "user_feedback",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "rating", "order": "ASCENDING" },
        { "fieldPath": "timestamp", "order": "DESCENDING" }
      ]
    }
  ],
  "fieldOverrides": []
}
```

### 4.2 Deploy Indexes

```bash
firebase deploy --only firestore:indexes
```

## 🔧 Step 5: Test Your Setup

### 5.1 Test from Android App

1. Build and run your app
2. Go to Settings → Bug Report
3. Fill out the form and submit
4. Check Firebase Console → Firestore Database
5. You should see your bug report in the `bug_reports` collection

### 5.2 Test Feedback

1. Go to Settings → Feedback
2. Rate the app and write feedback
3. Submit the feedback
4. Check the `user_feedback` collection in Firestore

## 👨‍💼 Step 6: Set Up Admin Access (Optional)

### 6.1 Create Admin User

In Firestore Console, create a new collection:

1. Collection ID: `admin_users`
2. Document ID: Your user ID (or email)
3. Fields:
   ```
   role: "admin"
   email: "your-email@example.com"
   createdAt: [Current timestamp]
   ```

### 6.2 Admin Capabilities

Admin users can:

- Read all bug reports and feedback
- View analytics and trends
- Export data for analysis

## 📈 Step 7: Monitor and Analytics

### 7.1 Set Up Usage Monitoring

1. Go to Firebase Console → Firestore Database → Usage
2. Monitor reads, writes, and storage usage
3. Set up billing alerts if needed

### 7.2 Set Up Performance Monitoring

1. Go to Firebase Console → Performance
2. Enable Performance Monitoring
3. Monitor app performance and Firestore query performance

## 🚨 Step 8: Important Security Considerations

### 8.1 Review Security Rules

- Anonymous submissions are allowed but validated
- Users can only read their own data
- Admin access is controlled via `admin_users` collection

### 8.2 Rate Limiting

- Basic rate limiting is in security rules (60 seconds between submissions)
- Consider implementing server-side rate limiting for production

### 8.3 Data Privacy

- Device info collection is privacy-safe
- No personal information is collected without consent
- Anonymous submissions are truly anonymous

## 🔍 Step 9: Troubleshooting

### Common Issues:

#### "Permission denied" errors:

- Check security rules are deployed
- Verify user authentication state
- Check document structure matches validation rules

#### Data not appearing:

- Check network connectivity
- Verify Firestore rules allow writes
- Check app logs for errors

#### Offline functionality not working:

- Ensure Firestore offline persistence is enabled (it is by default)
- Check Room database setup for local queuing

### Debug Commands:

```bash
# Check current rules
firebase firestore:rules:get

# Test rules locally
firebase emulators:start --only firestore

# View logs
firebase functions:log
```

## 📱 Step 10: Production Checklist

Before going live:

- [ ] Security rules deployed and tested
- [ ] Indexes created for performance
- [ ] Billing alerts configured
- [ ] Admin access set up
- [ ] Monitoring enabled
- [ ] Backup strategy implemented
- [ ] Data retention policy defined

## 🎯 Data Structure Reference

### Bug Report Document:

```javascript
{
  id: "auto-generated",
  userId: "user-id-or-null",
  title: "Bug title (1-200 chars)",
  description: "Bug description (1-5000 chars)",
  severity: "LOW|MEDIUM|HIGH|CRITICAL",
  category: "UI|PLAYBACK|SYNC|PERFORMANCE|CRASH|OTHER",
  deviceInfo: {
    deviceModel: "Device model",
    osVersion: "OS version",
    appVersion: "App version",
    locale: "Locale",
    screenResolution: "Screen resolution",
    availableMemory: 123456789,
    networkType: "WiFi|Cellular|etc"
  },
  appVersion: "App version",
  timestamp: "Firestore timestamp",
  attachments: ["attachment-urls"],
  status: "OPEN|IN_PROGRESS|RESOLVED|CLOSED",
  reproductionSteps: ["Step 1", "Step 2"]
}
```

### Feedback Document:

```javascript
{
  id: "auto-generated",
  userId: "user-id-or-null",
  rating: 1-5,
  category: "GENERAL|FEATURE_REQUEST|UI_UX|PERFORMANCE|OTHER",
  message: "Feedback message (1-2000 chars)",
  deviceInfo: { /* same as bug report */ },
  appVersion: "App version",
  timestamp: "Firestore timestamp",
  isAnonymous: true/false
}
```

## 🎉 You're All Set!

Your Firebase setup is now complete! Users can submit bug reports and feedback that will be:

- ✅ Stored securely in Firestore
- ✅ Validated by security rules
- ✅ Available offline with automatic sync
- ✅ Accessible through your admin dashboard

Need help? Check the troubleshooting section or Firebase documentation!
