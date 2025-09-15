#!/usr/bin/env python3
"""
Emergency Remote Config Push Script for Shaadow Tunes
Usage: python emergency-config-push.py [config-type]

Config types:
- youtube-api-issues: For YouTube API rate limiting or issues
- network-issues: For network connectivity problems
- crash-prevention: To prevent app crashes
- disable-playback: Complete playback disable (critical)
- restore-normal: Restore normal configuration
"""

import os
import sys
import json
import base64
import requests
from datetime import datetime

# GitHub configuration
GITHUB_OWNER = "shaadowapp"
GITHUB_REPO = "cdfheuee3"
GITHUB_TOKEN = os.getenv("GITHUB_TOKEN")  # Set this environment variable

def load_config(config_type):
    """Load emergency config from file"""
    if config_type == "restore-normal":
        return load_normal_config()
    
    config_file = f"emergency-configs/{config_type}.json"
    try:
        with open(config_file, 'r') as f:
            return f.read()
    except FileNotFoundError:
        print(f"❌ Config file not found: {config_file}")
        sys.exit(1)

def load_normal_config():
    """Load normal config"""
    try:
        with open("advanced-tunes-config.json", 'r') as f:
            return f.read()
    except FileNotFoundError:
        print("❌ Normal config file not found: advanced-tunes-config.json")
        sys.exit(1)

def get_current_file_sha():
    """Get current file SHA from GitHub"""
    url = f"https://api.github.com/repos/{GITHUB_OWNER}/{GITHUB_REPO}/contents/advanced-tunes-config.json"
    headers = {
        "Authorization": f"token {GITHUB_TOKEN}",
        "Accept": "application/vnd.github.v3+json"
    }
    
    try:
        response = requests.get(url, headers=headers)
        if response.status_code == 200:
            return response.json()["sha"]
        elif response.status_code == 404:
            return None  # File doesn't exist
        else:
            print(f"❌ Failed to get current file SHA: {response.status_code}")
            return None
    except Exception as e:
        print(f"❌ Error getting file SHA: {e}")
        return None

def push_config_to_github(config_content, config_type):
    """Push config to GitHub repository"""
    if not GITHUB_TOKEN:
        print("❌ GITHUB_TOKEN environment variable not set")
        sys.exit(1)
    
    # Get current file SHA
    current_sha = get_current_file_sha()
    
    # Prepare the update
    url = f"https://api.github.com/repos/{GITHUB_OWNER}/{GITHUB_REPO}/contents/advanced-tunes-config.json"
    headers = {
        "Authorization": f"token {GITHUB_TOKEN}",
        "Content-Type": "application/json"
    }
    
    # Encode content to base64
    encoded_content = base64.b64encode(config_content.encode()).decode()
    
    # Prepare payload
    payload = {
        "message": f"🚨 Emergency config update: {config_type} - {datetime.now().isoformat()}",
        "content": encoded_content
    }
    
    if current_sha:
        payload["sha"] = current_sha
    
    try:
        response = requests.put(url, headers=headers, json=payload)
        if response.status_code in [200, 201]:
            print(f"✅ Successfully pushed emergency config: {config_type}")
            print(f"📍 Config URL: https://raw.githubusercontent.com/{GITHUB_OWNER}/{GITHUB_REPO}/main/advanced-tunes-config.json")
            return True
        else:
            print(f"❌ Failed to push config: {response.status_code}")
            print(f"Response: {response.text}")
            return False
    except Exception as e:
        print(f"❌ Error pushing config: {e}")
        return False

def main():
    if len(sys.argv) != 2:
        print(__doc__)
        sys.exit(1)
    
    config_type = sys.argv[1]
    valid_types = [
        "youtube-api-issues",
        "network-issues", 
        "crash-prevention",
        "disable-playback",
        "restore-normal"
    ]
    
    if config_type not in valid_types:
        print(f"❌ Invalid config type: {config_type}")
        print(f"Valid types: {', '.join(valid_types)}")
        sys.exit(1)
    
    print(f"🚨 Pushing emergency config: {config_type}")
    
    # Load config
    config_content = load_config(config_type)
    
    # Validate JSON
    try:
        json.loads(config_content)
        print("✅ Config JSON is valid")
    except json.JSONDecodeError as e:
        print(f"❌ Invalid JSON in config: {e}")
        sys.exit(1)
    
    # Push to GitHub
    success = push_config_to_github(config_content, config_type)
    
    if success:
        print("🎉 Emergency config deployed successfully!")
        print("📱 Apps will receive the new config within 30 minutes (cache duration)")
        if config_type == "disable-playback":
            print("⚠️  WARNING: Playback is now DISABLED for all users!")
    else:
        print("💥 Failed to deploy emergency config")
        sys.exit(1)

if __name__ == "__main__":
    main()