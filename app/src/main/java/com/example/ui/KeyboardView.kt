package com.example.ui

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.inputmethod.EditorInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.handwriting.CharacterRecognizer
import com.example.handwriting.HandwritingCanvasView
import com.example.handwriting.Stroke
import com.example.ime.ImeConfig
import com.example.ime.KeyboardInputConnection
import com.example.ime.KeyboardPalette
import com.example.recognition.Candidate
import com.example.recognition.Language
import com.example.recognition.RecognitionMode
import com.example.recognition.RecognitionResult
import com.example.settings.KeyboardSettings
import kotlinx.coroutines.launch

/**
 * Primary Custom Handwriting Keyboard UI composed of:
 * 1. Top Candidate & Punctuation Bar
 * 2. Large Touch Handwriting Canvas with canvas actions (Undo, Clear, Commit)
 * 3. Mode, Cursor, and Language row
 * 4. Main Bottom Row: Spacebar (NO AUTO-SPACE RULE), Backspace, Enter/Action
 */
@Composable
fun KeyboardView(
    settings: KeyboardSettings,
    recognizer: CharacterRecognizer,
    keyboardConnection: KeyboardInputConnection,
    editorInfo: EditorInfo?,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val vibrator = remember { context.getSystemService(Vibrator::class.java) }

    // Palette
    val palette = remember(settings.theme) { KeyboardPalette.fromName(settings.theme) }

    // State
    var candidates by remember { mutableStateOf<List<Candidate>>(emptyList()) }
    var strokeCount by remember { mutableIntStateOf(0) }
    var currentLanguage by remember { mutableStateOf(Language.fromCode(settings.language)) }
    var currentMode by remember { mutableStateOf(RecognitionMode.AUTO) }
    var debugInfo by remember { mutableStateOf("") }
    var canvasViewRef by remember { mutableStateOf<HandwritingCanvasView?>(null) }

    fun vibrate() {
        if (settings.hapticFeedback && vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(20)
            }
        }
    }

    /**
     * CRITICAL CORE RULE:
     * Commits the recognized character directly to the text field WITHOUT adding a space!
     */
    fun commitCandidate(candidate: Candidate) {
        vibrate()
        // ABSOLUTE RULE: Commit raw character text with NO trailing space!
        keyboardConnection.commitCharacter(candidate.text)
        // Clear canvas and candidate list for the next character
        canvasViewRef?.clearCanvas()
        candidates = emptyList()
        strokeCount = 0
    }

    /**
     * Recognition callback triggered after inactivity timeout or manual commit.
     */
    fun onRecognize(strokes: List<Stroke>) {
        if (strokes.isEmpty()) return
        coroutineScope.launch {
            val result = recognizer.recognize(
                strokes = strokes,
                language = currentLanguage,
                mode = currentMode,
                maxCandidates = settings.candidateCount
            )
            debugInfo = result.debugInfo
            candidates = result.candidates

            // Auto-commit top candidate if auto-recognition is enabled
            if (settings.autoRecognize && result.primary != null) {
                val primary = result.primary!!
                // CRITICAL RULE: commitText(primary.text) with NO SPACE!
                keyboardConnection.commitCharacter(primary.text)
                canvasViewRef?.clearCanvas()
                candidates = emptyList()
                strokeCount = 0
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .background(palette.background),
        color = palette.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            // 1. Candidate Bar (Top)
            CandidateBar(
                candidates = candidates,
                palette = palette,
                onCandidateClick = { candidate ->
                    commitCandidate(candidate)
                },
                onPunctuationClick = { punct ->
                    vibrate()
                    // Punctuation also commits WITHOUT space
                    keyboardConnection.commitPunctuation(punct)
                }
            )

            // 2. Handwriting Canvas Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(settings.keyboardHeightDp.dp - 120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.surface)
            ) {
                // Native Canvas View for low-latency touch drawing
                AndroidView(
                    factory = { ctx ->
                        HandwritingCanvasView(ctx).apply {
                            setStrokeColor(palette.strokeColor.toArgb(), palette.strokeGlow.toArgb())
                            setStrokeWidthDip(settings.strokeWidth)
                            setGuideColor(palette.guideLines.toArgb())
                            recognitionDelayMs = settings.recognitionDelayMs
                            autoRecognizeEnabled = settings.autoRecognize
                            showGuides = settings.showGuides
                            hintText = if (currentLanguage == Language.ARABIC) "اكتب الحرف هنا" else "Draw character here"

                            onStrokesChanged = { count ->
                                strokeCount = count
                            }

                            onRecognitionRequested = { strokes ->
                                onRecognize(strokes)
                            }

                            canvasViewRef = this
                        }
                    },
                    update = { view ->
                        view.recognitionDelayMs = settings.recognitionDelayMs
                        view.autoRecognizeEnabled = settings.autoRecognize
                        view.showGuides = settings.showGuides
                        view.setStrokeWidthDip(settings.strokeWidth)
                        view.setStrokeColor(palette.strokeColor.toArgb(), palette.strokeGlow.toArgb())
                        view.setGuideColor(palette.guideLines.toArgb())
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("handwriting_canvas")
                )

                // Canvas Quick Action Icons (Top-Right Floating Overlay)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (strokeCount > 0) {
                        CanvasActionButton(
                            icon = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo last stroke",
                            palette = palette,
                            onClick = {
                                vibrate()
                                canvasViewRef?.undo()
                            }
                        )
                        CanvasActionButton(
                            icon = Icons.Default.Clear,
                            contentDescription = "Clear canvas",
                            palette = palette,
                            onClick = {
                                vibrate()
                                canvasViewRef?.clearCanvas()
                                candidates = emptyList()
                            }
                        )
                        // Immediate manual recognition trigger
                        CanvasActionButton(
                            icon = Icons.Default.Check,
                            contentDescription = "Confirm character",
                            palette = palette,
                            tint = palette.primary,
                            onClick = {
                                vibrate()
                                canvasViewRef?.triggerImmediateRecognition()
                            }
                        )
                    }
                }

                // Debug info & stroke counter (Top-Left)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (strokeCount > 0) {
                        Text(
                            text = "Strokes: $strokeCount",
                            color = palette.onSurface.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (settings.debugMode && debugInfo.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = debugInfo,
                            color = palette.primary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 3. Middle Control Row (Modes, Language, Cursor Navigation)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mode Toggle (ABC / 123 / !#$)
                KeyButton(
                    text = when (currentMode) {
                        RecognitionMode.NUMBERS -> "123"
                        RecognitionMode.SYMBOLS -> "#$="
                        RecognitionMode.LETTERS -> "ABC"
                        RecognitionMode.AUTO -> "AUTO"
                    },
                    palette = palette,
                    modifier = Modifier.weight(1.2f),
                    onClick = {
                        vibrate()
                        currentMode = when (currentMode) {
                            RecognitionMode.AUTO -> RecognitionMode.NUMBERS
                            RecognitionMode.NUMBERS -> RecognitionMode.SYMBOLS
                            RecognitionMode.SYMBOLS -> RecognitionMode.LETTERS
                            RecognitionMode.LETTERS -> RecognitionMode.AUTO
                        }
                    }
                )

                // Language Toggle (EN / AR)
                KeyButton(
                    text = if (currentLanguage == Language.ENGLISH) "EN" else "عربي",
                    palette = palette,
                    modifier = Modifier.weight(1.1f),
                    onClick = {
                        vibrate()
                        currentLanguage = if (currentLanguage == Language.ENGLISH) Language.ARABIC else Language.ENGLISH
                        canvasViewRef?.hintText = if (currentLanguage == Language.ARABIC) "اكتب الحرف هنا" else "Draw character here"
                        canvasViewRef?.invalidate()
                    }
                )

                // Cursor Left
                KeyIconButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Move cursor left",
                    palette = palette,
                    modifier = Modifier.weight(0.9f),
                    onClick = {
                        vibrate()
                        keyboardConnection.moveCursorLeft()
                    }
                )

                // Cursor Right
                KeyIconButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Move cursor right",
                    palette = palette,
                    modifier = Modifier.weight(0.9f),
                    onClick = {
                        vibrate()
                        keyboardConnection.moveCursorRight()
                    }
                )

                // Settings
                KeyIconButton(
                    icon = Icons.Default.Settings,
                    contentDescription = "Keyboard Settings",
                    palette = palette,
                    modifier = Modifier.weight(0.9f),
                    onClick = {
                        vibrate()
                        onOpenSettings()
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 4. Main Bottom Row: Quick Symbol, SPACEBAR, BACKSPACE, ACTION/ENTER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Punctuation key (. or ,)
                KeyButton(
                    text = ".",
                    palette = palette,
                    modifier = Modifier.width(48.dp),
                    onClick = {
                        vibrate()
                        keyboardConnection.commitPunctuation(".")
                    }
                )

                // ==========================================
                // CRITICAL SPACEBAR BUTTON
                // ==========================================
                // The ONLY way a space is inserted is when this button is explicitly pressed!
                Box(
                    modifier = Modifier
                        .weight(3.0f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.surfaceVariant)
                        .clickable {
                            vibrate()
                            // Commits exactly one single space!
                            keyboardConnection.commitSpace()
                        }
                        .testTag("space_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SPACE",
                        color = palette.onSurface.copy(alpha = 0.85f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp
                    )
                }

                // ==========================================
                // BACKSPACE BUTTON
                // ==========================================
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.surfaceVariant)
                        .clickable {
                            vibrate()
                            keyboardConnection.deleteBackward()
                        }
                        .testTag("backspace_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        tint = palette.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // ==========================================
                // ACTION / ENTER BUTTON
                // ==========================================
                val actionLabel = remember(editorInfo) { ImeConfig.getActionLabel(editorInfo) }
                Box(
                    modifier = Modifier
                        .weight(1.4f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.primary)
                        .clickable {
                            vibrate()
                            keyboardConnection.performAction()
                        }
                        .testTag("action_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = actionLabel,
                        color = palette.onPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun KeyButton(
    text: String,
    palette: KeyboardPalette,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(palette.surfaceVariant.copy(alpha = 0.75f))
            .clickable(onClick = onClick)
            .testTag("key_$text"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = palette.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun KeyIconButton(
    icon: ImageVector,
    contentDescription: String,
    palette: KeyboardPalette,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(palette.surfaceVariant.copy(alpha = 0.75f))
            .clickable(onClick = onClick)
            .testTag(contentDescription.lowercase().replace(" ", "_")),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = palette.onSurface,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun CanvasActionButton(
    icon: ImageVector,
    contentDescription: String,
    palette: KeyboardPalette,
    tint: Color = palette.onSurface,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(palette.surfaceVariant.copy(alpha = 0.85f))
            .clickable(onClick = onClick)
            .testTag("canvas_action_${contentDescription.lowercase().replace(" ", "_")}"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}
