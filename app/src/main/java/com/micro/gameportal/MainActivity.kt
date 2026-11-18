package com.micro.gameportal

import android.app.Activity
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.TextView

/** Home screen listing the available puzzle games, each with a small drawn preview. */
class MainActivity : Activity() {

    private data class GameEntry(
        val title: String,
        val key: String,
        val target: Class<out Activity>,
        val preview: () -> View,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = Ui.gameRoot(this)
        root.addView(Ui.title(this, "Game Portal"))
        listOf(
            GameEntry("Tic-Tac-Toe", "ttt", TicTacToeActivity::class.java, ::ticTacToePreview),
            GameEntry("15 Puzzle", "puzzle", SlidingPuzzleActivity::class.java, ::puzzlePreview),
            GameEntry("Memory Match", "memory", MemoryActivity::class.java, ::memoryPreview),
            GameEntry("2048", "2048", Game2048Activity::class.java, ::game2048Preview),
            GameEntry("Minesweeper", "mines", MinefieldActivity::class.java, ::minefieldPreview),
            GameEntry("Lights Out", "lights", LightsOutActivity::class.java, ::lightsOutPreview),
            GameEntry("Connect Four", "connect4", ConnectFourActivity::class.java, ::connectFourPreview),
        ).forEach { entry ->
            Ui.addCentered(root, buildEntryRow(entry))
        }
        Ui.addCentered(root, Ui.button(this, "Settings", size = Ui.ButtonSize.COMPACT, quiet = true) {
            startActivity(Intent(this, SettingsActivity::class.java))
        })
        setContentView(root)
    }

