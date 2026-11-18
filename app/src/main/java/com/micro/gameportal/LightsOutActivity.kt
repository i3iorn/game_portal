package com.micro.gameportal

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

/** Lights Out: tapping a light toggles it and its neighbours; turn them all off. */
class LightsOutActivity : Activity() {

    private companion object {
        const val KEY_BEST_MOVES = "best_moves"
        const val STATE_SIZE = "size"
        const val STATE_LIGHTS = "lights"
        const val STATE_MOVES = "moves"
    }

    private lateinit var game: LightsOut
    private lateinit var cells: Array<Button>
    private lateinit var status: TextView
    private var hintView: TextView? = null
    private var autosaveDisabled = false
    private val scores by lazy { ScoreStore(this, "lights_out") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.setupEnter(this)
        val autosave = Autosave.prefs(this, "lights")
        val size = savedSize(savedInstanceState)
            ?: if (autosave.contains("lights")) autosave.getInt("size", GameSettings(this).lightsOutSize)
            else GameSettings(this).lightsOutSize
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

    override fun onPause() {
        super.onPause()
        if (autosaveDisabled) {
            Autosave.clear(this, "lights")
            autosaveDisabled = false
        } else if (!game.isSolved) {
            Autosave.prefs(this, "lights").edit()
                .putInt("size", game.size)
                .putString("lights", game.lights.joinToString("") { if (it) "1" else "0" })
                .putInt("moves", game.moves)
                .apply()
        }
    }

    private fun savedSize(saved: Bundle?): Int? = saved?.getInt(STATE_SIZE, -1)?.takeIf { it > 0 }

    private fun bestKey(size: Int) = "${KEY_BEST_MOVES}_$size"

    private fun nextSize(options: List<Int>, current: Int): Int =
        options[(options.indexOf(current).coerceAtLeast(0) + 1) % options.size]

    private fun buildContent(size: Int): android.view.View {
        val scaffold = Ui.gameScaffold(this)
        val root = scaffold.root
        scaffold.controls.addView(
            Ui.topBar(this, "Lights Out", { Ui.back(this) }, { GameHelp.show(this, "lights") }),
        )
        status = Ui.status(this)
        scaffold.controls.addView(status)
        if (!GameSettings(this).hintSeen("lights")) {
            hintView = Ui.hint(this, "Tap a light to toggle it and its neighbours")
            scaffold.controls.addView(hintView)
        }
        cells = Array(size * size) { Button(this) }
        val grid = SquareGridLayout(this).apply {
            columnCount = size
            cells.forEachIndexed { index, button ->
                button.textSize = 0f
                button.setOnClickListener { onCellTap(index) }
                addView(button, cellParams(index / size, index % size, 4))
            }
        }
        scaffold.board.addView(grid, SquareGridLayout.fillLayoutParams())
        Ui.addCentered(
            scaffold.controls,
            Ui.button(this, "Size: ${size}x${size}", size = Ui.ButtonSize.COMPACT, quiet = true) {
                GameSettings(this).lightsOutSize = nextSize(GameSettings.LIGHTS_OUT_SIZES, game.size)
                autosaveDisabled = true
                recreate()
            },
        )
        Ui.addCentered(scaffold.controls, Ui.button(this, "New game") {
            Autosave.clear(this, "lights")
            game.newGame()
            render()
        })
        return root
    }

    private fun restoreOrStart(saved: Bundle?) {
        val lights = saved?.getBooleanArray(STATE_LIGHTS)
        if (lights != null) {
            game.restore(lights, saved.getInt(STATE_MOVES))
        } else if (saved == null && Autosave.exists(this, "lights", "lights")) {
            val autosave = Autosave.prefs(this, "lights")
            val lights = autosave.getString("lights", "")!!.map { it == '1' }.toBooleanArray()
            game.restore(lights, autosave.getInt("moves", 0))
        }
        render()
    }

    private fun onCellTap(index: Int) {
        game.tap(index)
        hintView?.let {
            GameSettings(this).markHintSeen("lights")
            it.visibility = View.GONE
            hintView = null
        }
        if (game.isSolved && game.moves > 0) {
            Feedback.haptic(cells[index])
            Feedback.play(this, Feedback.Sound.SUCCESS)
            scores.submit(bestKey(game.size), game.moves.toLong(), lowerIsBetter = true)
            GameStats.recordEnd(this, "lights", won = true)
            Autosave.clear(this, "lights")
            ResultOverlay.show(this, "Solved!", "${game.moves} moves") {
                game.newGame()
                render()
            }
        } else {
            Feedback.haptic(cells[index], Feedback.HapticStrength.LIGHT)
            Autosave.prefs(this, "lights").edit()
                .putInt("size", game.size)
                .putString("lights", game.lights.joinToString("") { if (it) "1" else "0" })
                .putInt("moves", game.moves)
                .apply()
        }
        render()
    }

    private fun bestScoreText(): String {
        val best = scores.best(bestKey(game.size)) ?: return ""
        return "  Best: ${best.toInt()} moves"
    }

    private fun render() {
        cells.forEachIndexed { index, button ->
            val palette = Ui.theme(this@LightsOutActivity)
            Ui.cell(button, if (game.lights[index]) palette.accent else palette.surfaceDim, radiusDp = 10f)
        }
        status.text = when {
            game.isSolved -> "Solved in ${game.moves} moves!${bestScoreText()}"
            else -> "${game.moves} moves — turn all lights off${bestScoreText()}"
        }
    }
}
