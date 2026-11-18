package com.micro.gameportal

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView

/** Memory match: 4x4 grid, eight emoji pairs, flip two at a time. */
class MemoryActivity : Activity() {

    private companion object {
        const val FLIP_BACK_MS = 700L
        const val STATE_FACES = "faces"
        const val STATE_MATCHED = "matched"
    }

    private lateinit var game: MemoryGame
    private lateinit var cells: Array<Button>
    private lateinit var status: TextView
    private var hintView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.setupEnter(this)
        game = MemoryGame()
        val scaffold = Ui.gameScaffold(this)
        val root = scaffold.root
        scaffold.controls.addView(Ui.topBar(this, "Memory Match", { Ui.back(this) }, { GameHelp.show(this, "memory") }))
        status = Ui.status(this, "Find all pairs!")
        scaffold.controls.addView(status)
        if (!GameSettings(this).hintSeen("memory")) {
            hintView = Ui.hint(this, "Flip two cards to find matching pairs")
            scaffold.controls.addView(hintView)
        }
        cells = Array(game.faces.size) { Button(this) }
        val grid = SquareGridLayout(this).apply {
            columnCount = 4
            cells.forEachIndexed { index, button ->
                button.textSize = 24f
                button.setTextColor(Ui.theme(this@MemoryActivity).text)
                Ui.cell(button, Ui.theme(this@MemoryActivity).surfaceDim)
                button.setOnClickListener { onCellTap(index) }
                addView(button, cellParams(index / 4, index % 4, 4))
            }
        }
        scaffold.board.addView(grid, SquareGridLayout.fillLayoutParams())
        setContentView(root)
        restoreOrStart(savedInstanceState)
        if (savedInstanceState == null) {
            val autosave = Autosave.prefs(this, "memory")
            if (autosave.contains("faces") && !autosave.getBoolean("cleared", false)) {
                val faces = autosave.getString("faces", "")!!.split("\u0001")
                val matched = autosave.getString("matched", "")!!.map { it == '1' }.toBooleanArray()
                if (faces.size == game.faces.size) game.restoreDeck(faces, matched)
            }
        }
        render()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArray(STATE_FACES, game.faces.toTypedArray())
        outState.putBooleanArray(STATE_MATCHED, game.matchedMask())
    }

    override fun onPause() {
        super.onPause()
        if (game.isCleared) {
            Autosave.clear(this, "memory")
        } else {
            Autosave.prefs(this, "memory").edit()
                .putString("faces", game.faces.joinToString("\u0001"))
                .putString("matched", game.matchedMask().joinToString("") { if (it) "1" else "0" })
                .putBoolean("cleared", false)
                .apply()
        }
    }

    private fun restoreOrStart(saved: Bundle?) {
        val faces = saved?.getStringArray(STATE_FACES)
        if (faces != null) {
            val matched = saved.getBooleanArray(STATE_MATCHED) ?: BooleanArray(faces.size)
            game.restoreDeck(faces.toList(), matched)
        }
    }

    private fun onCellTap(index: Int) {
        when (val result = game.flip(index)) {
            is MemoryGame.FlipResult.First -> {
                cells[index].text = game.faces[index]
                hintView?.let {
                    GameSettings(this).markHintSeen("memory")
                    it.visibility = View.GONE
                    hintView = null
                }
            }
            is MemoryGame.FlipResult.Matched -> {
                cells[result.first].text = game.faces[result.first]
                cells[result.second].text = game.faces[result.second]
                Feedback.play(this, Feedback.Sound.MATCH)
                Feedback.haptic(cells[result.second])
                if (game.isCleared) {
                    Feedback.play(this, Feedback.Sound.SUCCESS)
                    GameStats.recordEnd(this, "memory", won = true)
                    status.text = "Cleared! All pairs found."
                    Autosave.clear(this, "memory")
                    ResultOverlay.show(this, "Cleared!", "All pairs found") { }
                }
            }
            is MemoryGame.FlipResult.Mismatched -> {
                cells[result.first].text = game.faces[result.first]
                cells[result.second].text = game.faces[result.second]
                Feedback.play(this, Feedback.Sound.MISMATCH)
                Feedback.haptic(cells[result.second], Feedback.HapticStrength.LIGHT)
                cells[result.first].postDelayed({
                    game.hideMismatch(result.first, result.second)
                    render()
                }, FLIP_BACK_MS)
            }
            MemoryGame.FlipResult.Ignored -> return
        }
    }

    private fun render() {
        cells.forEachIndexed { index, button ->
            val hidden = game.states[index] == MemoryGame.CardState.HIDDEN
            button.text = if (hidden) "" else game.faces[index]
            Ui.cell(button, if (hidden) Ui.theme(this@MemoryActivity).surfaceDim else Ui.theme(this@MemoryActivity).primary)
        }
    }
}
