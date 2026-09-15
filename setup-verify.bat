@echo off
REM Setup verification script for Pragnya (Windows)
REM This script checks if the Gemma model file is properly placed

echo ==========================================
echo Pragnya Setup Verification
echo ==========================================
echo.

REM Check if we're in the right directory
if not exist "settings.gradle.kts" (
    echo ERROR: This script must be run from the root directory of the Pragnya project
    exit /b 1
)

echo [OK] Running from correct directory
echo.

REM Check if assets directory exists
if not exist "app\src\main\assets" (
    echo ERROR: Assets directory does not exist
    echo Creating it now...
    mkdir "app\src\main\assets"
    echo [OK] Created assets directory
) else (
    echo [OK] Assets directory exists
)
echo.

REM Check if model file exists
set MODEL_FILE=app\src\main\assets\gemma3-1b-it-int4.task
if not exist "%MODEL_FILE%" (
    echo [ERROR] Model file NOT found!
    echo.
    echo The Gemma model file is required for the app to work.
    echo.
    echo Please follow these steps:
    echo 1. Visit: https://www.kaggle.com/models/google/gemma/tfLite/gemma3-1b-it-int4
    echo 2. Create/Login to your Kaggle account
    echo 3. Accept the model's terms and conditions
    echo 4. Download the 'gemma3-1b-it-int4.task' file (~1-2 GB^)
    echo 5. Place it at: %MODEL_FILE%
    echo.
    echo After downloading, run this script again to verify.
    exit /b 1
) else (
    echo [OK] Model file found
    
    REM Check file size (in bytes)
    for %%A in ("%MODEL_FILE%") do set FILE_SIZE=%%~zA
    if defined FILE_SIZE (
        set /a FILE_SIZE_MB=%FILE_SIZE% / 1024 / 1024
        echo   File size: !FILE_SIZE_MB! MB
        
        if !FILE_SIZE_MB! LSS 100 (
            echo [WARNING] File seems too small (expected ~1000-2000 MB^)
            echo   The file might be corrupted or incomplete.
        ) else if !FILE_SIZE_MB! GTR 3000 (
            echo [WARNING] File seems too large (expected ~1000-2000 MB^)
            echo   Please verify you downloaded the correct file.
        ) else (
            echo [OK] File size looks good
        )
    )
)
echo.

REM Check if Android SDK is available (optional)
where adb >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] Android Debug Bridge (adb^) is available
    
    REM Check for connected devices
    for /f "skip=1" %%i in ('adb devices 2^>nul ^| find "device"') do (
        echo [OK] Found connected Android device(s^)
        goto :devices_found
    )
    echo [WARNING] No Android devices connected
    echo   Connect a device or start an emulator to run the app
    :devices_found
) else (
    echo [WARNING] Android SDK tools not found in PATH
    echo   Install Android Studio to get the SDK tools
)
echo.

REM Check for Gradle wrapper
if exist "gradlew.bat" (
    echo [OK] Gradle wrapper found
) else (
    echo [ERROR] Gradle wrapper not found
    exit /b 1
)
echo.

echo ==========================================
echo Setup Verification Complete!
echo ==========================================
echo.

if exist "%MODEL_FILE%" (
    echo [OK] Your setup is ready!
    echo.
    echo Next steps:
    echo 1. Open the project in Android Studio
    echo 2. Wait for Gradle sync to complete
    echo 3. Connect an Android device or start an emulator
    echo 4. Click the Run button (or press Shift+F10^)
    echo.
    echo Or build from command line:
    echo   gradlew.bat assembleDebug
) else (
    echo [ERROR] Setup is NOT complete
    echo.
    echo You need to download and place the Gemma model file.
    echo See instructions above.
)

echo.
pause
