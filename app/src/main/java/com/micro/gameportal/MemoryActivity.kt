package com.micro.gameportal

import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Memory match in Compose: flip two cards to find pairs.
 * The [MemoryHolder] exposes the mutable game behind Compose state.
 */
class MemoryHolder(initial: MemoryGame) {
    var version by mutableIntStateOf(0)
        private set
    var game: MemoryGame = initial
        private set

    fun flip(index: Int): MemoryGame.FlipResult {
        val result = game.flip(index)
        version++
        return result
    }

    fun hideMismatch(first: Int, second: Int) {
        game.hideMismatch(first, second)
        version++
    }

    fun restoreDeck(faces: List<String>, matched: BooleanArray) {
        game.restoreDeck(faces, matched)
        version++
    }
}

class MemoryActivity : ComponentActivity() {

    private lateinit var holder: MemoryHolder
    private val settings by lazy { GameSettings(this) }
    private var showOverlay by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.setupEnter(this)
        holder = MemoryHolder(MemoryGame())
        restoreSavedOrAutosave(savedInstanceState)

        val palette = Palette.current(this)
        val scope = CoroutineScope(Dispatchers.Main)
        setContent {
            MaterialTheme(colorScheme = palette.toColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MemoryScreen(
                        holder = holder,
                        palette = palette,
                        showHint = !settings.hintSeen("memory"),
                        onFirstFlip = { settings.markHintSeen("memory") },
                        onMoveEnd = {
                            if (holder.game.isCleared) {
                                Autosave.clear(this, "memory")
                            } else {
                                Autosave.prefs(this, "memory").edit()
                                    .putString("faces", holder.game.faces.joinToString("\u0001"))
                                    .putString("matched", holder.game.matchedMask().joinToString("") { if (it) "1" else "0" })
                                    .apply()
                            }
                        },
                        onCleared = {
                            Feedback.play(this, Feedback.Sound.SUCCESS)
                            GameStats.recordEnd(this, "memory", won = true)
                            Autosave.clear(this, "memory")
                            showOverlay = true
                        },
                        onMismatchFeedback = {
                            Feedback.play(this, Feedback.Sound.MISMATCH)
                            window.decorView.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        },
                        onMatchFeedback = {
                            Feedback.play(this, Feedback.Sound.MATCH)
                            window.decorView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        },
                        onNewGame = {
                            Autosave.clear(this, "memory")
                            holder = MemoryHolder(MemoryGame())
                            showOverlay = false
                        },
                        onBack = { Ui.back(this) },
                        onHelp = { GameHelp.show(this, "memory") },
                        scope = scope,
                    )
                    if (showOverlay) {
                        ResultCard(
                            title = "Cleared!",
                            detail = "All pairs found",
                            palette = palette,
                            onNewGame = {
                                Autosave.clear(this, "memory")
                                holder = MemoryHolder(MemoryGame())
                                showOverlay = false
                            },
                        )
                    }
                }
            }
        }
    }

    private fun restoreSavedOrAutosave(saved: Bundle?) {
        val faces = saved?.getStringArray("faces")
        when {
            faces != null -> holder.restoreDeck(
                faces.toList(),
                saved.getBooleanArray("matched") ?: BooleanArray(faces.size),
            )
            Autosave.exists(this, "memory", "faces") -> {
                val autosave = Autosave.prefs(this, "memory")
                val facesAutosave = autosave.getString("faces", "")!!.split("\u0001")
                val matched = autosave.getString("matched", "")!!.map { it == '1' }.toBooleanArray()
                if (facesAutosave.size == matched.size) holder.restoreDeck(facesAutosave, matched)
            }
        }
    }
}

@Composable
private fun MemoryScreen(
    holder: MemoryHolder,
    palette: Palette,
    showHint: Boolean,
    onFirstFlip: () -> Unit,
    onMoveEnd: () -> Unit,
    onCleared: () -> Unit,
    onMismatchFeedback: () -> Unit,
    onMatchFeedback: () -> Unit,
    onNewGame: () -> Unit,
    onBack: () -> Unit,
    onHelp: () -> Unit,
    scope: CoroutineScope,
) {
    var hintVisible by remember { mutableStateOf(showHint) }
    var revealed by remember { mutableStateOf(setOf<Int>()) }
    val buttonColors = buttonColors(palette)

    fun onCellTap(index: Int) {
        when (val result = holder.flip(index)) {
            is MemoryGame.FlipResult.First -> {
                revealed = revealed + index
                if (hintVisible) { onFirstFlip(); hintVisible = false }
            }
            is MemoryGame.FlipResult.Matched -> {
                revealed = revealed + result.first + result.second
                onMatchFeedback()
                if (holder.game.isCleared) onCleared()
                onMoveEnd()
            }
            is MemoryGame.FlipResult.Mismatched -> {
                revealed = revealed + result.first + result.second
                onMismatchFeedback()
                scope.launch {
                    delay(700)
                    holder.hideMismatch(result.first, result.second)
                    revealed = revealed - result.first - result.second
                    onMoveEnd()
                }
            }
            MemoryGame.FlipResult.Ignored -> {}
        }
    }

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
            Text("Memory Match", color = Color(palette.text), modifier = Modifier.padding(start = 12.dp))
            Spacer(modifier = Modifier.weight(1f))
            SquareIconButton(label = "?", palette = palette, onClick = onHelp)
        }

        Text(
            if (holder.game.isCleared) "Cleared! All pairs found."
            else "${holder.game.matchedPairs} of ${holder.game.faces.size / 2} pairs found",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(palette.textDim),
            modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally),
        )
        if (hintVisible) {
            Text(
                "Flip two cards to find matching pairs",
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
                repeat(4) { row ->
                    Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        repeat(4) { col ->
                            val index = row * 4 + col
                            val open = holder.game.states[index] == MemoryGame.CardState.MATCHED ||
                                index in revealed
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                                    .background(
                                        Color(if (open) palette.primary else palette.surfaceDim),
                                        RoundedCornerShape(10.dp),
                                    )
                                    .clickable { onCellTap(index) },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (open) {
                                    Text(
                                        holder.game.faces[index],
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = Color(palette.text),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = {
                onNewGame()
                revealed = emptySet()
            }, colors = buttonColors) {
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
private fun ResultCard(
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
