package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BackgroundElevated
import com.example.ui.theme.BackgroundPrimary
import com.example.ui.theme.BackgroundSurface
import com.example.ui.theme.BorderActive
import com.example.ui.theme.BorderDefault
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.MatrixGreenDim
import com.example.ui.theme.MatrixGreenGlow
import com.example.ui.theme.StatusFail
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusNeutral
import com.example.ui.theme.StatusOk
import com.example.ui.theme.StatusWarn
import com.example.ui.theme.TerminalFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * Cyberpunk Angled Corner Shape: 12dp diagonal cut on top-right corner.
 */
class AngledCornerShape(private val cutSize: Dp = 12.dp) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cutPx = with(density) { cutSize.toPx() }
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width - cutPx, 0f)
            lineTo(size.width, cutPx)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * Terminal Panel Container
 */
@Composable
fun TerminalPanel(
    modifier: Modifier = Modifier,
    hasAngledCut: Boolean = false,
    hasLeftAccentBar: Boolean = false,
    borderColor: Color = BorderDefault,
    content: @Composable () -> Unit
) {
    val shape: Shape = if (hasAngledCut) AngledCornerShape(12.dp) else RoundedCornerShape(6.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(BackgroundSurface)
            .border(1.dp, borderColor, shape)
    ) {
        if (hasLeftAccentBar) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(end = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxSize()
                        .background(MatrixGreen)
                )
            }
        }
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

/**
 * Terminal Data Row with Dot Leaders
 * e.g. "MANUFACTURER ......... SAMSUNG"
 */
@Composable
fun TerminalRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    labelColor: Color = TextSecondary,
    valueColor: Color = TextPrimary,
    status: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.uppercase(),
            fontFamily = TerminalFontFamily,
            fontSize = 12.sp,
            color = labelColor,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(4.dp))
        // Dot leader canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .height(14.dp)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxWidth()) {
                val step = 14f
                var x = 0f
                val y = size.height / 2
                while (x < size.width) {
                    drawCircle(
                        color = Color(0xFF2B2B2B),
                        radius = 1.2f,
                        center = Offset(x, y)
                    )
                    x += step
                }
            }
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            fontFamily = TerminalFontFamily,
            fontSize = 12.sp,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
        if (status != null) {
            Spacer(modifier = Modifier.width(8.dp))
            status()
        }
    }
}

/**
 * Terminal-Pure Status Indicators
 * [OK], [WARN], [FAIL], [..]
 */
@Composable
fun StatusIndicator(
    status: String,
    modifier: Modifier = Modifier
) {
    val (color, text) = when (status.uppercase()) {
        "OK", "CLEAN", "PASS" -> Pair(StatusOk, "[OK]")
        "WARN", "SUSPICIOUS", "MODERATE" -> Pair(StatusWarn, "[WARN]")
        "FAIL", "ROOTED", "DANGER", "CRITICAL" -> Pair(StatusFail, "[FAIL]")
        "BLOCKED" -> Pair(StatusWarn, "[BLOCKED]")
        else -> Pair(StatusInfo, "[..]")
    }

    Text(
        text = text,
        fontFamily = TerminalFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier
    )
}

/**
 * Glow Text with CRT Phosphor Glow
 */
@Composable
fun GlowText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MatrixGreen,
    fontSize: Int = 14,
    fontWeight: FontWeight = FontWeight.Bold
) {
    Box(modifier = modifier) {
        // Shadow/glow layer
        Text(
            text = text,
            fontFamily = TerminalFontFamily,
            fontSize = fontSize.sp,
            fontWeight = fontWeight,
            color = color.copy(alpha = 0.35f),
            modifier = Modifier.padding(1.dp)
        )
        // Sharp foreground layer
        Text(
            text = text,
            fontFamily = TerminalFontFamily,
            fontSize = fontSize.sp,
            fontWeight = fontWeight,
            color = color
        )
    }
}

/**
 * Scanline Overlay (persistent CRT monitor lines)
 */
@Composable
fun ScanlineOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val lineSpacing = 3.dp.toPx()
        val numLines = (size.height / lineSpacing).toInt()
        for (i in 0 until numLines) {
            val y = i * lineSpacing
            drawLine(
                color = Color.Black.copy(alpha = 0.08f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
        }
    }
}

/**
 * Typewriter Text Reveal with Blinking Cursor
 */
