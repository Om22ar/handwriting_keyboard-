package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ime.KeyboardPalette
import com.example.recognition.Candidate

/**
 * Modern candidate bar displaying real-time character recognition results
 * or quick-access punctuation shortcuts.
 */
@Composable
fun CandidateBar(
    candidates: List<Candidate>,
    palette: KeyboardPalette,
    onCandidateClick: (Candidate) -> Unit,
    onPunctuationClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(palette.surface)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (candidates.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                candidates.forEachIndexed { index, candidate ->
                    val isPrimary = index == 0
                    CandidateChip(
                        candidate = candidate,
                        isPrimary = isPrimary,
                        palette = palette,
                        onClick = { onCandidateClick(candidate) }
                    )
                }
            }
        } else {
            // Default quick punctuation shortcuts when idle
            val shortcuts = listOf(".", ",", "!", "?", "'", "\"", "-", "@", "#", ":")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                shortcuts.forEach { symbol ->
                    PunctuationChip(
                        symbol = symbol,
                        palette = palette,
                        onClick = { onPunctuationClick(symbol) }
                    )
                }
            }
        }
    }
}

@Composable
fun CandidateChip(
    candidate: Candidate,
    isPrimary: Boolean,
    palette: KeyboardPalette,
    onClick: () -> Unit
) {
    val bg = if (isPrimary) palette.primary else palette.surfaceVariant
    val textColor = if (isPrimary) palette.onPrimary else palette.onSurface

    Box(
        modifier = Modifier
            .widthIn(min = 52.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .testTag("candidate_${candidate.text}")
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = candidate.text,
                color = textColor,
                fontSize = 20.sp,
                fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Medium
            )
            if (isPrimary && candidate.confidence > 0f) {
                Text(
                    text = "${(candidate.confidence * 100).toInt()}%",
                    color = textColor.copy(alpha = 0.75f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun PunctuationChip(
    symbol: String,
    palette: KeyboardPalette,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .widthIn(min = 48.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(palette.surfaceVariant.copy(alpha = 0.6f))
            .clickable(onClick = onClick)
            .testTag("punct_$symbol"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = palette.onSurface,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
