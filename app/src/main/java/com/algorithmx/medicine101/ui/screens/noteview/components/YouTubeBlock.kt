package com.algorithmx.medicine101.ui.screens.noteview.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.algorithmx.medicine101.data.VideoTimestamp
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun YouTubeBlock(videoId: String, timestamps: List<VideoTimestamp>?) {
    var playerInstance by remember { mutableStateOf<YouTubePlayer?>(null) }
    var isFullScreen by remember { mutableStateOf(false) }
    var startSeconds by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // --- 1. THE INLINE PLAYER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
        ) {
            YouTubePlayerNative(
                videoId = videoId,
                startSeconds = startSeconds,
                onPlayerReady = { playerInstance = it }
            )
            
            // Fullscreen Button Overlay
            IconButton(
                onClick = { isFullScreen = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
            ) {
                Icon(Icons.Default.Fullscreen, contentDescription = "Full Screen", tint = Color.White)
            }
        }

        // --- 2. TIMESTAMP MAPPING LIST ---
        if (!timestamps.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Key Timestamps:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            
            Column(modifier = Modifier.padding(top = 4.dp)) {
                timestamps.forEach { ts ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Jump to timestamp!
                                val targetTime = ts.toSeconds()
                                playerInstance?.seekTo(targetTime)
                                startSeconds = targetTime // Save state in case they go fullscreen
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(ts.timestamp, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(ts.label, style = MaterialTheme.typography.bodyMedium)
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                }
            }
        }
    }

    // --- 3. SEPARATE FULL-SCREEN WINDOW ---
    if (isFullScreen) {
        Dialog(
            onDismissRequest = { isFullScreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                YouTubePlayerNative(
                    videoId = videoId,
                    startSeconds = startSeconds, // Start exactly where they left off!
                    onPlayerReady = { /* Fullscreen player instance */ }
                )
                
                IconButton(
                    onClick = { isFullScreen = false },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(32.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close Fullscreen", tint = Color.White)
                }
            }
        }
    }
}

// Helper Composable to bridge the XML View to Compose safely
@Composable
fun YouTubePlayerNative(videoId: String, startSeconds: Float, onPlayerReady: (YouTubePlayer) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            YouTubePlayerView(context).apply {
                lifecycleOwner.lifecycle.addObserver(this)
                addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        onPlayerReady(youTubePlayer)
                        youTubePlayer.loadVideo(videoId, startSeconds) // loadVideo autoplays. Use cueVideo to pause.
                    }
                })
            }
        }
    )
}