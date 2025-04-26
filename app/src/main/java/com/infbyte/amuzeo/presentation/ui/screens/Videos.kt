package com.infbyte.amuzeo.presentation.ui.screens

import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.infbyte.amuzeo.models.Video
import com.infbyte.amuzeo.presentation.theme.AmuzeoTheme
import com.infbyte.amuzeo.utils.format

@Composable
fun Videos(
    videos: List<Video>,
    onVideoClicked: (Video) -> Unit = {},
) {
    LazyColumn(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        items(videos) { video ->
            Video(
                video,
                onClick = { onVideoClicked(video) },
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Preview
@Composable
fun Video(
    video: Video = Video.EMPTY,
    onClick: () -> Unit = {},
) {
    Row(
        Modifier.background(
            MaterialTheme.colorScheme.background,
        ).padding(8.dp).fillMaxWidth().clip(RoundedCornerShape(10))
            .clickable { onClick() },
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
        Column(Modifier.padding(8.dp)) {
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
    }
}

@Preview
@Composable
fun PreviewVideosScreen() {
    AmuzeoTheme {
        Videos(listOf(Video.EMPTY, Video.EMPTY), {})
    }
}
