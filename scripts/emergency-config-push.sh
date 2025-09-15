#!/bin/bash
# Emergency Remote Config Push Script for Shaadow Tunes
# Usage: ./emergency-config-push.sh [config-type]

set -e

# GitHub configuration
GITHUB_OWNER="shaadowapp"
GITHUB_REPO="cdfheuee3"

if [ -z "$GITHUB_TOKEN" ]; then
    echo "❌ GITHUB_TOKEN environment variable not set"
    echo "Export your GitHub token: export GITHUB_TOKEN=your_token_here"
    exit 1
fi

if [ $# -ne 1 ]; then
    echo "Usage: $0 [config-type]"
    echo ""
    echo "Config types:"
    echo "  youtube-api-issues  - For YouTube API rate limiting or issues"
    echo "  network-issues      - For network connectivity problems"
    echo "  crash-prevention    - To prevent app crashes"
    echo "  disable-playback    - Complete playback disable (critical)"
    echo "  restore-normal      - Restore normal configuration"
    exit 1
fi

CONFIG_TYPE=$1

case $CONFIG_TYPE in
    "youtube-api-issues"|"network-issues"|"crash-prevention"|"disable-playback")
        CONFIG_FILE="emergency-configs/${CONFIG_TYPE}.json"
        ;;
    "restore-normal")
        CONFIG_FILE="advanced-tunes-config.json"
        ;;
    *)
        echo "❌ Invalid config type: $CONFIG_TYPE"
        exit 1
        ;;
esac

if [ ! -f "$CONFIG_FILE" ]; then
    echo "❌ Config file not found: $CONFIG_FILE"
    exit 1
fi

echo "🚨 Pushing emergency config: $CONFIG_TYPE"

# Validate JSON
if ! jq empty "$CONFIG_FILE" 2>/dev/null; then
    echo "❌ Invalid JSON in config file"
    exit 1
fi

echo "✅ Config JSON is valid"

# Get current file SHA
CURRENT_SHA=$(curl -s \
    -H "Authorization: token $GITHUB_TOKEN" \
    -H "Accept: application/vnd.github.v3+json" \
    "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/contents/advanced-tunes-config.json" \
    | jq -r '.sha // empty' 2>/dev/null || echo "")

# Encode content to base64
ENCODED_CONTENT=$(base64 -w 0 "$CONFIG_FILE")

# Prepare JSON payload
if [ -n "$CURRENT_SHA" ]; then
    PAYLOAD=$(jq -n \
        --arg message "🚨 Emergency config update: $CONFIG_TYPE - $(date -Iseconds)" \
        --arg content "$ENCODED_CONTENT" \
        --arg sha "$CURRENT_SHA" \
        '{message: $message, content: $content, sha: $sha}')
else
    PAYLOAD=$(jq -n \
        --arg message "🚨 Emergency config update: $CONFIG_TYPE - $(date -Iseconds)" \
        --arg content "$ENCODED_CONTENT" \
        '{message: $message, content: $content}')
fi

# Push to GitHub
RESPONSE=$(curl -s -w "%{http_code}" \
    -X PUT \
    -H "Authorization: token $GITHUB_TOKEN" \
    -H "Content-Type: application/json" \
    -d "$PAYLOAD" \
    "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/contents/advanced-tunes-config.json")

HTTP_CODE="${RESPONSE: -3}"
RESPONSE_BODY="${RESPONSE%???}"

if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "201" ]; then
    echo "✅ Successfully pushed emergency config: $CONFIG_TYPE"
    echo "📍 Config URL: https://raw.githubusercontent.com/$GITHUB_OWNER/$GITHUB_REPO/main/advanced-tunes-config.json"
    echo "🎉 Emergency config deployed successfully!"
    echo "📱 Apps will receive the new config within 30 minutes (cache duration)"
    
    if [ "$CONFIG_TYPE" = "disable-playback" ]; then
        echo "⚠️  WARNING: Playback is now DISABLED for all users!"
    fi
else
    echo "❌ Failed to push config: HTTP $HTTP_CODE"
    echo "Response: $RESPONSE_BODY"
    exit 1
fi