package com.micro.gameportal

import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Lights Out in Compose: tap a light to toggle it and its neighbours.
 * The [LightsOutHolder] exposes the mutable game behind Compose state.
 */
class LightsOutHolder(initial: LightsOut) {
    var version by mutableIntStateOf(0)
        private set
    var game: LightsOut = initial
        private set

    fun tap(index: Int) { game.tap(index); version++ }
    fun newGame() { game.newGame(); version++ }
    fun restore(lights: BooleanArray, moves: Int) { game.restore(lights, moves); version++ }
    fun replaceWith(newGame: LightsOut) { game = newGame; version++ }
}

class LightsOutActivity : ComponentActivity() {

    private lateinit var holder: LightsOutHolder
    private val scores by lazy { ScoreStore(this, "lights_out") }
    private val settings by lazy { GameSettings(this) }
    private var showOverlay by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.setupEnter(this)
        val autosave = Autosave.prefs(this, "lights")
        val size = instanceStateSize(savedInstanceState)
            ?: if (autosave.contains("lights")) autosave.getInt("size", settings.lightsOutSize)
            else settings.lightsOutSize
        holder = LightsOutHolder(LightsOut(size = size))
        restoreSavedOrAutosave(savedInstanceState)

        val palette = Palette.current(this)
        setContent {
            MaterialTheme(colorScheme = palette.toColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LightsOutScreen(
                        holder = holder,
                        palette = palette,
                        showHint = !settings.hintSeen("lights"),
                        bestText = bestText(holder.game.size, scores),
                        onBack = { Ui.back(this) },
                        onHelp = { GameHelp.show(this, "lights") },
                        onCellTap = ::onCellTap,
                        onResize = ::onResize,
                        onNewGame = ::onNewGame,
                    )
                    if (showOverlay) {
                        ResultOverlayCompose(
                            title = "Solved!",
                            detail = "${holder.game.moves} moves",
                            palette = palette,
                            onNewGame = ::onNewGame,
                        )
                    }
                }
            }
        }
    }

    private fun instanceStateSize(saved: Bundle?): Int? =
        saved?.getInt("size", -1)?.takeIf { it > 0 }

    private fun restoreSavedOrAutosave(saved: Bundle?) {
        val lights = saved?.getBooleanArray("lights")
        when {
            lights != null -> holder.restore(lights, saved.getInt("moves"))
            Autosave.exists(this, "lights", "lights") -> {
                val autosave = Autosave.prefs(this, "lights")
                val autosavedLights = autosave.getString("lights", "")!!.map { it == '1' }.toBooleanArray()
                holder.restore(autosavedLights, autosave.getInt("moves", 0))
            }
        }
    }

    private fun onCellTap(index: Int) {
        holder.tap(index)
        settings.markHintSeen("lights")
        window.decorView.performHapticFeedback(
            if (holder.game.isSolved) HapticFeedbackConstants.VIRTUAL_KEY
            else HapticFeedbackConstants.CLOCK_TICK,
        )
        if (holder.game.isSolved && holder.game.moves > 0) {
            Feedback.play(this, Feedback.Sound.SUCCESS)
            scores.submit(bestKeyFor(holder.game.size), holder.game.moves.toLong(), lowerIsBetter = true)
            GameStats.recordEnd(this, "lights", won = true)
            Autosave.clear(this, "lights")
            showOverlay = true
        } else {
            Autosave.prefs(this, "lights").edit()
                .putInt("size", holder.game.size)
                .putString("lights", holder.game.lights.joinToString("") { if (it) "1" else "0" })
                .putInt("moves", holder.game.moves)
                .apply()
        }
    }

    private fun onResize(newSize: Int) {
        settings.lightsOutSize = newSize
        Autosave.clear(this, "lights")
        holder.replaceWith(LightsOut(size = newSize).also { it.newGame() })
    }

    private fun onNewGame() {
        Autosave.clear(this, "lights")
        holder.newGame()
        showOverlay = false
    }
}

@Composable
private fun LightsOutScreen(
    holder: LightsOutHolder,
    palette: Palette,
    showHint: Boolean,
    bestText: String,
    onBack: () -> Unit,
    onHelp: () -> Unit,
    onCellTap: (Int) -> Unit,
    onResize: (Int) -> Unit,
    onNewGame: () -> Unit,
) {
    var hintVisible by mutableStateOf(showHint)
    val status = if (holder.game.isSolved) "Solved in ${holder.game.moves} moves!$bestText"
    else "${holder.game.moves} moves — turn all lights off$bestText"

    val buttonColors = buttonColors(palette)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(palette.background))
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp, 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SquareIconButton(label = "←", palette = palette, onClick = onBack)
            Text("Lights Out", color = Color(palette.text), modifier = Modifier.padding(start = 12.dp))
            Spacer(modifier = Modifier.weight(1f))
            SquareIconButton(label = "?", palette = palette, onClick = onHelp)
        }

        Text(
            status,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(palette.textDim),
            modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally),
        )
        if (hintVisible) {
            Text(
                "Tap a light to toggle it and its neighbours",
                style = MaterialTheme.typography.labelSmall,
                color = Color(palette.textDim),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                repeat(holder.game.size) { row ->
                    Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        repeat(holder.game.size) { col ->
                            val index = row * holder.game.size + col
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                                    .background(
                                        Color(
                                            if (holder.game.lights[index]) palette.accent else palette.surfaceDim,
                                        ),
                                        RoundedCornerShape(10.dp),
                                    )
                                    .clickable { onCellTap(index) },
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { onResize(nextSize(GameSettings.LIGHTS_OUT_SIZES, holder.game.size)) }, colors = buttonColors) {
                Text("Size: ${holder.game.size}x${holder.game.size}")
            }
            Spacer(modifier = Modifier.padding(4.dp))
            Button(onClick = onNewGame, colors = buttonColors) {
                Text("New game")
            }
        }
    }
}

@Composable
private fun SquareIconButton(label: String, palette: Palette, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(Color(palette.surfaceDim), RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) { Text(label, color = Color(palette.text)) }
}

@Composable
private fun ResultOverlayCompose(
    title: String,
    detail: String,
    palette: Palette,
    onNewGame: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .background(Color(palette.surface), RoundedCornerShape(18.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall, color = Color(palette.text))
            Text(
                detail,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(palette.textDim),
                modifier = Modifier.padding(vertical = 12.dp),
            )
            Button(onClick = onNewGame, colors = buttonColors(palette)) {
                Text("New game")
            }
        }
    }
}

@Composable
private fun buttonColors(palette: Palette) = ButtonDefaults.buttonColors(
    containerColor = Color(palette.primary),
    contentColor = Color(palette.text),
)

private fun bestKeyFor(size: Int) = "best_moves_$size"

private fun nextSize(options: List<Int>, current: Int): Int =
    options[(options.indexOf(current).coerceAtLeast(0) + 1) % options.size]

private fun bestText(size: Int, scores: ScoreStore): String {
    val best = scores.best(bestKeyFor(size))?.toInt() ?: return ""
    return "  Best: $best moves"
}
