package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class Collaborator(
    val id: String,
    val name: String,
    val colorHex: String,
    val activeBlockId: String? = null,
    val typingText: String? = null
)

class DocumentViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = DocumentRepository(db.documentDao())

    // All documents flow
    val allDocuments: StateFlow<List<DocumentEntity>> = repository.allDocuments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Editing State
    private val _currentDocumentState = MutableStateFlow<DocumentEntity?>(null)
    val currentDocumentState: StateFlow<DocumentEntity?> = _currentDocumentState.asStateFlow()

    private val _activeBlocks = MutableStateFlow<List<ParagraphBlock>>(emptyList())
    val activeBlocks: StateFlow<List<ParagraphBlock>> = _activeBlocks.asStateFlow()

    private val _selectedBlockId = MutableStateFlow<String?>(null)
    val selectedBlockId: StateFlow<String?> = _selectedBlockId.asStateFlow()

    // Preferences & Theme
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Offline / Sync Simulator
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    private val _lastSavedTime = MutableStateFlow<String>("Never")
    val lastSavedTime: StateFlow<String> = _lastSavedTime.asStateFlow()

    // Voice to Text State
    private val _isSpeechListening = MutableStateFlow(false)
    val isSpeechListening: StateFlow<Boolean> = _isSpeechListening.asStateFlow()

    private val _speechRecognizedText = MutableStateFlow("")
    val speechRecognizedText: StateFlow<String> = _speechRecognizedText.asStateFlow()

    private val _speechError = MutableStateFlow<String?>(null)
    val speechError: StateFlow<String?> = _speechError.asStateFlow()

    // Collaboration Simulator State
    private val _collaborationActive = MutableStateFlow(false)
    val collaborationActive: StateFlow<Boolean> = _collaborationActive.asStateFlow()

    private val _collaboratingUsers = MutableStateFlow<List<Collaborator>>(emptyList())
    val collaboratingUsers: StateFlow<List<Collaborator>> = _collaboratingUsers.asStateFlow()

    private val _collaborationLogs = MutableStateFlow<List<String>>(emptyList())
    val collaborationLogs: StateFlow<List<String>> = _collaborationLogs.asStateFlow()

    private var collaborationJob: Job? = null
    private var speechRecognizer: SpeechRecognizer? = null

    // Sample Collaborators
    private val sampleUsers = listOf(
        Collaborator("1", "Sarah Jenkins", "#E91E63"),
        Collaborator("2", "John Doe", "#4CAF50"),
        Collaborator("3", "Emma Watson", "#FF9800"),
        Collaborator("4", "David Lin", "#9C27B0")
    )

    init {
        // Initialize Speech Recognizer on Main Thread
        Handler(Looper.getMainLooper()).post {
            try {
                if (SpeechRecognizer.isRecognitionAvailable(application)) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(application)
                }
            } catch (e: Exception) {
                Log.e("Speech", "Failed to create recognizer", e)
            }
        }
    }

    // Toggle Theme Mode
    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    // Toggle Offline State
    fun toggleOfflineState() {
        _isOffline.value = !_isOffline.value
        addCollaborationLog(if (_isOffline.value) "You went offline. Sync paused." else "You are back online. Auto-sync fully active.")
        if (!_isOffline.value) {
            triggerCloudSync()
        }
    }

    // Trigger Cloud Sync Integration
    fun triggerCloudSync() {
        if (_isOffline.value) return
        viewModelScope.launch {
            _isCloudSyncing.value = true
            delay(1500) // Simulate network delay
            _isCloudSyncing.value = false
            _currentDocumentState.value?.let { doc ->
                val updated = doc.copy(isSynced = true)
                repository.update(updated)
                _currentDocumentState.value = updated
            }
            val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            _lastSavedTime.value = formatter.format(Date())
            addCollaborationLog("Document successfully synced with Cloud Storage.")
        }
    }

    // Create a Document (Empty or from Template)
    fun createDocument(title: String, category: String, templateType: String) {
        viewModelScope.launch {
            val blocks = when (templateType) {
                "Biodata" -> DocumentTemplates.BIODATA_TEMPLATE
                "Letter" -> DocumentTemplates.LETTER_TEMPLATE
                "Application" -> DocumentTemplates.APPLICATION_TEMPLATE
                else -> DocumentTemplates.BLANK_TEMPLATE
            }

            val plainText = blocks.joinToString("\n") { it.text }
            val newDoc = DocumentEntity(
                title = title,
                category = category,
                templateType = templateType,
                blocksJson = blocks.toJsonString(),
                plainText = plainText,
                isSynced = !_isOffline.value
            )

            val id = repository.insert(newDoc)
            val insertedDoc = newDoc.copy(id = id.toInt())
            openDocument(insertedDoc)
        }
    }

    // Open Document for Editing
    fun openDocument(doc: DocumentEntity) {
        _currentDocumentState.value = doc
        val blocks = doc.blocksJson.toParagraphBlocks()
        _activeBlocks.value = blocks
        _selectedBlockId.value = blocks.firstOrNull()?.id
        
        // Reset Collaboration
        stopCollaborationSimulation()
        _collaborationActive.value = false
        _collaboratingUsers.value = emptyList()
        _collaborationLogs.value = emptyList()

        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        _lastSavedTime.value = formatter.format(Date(doc.lastModifiedAt))
    }

    // Back to Dashboard
    fun closeDocument() {
        // Save before saving
        saveCurrentDocument()
        stopCollaborationSimulation()
        _currentDocumentState.value = null
        _activeBlocks.value = emptyList()
        _selectedBlockId.value = null
    }

    // Save Active Blocks to Local Database (Auto-saves continuously)
    fun saveCurrentDocument() {
        val doc = _currentDocumentState.value ?: return
        val currentBlocks = _activeBlocks.value
        val plainText = currentBlocks.joinToString("\n") { it.text }
        
        viewModelScope.launch {
            val updatedDoc = doc.copy(
                blocksJson = currentBlocks.toJsonString(),
                plainText = plainText,
                lastModifiedAt = System.currentTimeMillis(),
                isSynced = !_isOffline.value
            )
            repository.update(updatedDoc)
            _currentDocumentState.value = updatedDoc
            
            if (!_isOffline.value) {
                _isCloudSyncing.value = true
                delay(600) // quick network write
                _isCloudSyncing.value = false
                val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                _lastSavedTime.value = formatter.format(Date())
            } else {
                val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                _lastSavedTime.value = "Local " + formatter.format(Date())
            }
        }
    }

    // Delete Document
    fun deleteDocument(doc: DocumentEntity) {
        viewModelScope.launch {
            repository.delete(doc)
            if (_currentDocumentState.value?.id == doc.id) {
                closeDocument()
            }
        }
    }

    // BLOCK MANIPULATION & FORMATTING

    fun selectBlock(id: String) {
        _selectedBlockId.value = id
    }

    fun updateBlockText(id: String, newText: String) {
        _activeBlocks.value = _activeBlocks.value.map { block ->
            if (block.id == id) block.copy(text = newText) else block
        }
        // Continuous auto-save
        saveCurrentDocument()
    }

    fun formatSelectedBlockBold(isBold: Boolean) {
        val blockId = _selectedBlockId.value ?: return
        _activeBlocks.value = _activeBlocks.value.map { block ->
            if (block.id == blockId) block.copy(isBold = isBold) else block
        }
        saveCurrentDocument()
    }

    fun formatSelectedBlockItalic(isItalic: Boolean) {
        val blockId = _selectedBlockId.value ?: return
        _activeBlocks.value = _activeBlocks.value.map { block ->
            if (block.id == blockId) block.copy(isItalic = isItalic) else block
        }
        saveCurrentDocument()
    }

    fun formatSelectedBlockUnderline(isUnderline: Boolean) {
        val blockId = _selectedBlockId.value ?: return
        _activeBlocks.value = _activeBlocks.value.map { block ->
            if (block.id == blockId) block.copy(isUnderline = isUnderline) else block
        }
        saveCurrentDocument()
    }

    fun formatSelectedBlockFontSize(size: Int) {
        val blockId = _selectedBlockId.value ?: return
        _activeBlocks.value = _activeBlocks.value.map { block ->
            if (block.id == blockId) block.copy(fontSize = size.coerceIn(10, 48)) else block
        }
        saveCurrentDocument()
    }

    fun formatSelectedBlockColor(colorHex: String) {
        val blockId = _selectedBlockId.value ?: return
        _activeBlocks.value = _activeBlocks.value.map { block ->
            if (block.id == blockId) block.copy(colorHex = colorHex) else block
        }
        saveCurrentDocument()
    }

    fun formatSelectedBlockAlignment(alignment: BlockAlignment) {
        val blockId = _selectedBlockId.value ?: return
        _activeBlocks.value = _activeBlocks.value.map { block ->
            if (block.id == blockId) block.copy(alignment = alignment) else block
        }
        saveCurrentDocument()
    }

    fun formatSelectedBlockType(type: ParagraphType) {
        val blockId = _selectedBlockId.value ?: return
        _activeBlocks.value = _activeBlocks.value.map { block ->
            if (block.id == blockId) {
                // Adjust default font size based on type
                val defaultSize = when (type) {
                    ParagraphType.H1 -> 24
                    ParagraphType.H2 -> 20
                    ParagraphType.H3 -> 18
                    ParagraphType.BODY -> 16
                    ParagraphType.BULLET -> 16
                    ParagraphType.NUMBER -> 16
                    ParagraphType.QUOTE -> 14
                }
                block.copy(type = type, fontSize = defaultSize)
            } else block
        }
        saveCurrentDocument()
    }

    // Advanced Document Structure Controls (Move lines up and down, add, delete)
    fun addParagraphBlock(afterId: String? = null) {
        val newBlock = ParagraphBlock()
        val current = _activeBlocks.value.toMutableList()
        
        if (afterId == null) {
            current.add(newBlock)
        } else {
            val index = current.indexOfFirst { it.id == afterId }
            if (index != -1) {
                current.add(index + 1, newBlock)
            } else {
                current.add(newBlock)
            }
        }
        _activeBlocks.value = current
        _selectedBlockId.value = newBlock.id
        saveCurrentDocument()
    }

    fun deleteParagraphBlock(id: String) {
        val current = _activeBlocks.value.toMutableList()
        if (current.size <= 1) {
            // Keep at least one block to type in
            _activeBlocks.value = listOf(ParagraphBlock())
            _selectedBlockId.value = _activeBlocks.value.first().id
            saveCurrentDocument()
            return
        }
        val index = current.indexOfFirst { it.id == id }
        current.removeAt(index)
        _activeBlocks.value = current
        
        // Select nearest block
        val nextSelectIndex = if (index >= current.size) current.size - 1 else index
        _selectedBlockId.value = current.getOrNull(nextSelectIndex)?.id
        saveCurrentDocument()
    }

    fun moveBlockUp(id: String) {
        val current = _activeBlocks.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index > 0) {
            Collections.swap(current, index, index - 1)
            _activeBlocks.value = current
            saveCurrentDocument()
        }
    }

    fun moveBlockDown(id: String) {
        val current = _activeBlocks.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1 && index < current.size - 1) {
            Collections.swap(current, index, index + 1)
            _activeBlocks.value = current
            saveCurrentDocument()
        }
    }

    // REAL-TIME COLLABORATIVE EDITING SIMULATION

    fun toggleCollaborationSimulation() {
        if (_isOffline.value) {
            _collaborationActive.value = false
            addCollaborationLog("Cannot enable collaboration while Offline.")
            return
        }
        _collaborationActive.value = !_collaborationActive.value
        if (_collaborationActive.value) {
            startCollaborationSimulation()
        } else {
            stopCollaborationSimulation()
        }
    }

    private fun startCollaborationSimulation() {
        stopCollaborationSimulation()
        _collaboratingUsers.value = sampleUsers.take(2) // start with 2 users
        addCollaborationLog("Real-Time collaborative session initiated.")
        addCollaborationLog("Sarah Jenkins connected.")
        addCollaborationLog("John Doe connected.")

        collaborationJob = viewModelScope.launch {
            while (true) {
                delay((3000 + Random().nextInt(4000)).toLong()) // 3 to 7 seconds delay
                
                // Random simulation event
                val eventType = Random().nextInt(5)
                val currentUsers = _collaboratingUsers.value.toMutableList()
                val currentBlocks = _activeBlocks.value
                
                if (currentUsers.isEmpty() || currentBlocks.isEmpty()) continue

                when (eventType) {
                    0 -> {
                        // User changes writing target block
                        val randomUserIndex = Random().nextInt(currentUsers.size)
                        val randomBlockIndex = Random().nextInt(currentBlocks.size)
                        val targetBlock = currentBlocks[randomBlockIndex]
                        val user = currentUsers[randomUserIndex]
                        
                        currentUsers[randomUserIndex] = user.copy(
                            activeBlockId = targetBlock.id,
                            typingText = null
                        )
                        _collaboratingUsers.value = currentUsers
                        addCollaborationLog("${user.name} focused on Line ${randomBlockIndex + 1}")
                    }
                    1 -> {
                        // User types/edits something in their focused block
                        val withActiveBlock = currentUsers.filter { it.activeBlockId != null }
                        if (withActiveBlock.isNotEmpty()) {
                            val user = withActiveBlock[Random().nextInt(withActiveBlock.size)]
                            val blockId = user.activeBlockId!!
                            
                            val index = currentBlocks.indexOfFirst { it.id == blockId }
                            if (index != -1) {
                                val currentText = currentBlocks[index].text
                                val simulatedTyping = when (Random().nextInt(3)) {
                                    0 -> " [edited by ${user.name}]"
                                    1 -> " (reviewed)"
                                    else -> " *updated*"
                                }
                                
                                // Update block text asynchronously
                                _activeBlocks.value = _activeBlocks.value.map { block ->
                                    if (block.id == blockId) {
                                        block.copy(text = currentText + simulatedTyping)
                                    } else block
                                }
                                
                                // Update collaborator state
                                val userIndex = currentUsers.indexOfFirst { it.id == user.id }
                                if (userIndex != -1) {
                                    currentUsers[userIndex] = user.copy(typingText = "Typing...")
                                }
                                _collaboratingUsers.value = currentUsers
                                addCollaborationLog("${user.name} modified Line ${index + 1}: appended text.")
                                saveCurrentDocument()
                                
                                // Clear typing indicator shortly after
                                delay(1200)
                                val postUsers = _collaboratingUsers.value.toMutableList()
                                val uIdx = postUsers.indexOfFirst { it.id == user.id }
                                if (uIdx != -1) {
                                    postUsers[uIdx] = postUsers[uIdx].copy(typingText = null)
                                }
                                _collaboratingUsers.value = postUsers
                            }
                        }
                    }
                    2 -> {
                        // User joins
                        if (currentUsers.size < sampleUsers.size) {
                            val missingUsers = sampleUsers.filter { u -> currentUsers.none { it.id == u.id } }
                            if (missingUsers.isNotEmpty()) {
                                val newUser = missingUsers.random()
                                currentUsers.add(newUser)
                                _collaboratingUsers.value = currentUsers
                                addCollaborationLog("${newUser.name} connected to the session.")
                            }
                        }
                    }
                    3 -> {
                        // User leaves
                        if (currentUsers.size > 1) {
                            val leavingUser = currentUsers.random()
                            currentUsers.remove(leavingUser)
                            _collaboratingUsers.value = currentUsers
                            addCollaborationLog("${leavingUser.name} disconnected.")
                        }
                    }
                    4 -> {
                        // Collaborative suggestion/comment
                        val randomUser = currentUsers.random()
                        val greetings = listOf(
                            "Looks extremely professional!",
                            "Should we increase font size in headers?",
                            "The spacing of this template is perfect.",
                            "Ready to export this document."
                        )
                        addCollaborationLog("${randomUser.name}: \"${greetings.random()}\"")
                    }
                }
            }
        }
    }

    private fun stopCollaborationSimulation() {
        collaborationJob?.cancel()
        collaborationJob = null
    }

    private fun addCollaborationLog(log: String) {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val time = formatter.format(Date())
        val updated = _collaborationLogs.value.toMutableList()
        updated.add(0, "[$time] $log")
        _collaborationLogs.value = updated.take(40) // Keep last 40 logs
    }

    // REAL VOICE-TO-TEXT IMPLEMENTATION + FALLBACK MODAL

    fun startVoiceInput(context: Context) {
        if (_isSpeechListening.value) {
            stopVoiceInput()
            return
        }

        val selectedId = _selectedBlockId.value ?: return
        val currentBlock = _activeBlocks.value.find { it.id == selectedId } ?: return

        Handler(Looper.getMainLooper()).post {
            try {
                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                }

                speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isSpeechListening.value = true
                        _speechError.value = null
                    }

                    override fun onBeginningOfSpeech() {}

                    override fun onRmsChanged(rmsdB: Float) {}

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isSpeechListening.value = false
                    }

                    override fun onError(error: Int) {
                        val message = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_CLIENT -> "Client-side error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient record audio permissions"
                            SpeechRecognizer.ERROR_NETWORK -> "Network issue occurred"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Speak closer."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input received"
                            else -> "Speech service unavailable"
                        }
                        _speechError.value = message
                        _isSpeechListening.value = false
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val textSpeak = matches[0]
                            val appendedText = if (currentBlock.text.isEmpty()) textSpeak else "${currentBlock.text} $textSpeak"
                            updateBlockText(selectedId, appendedText)
                        }
                        _isSpeechListening.value = false
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            _speechRecognizedText.value = matches[0]
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                speechRecognizer?.startListening(intent)

            } catch (e: Exception) {
                Log.e("Speech", "Failed starting SpeechRecognizer", e)
                _speechError.value = "Speech recognition is not fully supported on this emulator. Please use key input."
                _isSpeechListening.value = false
            }
        }
    }

    fun stopVoiceInput() {
        Handler(Looper.getMainLooper()).post {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _isSpeechListening.value = false
        }
    }

    fun dismissSpeechError() {
        _speechError.value = null
    }

    override fun onCleared() {
        super.onCleared()
        stopCollaborationSimulation()
        Handler(Looper.getMainLooper()).post {
            speechRecognizer?.destroy()
            speechRecognizer = null
        }
    }
}
