package com.micro.gameportal

import android.app.Activity
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        game = MemoryGame()
        val root = Ui.gameRoot(this)
        status = Ui.status(this, "Find all pairs!")
        Ui.addCentered(root, status)
        cells = Array(game.faces.size) { Button(this) }
        val grid = GridLayout(this).apply {
            columnCount = 4
            cells.forEachIndexed { index, button ->
                button.textSize = 22f
                button.setOnClickListener { onCellTap(index) }
                addView(
                    button,
                    GridLayout.LayoutParams().apply {
                        width = 170; height = 170
                        setMargins(4, 4, 4, 4)
                    },
                )
            }
        }
        Ui.addCentered(root, grid)
        setContentView(root)
        restoreOrStart(savedInstanceState)
        render()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArray(STATE_FACES, game.faces.toTypedArray())
        outState.putBooleanArray(STATE_MATCHED, game.matchedMask())
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
            is MemoryGame.FlipResult.First -> cells[index].text = game.faces[index]
            is MemoryGame.FlipResult.Matched -> {
                cells[result.first].text = game.faces[result.first]
                cells[result.second].text = game.faces[result.second]
                if (game.isCleared) status.text = "Cleared! All pairs found."
            }
            is MemoryGame.FlipResult.Mismatched -> {
                cells[result.first].text = game.faces[result.first]
                cells[result.second].text = game.faces[result.second]
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
            button.text = if (game.states[index] == MemoryGame.CardState.HIDDEN) "" else game.faces[index]
        }
    }
}
