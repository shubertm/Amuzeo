package com.infbyte.amuzeo.presentation.ui.views

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.arkbuilders.arklib.user.tags.Tag
import dev.arkbuilders.arklib.user.tags.Tags

@Preview
@Composable
fun BoxScope.Tags(
    tags: Tags = emptySet(),
    modifier: Modifier = Modifier,
    onTagClicked: (Tag) -> Unit = {},
) {
    LazyRow(modifier.fillMaxWidth().align(Alignment.BottomCenter)) {
        items(tags.toList()) { tag ->
            Tag(
                tag,
                onSelect = { onTagClicked(tag) },
            )
        }
    }
}

@Preview
@Composable
fun Tag(
    name: String = "tag",
    onSelect: () -> Unit = {},
) {
    var selected by rememberSaveable { mutableStateOf(false) }
    FilterChip(
        selected,
        onClick = {
            selected = !selected
            onSelect()
        },
        label = { Text(name) },
        Modifier.padding(4.dp),
        leadingIcon = {
            if (selected) Icon(Icons.Outlined.Check, "")
        },
        colors = FilterChipDefaults.elevatedFilterChipColors(),
    )
}
