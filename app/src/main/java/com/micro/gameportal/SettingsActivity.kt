package com.micro.gameportal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Settings, rebuilt in Compose (12 migration reference screen). */
class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.setupEnter(this)
        val settings = GameSettings(this)
        setContent {
            MaterialTheme(
                colorScheme = paletteColorScheme(Palette.byIndex(settings.colorTheme)),
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SettingsContent(settings) { finish() }
                }
            }
        }
    }

    private fun nextSize(options: List<Int>, current: Int): Int =
        options[(options.indexOf(current).coerceAtLeast(0) + 1) % options.size]
}

@Composable
private fun SettingsContent(settings: GameSettings, onFinish: () -> Unit) {
    var colorTheme by remember { mutableIntStateOf(settings.colorTheme) }
    var feedback by remember { mutableStateOf(settings.feedbackEnabled) }

    val palette = Palette.byIndex(colorTheme)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(palette.background))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.titleLarge,
            color = Color(palette.text),
            modifier = Modifier.padding(vertical = 16.dp),
        )

        // Game-specific options live inside each game screen; only global
        // options belong here.
        SectionHeader("GENERAL", palette.textDim)
        OptionRow("Color theme", Palette.byIndex(colorTheme).name, palette) {
            colorTheme = (colorTheme + 1) % Palette.PRESETS.size
            settings.colorTheme = colorTheme
        }
        OptionRow("Sound & vibration", if (feedback) "On" else "Off", palette) {
            feedback = !feedback
            settings.feedbackEnabled = feedback
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Button(onClick = onFinish) {
                Text("Back")
            }
        }
    }
}

private fun nextSize(options: List<Int>, current: Int): Int =
    options[(options.indexOf(current).coerceAtLeast(0) + 1) % options.size]

@Composable
private fun SectionHeader(text: String, color: Int) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = Color(color),
        letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified,
        modifier = Modifier.padding(top = 20.dp, bottom = 4.dp),
    )
}

@Composable
private fun OptionRow(label: String, value: String, palette: Palette, onCycle: () -> Unit) {  // values are ARGB ints from Palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(palette.text),
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(palette.textDim),
            modifier = Modifier
                .background(
                    Color(palette.surfaceDim),
                    RoundedCornerShape(10.dp),
                )
                .clickable { onCycle() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}

/** Maps a [Palette] to a Material 3 color scheme so Compose follows the theme picker. */
private fun paletteColorScheme(palette: Palette) = androidx.compose.material3.darkColorScheme(
    primary = Color(palette.primary),
    onPrimary = Color(palette.background),
    surface = Color(palette.background),
    onSurface = Color(palette.text),
    surfaceVariant = Color(palette.surfaceDim),
    onSurfaceVariant = Color(palette.textDim),
    secondary = Color(palette.accent),
    onSecondary = Color(palette.background),
)
