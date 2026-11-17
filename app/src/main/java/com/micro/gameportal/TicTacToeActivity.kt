package com.micro.gameportal

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/** Two-player tic-tac-toe on a canvas-drawn board. */
class TicTacToeActivity : Activity() {

    private lateinit var game: TicTacToeGame
    private lateinit var board: TicTacToeBoardView
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }
        status = TextView(this).apply { textSize = 18f; setPadding(0, 24, 0, 24); setTextColor(Color.DKGRAY) }
        board = TicTacToeBoardView(this)
        board.onCellTapped = ::onCellTapped
        root.addView(status)
        root.addView(
            board,
            LinearLayout.LayoutParams(900, 900),
        )
        root.addView(newGameButton())
        setContentView(root)
        newGame()
    }

    private fun onCellTapped(index: Int) {
        if (!game.play(index)) return
        board.invalidate()
        when {
            game.winner != TicTacToeGame.EMPTY ->
                status.text = if (game.winner == TicTacToeGame.X) "X wins!" else "O wins!"
            game.isFull -> status.text = "Draw — board is full."
            else -> status.text = if (game.current == TicTacToeGame.X) "X to move" else "O to move"
        }
    }

    private fun newGameButton(): Button = Button(this).apply {
        text = "New game"
        setOnClickListener { newGame() }
    }

    private fun newGame() {
        game = TicTacToeGame()
        board.game = game
        status.text = "X to move"
    }
}
