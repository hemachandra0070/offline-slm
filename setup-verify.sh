#!/bin/bash

# Setup verification script for Pragnya
# This script checks if the Gemma model file is properly placed

set -e

echo "=========================================="
echo "Pragnya Setup Verification"
echo "=========================================="
echo ""

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if we're in the right directory
if [ ! -f "settings.gradle.kts" ]; then
    echo -e "${RED}❌ Error: This script must be run from the root directory of the Pragnya project${NC}"
    exit 1
fi

echo "✓ Running from correct directory"
echo ""

# Check if assets directory exists
if [ ! -d "app/src/main/assets" ]; then
    echo -e "${RED}❌ Error: Assets directory does not exist${NC}"
    echo "   Creating it now..."
    mkdir -p app/src/main/assets
    echo -e "${GREEN}✓ Created assets directory${NC}"
else
    echo "✓ Assets directory exists"
fi
echo ""

# Check if model file exists
MODEL_FILE="app/src/main/assets/gemma3-1b-it-int4.task"
if [ ! -f "$MODEL_FILE" ]; then
    echo -e "${RED}❌ Model file NOT found!${NC}"
    echo ""
    echo "The Gemma model file is required for the app to work."
    echo ""
    echo "Please follow these steps:"
    echo "1. Visit: https://www.kaggle.com/models/google/gemma/tfLite/gemma3-1b-it-int4"
    echo "2. Create/Login to your Kaggle account"
    echo "3. Accept the model's terms and conditions"
    echo "4. Download the 'gemma3-1b-it-int4.task' file (~1-2 GB)"
    echo "5. Place it at: $MODEL_FILE"
    echo ""
    echo "After downloading, run this script again to verify."
    exit 1
else
    echo -e "${GREEN}✓ Model file found${NC}"
    
    # Check file size
    FILE_SIZE=$(stat -f%z "$MODEL_FILE" 2>/dev/null || stat -c%s "$MODEL_FILE" 2>/dev/null || echo "unknown")
    if [ "$FILE_SIZE" != "unknown" ]; then
        FILE_SIZE_MB=$((FILE_SIZE / 1024 / 1024))
        echo "  File size: ${FILE_SIZE_MB} MB"
        
        if [ $FILE_SIZE_MB -lt 100 ]; then
            echo -e "${YELLOW}⚠️  Warning: File seems too small (expected ~1000-2000 MB)${NC}"
            echo "   The file might be corrupted or incomplete."
        elif [ $FILE_SIZE_MB -gt 3000 ]; then
            echo -e "${YELLOW}⚠️  Warning: File seems too large (expected ~1000-2000 MB)${NC}"
            echo "   Please verify you downloaded the correct file."
        else
            echo -e "${GREEN}✓ File size looks good${NC}"
        fi
    fi
fi
echo ""

# Check if Android SDK is available (optional)
if command -v adb &> /dev/null; then
    echo "✓ Android Debug Bridge (adb) is available"
    
    # Check for connected devices
    DEVICES=$(adb devices | grep -v "List" | grep "device" | wc -l)
    if [ $DEVICES -gt 0 ]; then
        echo -e "${GREEN}✓ Found $DEVICES connected Android device(s)${NC}"
    else
        echo -e "${YELLOW}⚠️  No Android devices connected${NC}"
        echo "   Connect a device or start an emulator to run the app"
    fi
else
    echo -e "${YELLOW}⚠️  Android SDK tools not found in PATH${NC}"
    echo "   Install Android Studio to get the SDK tools"
fi
echo ""

# Check for Gradle wrapper
if [ -f "gradlew" ]; then
    echo "✓ Gradle wrapper found"
else
    echo -e "${RED}❌ Gradle wrapper not found${NC}"
    exit 1
fi
echo ""

echo "=========================================="
echo "Setup Verification Complete!"
echo "=========================================="
echo ""

if [ -f "$MODEL_FILE" ]; then
    echo -e "${GREEN}✓ Your setup is ready!${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Open the project in Android Studio"
    echo "2. Wait for Gradle sync to complete"
    echo "3. Connect an Android device or start an emulator"
    echo "4. Click the Run button (or press Shift+F10)"
    echo ""
    echo "Or build from command line:"
    echo "  ./gradlew assembleDebug"
else
    echo -e "${RED}✗ Setup is NOT complete${NC}"
    echo ""
    echo "You need to download and place the Gemma model file."
    echo "See instructions above."
fi

echo ""
