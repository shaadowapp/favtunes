#!/usr/bin/env python3
"""
Setup script to upload initial remote config to GitHub
Usage: python setup-github-config.py
"""

import os
import json
import base64
import requests
from datetime import datetime

# Configuration
GITHUB_OWNER = "shaadowapp"  # Replace with your GitHub username
GITHUB_REPO = "cdfheuee3"    # Replace with your repository name
GITHUB_TOKEN = os.getenv("GITHUB_TOKEN")  # Set this environment variable

def upload_config_to_github():
    """Upload the initial config to GitHub repository"""
    
    if not GITHUB_TOKEN:
        print("❌ GITHUB_TOKEN environment variable not set")
        print("Set it with: export GITHUB_TOKEN=your_token_here")
        return False
    
    # Load the config file
    try:
        with open("advanced-tunes-config.json", 'r') as f:
            config_content = f.read()
    except FileNotFoundError:
        print("❌ advanced-tunes-config.json not found")
        return False
    
    # Validate JSON
    try:
        json.loads(config_content)
        print("✅ Config JSON is valid")
    except json.JSONDecodeError as e:
        print(f"❌ Invalid JSON: {e}")
        return False
    
    # Encode content to base64
    encoded_content = base64.b64encode(config_content.encode()).decode()
    
    # Prepare the API request
    url = f"https://api.github.com/repos/{GITHUB_OWNER}/{GITHUB_REPO}/contents/advanced-tunes-config.json"
    headers = {
        "Authorization": f"token {GITHUB_TOKEN}",
        "Content-Type": "application/json"
    }
    
    payload = {
        "message": f"🚀 Initial remote config setup - {datetime.now().isoformat()}",
        "content": encoded_content
    }
    
    # Upload to GitHub
    try:
        response = requests.put(url, headers=headers, json=payload)
        if response.status_code in [200, 201]:
            print("✅ Successfully uploaded config to GitHub!")
            print(f"📍 Config URL: https://raw.githubusercontent.com/{GITHUB_OWNER}/{GITHUB_REPO}/main/advanced-tunes-config.json")
            return True
        else:
            print(f"❌ Failed to upload: {response.status_code}")
            print(f"Response: {response.text}")
            return False
    except Exception as e:
        print(f"❌ Error uploading: {e}")
        return False

def test_config_access():
    """Test if the config can be accessed from the raw URL"""
    url = f"https://raw.githubusercontent.com/{GITHUB_OWNER}/{GITHUB_REPO}/main/advanced-tunes-config.json"
    
    try:
        response = requests.get(url)
        if response.status_code == 200:
            config = response.json()
            print("✅ Config successfully accessible from GitHub!")
            print(f"📊 Config version: {config.get('version', 'Unknown')}")
            return True
        else:
            print(f"❌ Cannot access config: {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ Error accessing config: {e}")
        return False

if __name__ == "__main__":
    print("🚀 Setting up GitHub Remote Config...")
    print(f"📂 Repository: {GITHUB_OWNER}/{GITHUB_REPO}")
    
    if upload_config_to_github():
        print("\n🧪 Testing config access...")
        test_config_access()
        print("\n🎉 GitHub Remote Config setup complete!")
        print("\n📱 Your app will now fetch config from:")
        print(f"   https://raw.githubusercontent.com/{GITHUB_OWNER}/{GITHUB_REPO}/main/advanced-tunes-config.json")
    else:
        print("\n💥 Setup failed. Please check your credentials and try again.")