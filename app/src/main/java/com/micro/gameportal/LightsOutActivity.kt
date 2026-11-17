package com.micro.gameportal

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView

/** Lights Out 5x5: tapping a light toggles it and its neighbours; turn them all off. */
class LightsOutActivity : Activity() {

    private companion object {
        const val PREFS = "lights_out"
        const val KEY_BEST_MOVES = "best_moves"
        const val STATE_LIGHTS = "lights"
        const val STATE_MOVES = "moves"
    }

    private lateinit var game: LightsOut
    private lateinit var cells: Array<Button>
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        game = LightsOut()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }
        status = TextView(this).apply { textSize = 18f; setPadding(0, 24, 0, 24) }
        root.addView(status)
        root.addView(buildGrid())
        root.addView(newGameButton())
        setContentView(root)
        restoreOrStart(savedInstanceState)
        render()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBooleanArray(STATE_LIGHTS, game.lights)
        outState.putInt(STATE_MOVES, game.moves)
    }

    private fun restoreOrStart(saved: Bundle?) {
        val lights = saved?.getBooleanArray(STATE_LIGHTS)
        if (lights != null) game.restore(lights, saved.getInt(STATE_MOVES))
    }

    private fun buildGrid(): View {
        cells = Array(game.lights.size) { Button(this) }
        return GridLayout(this).apply {
            columnCount = game.size
            cells.forEachIndexed { index, button ->
                button.textSize = 0f
                button.setOnClickListener { onCellTap(index) }
                addView(
                    button,
                    GridLayout.LayoutParams().apply {
                        width = 165; height = 165
                        setMargins(4, 4, 4, 4)
                    },
                )
            }
        }
    }

    private fun onCellTap(index: Int) {
        game.tap(index)
        if (game.isSolved && game.moves > 0) saveBestScore()
        render()
    }

    private fun saveBestScore() {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val best = prefs.getInt(KEY_BEST_MOVES, Int.MAX_VALUE)
        if (game.moves < best) prefs.edit().putInt(KEY_BEST_MOVES, game.moves).apply()
    }

    private fun bestScoreText(): String {
        val best = getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_BEST_MOVES, -1)
        if (best < 0) return ""
        return "  Best: $best moves"
    }

    private fun newGameButton(): Button = Button(this).apply {
        text = "New game"
        setOnClickListener {
            game.newGame()
            render()
        }
    }

    private fun render() {
        cells.forEachIndexed { index, button ->
            button.setBackgroundColor(if (game.lights[index]) 0xFFFFD54F.toInt() else 0xFF37301F.toInt())
        }
        status.text = when {
            game.isSolved -> "Solved in ${game.moves} moves!${bestScoreText()}"
            else -> "${game.moves} moves — turn all lights off${bestScoreText()}"
        }
    }
}