    private fun buildEntryRow(entry: GameEntry): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }
        row.addView(entry.preview(), 160, 160)
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }
        column.addView(Ui.button(this, entry.title) { startActivity(Intent(this@MainActivity, entry.target)) })
        GameStats.summary(this, entry.key)?.let { summary ->
            column.addView(
                TextView(this).apply {
                    text = summary
                    textSize = 11f
                    setTextColor(Ui.theme(this@MainActivity).textDim)
                    setPadding(Ui.dp(this@MainActivity, 12f), 0, 0, 0)
                },
            )
        }
        row.addView(column)
        row.setPadding(0, 0, 0, 0)
        return Ui.card(this, row)
    }

    private fun ticTacToePreview(): View = GamePreviewView(this) { canvas, s ->
        val line = Paint().apply {
            color = GRAY; strokeWidth = s * 0.04f; style = Paint.Style.STROKE
        }
        canvas.drawLine(s * 0.5f, s * 0.15f, s * 0.5f, s * 0.85f, line)
        canvas.drawLine(s * 0.15f, s * 0.5f, s * 0.85f, s * 0.5f, line)
        val x = Paint().apply {
            color = BLUE; strokeWidth = s * 0.07f; style = Paint.Style.STROKE; isAntiAlias = true
        }
        canvas.drawLine(s * 0.62f, s * 0.62f, s * 0.82f, s * 0.82f, x)
        canvas.drawLine(s * 0.82f, s * 0.62f, s * 0.62f, s * 0.82f, x)
        canvas.drawCircle(s * 0.27f, s * 0.27f, s * 0.11f, x.apply { color = RED })
    }

    private fun puzzlePreview(): View = GamePreviewView(this) { canvas, s ->
        val paint = Paint().apply {
            color = GRAY; strokeWidth = s * 0.03f; style = Paint.Style.STROKE
        }
        (1..2).forEach { i ->
            canvas.drawLine(s * 0.2f + i * s * 0.2f, s * 0.2f, s * 0.2f + i * s * 0.2f, s * 0.8f, paint)
            canvas.drawLine(s * 0.2f, s * 0.2f + i * s * 0.2f, s * 0.8f, s * 0.2f + i * s * 0.2f, paint)
        }
        val tile = Paint().apply { color = BLUE }
        canvas.drawRect(s * 0.2f, s * 0.6f, s * 0.4f, s * 0.8f, tile)
    }

    private fun memoryPreview(): View = GamePreviewView(this) { canvas, s ->
        val paint = Paint().apply {
            color = GRAY; strokeWidth = s * 0.03f; style = Paint.Style.STROKE
        }
        val tile = s * 0.2f
        (0..1).forEach { r ->
            (0..1).forEach { c ->
                val l = s * 0.1f + c * tile * 1.2f
                val t = s * 0.25f + r * tile * 1.2f
                canvas.drawRect(l, t, l + tile, t + tile, paint)
            }
        }
        val card = Paint().apply { color = GREEN }
        canvas.drawRect(s * 0.1f, s * 0.25f, s * 0.3f, s * 0.45f, card)
    }

    private fun game2048Preview(): View = GamePreviewView(this) { canvas, s ->
        val paint = Paint().apply { color = GRAY; strokeWidth = s * 0.02f; style = Paint.Style.STROKE }
        (1..3).forEach { i ->
            canvas.drawLine(s * 0.15f + i * s * 0.175f, s * 0.15f, s * 0.15f + i * s * 0.175f, s * 0.85f, paint)
            canvas.drawLine(s * 0.15f, s * 0.15f + i * s * 0.175f, s * 0.85f, s * 0.15f + i * s * 0.175f, paint)
        }
        canvas.drawRect(s * 0.15f, s * 0.675f, s * 0.325f, s * 0.85f, Paint().apply { color = ORANGE })
        canvas.drawRect(s * 0.5f, s * 0.15f, s * 0.675f, s * 0.325f, Paint().apply { color = BLUE })
    }

    private fun minefieldPreview(): View = GamePreviewView(this) { canvas, s ->
        val paint = Paint().apply { color = GRAY; strokeWidth = s * 0.03f; style = Paint.Style.STROKE }
        (1..2).forEach { i ->
            canvas.drawLine(s * 0.2f + i * s * 0.2f, s * 0.2f, s * 0.2f + i * s * 0.2f, s * 0.8f, paint)
            canvas.drawLine(s * 0.2f, s * 0.2f + i * s * 0.2f, s * 0.8f, s * 0.2f + i * s * 0.2f, paint)
        }
        canvas.drawCircle(s * 0.7f, s * 0.7f, s * 0.07f, Paint().apply { color = RED })
        canvas.drawCircle(s * 0.3f, s * 0.3f, s * 0.07f, Paint().apply { color = GREEN })
    }

    private fun lightsOutPreview(): View = GamePreviewView(this) { canvas, s ->
        val off = Paint().apply { color = GRAY; strokeWidth = s * 0.03f; style = Paint.Style.STROKE }
        val on = Paint().apply { color = 0xFFFFD54F.toInt() }
        val cell = s * 0.16f
        (0..3).forEach { r ->
            (0..3).forEach { c ->
                val l = s * 0.18f + c * cell * 1.1f
                val t = s * 0.18f + r * cell * 1.1f
                if ((r + c) % 3 == 0) canvas.drawRect(l, t, l + cell, t + cell, on)
                else canvas.drawRect(l, t, l + cell, t + cell, off)
            }
        }
    }

    private fun connectFourPreview(): View = GamePreviewView(this) { canvas, s ->
        val paint = Paint().apply { color = GRAY; strokeWidth = s * 0.03f; style = Paint.Style.STROKE }
        (1..2).forEach { i ->
            canvas.drawLine(s * 0.2f + i * s * 0.2f, s * 0.2f, s * 0.2f + i * s * 0.2f, s * 0.8f, paint)
            canvas.drawLine(s * 0.2f, s * 0.2f + i * s * 0.2f, s * 0.8f, s * 0.2f + i * s * 0.2f, paint)
        }
        canvas.drawCircle(s * 0.3f, s * 0.7f, s * 0.07f, Paint().apply { color = RED })
        canvas.drawCircle(s * 0.7f, s * 0.5f, s * 0.07f, Paint().apply { color = 0xFFFFD54F.toInt() })
    }

    private companion object {
        val GRAY = 0xFF5B6770.toInt()
        val BLUE = 0xFF1E88E5.toInt()
        val RED = 0xFFE53935.toInt()
        val GREEN = 0xFF43A047.toInt()
        val ORANGE = 0xFFFB8C00.toInt()
    }
}
