package com.micro.gameportal

import android.app.Activity
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/** Home screen listing the available puzzle games, each with a small drawn preview. */
class MainActivity : Activity() {

    private data class GameEntry(
        val title: String,
        val target: Class<out Activity>,
        val preview: () -> View,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }
        root.addView(titleView())
        listOf(
            GameEntry("Tic-Tac-Toe", TicTacToeActivity::class.java, ::ticTacToePreview),
            GameEntry("15 Puzzle", SlidingPuzzleActivity::class.java, ::puzzlePreview),
            GameEntry("Memory Match", MemoryActivity::class.java, ::memoryPreview),
        ).forEach { root.addEntryFor(it) }
        setContentView(root)
    }

    private fun titleView(): TextView = TextView(this).apply {
        text = "Game Portal"
        textSize = 28f
        gravity = Gravity.CENTER
        setPadding(0, 48, 0, 64)
    }

    private fun LinearLayout.addEntryFor(entry: GameEntry) {
        val row = LinearLayout(this@MainActivity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        row.addView(entry.preview(), 160, 160)
        val button = Button(this@MainActivity).apply {
            text = entry.title
            setOnClickListener { startActivity(Intent(context, entry.target)) }
        }
        row.addView(button, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { setMargins(32, 16, 64, 16) })
        addView(row, MATCH_PARENT, WRAP_CONTENT)
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

    private companion object {
        val GRAY = 0xFF5B6770.toInt()
        val BLUE = 0xFF1E88E5.toInt()
        val RED = 0xFFE53935.toInt()
        val GREEN = 0xFF43A047.toInt()
    }
}
