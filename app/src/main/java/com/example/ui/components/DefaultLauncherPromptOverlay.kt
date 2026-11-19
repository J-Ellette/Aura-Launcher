package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AuraBlue
import com.example.ui.theme.AuraIndigo
import com.example.ui.theme.AuraTeal

@Composable
fun DefaultLauncherPromptOverlay(
    visible: Boolean,
    onSetAsDefault: () -> Unit,
    onDismissSession: () -> Unit,
    onDontAskAgain: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(
            initialScale = 0.90f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ),
        exit = fadeOut() + scaleOut(targetScale = 0.92f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xA6070B14))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismissSession
                )
                .padding(horizontal = 24.dp)
                .testTag("default_launcher_prompt_overlay"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xF5151C2F), Color(0xF50E1424))
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.24f), RoundedCornerShape(30.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .padding(horizontal = 22.dp, vertical = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(listOf(AuraBlue, AuraIndigo))
                        )
                        .border(0.8.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Home,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.default_launcher_title),
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.default_launcher_subtitle),
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(0.75.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    FeatureRow(
                        icon = Icons.Rounded.AutoAwesome,
                        tint = AuraTeal,
                        text = "Press Home anytime to jump straight to your Aura workspace"
                    )
                    FeatureRow(
                        icon = Icons.Rounded.Tune,
                        tint = AuraBlue,
                        text = "Instant swipe access to Control Center, Focus Mode & Lock Screen"
                    )
                    FeatureRow(
                        icon = Icons.Rounded.Search,
                        tint = AuraIndigo,
                        text = "Spotlight Search & organized App Library across all installed apps"
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onSetAsDefault,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AuraBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("set_default_launcher_button")
                ) {
                    Text(
                        text = stringResource(R.string.default_launcher_confirm),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismissSession,
                        modifier = Modifier.testTag("dismiss_default_launcher_button")
                    ) {
                        Text(
                            text = stringResource(R.string.default_launcher_not_now),
                            color = Color.White.copy(alpha = 0.82f),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    TextButton(
                        onClick = onDontAskAgain,
                        modifier = Modifier.testTag("suppress_default_launcher_button")
                    ) {
                        Text(
                            text = stringResource(R.string.default_launcher_dont_ask),
                            color = Color.White.copy(alpha = 0.56f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    tint: Color,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(tint.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.88f),
            fontSize = 12.5.sp,
            lineHeight = 17.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
