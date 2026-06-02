package com.example.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.export.DocumentExporter
import com.example.viewmodel.Collaborator
import com.example.viewmodel.DocumentViewModel
import kotlinx.coroutines.delay
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.material3.ExperimentalMaterial3Api
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ScribeFlowApp(
    viewModel: DocumentViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showSplash by remember { mutableStateOf(true) }

    // SplashScreen animation layout duration
    LaunchedEffect(Unit) {
        delay(2500)
        showSplash = false
    }

    AnimatedContent(
        targetState = showSplash,
        transitionSpec = {
            fadeIn(animationSpec = tween(600)) with fadeOut(animationSpec = tween(500))
        },
        label = "SplashToApp"
    ) { splash ->
        if (splash) {
            ScribeFlowSplashScreen()
        } else {
            ScribeFlowMainContent(viewModel = viewModel)
        }
    }
}

// 1. SPLASH SCREEN WITH GORGEOUS APP LOADING ANIMATION
@Composable
fun ScribeFlowSplashScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "SplashInfinite")
    
    // Scale pulsing of the quill logo
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LogoScale"
    )

    // Smooth quill rotation
    val quillRotation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "QuillRotation"
    )

    // Animated loading progress wave
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "LoadingWave"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F2027),
                        Color(0xFF203A43),
                        Color(0xFF2C5364)
                    )
                )
            )
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant Vector Logo Drawn Directly on Canvas Representing Document + Stylized Quill
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale)
                    .drawBehind {
                        // Shadow
                        drawCircle(
                            color = Color(0x2A000000),
                            radius = size.width / 2.1f,
                            center = Offset(size.width / 2f + 5f, size.height / 2f + 8f)
                        )
                        // Backdrop circle
                        drawCircle(
                            color = Color(0x3BFFFFFF),
                            radius = size.width / 2.2f
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Customized Vector Pen / Quill drawing
                Icon(
                    imageVector = Icons.Filled.MenuBook,
                    contentDescription = "Document Book",
                    modifier = Modifier
                        .size(64.dp),
                    tint = Color.White
                )

                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Editing Quill",
                    modifier = Modifier
                        .size(44.dp)
                        .offset(x = 18.dp, y = (-18).dp)
                        .rotate(quillRotation),
                    tint = Color(0xFFFF4081)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "ScribeFlow",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 2.sp,
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "The Modern Standard Word Processor",
                fontSize = 14.sp,
                color = Color(0xFFB0BEC5),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Custom modern Loading bar wave implementation
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0x3DFFFFFF))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.6f)
                        .align(Alignment.CenterStart)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF2196F3))
                )
            }
        }
    }
}

// MAIN CONTENT CONTAINER supporting dark theme routing
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ScribeFlowMainContent(
    viewModel: DocumentViewModel
) {
    val currentDoc by viewModel.currentDocumentState.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = currentDoc == null,
        transitionSpec = {
            slideInHorizontally(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                initialOffsetX = { fullWidth -> if (targetState) -fullWidth else fullWidth }
            ) with slideOutHorizontally(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                targetOffsetX = { fullWidth -> if (targetState) fullWidth else -fullWidth }
            )
        },
        label = "ScreenTransition"
    ) { isDashboard ->
        if (isDashboard) {
            ScribeFlowDashboardScreen(viewModel = viewModel)
        } else {
            ScribeFlowEditorScreen(viewModel = viewModel)
        }
    }
}

