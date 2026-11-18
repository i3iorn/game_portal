package com.micro.gameportal

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

/** Connect Four: tap a column to drop your disc; connect four to win. */
class ConnectFourActivity : Activity() {

    private lateinit var game: ConnectFourGame
    private lateinit var board: ConnectFourBoardView
    private lateinit var status: TextView
    private var hintView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.setupEnter(this)
        game = ConnectFourGame()
        val scaffold = Ui.gameScaffold(this)
        val root = scaffold.root
        scaffold.controls.addView(
            Ui.topBar(this, "Connect Four", { Ui.back(this) }, { GameHelp.show(this, "connect4") }),
        )
        status = Ui.status(this)
        scaffold.controls.addView(status)
        if (!GameSettings(this).hintSeen("connect4")) {
            hintView = Ui.hint(this, "Tap a column to drop your disc")
            scaffold.controls.addView(hintView)
        }
        board = ConnectFourBoardView(this).apply {
            onColumnTapped = ::onColumnTapped
        }
        scaffold.board.addView(board, SquareGridLayout.fillLayoutParams())
        scaffold.controls.addView(
            Ui.button(this, "New game") {
                Autosave.clear(this, "connect4")
                game.newGame()
                board.game = game
                render()
            },
        )
        setContentView(root)
        restoreOrStart(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray(STATE_CELLS, game.cells)
        outState.putInt(STATE_CURRENT, game.current)
    }

    override fun onPause() {
        super.onPause()
        if (game.isOver) {
            Autosave.clear(this, "connect4")
        } else {
            Autosave.prefs(this, "connect4").edit()
                .putString("cells", game.cells.joinToString(","))
                .putInt("current", game.current)
                .apply()
        }
    }

    private companion object {
        const val STATE_CELLS = "cells"
        const val STATE_CURRENT = "current"
    }

    private fun restoreOrStart(saved: Bundle?) {
        val cells = saved?.getIntArray(STATE_CELLS)
        if (cells != null) {
            game.restore(cells, saved.getInt(STATE_CURRENT))
        } else if (saved == null && Autosave.exists(this, "connect4", "cells")) {
            val autosave = Autosave.prefs(this, "connect4")
            game.restore(
                autosave.getString("cells", "")!!.split(",").map { it.toInt() }.toIntArray(),
                autosave.getInt("current", ConnectFourGame.RED),
            )
        }
        board.game = game
        render()
    }

    private fun onColumnTapped(col: Int) {
        hintView?.let {
            GameSettings(this).markHintSeen("connect4")
            it.visibility = View.GONE
            hintView = null
        }
        if (!game.drop(col)) return
        Feedback.haptic(board, Feedback.HapticStrength.LIGHT)
        if (game.winner != ConnectFourGame.EMPTY) {
            Feedback.play(this, Feedback.Sound.SUCCESS)
            Feedback.haptic(board)
        }
        board.game = game
        board.invalidate()
        render()
        if (game.isOver) {
            GameStats.recordEnd(this, "connect4", won = game.winner == ConnectFourGame.RED)
            Autosave.clear(this, "connect4")
            ResultOverlay.show(
                this,
                when {
                    game.winner == ConnectFourGame.RED -> "RED wins!"
                    game.winner == ConnectFourGame.YELLOW -> "YELLOW wins!"
                    else -> "Draw"
                },
                "Tap New game below for a rematch",
            ) { }
        }
    }

    private fun render() {
        status.text = when {
            game.winner != ConnectFourGame.EMPTY ->
                if (game.winner == ConnectFourGame.RED) "RED wins!" else "YELLOW wins!"
            game.isFull -> "Draw — board is full."
            else -> if (game.current == ConnectFourGame.RED) "RED to move" else "YELLOW to move"
        }
    }
}
