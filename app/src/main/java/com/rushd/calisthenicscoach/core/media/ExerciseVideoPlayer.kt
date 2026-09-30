package com.rushd.calisthenicscoach.core.media

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.rushd.calisthenicscoach.domain.ExerciseVideo

/**
 * Shared Media3 player for exercise demonstrations.
 *
 * The player owns its lifecycle, autoplays muted by default and loops the
 * current exercise without a transition flash.
 */
@Composable
fun ExerciseVideoPlayer(
    video: ExerciseVideo,
    modifier: Modifier = Modifier,
    showControls: Boolean = false,
    autoPlay: Boolean = true,
    muted: Boolean = true
) {
    val context = LocalContext.current
    val uri = remember(video.resId, context.packageName) {
        Uri.parse("android.resource://${context.packageName}/${video.resId}")
    }

    val player = remember(video.resId) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            volume = if (muted) 0f else 1f
            playWhenReady = autoPlay
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
        }
    }

    LaunchedEffect(autoPlay, muted) {
        player.volume = if (muted) 0f else 1f
        player.playWhenReady = autoPlay
        if (autoPlay) player.play() else player.pause()
    }

    DisposableEffect(player) {
        onDispose {
            player.stop()
            player.release()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PlayerView(ctx).apply {
                useController = showControls
                this.player = player
                keepScreenOn = true
            }
        },
        update = { view ->
            view.useController = showControls
            view.player = player
        }
    )
}
