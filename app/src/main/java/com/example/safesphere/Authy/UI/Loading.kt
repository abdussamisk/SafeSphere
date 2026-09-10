package com.example.safesphere.Authy.UI

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safesphere.R
import kotlinx.coroutines.delay

private object SafeSphereColors {
    val BackgroundCream = Color(0xFFF3F1EA)
    val TextDark = Color(0xFF2C2A26)
    val TextGray = Color(0xFF8A8A85)
    val PrimaryGreen = Color(0xFF2F4A3B)
    val PrimaryGreenSoft = Color(0xFF4C8062)
}

@Composable
fun Loading(
    statusText: String = "Setting up your safety circle"
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .radialAuthBackground(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // Radar-style pulse rings behind the logo — evokes live
            // location broadcasting, the core of the app
            RadarPulseLogo()

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "SafeSphere",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SafeSphereColors.TextDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            AnimatedStatusText(baseText = statusText)

            Spacer(modifier = Modifier.height(36.dp))

            SegmentedLoaderBar()
        }
    }
}

@Composable
private fun RadarPulseLogo() {
    val ringCount = 3
    val transition = rememberInfiniteTransition(label = "radar")

    Box(
        modifier = Modifier.size(180.dp),
        contentAlignment = Alignment.Center
    ) {
        repeat(ringCount) { index ->
            val delayMillis = index * 600

            val scale by transition.animateFloat(
                initialValue = 0.35f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1800, delayMillis = delayMillis, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "ringScale$index"
            )
            val alpha by transition.animateFloat(
                initialValue = 0.45f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1800, delayMillis = delayMillis, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "ringAlpha$index"
            )

            Canvas(
                modifier = Modifier
                    .size(180.dp)
                    .scale(scale)
            ) {
                drawCircle(
                    color = SafeSphereColors.PrimaryGreenSoft.copy(alpha = alpha),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }

        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            SafeSphereColors.PrimaryGreenSoft.copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    )
                )
        )

        Image(
            painter = painterResource(id = R.drawable.ic_safesphere_logo),
            contentDescription = "SafeSphere logo",
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
        )
    }
}

@Composable
private fun AnimatedStatusText(baseText: String) {
    var dotCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(450)
            dotCount = (dotCount + 1) % 4
        }
    }

    Text(
        text = baseText + ".".repeat(dotCount),
        fontSize = 14.sp,
        color = SafeSphereColors.TextGray,
        textAlign = TextAlign.Center,
        modifier = Modifier.height(20.dp)
    )
}

@Composable
private fun SegmentedLoaderBar() {
    val transition = rememberInfiniteTransition(label = "segments")

    Box(
        modifier = Modifier
            .width(120.dp)
            .height(4.dp)
    ) {
        val trackColor = SafeSphereColors.PrimaryGreen.copy(alpha = 0.15f)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(trackColor)
        )

        val offset by transition.animateFloat(
            initialValue = -0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1100, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "sweep"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(40.dp)
                    .offset(x = (120 * offset).dp)
                    .clip(CircleShape)
                    .background(SafeSphereColors.PrimaryGreen)
            )
        }
    }
}

private fun Modifier.radialAuthBackground(): Modifier = this.background(
    brush = Brush.radialGradient(
        colors = listOf(
            SafeSphereColors.PrimaryGreenSoft.copy(alpha = 0.16f),
            SafeSphereColors.BackgroundCream
        ),
        center = Offset(0.5f, 0.05f),
        radius = 900f
    )
)