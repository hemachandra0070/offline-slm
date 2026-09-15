package com.example.pragnya

import android.app.Application
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.example.pragnya.ui.theme.PragnyaTheme
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

// --- Data Models for UI State ---
enum class Participant { USER, MODEL, ERROR }

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    var text: String = "",
    val participant: Participant = Participant.USER,
    var imageUri: Uri? = null,
    var isPending: Boolean = false
)

data class UiState(
    val messages: List<ChatMessage> = emptyList(),
    var selectedImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val isGenerating: Boolean = false
)

// --- ViewModel for All AI and State Logic ---
class LlmViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var llmInference: LlmInference? = null
    private var llmSession: LlmInferenceSession? = null
    private var currentGenerationFuture: ListenableFuture<String>? = null
    private val modelName = "gemma3-1b-it-int4.task"
    private val isGenerating = AtomicBoolean(false)

    // ✅ This now stores the OCR text privately
    private var extractedOcrText: String? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, messages = listOf(ChatMessage(text = "Loading model..."))) }
            try {
                val modelPath = getModelPath(application, modelName)
                val inferenceOptions = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelPath)
                    .build()
                llmInference = LlmInference.createFromOptions(application, inferenceOptions)
                createNewSession()
                _uiState.update {
                    it.copy(isLoading = false, messages = listOf(ChatMessage(text = "Model loaded. Ready to chat!", participant = Participant.MODEL)))
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, messages = listOf(ChatMessage(text = "Error loading model: ${e.message}", participant = Participant.ERROR)))
                }
            }
        }
    }

    private fun createNewSession() {
        llmSession = llmInference?.let {
            val sessionOptions = LlmInferenceSession.LlmInferenceSessionOptions.builder()
                .setTopK(40)
                .setTemperature(0.8f)
                .build()
            LlmInferenceSession.createFromOptions(it, sessionOptions)
        }
    }

    // ✅ UPDATED: This function now performs OCR and stores the text privately
    fun onImageSelected(uri: Uri?) {
        if (uri == null) {
            extractedOcrText = null
            _uiState.update { it.copy(selectedImageUri = null) }
            return
        }
        _uiState.update { it.copy(selectedImageUri = uri) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputImage = InputImage.fromFilePath(getApplication(), uri)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        extractedOcrText = visionText.text
                    }
                    .addOnFailureListener { e ->
                        // Optionally, you could show this error in the chat
                        extractedOcrText = "Error scanning image: ${e.localizedMessage}"
                    }
            } catch (e: Exception) {
                extractedOcrText = "Error preparing image: ${e.localizedMessage}"
            }
        }
    }

    fun generateResponse(prompt: String) {
        if (!isGenerating.compareAndSet(false, true)) { return }

        // ✅ This is the key change: construct the final prompt with hidden context
        val ocrContext = extractedOcrText ?: ""
        val basePrompt = if (ocrContext.isNotBlank()) {
            """
            Context from image:
            "$ocrContext"

            User prompt:
            "$prompt"
            """.trimIndent()
        } else {
            prompt
        }

        val finalPrompt = "Be Brief.Use atleast 4 sentences and maximum of 7 sentences\n\n$basePrompt"

        val selectedUri = _uiState.value.selectedImageUri
        val userMessage = ChatMessage(text = prompt, participant = Participant.USER, imageUri = selectedUri)
        val modelPlaceholder = ChatMessage(participant = Participant.MODEL, isPending = true)

        // Clear the private OCR text after it has been used
        extractedOcrText = null

        _uiState.update { it.copy(messages = it.messages + userMessage + modelPlaceholder, selectedImageUri = null, isGenerating = true) }

        val progressListener = com.google.mediapipe.tasks.genai.llminference.ProgressListener<String> { partialResult, done ->
            _uiState.update { currentState ->
                val updatedMessages = currentState.messages.map { msg ->
                    if (msg.id == modelPlaceholder.id) {
                        msg.copy(text = msg.text + partialResult)
                    } else { msg }
                }
                currentState.copy(messages = updatedMessages)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Send the final, combined prompt to the model
                llmSession?.addQueryChunk(finalPrompt)
                currentGenerationFuture = llmSession?.generateResponseAsync(progressListener)

                Futures.addCallback(currentGenerationFuture, object : FutureCallback<String> {
                    override fun onSuccess(result: String?) {
                        _uiState.update { currentState ->
                            val finalMessages = currentState.messages.map {
                                if (it.id == modelPlaceholder.id) it.copy(isPending = false) else it
                            }
                            currentState.copy(messages = finalMessages, isGenerating = false)
                        }
                        isGenerating.set(false)
                    }

                    override fun onFailure(t: Throwable) {
                        _uiState.update { currentState ->
                            val updatedMessages = currentState.messages.map { msg ->
                                if (msg.id == modelPlaceholder.id) {
                                    msg.copy(
                                        text = if (t is java.util.concurrent.CancellationException) msg.text.ifEmpty { "[Generation stopped]" } else "Error: ${t.message}",
                                        participant = if (t is java.util.concurrent.CancellationException) msg.participant else Participant.ERROR,
                                        isPending = false
                                    )
                                } else { msg }
                            }
                            currentState.copy(messages = updatedMessages, isGenerating = false)
                        }
                        isGenerating.set(false)
                    }
                }, Executors.newSingleThreadExecutor())

            } catch (e: Exception) {
                _uiState.update { currentState ->
                    val updatedMessages = currentState.messages.map {
                        if (it.id == modelPlaceholder.id) {
                            it.copy(text = "Error: ${e.message}", participant = Participant.ERROR, isPending = false)
                        } else { it }
                    }
                    currentState.copy(messages = updatedMessages, isGenerating = false)
                }
                isGenerating.set(false)
            }
        }
    }

    fun stopGeneration() {
        currentGenerationFuture?.cancel(true)
    }

    private fun getModelPath(context: android.content.Context, modelName: String): String {
        val modelFile = File(context.cacheDir, modelName)
        if (modelFile.exists()) return modelFile.absolutePath
        try {
            context.assets.open(modelName).use { inputStream ->
                modelFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        } catch (e: Exception) {
            throw IllegalStateException("Failed to copy model from assets: ${e.message}", e)
        }
        return modelFile.absolutePath
    }

    override fun onCleared() {
        super.onCleared()
        currentGenerationFuture?.cancel(true)
        llmSession?.close()
        llmInference?.close()
    }
}

