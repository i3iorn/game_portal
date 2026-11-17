package com.micro.gameportal

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView

/** Lights Out: tapping a light toggles it and its neighbours; turn them all off. */
class LightsOutActivity : Activity() {

    private companion object {
        const val GRID_MAX_PX = 640
        const val KEY_BEST_MOVES = "best_moves"
        const val STATE_SIZE = "size"
        const val STATE_LIGHTS = "lights"
        const val STATE_MOVES = "moves"
    }

    private lateinit var game: LightsOut
    private lateinit var cells: Array<Button>
    private lateinit var status: TextView
    private val scores by lazy { ScoreStore(this, "lights_out") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val size = savedSize(savedInstanceState) ?: GameSettings(this).lightsOutSize
        game = LightsOut(size = size)
        setContentView(buildContent(size))
        restoreOrStart(savedInstanceState)
        render()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_SIZE, game.size)
        outState.putBooleanArray(STATE_LIGHTS, game.lights)
        outState.putInt(STATE_MOVES, game.moves)
    }

    private fun savedSize(saved: Bundle?): Int? = saved?.getInt(STATE_SIZE, -1)?.takeIf { it > 0 }

    private fun bestKey(size: Int) = "${KEY_BEST_MOVES}_$size"

    private fun buildContent(size: Int): android.view.View {
        val cell = (GRID_MAX_PX / size).coerceIn(56, 165)
        val root = Ui.gameRoot(this)
        status = Ui.status(this)
        Ui.addCentered(root, status)
        cells = Array(size * size) { Button(this) }
        val grid = GridLayout(this).apply {
            columnCount = size
            cells.forEachIndexed { index, button ->
                button.textSize = 0f
                button.setOnClickListener { onCellTap(index) }
                addView(
                    button,
                    GridLayout.LayoutParams().apply {
                        width = cell; height = cell
                        setMargins(4, 4, 4, 4)
                    },
                )
            }
        }
        Ui.addCentered(root, grid)
        Ui.addCentered(root, Ui.button(this, "New game") {
            game.newGame()
            render()
        })
        return root
    }

    private fun restoreOrStart(saved: Bundle?) {
        val lights = saved?.getBooleanArray(STATE_LIGHTS)
        if (lights != null) game.restore(lights, saved.getInt(STATE_MOVES))
        render()
    }

    private fun onCellTap(index: Int) {
        game.tap(index)
        if (game.isSolved && game.moves > 0) scores.submit(bestKey(game.size), game.moves.toLong(), lowerIsBetter = true)
        render()
    }

    private fun bestScoreText(): String {
        val best = scores.best(bestKey(game.size)) ?: return ""
        return "  Best: ${best.toInt()} moves"
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