// 2. MAIN DASHBOARD SCREEN (Templates and Document List)
@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScribeFlowDashboardScreen(viewModel: DocumentViewModel) {
    val docs by viewModel.allDocuments.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    var searchTxt by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedTemplateForDialog by remember { mutableStateOf("Blank") }
    var newDocTitleInput by remember { mutableStateOf("") }

    val filteredDocs = docs.filter {
        it.title.contains(searchTxt, ignoreCase = true) ||
                it.plainText.contains(searchTxt, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.MenuBook,
                            contentDescription = "Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "ScribeFlow",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                },
                actions = {
                    // Offline Simulator Toggle Button
                    IconButton(
                        onClick = { viewModel.toggleOfflineState() },
                        modifier = Modifier.testTag("offline_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isOffline) Icons.Filled.CloudOff else Icons.Filled.CloudSync,
                            contentDescription = "Connection Status",
                            tint = if (isOffline) Color.Red else Color(0xFF4CAF50)
                        )
                    }

                    // Dark Mode Toggle
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("dark_mode_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    newDocTitleInput = ""
                    selectedTemplateForDialog = "Blank"
                    showCreateDialog = true
                },
                icon = { Icon(Icons.Filled.Add, "Create Document") },
                text = { Text("New Document") },
                modifier = Modifier.testTag("create_document_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Searh bar
            OutlinedTextField(
                value = searchTxt,
                onValueChange = { searchTxt = it },
                placeholder = { Text("Search letters, biodata, notes...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search icon") },
                trailingIcon = {
                    if (searchTxt.isNotEmpty()) {
                        IconButton(onClick = { searchTxt = "" }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Clear") // fallback icon
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("search_document_input"),
                shape = RoundedCornerShape(12.dp)
            )

            // Templates section Header
            Text(
                text = "Start with a Template",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            // Dynamic Templates horizontal list row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TemplateCard(
                    title = "Blank Document",
                    icon = Icons.Filled.Add,
                    color = Color(0xFF607D8B),
                    category = "Blank",
                    onClick = {
                        newDocTitleInput = "Blank Scribe"
                        selectedTemplateForDialog = "Blank"
                        showCreateDialog = true
                    }
                )

                TemplateCard(
                    title = "Marriage Biodata",
                    icon = Icons.Filled.Group, // representative icon
                    color = Color(0xFF2196F3),
                    category = "Biodata",
                    onClick = {
                        newDocTitleInput = "My Marriage Biodata"
                        selectedTemplateForDialog = "Biodata"
                        showCreateDialog = true
                    }
                )

                TemplateCard(
                    title = "Standard Letter",
                    icon = Icons.Filled.Email, // representative
                    color = Color(0xFFE91E63),
                    category = "Letter",
                    onClick = {
                        newDocTitleInput = "Business Letter Proposal"
                        selectedTemplateForDialog = "Letter"
                        showCreateDialog = true
                    }
                )

                TemplateCard(
                    title = "Job Application",
                    icon = Icons.Filled.Work, // representative
                    color = Color(0xFF3F51B5),
                    category = "Application",
                    onClick = {
                        newDocTitleInput = "Lead Architect Application"
                        selectedTemplateForDialog = "Application"
                        showCreateDialog = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Offline resilient status banner
            if (isOffline) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CloudOff, "Offline Status", tint = Color.Red)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Offline Mode Active", fontWeight = FontWeight.Bold, color = Color(0xFFB71C1C), fontSize = 13.sp)
                            Text("Your edits auto-save safely locally. No cloud latency.", color = Color(0xFFD32F2F), fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // My documents section header
            Text(
                text = "Recent Documents",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            // Documents List empty state/lazy column
            if (filteredDocs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FolderOpen, // representative placeholder representation
                            contentDescription = "Search Circle",
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchTxt.isEmpty()) "No documents saved yet" else "No matching results found",
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchTxt.isEmpty()) "Tap + to create a custom Biodata, Letter or blank template." else "Try adjusting your search filters.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    itemsIndexed(filteredDocs) { idx, doc ->
                        DocumentItemCard(
                            document = doc,
                            onClick = { viewModel.openDocument(doc) },
                            onDelete = { viewModel.deleteDocument(doc) }
                        )
                    }
                }
            }
        }
    }

    // Modal dialog to create new document cleanly
    if (showCreateDialog) {
        Dialog(onDismissRequest = { showCreateDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Create New Scribe",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = newDocTitleInput,
                        onValueChange = { newDocTitleInput = it },
                        label = { Text("Document Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_doc_title_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Selected Template: $selectedTemplateForDialog", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCreateDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newDocTitleInput.trim().isEmpty()) {
                                    newDocTitleInput = "Untitled " + SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date())
                                }
                                viewModel.createDocument(
                                    title = newDocTitleInput,
                                    category = when (selectedTemplateForDialog) {
                                        "Biodata" -> "Biodata"
                                        "Letter" -> "Letter"
                                        "Application" -> "Application"
                                        else -> "General"
                                    },
                                    templateType = selectedTemplateForDialog
                                )
                                showCreateDialog = false
                            },
                            modifier = Modifier.testTag("dialog_confirm_create_button")
                        ) {
                            Text("Create")
                        }
                    }
                }
            }
        }
    }
}