// --- Main Activity (Entry Point) ---
class MainActivity : ComponentActivity() {
    private val llmViewModel: LlmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PragnyaTheme {
                LlmScreen(viewModel = llmViewModel)
            }
        }
    }
}

// --- Jetpack Compose UI ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LlmScreen(viewModel: LlmViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    val listState = rememberLazyListState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> viewModel.onImageSelected(uri) }
    )

    // In the LlmScreen composable
//    LaunchedEffect(uiState.messages.lastOrNull()?.text) {
//        if (uiState.messages.isNotEmpty()) {
//            listState.scrollToItem(uiState.messages.size - 1) // This is instant
//        }
//    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Pragnya AI") })
        },
        bottomBar = {
            MessageInput(
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
                onSendClick = {
                    if (textFieldValue.text.isNotBlank() || uiState.selectedImageUri != null) {
                        viewModel.generateResponse(textFieldValue.text)
                        textFieldValue = TextFieldValue("")
                    }
                },
                onStopClick = { viewModel.stopGeneration() },
                onImageClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                isGenerating = uiState.isGenerating,
                enabled = !uiState.isGenerating && !uiState.isLoading,
                selectedImageUri = uiState.selectedImageUri,
                onClearImage = { viewModel.onImageSelected(null) }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading && uiState.messages.size <= 1) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.messages, key = { it.id }) { message ->
                        MessageBubble(message = message)
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val alignment = if (message.participant == Participant.USER) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = when (message.participant) {
        Participant.USER -> MaterialTheme.colorScheme.primaryContainer
        Participant.MODEL -> MaterialTheme.colorScheme.secondaryContainer
        Participant.ERROR -> MaterialTheme.colorScheme.errorContainer
    }

    Box(modifier = Modifier.fillMaxWidth().padding(start = if (message.participant == Participant.USER) 40.dp else 0.dp, end = if (message.participant == Participant.USER) 0.dp else 40.dp), contentAlignment = alignment) {
        Surface(color = backgroundColor, shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                message.imageUri?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Selected image",
                        modifier = Modifier
                            .padding(bottom = if (message.text.isNotEmpty()) 8.dp else 0.dp)
                            .heightIn(max = 200.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
                if (message.text.isNotEmpty()) {
                    Text(
                        text = message.text + if (message.isPending) "..." else "",
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInput(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onSendClick: () -> Unit,
    onStopClick: () -> Unit,
    onImageClick: () -> Unit,
    isGenerating: Boolean,
    enabled: Boolean,
    selectedImageUri: Uri?,
    onClearImage: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AnimatedVisibility(visible = selectedImageUri != null) {
            ImageThumbnail(
                uri = selectedImageUri,
                onClearClick = onClearImage,
            )
        }
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp)).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onImageClick, enabled = enabled) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add Photo")
            }
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                enabled = enabled
            )
            if (isGenerating) {
                IconButton(onClick = onStopClick) {
                    Icon(imageVector = Icons.Filled.StopCircle, contentDescription = "Stop generation")
                }
            } else {
                IconButton(onClick = onSendClick, enabled = enabled && (value.text.isNotBlank() || selectedImageUri != null)) {
                    Icon(imageVector = Icons.Filled.Send, contentDescription = "Send message")
                }
            }
        }
    }
}

@Composable
fun ImageThumbnail(uri: Uri?, onClearClick: () -> Unit) {
    if (uri == null) return
    Box(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 8.dp)
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
    ) {
        AsyncImage(
            model = uri,
            contentDescription = "Selected image thumbnail",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        IconButton(
            onClick = onClearClick,
            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(20.dp).background(Color.Black.copy(alpha = 0.6f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Clear Image",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
