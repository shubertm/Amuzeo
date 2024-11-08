package com.infbyte.amuzeo.presentation.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun BoxScope.AmuzeSeekBar(
    progress: Float = 0.5f,
    alignment: Alignment = Alignment.BottomCenter,
    onSeekTo: (Float) -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    Slider(
        value = progress,
        onValueChange = { onSeekTo(it) },
        modifier =
            Modifier
                .padding(
                    start = 32.dp,
                    end = 32.dp,
                    bottom = 8.dp,
                ).align(alignment),
        interactionSource = interactionSource,
        track = { SliderDefaults.Track(it, Modifier.height(4.dp)) },
        thumb = { SliderDefaults.Thumb(interactionSource, thumbSize = DpSize(3.dp, 16.dp)) },
        colors =
            SliderDefaults.colors(inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer),
    )
}
