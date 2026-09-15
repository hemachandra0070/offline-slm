# Pragnya - AI-Powered Chat with Image OCR

Pragnya is an Android application that provides an intelligent chat interface powered by Google's Gemma AI model. The app features text-based conversations and image text extraction (OCR) capabilities using Google's ML Kit.

> **🚀 Quick Start**: Want to get started fast? Check out [QUICKSTART.md](QUICKSTART.md) for a 3-step setup guide!

## Features

- 💬 **AI Chat Interface**: Engage in natural conversations with the Gemma 3 language model
- 📸 **Image OCR**: Extract text from images using ML Kit's text recognition
- 🎨 **Modern Material Design**: Clean, intuitive UI built with Jetpack Compose
- ⚡ **Real-time Responses**: Fast, on-device AI inference
- 🛑 **Generation Control**: Stop ongoing AI responses at any time

## Prerequisites

Before building and running the app, ensure you have:

- **Android Studio**: Latest version (Arctic Fox or newer recommended)
- **JDK 11**: Required for building the project
- **Android SDK**: API level 24 (Android 7.0) or higher
- **Minimum 4GB RAM**: For running Android Studio and the emulator
- **Storage**: At least 5GB free space (including model file)

## Setup Instructions

### Step 1: Clone the Repository

```bash
git clone https://github.com/prakash2077/Pragnya.git
cd Pragnya
```

### Step 2: Download the Gemma Model File

**This is the most important step!** The app requires the Gemma AI model file to function.

1. **Visit Kaggle**: Go to https://www.kaggle.com/models/google/gemma/tfLite/gemma3-1b-it-int4

2. **Create/Login to Kaggle Account**: You'll need a Kaggle account to download models

3. **Accept Terms**: Read and accept the model's terms and conditions

4. **Download the Model**: Download the `gemma3-1b-it-int4.task` file
   - File size: Approximately 1-2 GB
   - Download time: Depends on your internet connection

5. **Place the Model File**: Copy the downloaded file to the assets directory
   ```
   Pragnya/app/src/main/assets/gemma3-1b-it-int4.task
   ```

