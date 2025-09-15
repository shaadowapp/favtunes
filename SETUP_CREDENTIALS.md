# 🔐 Credentials Setup Guide

## Quick Setup

1. **Copy the template**:
   ```bash
   cp local.properties.template local.properties
   ```

2. **Edit `local.properties`** with your actual credentials:
   ```properties
   # Your Android SDK path
   sdk.dir=/path/to/your/android/sdk
   
   # API Keys (required)
   YOUTUBE_API_KEY=your_actual_youtube_api_key
   ONESIGNAL_APP_ID=your_actual_onesignal_app_id
   
   # GitHub Remote Config (optional)
   GITHUB_TOKEN=your_github_personal_access_token
   REMOTE_CONFIG_OWNER=your_github_username
   REMOTE_CONFIG_REPO=your_private_repo_name
   ```

3. **Build the app** - credentials are automatically injected!

## 🔒 Security Features

- ✅ `local.properties` is **gitignored** - never committed
- ✅ Credentials injected at **build time** via BuildConfig
- ✅ **Encrypted fallback** storage for runtime updates
- ✅ **No hardcoded** sensitive data in source code

## 🎯 Credential Priority

1. **local.properties** → BuildConfig (Primary)
2. EncryptedSharedPreferences (Fallback)
3. Hardcoded defaults (Last resort)

## 📝 Notes

- The app works with default values if credentials aren't set
- You can update credentials without rebuilding using the SecureCredentialsManager API
- All sensitive data is handled securely at runtime