# Emergency Remote Config System

This directory contains emergency configurations for **Shaadow Tunes** (`shaadowapp/cdfheuee3`) that can be deployed during crisis situations.

## 🚨 Crisis Scenarios & Configs

### 1. YouTube API Issues (`youtube-api-issues.json`)
**When to use:** YouTube API rate limiting, temporary API issues, or service degradation

**Features:**
- Increased retry attempts with longer delays
- Enhanced error logging
- Automatic skipping of problematic songs
- Larger cache to reduce API calls
- Conservative connection settings

### 2. Network Connectivity Issues (`network-connectivity-issues.json`)
**When to use:** Poor network conditions, server connectivity problems

**Features:**
- Reduced concurrent requests (1 max)
- Larger buffer sizes for stability
- Disabled preloading to save bandwidth
- Extended timeouts
- Minimal logging to reduce overhead

### 3. Critical App Crashes (`critical-app-crash.json`)
**When to use:** Reports of widespread app crashes or instability

**Features:**
- Minimal retry attempts
- Disabled caching and preloading
- Reduced buffer sizes
- Immediate skipping of problematic content
- All non-essential features disabled

### 4. Complete Playback Disable (`disable-playback.json`)
**When to use:** Critical security issues, legal problems, or major service outage

**Features:**
- Playback completely disabled
- All streaming features turned off
- Emergency message displayed to users
- Minimal resource usage

## 🛠️ Deployment Methods

### Method 1: Python Script (Recommended)
```bash
# Set your GitHub token
export GITHUB_TOKEN=your_github_token_here

# Deploy emergency config
python scripts/emergency-config-push.py youtube-api-issues

# Restore normal config
python scripts/emergency-config-push.py restore-normal
```

### Method 2: Shell Script
```bash
# Set your GitHub token
export GITHUB_TOKEN=your_github_token_here

# Deploy emergency config
./scripts/emergency-config-push.sh network-issues

# Restore normal config
./scripts/emergency-config-push.sh restore-normal
```

### Method 3: Manual GitHub Upload
1. Go to your GitHub repository: `https://github.com/shaadowapp/cdfheuee3`
2. Navigate to `advanced-tunes-config.json`
3. Click "Edit" and replace content with emergency config
4. Commit with message: "🚨 Emergency config: [crisis-type]"

### Method 4: GitHub API (curl)
```bash
# Get the emergency config content
CONFIG_CONTENT=$(cat emergency-configs/youtube-api-issues.json | base64 -w 0)

# Push to GitHub
curl -X PUT \
  -H "Authorization: token $GITHUB_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"message\": \"🚨 Emergency config: YouTube API issues\",
    \"content\": \"$CONFIG_CONTENT\"
  }" \
  "https://api.github.com/repos/shaadowapp/cdfheuee3/contents/advanced-tunes-config.json"
```

## ⏱️ Deployment Timeline

1. **Config Push**: Immediate (< 1 minute)
2. **GitHub CDN**: 1-2 minutes
3. **App Cache Refresh**: Up to 30 minutes (configurable)
4. **User Impact**: 30-35 minutes maximum

## 🔧 Configuration Details

### Repository Information
- **Owner**: `shaadowapp`
- **Repository**: `cdfheuee3`
- **Config File**: `advanced-tunes-config.json`
- **Raw URL**: `https://raw.githubusercontent.com/shaadowapp/cdfheuee3/main/advanced-tunes-config.json`

### GitHub Token Requirements
Your GitHub token needs these permissions:
- **Contents**: Read and Write
- **Repository**: Access to `shaadowapp/cdfheuee3`

### App Integration
The app checks for config updates every 30 minutes and applies changes automatically. No app restart required.

## 📱 User Experience During Crisis

### YouTube API Issues
- Users see: "⚠️ YouTube API experiencing issues. Some songs may skip automatically."
- Behavior: Automatic retries with graceful fallbacks

### Network Issues  
- Users see: "🌐 Network connectivity issues detected. Reduced functionality for better stability."
- Behavior: Conservative streaming with larger buffers

### Crash Prevention
- Users see: "🚨 Stability mode activated. Some features disabled to prevent crashes."
- Behavior: Minimal features, maximum stability

### Playback Disabled
- Users see: "🚨 CRITICAL: Playback temporarily disabled due to emergency. Please update the app or check for announcements."
- Behavior: No playback functionality

## 🔄 Restoring Normal Operation

Always restore normal config after crisis resolution:

```bash
# Using Python script
python scripts/emergency-config-push.py restore-normal

# Using shell script
./scripts/emergency-config-push.sh restore-normal
```

## 📊 Monitoring & Alerts

Consider setting up monitoring for:
- Config deployment success/failure
- App error rates after deployment
- User engagement metrics
- GitHub API rate limits

## 🔒 Security Notes

- Keep GitHub tokens secure and rotate regularly
- Use environment variables, never commit tokens
- Monitor repository access logs
- Consider using GitHub Apps for better security

## 📞 Emergency Contacts

During a crisis, ensure these people have access to deploy configs:
- Primary: [Your contact info]
- Secondary: [Backup contact info]
- GitHub Repository: `https://github.com/shaadowapp/cdfheuee3`

---

**Remember**: Emergency configs are temporary solutions. Always investigate root causes and deploy proper fixes through regular app updates.