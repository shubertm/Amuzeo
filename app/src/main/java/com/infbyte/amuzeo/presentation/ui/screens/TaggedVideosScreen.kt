package com.infbyte.amuzeo.presentation.ui.screens

import androidx.annotation.OptIn
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.infbyte.amuzeo.R
import com.infbyte.amuzeo.models.Video
import com.infbyte.amuzeo.presentation.theme.AmuzeoTheme
import com.infbyte.amuzeo.presentation.ui.dialogs.AddTagDialog
import com.infbyte.amuzeo.presentation.ui.views.Tags
import com.infbyte.amuzeo.repo.ContentId
import com.infbyte.amuzeo.utils.format
import com.infbyte.amuzeo.utils.toDp

@Composable
fun TaggedVideosScreen(
    videos: List<Video>,
    allTags: Set<String>,
    onVideoClicked: (Video) -> Unit,
    onApplyTag: (ContentId, Set<String>) -> Unit = { _, _ -> },
    onTagClicked: (String) -> Unit = {},
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        var tagsHeight by rememberSaveable { mutableStateOf(0) }

        LazyColumn(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            itemsIndexed(videos) { index, video ->
                TaggedVideo(
                    Modifier.padding(
                        start = 8.dp,
                        end = 8.dp,
                        top = 8.dp,
                        bottom = if (index == videos.size - 1) tagsHeight.toDp() else 8.dp,
                    ),
                    video,
                    onClick = { onVideoClicked(video) },
                    onApplyTag = onApplyTag,
                )
            }
        }
        Tags(
            Modifier.onSizeChanged { size ->
                tagsHeight = size.height
            },
            allTags,
            onTagClicked = onTagClicked,
        )
    }
}

@OptIn(UnstableApi::class)
@Preview
@Composable
fun TaggedVideo(
    modifier: Modifier = Modifier,
    video: Video = Video.EMPTY,
    onClick: () -> Unit = {},
    onApplyTag: (ContentId, Set<String>) -> Unit = { _, _ -> },
) {
    var showTagDialog by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier
            .clip(RoundedCornerShape(10))
            .clickable { onClick() }
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            bitmap = video.thumbnail,
            contentDescription = "",
            modifier =
                Modifier
                    .padding(8.dp)
                    .size(96.dp, 62.dp)
                    .clip(RoundedCornerShape(10))
                    .background(MaterialTheme.colorScheme.onBackground),
            contentScale = ContentScale.Crop,
        )
        Column(Modifier.padding(8.dp).fillMaxWidth().weight(1f, false)) {
            Text(
                video.item.mediaMetadata.title.toString(),
                style = MaterialTheme.typography.bodyLarge,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
            video.item.mediaMetadata.durationMs?.format()?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
        }

        IconButton(
            onClick = { showTagDialog = true },
        ) {
            Icon(painterResource(R.drawable.ic_bookmark), "")
        }
    }

    if (showTagDialog) {
        AddTagDialog(
            onDismiss = { showTagDialog = false },
            onApplyTag = { tag ->
                video.addTag(tag)
                onApplyTag(video.fileId, video.tags)
            },
        )
    }
}

@Preview
@Composable
fun PreviewTaggedVideosScreen() {
    AmuzeoTheme {
        TaggedVideosScreen(listOf(Video.EMPTY, Video.EMPTY), emptySet(), onVideoClicked = {})
    }
}
