package com.infbyte.amuzeo.presentation.ui.screens

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import coil.ImageLoader
import coil.compose.AsyncImage
import com.infbyte.amuzeo.models.Video
import com.infbyte.amuzeo.presentation.theme.AmuzeoTheme
import com.infbyte.amuzeo.utils.format
import com.infbyte.amuzeo.utils.getVideoDuration

@Composable
fun VideosScreen(
    videos: List<Video>,
    imageLoader: ImageLoader,
    onVideoClicked: (Int) -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        itemsIndexed(videos) { index, video ->
            Video(video, imageLoader) {
                onVideoClicked(index)
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Preview
@Composable
fun Video(
    video: Video = Video.EMPTY,
    imageLoader: ImageLoader = ImageLoader(LocalContext.current),
    onClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val videoUri = remember { video.item.localConfiguration?.uri }

    Row(
        Modifier.padding(8.dp).fillMaxWidth().clip(RoundedCornerShape(10))
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = video.thumbnailRequest,
            imageLoader = imageLoader,
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
            Text(
                context.getVideoDuration(videoUri).format(),
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        }
    }
}

@Preview
@Composable
fun PreviewVideosScreen() {
    AmuzeoTheme {
        VideosScreen(listOf(Video.EMPTY, Video.EMPTY), ImageLoader(LocalContext.current), {})
    }
}