@Composable
fun TypewriterText(
    fullText: String,
    modifier: Modifier = Modifier,
    charDelayMs: Long = 20L,
    style: TextStyle = TextStyle(
        fontFamily = TerminalFontFamily,
        fontSize = 14.sp,
        color = MatrixGreen
    ),
    showCursor: Boolean = true,
    onComplete: (() -> Unit)? = null
) {
    var displayedLength by remember(fullText) { mutableIntStateOf(0) }
    var cursorVisible by remember { mutableStateOf(true) }

    LaunchedEffect(fullText) {
        displayedLength = 0
        for (i in 1..fullText.length) {
            delay(charDelayMs)
            displayedLength = i
        }
        onComplete?.invoke()
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            cursorVisible = !cursorVisible
        }
    }

    val visibleText = fullText.take(displayedLength)
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(text = visibleText, style = style)
        if (showCursor && cursorVisible && displayedLength < fullText.length) {
            Text(
                text = "_",
                style = style.copy(color = MatrixGreen, fontWeight = FontWeight.Bold)
            )
        }
    }
}

/**
 * Radial Security Score Arc with radar sweep indicator
 */
@Composable
fun ScoreArc(
    score: Int,
    bandLabel: String,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 180.dp
) {
    val targetScore = score.coerceIn(0, 100)
    val animatedScore by animateFloatAsState(
        targetValue = targetScore.toFloat(),
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "scoreAnim"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "radarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAngle"
    )

    val scoreColor = when {
        targetScore >= 85 -> StatusOk
        targetScore >= 70 -> StatusOk
        targetScore >= 50 -> StatusWarn
        targetScore >= 30 -> StatusWarn
        else -> StatusFail
    }

    Box(
        modifier = modifier.size(sizeDp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            val arcSize = size.width - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            // Background track
            drawArc(
                color = Color(0xFF141414),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Score arc
            val progressSweep = (animatedScore / 100f) * 270f
            drawArc(
                brush = Brush.sweepGradient(
                    0.0f to MatrixGreenDim,
                    0.7f to scoreColor,
                    1.0f to scoreColor
                ),
                startAngle = 135f,
                sweepAngle = progressSweep,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Cyan rotating radar tick mark
            val rad = Math.toRadians((sweepAngle).toDouble())
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = arcSize / 2
            val tickStart = Offset(
                (centerX + (radius - 12f) * cos(rad)).toFloat(),
                (centerY + (radius - 12f) * sin(rad)).toFloat()
            )
            val tickEnd = Offset(
                (centerX + (radius + 4f) * cos(rad)).toFloat(),
                (centerY + (radius + 4f) * sin(rad)).toFloat()
            )
            drawLine(
                color = StatusInfo.copy(alpha = 0.8f),
                start = tickStart,
                end = tickEnd,
                strokeWidth = 2.dp.toPx()
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${animatedScore.toInt()}",
                fontFamily = TerminalFontFamily,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = scoreColor,
                letterSpacing = 0.05.sp
            )
            Text(
                text = bandLabel,
                fontFamily = TerminalFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.1.sp
            )
        }
    }
}

/**
 * Matrix Rain Effect for Splash
 */
@Composable
fun MatrixRain(modifier: Modifier = Modifier) {
    val glyphs = "0123456789ABCDEFｦｱｳｴｵｶｷｹｺｻｼｽｾｿﾀﾂﾃﾅﾆﾇﾈﾊﾋﾎﾏﾐﾑﾒﾓﾔﾕﾗﾘﾜ"
    val columns = 20
    val rows = 24

    val offsets = remember {
        List(columns) { (0 until rows).random() }
    }
    var tick by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(80)
            tick++
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val colWidth = size.width / columns
        val rowHeight = size.height / rows

        for (col in 0 until columns) {
            val head = (offsets[col] + tick) % rows
            for (row in 0 until rows) {
                val dist = (head - row + rows) % rows
                val alpha = when {
                    dist == 0 -> 0.9f
                    dist < 5 -> 0.5f - (dist * 0.08f)
                    else -> 0.0f
                }
                if (alpha > 0.05f) {
                    val color = if (dist == 0) MatrixGreen else MatrixGreenDim.copy(alpha = alpha)
                    val x = col * colWidth + colWidth / 2
                    val y = row * rowHeight + rowHeight / 2
                    drawCircle(color = color, radius = 2.5f, center = Offset(x, y))
                }
            }
        }
    }
}

/**
 * Radar Sweep Component for Network Monitor
 */
@Composable
fun RadarSweep(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "radarTransition")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAngle"
    )

    Box(
        modifier = modifier
            .size(80.dp)
            .clip(RoundedCornerShape(40.dp))
            .background(BackgroundElevated)
            .border(1.dp, BorderActive, RoundedCornerShape(40.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2

            // Grid circles
            drawCircle(color = BorderDefault, radius = radius * 0.4f, style = Stroke(1f))
            drawCircle(color = BorderDefault, radius = radius * 0.75f, style = Stroke(1f))

            // Crosshairs
            drawLine(BorderDefault, Offset(0f, center.y), Offset(size.width, center.y), 1f)
            drawLine(BorderDefault, Offset(center.x, 0f), Offset(center.x, size.height), 1f)

            // Sweep line
            val rad = Math.toRadians(angle.toDouble())
            val end = Offset(
                (center.x + radius * cos(rad)).toFloat(),
                (center.y + radius * sin(rad)).toFloat()
            )
            drawLine(
                color = MatrixGreen,
                start = center,
                end = end,
                strokeWidth = 2f
            )

            // Blip points
            drawCircle(color = StatusOk, radius = 2.5f, center = Offset(center.x + 18f, center.y - 12f))
            drawCircle(color = StatusInfo, radius = 2f, center = Offset(center.x - 14f, center.y + 16f))
        }
    }
}

