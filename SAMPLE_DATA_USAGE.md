# 🎯 Firebase Sample Data Seeder Usage Guide

## 📋 Overview
The Firebase Sample Data Seeder automatically populates your Firestore database with realistic test data for bug reports, user feedback, and admin users.

## 🚀 How It Works

### Automatic Seeding
- **Runs once**: The seeder runs automatically when you first launch the app
- **Smart detection**: It won't seed data again unless you reset the flag
- **Background operation**: Seeding happens in the background without blocking the UI

### What Gets Seeded

#### 🐛 Bug Reports (5 samples)
1. **High Severity**: App crashes when playing music
2. **Medium Severity**: Search function not working properly  
3. **Medium Severity**: Sync issues with offline downloads
4. **Low Severity**: UI elements overlapping on small screens
5. **Critical Severity**: Performance issues during peak hours

#### 💬 User Feedback (7 samples)
- Ratings from 1-5 stars
- Different categories (General, Feature Request, UI/UX, Performance, Other)
- Mix of anonymous and identified feedback
- Realistic user comments and suggestions

#### 👨‍💼 Admin Users (2 samples)
- **System Administrator**: Full admin permissions
- **Content Moderator**: Limited admin permissions

## 📱 Using the Sample Data

### Method 1: Automatic (Recommended)
1. **First Launch**: Simply run your app - data will be seeded automatically
2. **Check Firebase**: Go to Firebase Console → Firestore Database
3. **View Collections**: You'll see `bug_reports`, `user_feedback`, and `admin_users` collections

### Method 2: Manual Debug Tool (Debug Builds Only)
1. **Open Settings**: Go to app Settings
2. **Find Debug Section**: Scroll down to see "Debug" section (only in debug builds)
3. **Tap "Firebase Sample Data"**: Opens the debug tool
4. **Seed Data**: Tap "Seed Sample Data" button
5. **Check Status**: View success/error messages

### Method 3: Reset and Re-seed
1. **Open Debug Tool**: Settings → Firebase Sample Data
2. **Reset Flag**: Tap "Clear Flag" button
3. **Restart App**: Close and reopen the app
4. **Auto-seed**: Data will be seeded again automatically

## 🔍 Verifying the Data

### In Firebase Console
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Navigate to Firestore Database
4. Check these collections:
   - `bug_reports` (5 documents)
   - `user_feedback` (7 documents) 
   - `admin_users` (2 documents)

### In Your App
1. **Bug Reports**: Go to Settings → Bug Report → Check if form works
2. **Feedback**: Go to Settings → Feedback → Submit test feedback
3. **Offline Mode**: Turn off internet, submit data, turn on internet to see sync

## 🛠️ Development Tips

### Testing Different Scenarios
```kotlin
// Force seed data (in debug code)
val seeder = FirebaseSampleDataSeeder(context)
seeder.forceSeedSampleData()

// Clear flag to re-seed on next launch
seeder.clearSeededDataFlag()
```

### Customizing Sample Data
Edit `FirebaseSampleDataSeeder.kt` to:
- Add more sample bug reports
- Change feedback messages
- Modify admin user permissions
- Adjust device information

### Production Considerations
- **Debug Only**: The debug menu only appears in debug builds
- **One-time Seeding**: Production apps won't re-seed data
- **Clean Slate**: Remove seeder calls before production release

## 📊 Sample Data Structure

### Bug Report Example
```json
{
  "title": "App crashes when playing music",
  "description": "The app crashes randomly when I try to play music...",
  "severity": "HIGH",
  "category": "PLAYBACK", 
  "deviceInfo": {
    "deviceModel": "Your Device Model",
    "osVersion": "Android Version",
    "appVersion": "1.0.0"
  },
  "reproductionSteps": [
    "Open the app",
    "Go to playlists", 
    "Select any playlist"
  ],
  "timestamp": "2024-01-01T12:00:00Z"
}
```

### Feedback Example
```json
{
  "rating": 5,
  "category": "GENERAL",
  "message": "Absolutely love this app! The music quality is excellent...",
  "deviceInfo": { /* same as bug report */ },
  "isAnonymous": true,
  "timestamp": "2024-01-01T12:00:00Z"
}
```

## 🚨 Troubleshooting

### Data Not Appearing
- **Check Internet**: Ensure device has internet connection
- **Check Firebase Rules**: Verify security rules are deployed
- **Check Logs**: Look for error messages in Android Studio logs
- **Verify Project**: Ensure `google-services.json` is correct

### Permission Errors
- **Security Rules**: Make sure Firestore security rules allow writes
- **Anonymous Auth**: Verify anonymous authentication is enabled

### Debug Menu Missing
- **Debug Build**: Debug menu only shows in debug builds
- **Build Config**: Verify `BuildConfig.DEBUG` is true

## 🎉 Success Indicators

You'll know it worked when:
- ✅ Firebase Console shows the 3 collections with sample data
- ✅ App logs show "Sample data seeded successfully!"
- ✅ Bug report and feedback forms work properly
- ✅ Offline submissions sync when back online

## 🔄 Next Steps

After seeding sample data:
1. **Test the UI**: Try submitting real bug reports and feedback
2. **Test Offline**: Submit data while offline, then go online
3. **Check Security**: Verify only appropriate users can read data
4. **Monitor Usage**: Watch Firebase Console for read/write metrics
5. **Deploy Rules**: Ensure security rules are properly deployed

Happy testing! 🚀