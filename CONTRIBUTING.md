# Contributing to Pragnya

Thank you for your interest in contributing to Pragnya! This document provides guidelines for contributing to the project.

## Before You Start

### Setting Up Your Development Environment

1. **Fork the Repository**: Click the "Fork" button on GitHub to create your own copy

2. **Clone Your Fork**:
   ```bash
   git clone https://github.com/YOUR-USERNAME/Pragnya.git
   cd Pragnya
   ```

3. **Download the Model File**: **CRITICAL STEP!**
   - You must download the Gemma model file to build and test the app
   - See [QUICKSTART.md](QUICKSTART.md) for detailed instructions
   - Place `gemma3-1b-it-int4.task` in `app/src/main/assets/`
   - The app will NOT work without this file

4. **Verify Setup**:
   ```bash
   ./setup-verify.sh    # Linux/Mac
   setup-verify.bat     # Windows
   ```

5. **Open in Android Studio**: Import the project and wait for Gradle sync

## How to Contribute

### Reporting Bugs

When reporting bugs, please include:
- **Device Information**: Android version, device model
- **Steps to Reproduce**: Clear steps to trigger the bug
- **Expected vs Actual**: What should happen vs what actually happens
- **Logs**: Relevant error messages or logcat output
- **Screenshots**: If applicable

### Suggesting Features

Feature requests are welcome! Please:
- Check existing issues to avoid duplicates
- Clearly describe the feature and its benefits
- Provide examples or mockups if possible

### Code Contributions

#### Before Making Changes

1. **Create a Branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Ensure Model File is Present**: The app must build and run on your device

#### Code Style

- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add comments for complex logic
- Keep functions focused and single-purpose
- Use Jetpack Compose best practices for UI code

#### Testing Your Changes

Before submitting:
1. **Build Successfully**: `./gradlew assembleDebug`
2. **Test on Device**: Run the app and test your changes thoroughly
3. **Check for Regressions**: Ensure existing features still work
4. **Test Edge Cases**: Try different scenarios and inputs

#### Submitting a Pull Request

1. **Commit Your Changes**:
   ```bash
   git add .
   git commit -m "Brief description of changes"
   ```

2. **Push to Your Fork**:
   ```bash
   git push origin feature/your-feature-name
   ```

3. **Create Pull Request**: Go to the original repository and click "New Pull Request"

4. **PR Description Should Include**:
   - What changes were made
   - Why the changes were necessary
   - How to test the changes
   - Screenshots/videos for UI changes
   - Any breaking changes or dependencies

## Development Guidelines

### Project Structure

- `MainActivity.kt`: Main app logic, ViewModel, and UI
- `ui/theme/`: Material 3 theming
- `app/src/main/assets/`: Place model file here (not committed to git)

### Key Technologies

- **Kotlin**: Primary language
- **Jetpack Compose**: UI framework
- **MediaPipe GenAI**: For running Gemma model
- **ML Kit**: For OCR functionality
- **Material 3**: Design system

### Common Development Tasks

#### Building the APK
```bash
./gradlew assembleDebug
```

#### Installing on Device
```bash
./gradlew installDebug
```

#### Cleaning Build
```bash
./gradlew clean
```

#### Running Tests
```bash
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest   # Instrumented tests
```

## What NOT to Commit

- **Model Files**: `*.task`, `*.tflite` files are excluded in `.gitignore`
- **Build Artifacts**: `/build`, `/captures`, etc.
- **IDE Files**: `.idea/workspace.xml`, `.idea/tasks.xml`, etc.
- **Local Configuration**: `local.properties`

## Areas We'd Love Help With

- 🐛 **Bug Fixes**: Check open issues labeled "bug"
- 📱 **UI Improvements**: Enhance the chat interface
- ⚡ **Performance**: Optimize model loading and inference
- 📝 **Documentation**: Improve README, add code comments
- 🧪 **Testing**: Add unit and integration tests
- 🌐 **Localization**: Add support for more languages
- ♿ **Accessibility**: Improve app accessibility features

## Questions or Need Help?

- Open an issue with the "question" label
- Check existing issues for similar questions
- Refer to [README.md](README.md) for setup instructions

## Code of Conduct

- Be respectful and constructive
- Welcome newcomers and help them learn
- Focus on the code, not the person
- Give credit where it's due

## License

By contributing to Pragnya, you agree that your contributions will be subject to the same license terms as the project.

---

Thank you for contributing to Pragnya! 🙏
