package com.infbyte.amuzeo.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infbyte.amuzeo.models.Folder
import com.infbyte.amuzeo.presentation.theme.AmuzeoTheme
import com.infbyte.amuzeo.utils.getInitialChar

@Composable
fun FoldersScreen(
    folders: List<Folder>,
    onFolderClicked: (Folder) -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        items(folders) { folder ->
            Folder(folder) { onFolderClicked(folder) }
        }
    }
}

@Preview
@Composable
fun Folder(
    folder: Folder = Folder.DEFAULT,
    onClick: () -> Unit = {},
) {
    Row(
        Modifier.padding(8.dp).fillMaxWidth().clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.padding(8.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                .size(62.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                folder.name.getInitialChar(),
                style = MaterialTheme.typography.headlineLarge,
            )
        }

        Column(Modifier.padding(8.dp)) {
            Text(
                folder.name,
                style = MaterialTheme.typography.bodyLarge,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
            Text(
                folder.numberOfVideos.toString(),
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        }
    }
}

@Preview
@Composable
fun PreviewFoldersScreen() {
    AmuzeoTheme {
        FoldersScreen(listOf(Folder.DEFAULT, Folder.DEFAULT), {})
    }
}
