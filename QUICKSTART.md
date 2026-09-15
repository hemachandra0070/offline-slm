# Quick Start Guide

## TL;DR - Get Started in 3 Steps

### 1️⃣ Download Model File

Go to: https://www.kaggle.com/models/google/gemma/tfLite/gemma3-1b-it-int4

- Login/Create Kaggle account
- Accept terms
- Download `gemma3-1b-it-int4.task` (~1-2 GB)

### 2️⃣ Place Model File

Put the downloaded file here:
```
app/src/main/assets/gemma3-1b-it-int4.task
```

### 3️⃣ Build & Run

```bash
# Verify setup (optional)
./setup-verify.sh      # Linux/Mac
setup-verify.bat       # Windows

# Build and run
./gradlew installDebug  # or open in Android Studio
```

---

**Need more details?** See [README.md](README.md)

**Having issues?** Check the [Troubleshooting section](README.md#troubleshooting)
