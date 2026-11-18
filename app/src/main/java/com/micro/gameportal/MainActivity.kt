package com.micro.gameportal

import android.app.Activity
import android.content.Intent
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

private data class GameSpec(
    val title: String,
    val key: String,
    val target: Class<out Activity>,
    val draw: (android.graphics.Canvas, Float) -> Unit,
)

private val games = listOf(
    GameSpec("Tic-Tac-Toe", "ttt", TicTacToeActivity::class.java, GamePreviewDraw::ticTacToe),
    GameSpec("15 Puzzle", "puzzle", SlidingPuzzleActivity::class.java, GamePreviewDraw::puzzle),
    GameSpec("Memory Match", "memory", MemoryActivity::class.java, GamePreviewDraw::memory),
    GameSpec("2048", "2048", Game2048Activity::class.java, GamePreviewDraw::game2048),
    GameSpec("Minesweeper", "mines", MinefieldActivity::class.java, GamePreviewDraw::minesweeper),
    GameSpec("Lights Out", "lights", LightsOutActivity::class.java, GamePreviewDraw::lightsOut),
    GameSpec("Connect Four", "connect4", ConnectFourActivity::class.java, GamePreviewDraw::connectFour),
)

/** Home screen listing the games, rebuilt in Compose (12 migration step 2). */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.setupEnter(this)
        setContent {
            MaterialTheme(colorScheme = Palette.current(this).toColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeContent(
                        activity = this,
                        onOpenSettings = { startActivity(Intent(this, SettingsActivity::class.java)) },
                        onOpen = { startActivity(Intent(this, it)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    activity: Activity,
    onOpenSettings: () -> Unit,
    onOpen: (Class<out Activity>) -> Unit,
) {
    // Re-read stats whenever we come back to the foreground.
    var refresh by remember { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refresh++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val palette = Palette.current(activity)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(palette.background))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Game Portal",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(palette.text),
            modifier = Modifier.padding(vertical = 24.dp),
        )
        games.forEach { spec ->
            GameCard(
                title = spec.title,
                key = spec.key,
                draw = spec.draw,
                stats = remember(refresh) { GameStats.summary(activity, spec.key) },
                palette = palette,
                onClick = { onOpen(spec.target) },
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onOpenSettings,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(palette.surfaceDim),
                contentColor = Color(palette.textDim),
            ),
        ) {
            Text("Settings")
        }
    }
}

@Composable
private fun GameCard(
    title: String,
    key: String,
    draw: (android.graphics.Canvas, Float) -> Unit,
    stats: String?,
    palette: Palette,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
            .background(Color(palette.surface), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(12.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AndroidView(
            factory = { context -> GamePreviewView(context) { canvas, s -> draw(canvas, s) } },
            modifier = Modifier.size(80.dp),
        )
        Spacer(modifier = Modifier.size(12.dp))
        Column(verticalArrangement = Arrangement.Center) {
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(palette.primary),
                    contentColor = Color(palette.text),
                ),
            ) {
                Text(title.uppercase())
            }
            stats?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(palette.textDim),
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        }
    }
}