/**
 * 24-Waveform Bars for telemetry/battery readings
 */
@Composable
fun WaveformBars(
    currentLevelPercent: Int,
    modifier: Modifier = Modifier
) {
    val barCount = 24
    val heights = remember(currentLevelPercent) {
        List(barCount) { idx ->
            val factor = ((idx + 1).toFloat() / barCount)
            (currentLevelPercent * factor * 0.9f).coerceIn(15f, 100f)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        heights.forEach { heightPct ->
            val color = when {
                currentLevelPercent >= 50 -> MatrixGreen
                currentLevelPercent >= 20 -> StatusWarn
                else -> StatusFail
            }

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((28 * (heightPct / 100f)).dp)
                    .background(color)
            )
        }
    }
}

/**
 * Terminal Button
 * Primary: transparent fill, 1dp green border, green uppercase label. Pressed: fill green, label black.
 * Secondary: 1dp grey border, grey label.
 * Danger: 1dp red border, red label. Pressed: fill red, label black.
 */
enum class TerminalButtonType { PRIMARY, SECONDARY, DANGER }

@Composable
fun TerminalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: TerminalButtonType = TerminalButtonType.PRIMARY,
    enabled: Boolean = true,
    testTag: String = "terminal_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val (borderColor, textColor, fillPressed) = when (type) {
        TerminalButtonType.PRIMARY -> Triple(MatrixGreen, MatrixGreen, MatrixGreen)
        TerminalButtonType.SECONDARY -> Triple(BorderDefault, TextSecondary, BorderDefault)
        TerminalButtonType.DANGER -> Triple(StatusFail, StatusFail, StatusFail)
    }

    val actualBorder = if (!enabled) BorderDefault else borderColor
    val actualBg = if (isPressed && enabled) fillPressed else BackgroundSurface
    val actualText = if (isPressed && enabled) BackgroundPrimary else if (!enabled) TextTertiary else textColor

    Box(
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(4.dp))
            .background(actualBg)
            .border(1.dp, actualBorder, RoundedCornerShape(4.dp))
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            fontFamily = TerminalFontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = actualText,
            letterSpacing = 0.08.sp
        )
    }
}

/**
 * Top Status Bar Strip (24dp)
 */
@Composable
fun TerminalStatusBar(
    score: Int,
    isLocked: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "> CIPHERLOCK [OFFLINE]",
                fontFamily = TerminalFontFamily,
                fontSize = 11.sp,
                color = MatrixGreenDim,
                fontWeight = FontWeight.Medium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "SEC: $score/100",
                    fontFamily = TerminalFontFamily,
                    fontSize = 11.sp,
                    color = if (score >= 70) MatrixGreen else if (score >= 50) StatusWarn else StatusFail,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isLocked) "[LOCK]" else "[UNLOCKED]",
                    fontFamily = TerminalFontFamily,
                    fontSize = 11.sp,
                    color = if (isLocked) StatusWarn else MatrixGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFF1A1A1A))
        )
    }
}

/**
 * Bottom Footer Strip
 */
@Composable
fun TerminalFooter(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFF1A1A1A))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "> CIPHERLOCK v1.0 | SAMSUNG GALAXY A17 5G",
                fontFamily = TerminalFontFamily,
                fontSize = 10.sp,
                color = MatrixGreenDim,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "100% OFFLINE",
                fontFamily = TerminalFontFamily,
                fontSize = 10.sp,
                color = StatusOk,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
