#!/bin/bash

# Emergency Config Deployment Script
# Usage: ./deploy-emergency-config.sh [crisis-type] [server-endpoint]

set -e

CRISIS_TYPE=${1:-"video-id-crisis"}
SERVER_ENDPOINT=${2:-"https://your-domain.com/api"}
CONFIG_DIR="emergency-configs"
BACKUP_DIR="config-backups"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}🚨 Emergency Config Deployment Tool${NC}"
echo "Crisis Type: $CRISIS_TYPE"
echo "Server: $SERVER_ENDPOINT"
echo ""

# Create backup directory if it doesn't exist
mkdir -p "$BACKUP_DIR"

# Backup current config
echo -e "${YELLOW}📦 Backing up current config...${NC}"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
curl -s "$SERVER_ENDPOINT/advanced-tunes-config.json" > "$BACKUP_DIR/config_backup_$TIMESTAMP.json"
echo -e "${GREEN}✅ Backup saved: config_backup_$TIMESTAMP.json${NC}"

# Validate emergency config exists
EMERGENCY_CONFIG="$CONFIG_DIR/$CRISIS_TYPE.json"
if [ ! -f "$EMERGENCY_CONFIG" ]; then
    echo -e "${RED}❌ Emergency config not found: $EMERGENCY_CONFIG${NC}"
    echo "Available configs:"
    ls -1 "$CONFIG_DIR"/*.json | sed 's/.*\//  - /'
    exit 1
fi

# Validate JSON
echo -e "${YELLOW}🔍 Validating emergency config...${NC}"
if ! jq empty "$EMERGENCY_CONFIG" 2>/dev/null; then
    echo -e "${RED}❌ Invalid JSON in $EMERGENCY_CONFIG${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Config validation passed${NC}"

# Deploy emergency config
echo -e "${YELLOW}🚀 Deploying emergency config...${NC}"
if curl -X PUT "$SERVER_ENDPOINT/advanced-tunes-config.json" \
   -H "Content-Type: application/json" \
   -d @"$EMERGENCY_CONFIG" \
   --fail --silent --show-error; then
    echo -e "${GREEN}✅ Emergency config deployed successfully${NC}"
else
    echo -e "${RED}❌ Failed to deploy emergency config${NC}"
    exit 1
fi

# Verify deployment
echo -e "${YELLOW}🔍 Verifying deployment...${NC}"
DEPLOYED_VERSION=$(curl -s "$SERVER_ENDPOINT/advanced-tunes-config.json" | jq -r '.version // "unknown"')
EXPECTED_VERSION=$(jq -r '.version // "unknown"' "$EMERGENCY_CONFIG")

if [ "$DEPLOYED_VERSION" = "$EXPECTED_VERSION" ]; then
    echo -e "${GREEN}✅ Deployment verified (version: $DEPLOYED_VERSION)${NC}"
else
    echo -e "${RED}❌ Deployment verification failed${NC}"
    echo "Expected: $EXPECTED_VERSION, Got: $DEPLOYED_VERSION"
    exit 1
fi

# Send notification to users
echo -e "${YELLOW}📢 Sending user notification...${NC}"
NOTIFICATION_PAYLOAD=$(cat <<EOF
{
  "title": "🔄 Service Update",
  "body": "We're addressing playback issues. Your music experience may improve shortly.",
  "type": "service_update"
}
EOF
)

if command -v firebase &> /dev/null; then
    echo "$NOTIFICATION_PAYLOAD" | firebase functions:shell --project your-project-id <<< "sendNotificationToAll($NOTIFICATION_PAYLOAD)"
    echo -e "${GREEN}✅ User notification sent${NC}"
else
    echo -e "${YELLOW}⚠️  Firebase CLI not found. Skipping notification.${NC}"
fi

echo ""
echo -e "${GREEN}🎉 Emergency deployment complete!${NC}"
echo -e "${YELLOW}📊 Monitor the situation:${NC}"
echo "  - Check logs: tail -f /var/log/your-app/errors.log"
echo "  - Monitor endpoint: curl $SERVER_ENDPOINT/advanced-tunes-config.json"
echo "  - Rollback if needed: ./rollback-config.sh $TIMESTAMP"
echo ""
echo -e "${YELLOW}⏰ Config will be fetched by apps within 15 minutes${NC}"