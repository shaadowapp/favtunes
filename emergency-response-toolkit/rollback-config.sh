#!/bin/bash

# Config Rollback Script
# Usage: ./rollback-config.sh [backup-timestamp] [server-endpoint]

set -e

BACKUP_TIMESTAMP=${1}
SERVER_ENDPOINT=${2:-"https://your-domain.com/api"}
BACKUP_DIR="config-backups"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}🔄 Config Rollback Tool${NC}"

if [ -z "$BACKUP_TIMESTAMP" ]; then
    echo -e "${YELLOW}Available backups:${NC}"
    ls -1t "$BACKUP_DIR"/config_backup_*.json | head -10 | sed 's/.*config_backup_\(.*\)\.json/  - \1/'
    echo ""
    read -p "Enter backup timestamp to restore: " BACKUP_TIMESTAMP
fi

BACKUP_FILE="$BACKUP_DIR/config_backup_$BACKUP_TIMESTAMP.json"

if [ ! -f "$BACKUP_FILE" ]; then
    echo -e "${RED}❌ Backup file not found: $BACKUP_FILE${NC}"
    exit 1
fi

echo "Backup file: $BACKUP_FILE"
echo "Server: $SERVER_ENDPOINT"
echo ""

# Validate backup file
echo -e "${YELLOW}🔍 Validating backup file...${NC}"
if ! jq empty "$BACKUP_FILE" 2>/dev/null; then
    echo -e "${RED}❌ Invalid JSON in backup file${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Backup validation passed${NC}"

# Confirm rollback
read -p "Are you sure you want to rollback? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Rollback cancelled"
    exit 0
fi

# Create backup of current config before rollback
echo -e "${YELLOW}📦 Creating safety backup...${NC}"
SAFETY_TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
curl -s "$SERVER_ENDPOINT/advanced-tunes-config.json" > "$BACKUP_DIR/config_before_rollback_$SAFETY_TIMESTAMP.json"

# Perform rollback
echo -e "${YELLOW}🔄 Rolling back configuration...${NC}"
if curl -X PUT "$SERVER_ENDPOINT/advanced-tunes-config.json" \
   -H "Content-Type: application/json" \
   -d @"$BACKUP_FILE" \
   --fail --silent --show-error; then
    echo -e "${GREEN}✅ Rollback completed successfully${NC}"
else
    echo -e "${RED}❌ Rollback failed${NC}"
    exit 1
fi

# Verify rollback
echo -e "${YELLOW}🔍 Verifying rollback...${NC}"
DEPLOYED_VERSION=$(curl -s "$SERVER_ENDPOINT/advanced-tunes-config.json" | jq -r '.version // "unknown"')
BACKUP_VERSION=$(jq -r '.version // "unknown"' "$BACKUP_FILE")

if [ "$DEPLOYED_VERSION" = "$BACKUP_VERSION" ]; then
    echo -e "${GREEN}✅ Rollback verified (version: $DEPLOYED_VERSION)${NC}"
else
    echo -e "${YELLOW}⚠️  Version mismatch but rollback may still be successful${NC}"
    echo "Expected: $BACKUP_VERSION, Got: $DEPLOYED_VERSION"
fi

echo ""
echo -e "${GREEN}🎉 Rollback complete!${NC}"
echo -e "${YELLOW}📊 Next steps:${NC}"
echo "  - Monitor app behavior for 15-30 minutes"
echo "  - Check error rates in logs"
echo "  - Safety backup saved as: config_before_rollback_$SAFETY_TIMESTAMP.json"