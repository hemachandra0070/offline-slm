# Model Files Directory

This directory should contain the Gemma model file required for the app to function.

## Required File

You need to place the following file in this directory:
- **gemma3-1b-it-int4.task**

## How to Get the Model File

1. Download the Gemma 3 1B IT INT4 model file from Google's Kaggle:
   - Visit: https://www.kaggle.com/models/google/gemma/tfLite/gemma3-1b-it-int4
   - You may need to create a Kaggle account if you don't have one
   - Accept the model's terms and conditions
   - Download the `gemma3-1b-it-int4.task` file

2. Place the downloaded file directly in this directory:
   ```
   app/src/main/assets/gemma3-1b-it-int4.task
   ```

3. The file should be approximately 1-2 GB in size

## Important Notes

- The model file is **NOT** included in the repository due to its large size
- The app will not work without this model file
- Make sure the filename exactly matches `gemma3-1b-it-int4.task`
- Do not rename the file

After placing the model file here, you can build and run the app successfully.
