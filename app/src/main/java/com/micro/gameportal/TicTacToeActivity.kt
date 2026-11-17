package com.micro.gameportal

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/** Tic-tac-toe: two players locally, or a built-in opponent at three strengths. */
class TicTacToeActivity : Activity() {

    private companion object {
        const val AI_DELAY_MS = 350L
        const val STATE_CELLS = "cells"
        const val STATE_CURRENT = "current"
        const val STATE_MODE = "mode"
    }

    private lateinit var game: TicTacToeGame
    private lateinit var board: TicTacToeBoardView
    private lateinit var status: TextView
    private lateinit var opponentButton: Button

    private var mode = TicTacToeAi.Difficulty.HUMAN
    private var pendingAi: Runnable? = null

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
        root.addView(board, LinearLayout.LayoutParams(900, 900))
        root.addView(opponentButton())
        root.addView(newGameButton())
        setContentView(root)
        restoreOrStart(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray(STATE_CELLS, game.cells)
        outState.putInt(STATE_CURRENT, game.current)
        outState.putInt(STATE_MODE, mode.ordinal)
    }

    private fun restoreOrStart(saved: Bundle?) {
        mode = TicTacToeAi.Difficulty.entries.getOrElse(saved?.getInt(STATE_MODE, 0) ?: 0) { TicTacToeAi.Difficulty.HUMAN }
        opponentButton.text = "Opponent: ${modeLabel()}"
        game = TicTacToeGame()
        val cells = saved?.getIntArray(STATE_CELLS)
        if (cells != null) {
            game.restore(cells, saved?.getInt(STATE_CURRENT) ?: TicTacToeGame.X)
        }
        board.game = game
        render()
        scheduleAiMove()
    }

    private fun opponentButton(): Button {
        opponentButton = Button(this)
        opponentButton.setOnClickListener { cycleOpponent() }
        return opponentButton
    }

    private fun newGameButton(): Button = Button(this).apply {
        text = "New game"
        setOnClickListener {
            game.reset()
            board.game = game
            render()
            scheduleAiMove()
        }
    }

    private fun cycleOpponent() {
        val next = TicTacToeAi.Difficulty.entries[(mode.ordinal + 1) % TicTacToeAi.Difficulty.entries.size]
        mode = next
        opponentButton.text = "Opponent: ${modeLabel()}"
        game.reset()
        board.game = game
        render()
        scheduleAiMove()
    }

    private fun modeLabel(): String = when (mode) {
        TicTacToeAi.Difficulty.HUMAN -> "2 players"
        TicTacToeAi.Difficulty.EASY -> "Easy"
        TicTacToeAi.Difficulty.MEDIUM -> "Medium"
        TicTacToeAi.Difficulty.HARD -> "Hard"
    }

    private fun onCellTapped(index: Int) {
        if (mode != TicTacToeAi.Difficulty.HUMAN && game.current == TicTacToeGame.O) return
        if (!game.play(index)) return
        board.invalidate()
        render()
        scheduleAiMove()
    }

    /** Queues the AI's move (non-HUMAN modes play O), cancelling any pending one. */
    private fun scheduleAiMove() {
        pendingAi?.let { board.removeCallbacks(it) }
        pendingAi = null
        if (mode == TicTacToeAi.Difficulty.HUMAN || game.winner != TicTacToeGame.EMPTY || game.isFull) return
        val runnable = Runnable {
            pendingAi = null
            // Only ever move as O, and only if the position is still live (guards
            // against stale callbacks firing after a reset or a human move).
            if (mode == TicTacToeAi.Difficulty.HUMAN || game.current != TicTacToeGame.O) return@Runnable
            if (game.winner != TicTacToeGame.EMPTY || game.isFull) return@Runnable
            val move = TicTacToeAi(game, mode).bestMove()
            if (move >= 0 && game.play(move)) {
                board.invalidate()
                render()
            }
        }
        pendingAi = runnable
        board.postDelayed(runnable, AI_DELAY_MS)
    }

    private fun render() {
        status.text = when {
            game.winner != TicTacToeGame.EMPTY ->
                if (game.winner == TicTacToeGame.X) "X wins!" else "O wins!"
            game.isFull -> "Draw — board is full."
            else -> if (game.current == TicTacToeGame.X) "X to move" else "O to move"
        }
    }
}