**Important Notes:**
- The filename must be **exactly** `gemma3-1b-it-int4.task`
- Do NOT rename the file
- The model file is excluded from git (it's too large to commit)
- Without this file, the app will show an error: "Failed to copy model from assets"

### Step 3: Verify Setup (Optional but Recommended)

Run the setup verification script to ensure everything is configured correctly:

**On Linux/Mac:**
```bash
./setup-verify.sh
```

**On Windows:**
```cmd
setup-verify.bat
```

This script will:
- Check if the assets directory exists
- Verify the model file is present and has the correct size
- Check for connected Android devices
- Confirm all required files are in place

### Step 4: Open Project in Android Studio

1. Launch Android Studio
2. Select **File → Open**
3. Navigate to the cloned `Pragnya` directory
4. Click **OK** to open the project
5. Wait for Gradle sync to complete (this may take a few minutes)

### Step 5: Build and Run

#### Option A: Using Android Studio

1. Connect an Android device via USB (with USB debugging enabled) OR start an emulator
2. Click the **Run** button (green play icon) or press `Shift + F10`
3. Select your target device
4. Wait for the app to build and install

#### Option B: Using Command Line

```bash
# Build the APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Or build and install in one step
./gradlew installDebug
```

## Project Structure

```
Pragnya/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── assets/
│   │   │   │   ├── README.md           # Instructions for model file
│   │   │   │   └── .gitkeep            # Ensures directory is tracked
│   │   │   │   └── [gemma3-1b-it-int4.task]  # Model file (YOU must add this)
│   │   │   ├── java/com/example/pragnya/
│   │   │   │   ├── MainActivity.kt     # Main app logic
│   │   │   │   └── ui/theme/           # App theming
│   │   │   └── res/                    # Android resources
│   │   ├── androidTest/                # Instrumented tests
│   │   └── test/                       # Unit tests
│   └── build.gradle.kts                # App-level Gradle config
├── build.gradle.kts                    # Project-level Gradle config
├── settings.gradle.kts                 # Project settings
├── setup-verify.sh                     # Setup verification script (Linux/Mac)
└── setup-verify.bat                    # Setup verification script (Windows)
```

## Troubleshooting

### "Failed to copy model from assets" Error

**Cause**: The Gemma model file is missing from the assets directory.

**Solution**: 
1. Verify that `gemma3-1b-it-int4.task` exists in `app/src/main/assets/`
2. Check the filename is correct (exact match, case-sensitive)
3. Ensure the file is not corrupted (should be ~1-2 GB)

### "Model loading..." Stays Forever

**Cause**: The model file might be corrupted or incomplete.

**Solution**:
1. Delete the existing model file
2. Re-download from Kaggle
3. Verify the file size matches the expected size
4. Clean and rebuild the project: **Build → Clean Project** then **Build → Rebuild Project**

### App Crashes on Startup

**Possible Causes & Solutions**:

1. **Insufficient Memory**: 
   - Close other apps
   - Use a device with at least 2GB RAM
   - Try on a real device instead of an emulator

2. **Missing Dependencies**:
   - Run `./gradlew clean build`
   - Sync Gradle files in Android Studio

3. **Android Version Too Old**:
   - Ensure your device runs Android 7.0 (API 24) or higher

### Gradle Sync Failed

**Solution**:
1. Check your internet connection
2. Go to **File → Invalidate Caches / Restart**
3. Update Android Studio to the latest version
4. Try running `./gradlew --refresh-dependencies`

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **AI Model**: Google Gemma 3 1B (INT4 quantized)
- **ML Framework**: MediaPipe GenAI (v0.10.27)
- **OCR**: Google ML Kit Text Recognition
- **Architecture**: MVVM with ViewModel and StateFlow
- **Build System**: Gradle with Kotlin DSL

## Dependencies

Key libraries used in this project:

- `com.google.mediapipe:tasks-genai:0.10.27` - Gemma model inference
- `com.google.mlkit:text-recognition:16.0.0` - OCR functionality
- `androidx.compose.*` - Modern Android UI
- `com.google.guava:guava` - Utilities and concurrency
- `io.coil-kt:coil-compose` - Image loading

## How to Use the App

1. **Start a Chat**: Type your message in the text field at the bottom
2. **Send Message**: Click the send icon to submit your message
3. **OCR from Image**: 
   - Click the photo icon
   - Select an image from your gallery
   - The app will extract text and analyze it
4. **Stop Generation**: Click the stop button if the AI response is taking too long
5. **View History**: Scroll through previous messages

## Performance Tips

- **First Load**: The first time you open the app, it loads the model into memory (may take 10-30 seconds)
- **Response Time**: Generating responses typically takes 5-15 seconds depending on device performance
- **Memory Usage**: The app uses approximately 1.5-2GB RAM when running
- **Best Performance**: Use on devices with Snapdragon 8-series or equivalent processors

## License

This project demonstrates the use of Google's Gemma AI model and various Google libraries. Please refer to their respective licenses:

- Gemma Model: [Google's Gemma Terms](https://www.kaggle.com/models/google/gemma)
- MediaPipe: [Apache License 2.0](https://github.com/google/mediapipe)
- ML Kit: [Google's Terms of Service](https://developers.google.com/ml-kit/terms)

## Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

## Support

For issues, questions, or suggestions:
- Open an issue on GitHub
- Check existing issues for solutions
- Refer to the Troubleshooting section above

## Acknowledgments

- **Google Gemma Team** for the powerful language model
- **MediaPipe Team** for the inference framework
- **ML Kit Team** for OCR capabilities
- **Jetpack Compose** community for UI inspiration

---

**Note**: This app runs AI inference on-device, which means it works offline once the model is loaded. No API keys or internet connection required for chat functionality!