// Visual Card for templates selection
@Composable
fun TemplateCard(
    title: String,
    icon: ImageVector,
    color: Color,
    category: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(115.dp)
            .height(120.dp)
            .clickable(onClick = onClick)
            .testTag("template_card_$category"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, title, modifier = Modifier.size(18.dp), tint = Color.White)
            }
            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    lineHeight = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = category,
                    fontSize = 9.sp,
                    color = color.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Single Document list item representation with visual swipe or clean card actions
@Composable
fun DocumentItemCard(
    document: DocumentEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val formatter = SimpleDateFormat("MMM dd, yyyy | HH:mm", Locale.getDefault())
    val dateText = formatter.format(Date(document.lastModifiedAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("document_item_${document.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Document Category leading badge circle block elements
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when (document.category) {
                            "Biodata" -> Color(0x212196F3)
                            "Letter" -> Color(0x21E91E63)
                            "Application" -> Color(0x213F51B5)
                            else -> Color(0x21607D8B)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (document.category) {
                        "Biodata" -> Icons.Filled.Badge
                        "Letter" -> Icons.Filled.Send
                        "Application" -> Icons.Filled.WorkHistory
                        else -> Icons.Filled.InsertDriveFile
                    },
                    contentDescription = null,
                    tint = when (document.category) {
                        "Biodata" -> Color(0xFF2196F3)
                        "Letter" -> Color(0xFFE91E63)
                        "Application" -> Color(0xFF3F51B5)
                        else -> Color(0xFF607D8B)
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$dateText • ${document.category}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    
                    // Connected to cloud icon block
                    Icon(
                        imageVector = if (document.isSynced) Icons.Filled.CloudQueue else Icons.Filled.CloudOff,
                        contentDescription = "Sync state",
                        tint = if (document.isSynced) Color(0xFF4CAF50) else Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            // Quick Delete option
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_doc_${document.id}")
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete Document",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// 3. FULL EDITOR VIEW SCREEN WITH RICH FORMATTING & REAL-TIME COLLABORATIVE PANELS
@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScribeFlowEditorScreen(viewModel: DocumentViewModel) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val document by viewModel.currentDocumentState.collectAsStateWithLifecycle()
    val blocks by viewModel.activeBlocks.collectAsStateWithLifecycle()
    val selectedBlockId by viewModel.selectedBlockId.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsStateWithLifecycle()
    val lastSavedAt by viewModel.lastSavedTime.collectAsStateWithLifecycle()

    // Voice states
    val speechError by viewModel.speechError.collectAsStateWithLifecycle()
    val isSpeechListening by viewModel.isSpeechListening.collectAsStateWithLifecycle()
    val partialSpeechRecognizedText by viewModel.speechRecognizedText.collectAsStateWithLifecycle()

    // Collaborative state representation
    val collaborationActive by viewModel.collaborationActive.collectAsStateWithLifecycle()
    val collaboratingUsers by viewModel.collaboratingUsers.collectAsStateWithLifecycle()
    val logs by viewModel.collaborationLogs.collectAsStateWithLifecycle()

    var showExportResultDialog by remember { mutableStateOf<File?>(null) }
    var exportResultMimeType by remember { mutableStateOf("application/pdf") }
    var showCollaborationDrawer by remember { mutableStateOf(false) }

    val activeBlock = blocks.find { it.id == selectedBlockId }

    // Audio recording permission activity result launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceInput(context)
        } else {
            Toast.makeText(context, "Microphone access is required for real-time speech transcription input.", Toast.LENGTH_SHORT).show()
        }
    }

    if (document == null) return

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = document?.title ?: "Doc Editor",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isOffline) Icons.Filled.CloudOff else Icons.Filled.CloudDone,
                                contentDescription = "Sync",
                                tint = if (isOffline) Color.Gray else Color(0xFF4CAF50),
                                modifier = Modifier
                                    .size(10.dp)
                                    .padding(end = 2.dp)
                            )
                            Text(
                                text = if (isOffline) "Saved Offline • $lastSavedAt" else "Auto-saved Cloud • $lastSavedAt",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.closeDocument()
                        },
                        modifier = Modifier.testTag("save_and_exit_button")
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Exit to dashboard")
                    }
                },
                actions = {
                    // Export to PDF
                    IconButton(
                        onClick = {
                            val pdfFile = DocumentExporter.exportToPdf(context, document?.title ?: "Untitled", blocks)
                            if (pdfFile != null) {
                                exportResultMimeType = "application/pdf"
                                showExportResultDialog = pdfFile
                            }
                        },
                        modifier = Modifier.testTag("export_to_pdf_button")
                    ) {
                        Icon(Icons.Filled.PictureAsPdf, contentDescription = "Export to PDF", tint = Color(0xFFF44336))
                    }

                    // Export to JPG
                    IconButton(
                        onClick = {
                            val jpgFile = DocumentExporter.exportToJpg(context, document?.title ?: "Untitled", blocks)
                            if (jpgFile != null) {
                                exportResultMimeType = "image/jpeg"
                                showExportResultDialog = jpgFile
                            }
                        },
                        modifier = Modifier.testTag("export_to_jpg_button")
                    ) {
                        Icon(Icons.Filled.Image, contentDescription = "Export to JPG", tint = Color(0xFFE91E63))
                    }

                    // Collaborators Session status toggle
                    IconButton(
                        onClick = {
                            viewModel.toggleCollaborationSimulation()
                            showCollaborationDrawer = true
                        },
                        modifier = Modifier.testTag("collab_session_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.People,
                            contentDescription = "Active Collaborators Drawer",
                            tint = if (collaborationActive) Color(0xFF2196F3) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // FORMATTING TOOLBAR PANEL
            FormattingToolbarPanel(
                activeBlock = activeBlock,
                onBoldToggle = { isBold -> viewModel.formatSelectedBlockBold(isBold) },
                onItalicToggle = { isIt -> viewModel.formatSelectedBlockItalic(isIt) },
                onUnderlineToggle = { isUn -> viewModel.formatSelectedBlockUnderline(isUn) },
                onSizeChange = { sz -> viewModel.formatSelectedBlockFontSize(sz) },
                onColorChange = { color -> viewModel.formatSelectedBlockColor(color) },
                onAlignmentChange = { align -> viewModel.formatSelectedBlockAlignment(align) },
                onTypeChange = { type -> viewModel.formatSelectedBlockType(type) }
            )

            // Dynamic Live Collaboration Bar inside editor
            if (collaborationActive && collaboratingUsers.isNotEmpty()) {
                Surface(
                    color = Color(0xFFE3F2FD),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2196F3))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Live Session", fontWeight = FontWeight.Bold, color = Color(0xFF1E88E5), fontSize = 11.sp)
                        }

                        // Avatar row representing online typers
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            collaboratingUsers.forEach { user ->
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(user.colorHex))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user.name.take(1),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // MAIN EDITING SHEET OF PAPER CANVAS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        if (viewModel.isDarkMode.value) MaterialTheme.colorScheme.background
                        else Color(0xFFECEFF1) // standard office twilight backdrop representation
                    )
            ) {
                // A4 Styled sheet container wrapping LazyColumn paragraphs fields
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .testTag("a4_paper_canvas"),
                    shape = RoundedCornerShape(4.dp),
                    tonalElevation = 2.dp,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        contentPadding = PaddingValues(bottom = 200.dp)
                    ) {
                        itemsIndexed(blocks) { index, block ->
                            val isFocused = block.id == selectedBlockId
                            
                            // Collaborative user highlighting cursor
                            val formattingTyper = collaboratingUsers.find { it.activeBlockId == block.id }

                            ParagraphEditableFieldRow(
                                block = block,
                                index = index,
                                isFocused = isFocused,
                                collaborator = formattingTyper,
                                onGainFocus = { viewModel.selectBlock(block.id) },
                                onTextChange = { txt -> viewModel.updateBlockText(block.id, txt) },
                                onAddParagraphAfter = { viewModel.addParagraphBlock(block.id) },
                                onDeleteParagraph = { viewModel.deleteParagraphBlock(block.id) },
                                onMoveUp = { viewModel.moveBlockUp(block.id) },
                                onMoveDown = { viewModel.moveBlockDown(block.id) }
                            )

                            // Quick horizontal separation lines for H1 structures
                            if (block.type == ParagraphType.H1) {
                                Divider(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    color = Color(android.graphics.Color.parseColor(block.colorHex)).copy(alpha = 0.15f),
                                    thickness = 1.dp
                                )
                            }
                        }

                        // Add bottom paragraph button representing word layouts
                        item {
                            TextButton(
                                onClick = { viewModel.addParagraphBlock(null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                                    .testTag("append_paragraph_button"),
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.AddCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add New Paragraph", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }

                // Collaborative Transaction panel (Live activity chat box overlays in bottom corner)
                if (showCollaborationDrawer) {
                    Card(
                        modifier = Modifier
                            .width(280.dp)
                            .fillMaxHeight(0.40f)
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .testTag("collab_drawer_overlay"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.History, "Logs", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Collaborators Sync logs", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                IconButton(
                                    onClick = { showCollaborationDrawer = false },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(Icons.Filled.Cancel, "Close", modifier = Modifier.size(14.dp))
                                }
                            }

                            // Logs list
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (logs.isEmpty()) {
                                    item {
                                        Text(
                                            "No active transaction sync events. Toggle Live Session on.",
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                } else {
                                    items(logs.size) { idx ->
                                        Text(
                                            text = logs[idx],
                                            fontSize = 10.sp,
                                            lineHeight = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SPEECH INPUT MIC UTILITY PANEL AT BOTTOM
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 4.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = {
                                val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                    viewModel.startVoiceInput(context)
                                } else {
                                    requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            modifier = Modifier
                                .background(if (isSpeechListening) Color.Red else MaterialTheme.colorScheme.primary, CircleShape)
                                .size(44.dp)
                                .testTag("voice_input_trigger")
                        ) {
                            Icon(
                                imageVector = if (isSpeechListening) Icons.Filled.Mic else Icons.Filled.MicNone,
                                contentDescription = "Voice To Text Microphone Input",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = if (isSpeechListening) "Transcribing Speech..." else "Voice-to-Text Input",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isSpeechListening) "\"$partialSpeechRecognizedText\"" else "Click mic icon and dictate to selected line",
                                fontSize = 11.sp,
                                fontStyle = FontStyle.Italic,
                                color = if (isSpeechListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // Done keyboard layout button
                    Button(
                        onClick = { focusManager.clearFocus() },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Finish Line", fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Modal dialog displaying successfully exported assets share triggers
    if (showExportResultDialog != null) {
        val file = showExportResultDialog!!
        Dialog(onDismissRequest = { showExportResultDialog = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(54.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Document Exported!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Created ${file.name}",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Location: Internal Storage/Android/data/\n${context.packageName}/files/Download/",
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showExportResultDialog = null },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Dismiss")
                        }
                        
                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = {
                                DocumentExporter.shareExportedFile(context, file, exportResultMimeType)
                                showExportResultDialog = null
                            },
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("share_exported_file_confirm")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Share, "Share", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share Scribe")
                            }
                        }
                    }
                }
            }
        }
    }

    // Display Speech Engine Error Modal
    if (speechError != null) {
        Dialog(onDismissRequest = { viewModel.dismissSpeechError() }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Speech recognition notice", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(speechError ?: "", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.dismissSpeechError() },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Understood")
                    }
                }
            }
        }
    }
}

// RICH FORMATTING TOOLBAR COMPONENT
@Composable
fun FormattingToolbarPanel(
    activeBlock: ParagraphBlock?,
    onBoldToggle: (Boolean) -> Unit,
    onItalicToggle: (Boolean) -> Unit,
    onUnderlineToggle: (Boolean) -> Unit,
    onSizeChange: (Int) -> Unit,
    onColorChange: (String) -> Unit,
    onAlignmentChange: (BlockAlignment) -> Unit,
    onTypeChange: (ParagraphType) -> Unit
) {
    if (activeBlock == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Select a paragraph to write/format it.", fontSize = 13.sp, color = Color.Gray)
        }
        return
    }

    // Palette HEX colors definition
    val colors = listOf(
        "#1D1B20" to "Black",   // default
        "#2196F3" to "Blue",    // primary biodata
        "#E91E63" to "Pink",    // official brand
        "#3F51B5" to "Indigo",  // letter heads
        "#4CAF50" to "Green",   // updates/success
        "#FF9800" to "Orange",  // warnings highlights
        "#F44336" to "Red"      // alert markers
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        // ROW 1: TEXT FOCUS STYLING (H1/H2/H3/BODY), BOLDS, ITALICS, UNDERLINES, SIZE SQUEEZE
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Dropdown style selector simple representations
            ParagraphType.values().forEach { t ->
                val isSelected = activeBlock.type == t
                FilterChip(
                    selected = isSelected,
                    onClick = { onTypeChange(t) },
                    label = { Text(t.name, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }

            Spacer(modifier = Modifier.width(4.dp))
            Divider(modifier = Modifier.height(24.dp).width(1.dp))
            Spacer(modifier = Modifier.width(4.dp))

            // Bold Toggle
            IconToggleButton(
                checked = activeBlock.isBold,
                onCheckedChange = onBoldToggle,
                modifier = Modifier.testTag("format_bold_toggle")
            ) {
                Icon(
                    imageVector = Icons.Filled.FormatBold,
                    contentDescription = "Format Bold",
                    tint = if (activeBlock.isBold) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }

            // Italic Toggle
            IconToggleButton(
                checked = activeBlock.isItalic,
                onCheckedChange = onItalicToggle,
                modifier = Modifier.testTag("format_italic_toggle")
            ) {
                Icon(
                    imageVector = Icons.Filled.FormatItalic,
                    contentDescription = "Format Italic",
                    tint = if (activeBlock.isItalic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }

            // Underline Toggle
            IconToggleButton(
                checked = activeBlock.isUnderline,
                onCheckedChange = onUnderlineToggle,
                modifier = Modifier.testTag("format_underline_toggle")
            ) {
                Icon(
                    imageVector = Icons.Filled.FormatUnderlined,
                    contentDescription = "Format Underline",
                    tint = if (activeBlock.isUnderline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // ROW 2: COLOR DOTS & TEXT SIZES & ALIGNMENTS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Font sizes
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onSizeChange(activeBlock.fontSize - 2) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Filled.RemoveCircleOutline, "Decrease Font Size", modifier = Modifier.size(20.dp))
                }
                Text(
                    text = "${activeBlock.fontSize}sp",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
                IconButton(
                    onClick = { onSizeChange(activeBlock.fontSize + 2) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Filled.AddCircleOutline, "Increase Font Size", modifier = Modifier.size(20.dp))
                }
            }

            Divider(modifier = Modifier.height(20.dp).width(1.dp))

            // Alignments
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                BlockAlignment.values().forEach { align ->
                    val isSelected = activeBlock.alignment == align
                    IconButton(
                        onClick = { onAlignmentChange(align) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = when (align) {
                                BlockAlignment.LEFT -> Icons.Filled.FormatAlignLeft
                                BlockAlignment.CENTER -> Icons.Filled.FormatAlignCenter
                                BlockAlignment.RIGHT -> Icons.Filled.FormatAlignRight
                                BlockAlignment.JUSTIFY -> Icons.Filled.FormatAlignJustify
                            },
                            contentDescription = align.name,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Divider(modifier = Modifier.height(20.dp).width(1.dp))

            // Color selection dots
            Row(
                modifier = Modifier.padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                colors.forEach { (hex, name) ->
                    val isSelected = activeBlock.colorHex.equals(hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(hex)))
                            .border(
                                width = if (isSelected) 2.dp else 0.5.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                shape = CircleShape
                            )
                            .clickable { onColorChange(hex) }
                            .testTag("color_dot_$name")
                    )
                }
            }
        }
    }
}

// SINGLE PARAGRAPH BLOCK COMPOSABLE FIELD
@Composable
fun ParagraphEditableFieldRow(
    block: ParagraphBlock,
    index: Int,
    isFocused: Boolean,
    collaborator: Collaborator?,
    onGainFocus: () -> Unit,
    onTextChange: (String) -> Unit,
    onAddParagraphAfter: () -> Unit,
    onDeleteParagraph: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val context = LocalContext.current
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }

    LaunchedEffect(isFocused) {
        if (isFocused) {
            focusRequester.requestFocus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onGainFocus,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            )
            .border(
                width = if (isFocused) 1.dp else 0.dp,
                color = if (isFocused) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
            .background(
                color = if (isFocused) MaterialTheme.colorScheme.primary.copy(alpha = 0.04f) else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        // Paragraph action toolbar overlay (When focused, show up/down reorder buttons & quick delete button!)
        if (isFocused) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Line Number indicator representing word structures
                Text(
                    text = "L${index + 1} (${block.type.name})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Reorder / edit layouts row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onMoveUp,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Filled.ArrowUpward, "Move Line Up", modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = onMoveDown,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Filled.ArrowDownward, "Move Line Down", modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = onAddParagraphAfter,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Filled.Add, "Add Line Below", modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = onDeleteParagraph,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Filled.Delete, "Delete Line", modifier = Modifier.size(16.dp), tint = Color.Red)
                    }
                }
            }
        }

        // Collaboration Cursor highlight indicators representing user typers
        if (collaborator != null) {
            Surface(
                color = Color(android.graphics.Color.parseColor(collaborator.colorHex)).copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(collaborator.colorHex)))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${collaborator.name} ${collaborator.typingText ?: "viewing"}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(android.graphics.Color.parseColor(collaborator.colorHex))
                    )
                }
            }
        }

        // TEXT EDITOR VIEW
        val baseStyle = when (block.type) {
            ParagraphType.H1 -> TextStyle(
                fontSize = block.fontSize.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = (block.fontSize * 1.25).sp
            )
            ParagraphType.H2 -> TextStyle(
                fontSize = block.fontSize.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = (block.fontSize * 1.25).sp
            )
            ParagraphType.H3 -> TextStyle(
                fontSize = block.fontSize.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = (block.fontSize * 1.2).sp
            )
            ParagraphType.QUOTE -> TextStyle(
                fontSize = block.fontSize.sp,
                fontStyle = FontStyle.Italic,
                lineHeight = (block.fontSize * 1.3).sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
            )
            else -> TextStyle(
                fontSize = block.fontSize.sp,
                lineHeight = (block.fontSize * 1.35).sp
            )
        }

        val alignmentModifier = when (block.alignment) {
            BlockAlignment.LEFT -> TextAlign.Left
            BlockAlignment.CENTER -> TextAlign.Center
            BlockAlignment.RIGHT -> TextAlign.Right
            BlockAlignment.JUSTIFY -> TextAlign.Justify
        }

        val mergedStyle = baseStyle.copy(
            color = Color(android.graphics.Color.parseColor(block.colorHex)),
            textAlign = alignmentModifier,
            fontWeight = if (block.isBold) FontWeight.Bold else baseStyle.fontWeight,
            fontStyle = if (block.isItalic) FontStyle.Italic else baseStyle.fontStyle,
            textDecoration = if (block.isUnderline) androidx.compose.ui.text.style.TextDecoration.Underline else null
        )

        // Custom stylized borders and paddings for lists & quotes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = if (block.type == ParagraphType.BULLET || block.type == ParagraphType.NUMBER) 12.dp else 0.dp,
                    end = 0.dp
                )
                .drawBehind {
                    if (block.type == ParagraphType.QUOTE) {
                        // Vertical block quote border line
                        drawLine(
                            color = Color(android.graphics.Color.parseColor(block.colorHex)),
                            start = Offset(0f, 0f),
                            end = Offset(0f, size.height),
                            strokeWidth = 6f
                        )
                    }
                }
                .padding(
                    start = if (block.type == ParagraphType.QUOTE) 14.dp else 0.dp
                ),
            verticalAlignment = Alignment.Top
        ) {
            if (block.type == ParagraphType.BULLET) {
                Text(
                    text = "•  ",
                    fontSize = block.fontSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(android.graphics.Color.parseColor(block.colorHex)),
                    modifier = Modifier.padding(top = 1.dp)
                )
            } else if (block.type == ParagraphType.NUMBER) {
                Text(
                    text = "${index + 1}.  ",
                    fontSize = block.fontSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(android.graphics.Color.parseColor(block.colorHex)),
                    modifier = Modifier.padding(top = 1.dp)
                )
            }

            BasicTextField(
                value = block.text,
                onValueChange = { onTextChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("line_text_field_$index")
                    .focusRequester(focusRequester),
                textStyle = mergedStyle,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (block.text.isEmpty()) {
                            Text(
                                text = when (block.type) {
                                    ParagraphType.H1 -> "Header 1"
                                    ParagraphType.H2 -> "Header 2"
                                    ParagraphType.H3 -> "Header 3"
                                    ParagraphType.QUOTE -> "Write your block quote quote..."
                                    else -> "Start writing..."
                                },
                                style = mergedStyle.copy(color = mergedStyle.color.copy(alpha = 0.35f))
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}
