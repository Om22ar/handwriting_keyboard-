package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.handwriting.HandwritingCanvasView
import com.example.handwriting.Stroke
import com.example.ime.ImeConfig
import com.example.ime.KeyboardPalette
import com.example.recognition.Candidate
import com.example.recognition.Language
import com.example.recognition.LocalCharacterRecognizer
import com.example.recognition.RecognitionMode
import com.example.settings.KeyboardSettings
import com.example.settings.SettingsRepository
import com.example.ui.CandidateBar
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsRepo = SettingsRepository.getInstance(this)

        setContent {
            MyApplicationTheme {
                MainAppScreen(settingsRepo = settingsRepo)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(settingsRepo: SettingsRepository) {
    val context = LocalContext.current
    val settings by settingsRepo.settings.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    var isEnabled by remember { mutableStateOf(false) }
    var isSelected by remember { mutableStateOf(false) }

    fun refreshImeStatus() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        if (imm != null) {
            val enabledList = imm.enabledInputMethodList
            isEnabled = enabledList.any { it.packageName == context.packageName }
        }
        val defaultIme = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        )
        isSelected = defaultIme?.contains(context.packageName) == true
    }

    // Refresh IME status when resuming
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        refreshImeStatus()
    }

    val tabs = listOf("Setup & Sandbox", "Settings", "Help & Privacy")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Draw,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Handwriting Keyboard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1E293B),
                contentColor = Color(0xFF38BDF8),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF38BDF8)
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> SetupAndSandboxTab(
                    isEnabled = isEnabled,
                    isSelected = isSelected,
                    settings = settings,
                    onEnableClick = {
                        context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                    },
                    onSelectClick = {
                        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                        imm?.showInputMethodPicker()
                    }
                )
                1 -> SettingsTab(settingsRepo = settingsRepo, settings = settings)
                2 -> HelpAndPrivacyTab()
            }
        }
    }
}

