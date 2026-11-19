package com.example.ui.components

import android.os.Build
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.model.TimeOfDayPhase

@Composable
fun DynamicWallpaperBackground(
    phase: TimeOfDayPhase,
    isDimmed: Boolean,
    isBlurred: Boolean,
    pageParallaxOffset: Float = 0f,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wallpaper_ambient")
    val ambientPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_pulse"
    )

    val blurRadius by animateDpAsState(
        targetValue = if (isBlurred) 24.dp else 0.dp,
        animationSpec = spring(),
        label = "wallpaper_blur"
    )
    val dimAlpha by animateFloatAsState(
        targetValue = if (isDimmed) 0.42f else 0.08f,
        animationSpec = tween(260),
        label = "wallpaper_dim"
    )
    val topGlow by animateColorAsState(
        targetValue = phase.topColor.copy(alpha = 0.24f),
        animationSpec = tween(900),
        label = "top_glow"
    )
    val bottomGlow by animateColorAsState(
        targetValue = phase.bottomColor.copy(alpha = 0.28f),
        animationSpec = tween(900),
        label = "bottom_glow"
    )

    val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadius > 0.dp) {
        Modifier.blur(blurRadius)
    } else {
        Modifier
    }

    Box(modifier = modifier.fillMaxSize()) {
        Crossfade(
            targetState = phase,
            animationSpec = tween(durationMillis = 850),
            modifier = Modifier
                .fillMaxSize()
                .then(blurModifier),
            label = "wallpaper_crossfade"
        ) { currentPhase ->
            Image(
                painter = painterResource(id = currentPhase.wallpaperRes),
                contentDescription = "${currentPhase.label} dynamic wallpaper",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = 1.08f
                        scaleY = 1.08f
                        translationX = -pageParallaxOffset * 28f
                    }
            )
        }

        // Subtle living atmospheric color gradient that breathes gently
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(topGlow, Color.Transparent, bottomGlow)
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                topGlow.copy(alpha = 0.30f),
                                Color.Transparent
                            ),
                            center = Offset(
                                x = size.width * (0.25f + 0.5f * ambientPulse),
                                y = size.height * 0.22f
                            ),
                            radius = size.maxDimension * 0.55f
                        )
                    )
                }
        )

        // Contrast & glass overlay scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = dimAlpha))
        )
    }
}