@Composable
fun SetupAndSandboxTab(
    isEnabled: Boolean,
    isSelected: Boolean,
    settings: KeyboardSettings,
    onEnableClick: () -> Unit,
    onSelectClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val recognizer = remember { LocalCharacterRecognizer() }
    val coroutineScope = rememberCoroutineScope()

    var testInputText by remember { mutableStateOf("") }
    var sandboxCandidates by remember { mutableStateOf<List<Candidate>>(emptyList()) }
    var canvasRef by remember { mutableStateOf<HandwritingCanvasView?>(null) }
    val palette = remember(settings.theme) { KeyboardPalette.fromName(settings.theme) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Activation Checklist Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "System Keyboard Activation",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Step 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isEnabled) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isEnabled) Color(0xFF4ADE80) else Color(0xFF94A3B8),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "1. Enable in System Settings",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = if (isEnabled) "Active in Android settings" else "Tap button to enable",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                    Button(
                        onClick = onEnableClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEnabled) Color(0xFF334155) else Color(0xFF0EA5E9)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("enable_keyboard_button")
                    ) {
                        Text(if (isEnabled) "Configured" else "Enable", fontSize = 12.sp)
                    }
                }

                // Step 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isSelected) Color(0xFF4ADE80) else Color(0xFF94A3B8),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "2. Select as Active Keyboard",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = if (isSelected) "Currently selected keyboard" else "Tap button to select",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                    Button(
                        onClick = onSelectClick,
                        enabled = isEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color(0xFF334155) else Color(0xFF0EA5E9)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("select_keyboard_button")
                    ) {
                        Text(if (isSelected) "Selected" else "Select", fontSize = 12.sp)
                    }
                }
            }
        }

        // Live Interactive Sandbox Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Interactive Handwriting Sandbox",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    OutlinedButton(
                        onClick = {
                            testInputText = ""
                            canvasRef?.clearCanvas()
                            sandboxCandidates = emptyList()
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Reset", fontSize = 12.sp)
                    }
                }

                // Core Rule Notice Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0EA5E9).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFF0EA5E9).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NO-AUTO-SPACE RULE: Characters are appended directly (A + B = AB). A space is inserted ONLY when you explicitly press [SPACE].",
                            fontSize = 12.sp,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 16.sp
                        )
                    }
                }

                // Active Text Field Display
                OutlinedTextField(
                    value = testInputText,
                    onValueChange = { testInputText = it },
                    label = { Text("Output Text (Test Field)") },
                    placeholder = { Text("Drawn characters will appear here without spaces...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sandbox_text_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Sandbox Candidate Bar
                CandidateBar(
                    candidates = sandboxCandidates,
                    palette = palette,
                    onCandidateClick = { candidate ->
                        // CRITICAL: Commit character with NO trailing space!
                        testInputText += candidate.text
                        canvasRef?.clearCanvas()
                        sandboxCandidates = emptyList()
                    },
                    onPunctuationClick = { punct ->
                        // Punctuation also commits WITHOUT space
                        testInputText += punct
                    }
                )

                // Embedded Sandbox Drawing Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                ) {
                    AndroidView(
                        factory = { ctx ->
                            HandwritingCanvasView(ctx).apply {
                                setStrokeColor(0xFF38BDF8.toInt(), 0x5538BDF8.toInt())
                                setStrokeWidthDip(settings.strokeWidth)
                                setGuideColor(0x3394A3B8.toInt())
                                recognitionDelayMs = settings.recognitionDelayMs
                                autoRecognizeEnabled = true
                                showGuides = true
                                hintText = "Draw character here"

                                onRecognitionRequested = { strokes ->
                                    coroutineScope.launch {
                                        val result = recognizer.recognize(
                                            strokes = strokes,
                                            language = Language.fromCode(settings.language),
                                            mode = RecognitionMode.AUTO,
                                            maxCandidates = 5
                                        )
                                        sandboxCandidates = result.candidates

                                        // Auto-commit top candidate WITHOUT SPACE
                                        if (result.primary != null) {
                                            testInputText += result.primary!!.text
                                            clearCanvas()
                                            sandboxCandidates = emptyList()
                                        }
                                    }
                                }
                                canvasRef = this
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("sandbox_canvas")
                    )
                }

                // Quick Simulation Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            // Commits EXACTLY one space
                            testInputText += " "
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sandbox_space_button")
                    ) {
                        Text("SPACE", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (testInputText.isNotEmpty()) {
                                testInputText = testInputText.substring(0, testInputText.length - 1)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sandbox_backspace_button")
                    ) {
                        Text("BACKSPACE", fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            canvasRef?.clearCanvas()
                            sandboxCandidates = emptyList()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CLEAR", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsTab(settingsRepo: SettingsRepository, settings: KeyboardSettings) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Recognition Timing Section
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Recognition Engine",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Recognition Delay
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Recognition Delay", color = Color.White, fontSize = 14.sp)
                        Text("${settings.recognitionDelayMs} ms", color = Color(0xFF38BDF8), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "Time to wait after finger lift before recognizing multi-stroke characters.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                    Slider(
                        value = settings.recognitionDelayMs.toFloat(),
                        onValueChange = { settingsRepo.updateRecognitionDelay(it.toLong()) },
                        valueRange = 300f..1500f,
                        steps = 11,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF38BDF8),
                            activeTrackColor = Color(0xFF38BDF8),
                            inactiveTrackColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.testTag("slider_recognition_delay")
                    )
                }

                // Auto-Recognize Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Confirm Character", color = Color.White, fontSize = 14.sp)
                        Text("Automatically commits recognized character when delay finishes.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                    Switch(
                        checked = settings.autoRecognize,
                        onCheckedChange = { settingsRepo.updateAutoRecognize(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF0EA5E9)
                        ),
                        modifier = Modifier.testTag("switch_auto_recognize")
                    )
                }

                // Automatic Space After Character Setting (CRITICAL REQUIREMENT)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Automatic Space After Character", color = Color.White, fontSize = 14.sp)
                        Text("Strictly disabled by default (OFF) to ensure A + B = AB.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                    Switch(
                        checked = settings.autoSpaceAfterCharacter,
                        onCheckedChange = { settingsRepo.updateAutoSpace(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF0EA5E9)
                        ),
                        modifier = Modifier.testTag("switch_auto_space")
                    )
                }
            }
        }

        // Canvas & Appearance Section
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Canvas & Drawing",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Stroke Width
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Stroke Width", color = Color.White, fontSize = 14.sp)
                        Text("${settings.strokeWidth.toInt()} dp", color = Color(0xFF38BDF8), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = settings.strokeWidth,
                        onValueChange = { settingsRepo.updateStrokeWidth(it) },
                        valueRange = 3f..14f,
                        steps = 10,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF38BDF8),
                            activeTrackColor = Color(0xFF38BDF8),
                            inactiveTrackColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.testTag("slider_stroke_width")
                    )
                }

                // Keyboard Height
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Keyboard Height", color = Color.White, fontSize = 14.sp)
                        Text("${settings.keyboardHeightDp} dp", color = Color(0xFF38BDF8), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = settings.keyboardHeightDp.toFloat(),
                        onValueChange = { settingsRepo.updateKeyboardHeight(it.toInt()) },
                        valueRange = 260f..420f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF38BDF8),
                            activeTrackColor = Color(0xFF38BDF8),
                            inactiveTrackColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.testTag("slider_keyboard_height")
                    )
                }

                // Show Guides Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Show Baseline Guides", color = Color.White, fontSize = 14.sp)
                        Text("Displays ascender, midline, and baseline guidelines.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                    Switch(
                        checked = settings.showGuides,
                        onCheckedChange = { settingsRepo.updateShowGuides(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF0EA5E9)
                        ),
                        modifier = Modifier.testTag("switch_show_guides")
                    )
                }

                // Haptic Feedback Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Vibration / Haptic Feedback", color = Color.White, fontSize = 14.sp)
                        Text("Provides subtle haptic feedback on key and canvas interactions.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                    Switch(
                        checked = settings.hapticFeedback,
                        onCheckedChange = { settingsRepo.updateHapticFeedback(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF0EA5E9)
                        ),
                        modifier = Modifier.testTag("switch_haptic")
                    )
                }

                // Debug Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Developer / Debug Mode", color = Color.White, fontSize = 14.sp)
                        Text("Shows recognition latency, candidate confidence, and raw stroke metrics.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                    Switch(
                        checked = settings.debugMode,
                        onCheckedChange = { settingsRepo.updateDebugMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF0EA5E9)
                        ),
                        modifier = Modifier.testTag("switch_debug")
                    )
                }
            }
        }

        // Theme and Language Section
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Theme & Language",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Theme selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("midnight" to "Midnight Cyan", "dark" to "Dark Purple", "light" to "Light Blue").forEach { (themeId, label) ->
                        val isSelectedTheme = settings.theme.equals(themeId, ignoreCase = true)
                        Button(
                            onClick = { settingsRepo.updateTheme(themeId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelectedTheme) Color(0xFF0EA5E9) else Color(0xFF334155)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(label, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }

                // Default Language selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("en" to "English (Latin)", "ar" to "العربية (Arabic)").forEach { (code, label) ->
                        val isSelectedLang = settings.language.equals(code, ignoreCase = true)
                        Button(
                            onClick = { settingsRepo.updateLanguage(code) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelectedLang) Color(0xFF0EA5E9) else Color(0xFF334155)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(label, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HelpAndPrivacyTab() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Security & Privacy Guarantee Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFF4ADE80),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "100% Offline & Private",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(
                    text = "• Zero Internet Permission: The AndroidManifest declares NO internet access whatsoever.\n" +
                            "• On-Device Processing: All stroke analysis and character recognition algorithms run 100% locally on your device CPU.\n" +
                            "• No Keystroke Logging: Your typed text and handwriting strokes are never logged, stored, or sent to external servers.\n" +
                            "• Password Field Protection: Secure password input types are treated strictly with candidate isolation.",
                    fontSize = 13.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 20.sp
                )
            }
        }

        // How to Draw Multi-Stroke Characters
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Drawing Multi-Stroke Characters",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Characters like 'A', 'H', 'E', 't', 'k', 'X', 'i', or '?' require multiple strokes:\n\n" +
                            "1. Draw the first stroke.\n" +
                            "2. Lift your finger and quickly draw the next stroke(s).\n" +
                            "3. The keyboard automatically collects all strokes while you draw and triggers recognition only after the configured inactivity delay (default: 600ms).\n" +
                            "4. You can also tap the [✓] checkmark button on the canvas to immediately commit the character without waiting.",
                    fontSize = 13.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 19.sp
                )
            }
        }

        // Troubleshooting Guide
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Troubleshooting & Switching",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "• Keyboard doesn't appear? Go to Android Settings > System > Languages & input > On-screen keyboard and enable 'Handwriting Keyboard'.\n" +
                            "• Switching keyboards anytime: When any text field is focused, tap the keyboard icon at the bottom-right of the navigation bar or swipe down notifications to pick 'Handwriting Keyboard'.\n" +
                            "• Need more drawing time? Increase the 'Recognition Delay' in the Settings tab to 900ms or 1200ms.",
                    fontSize = 13.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 19.sp
                )
            }
        }
    }
}
