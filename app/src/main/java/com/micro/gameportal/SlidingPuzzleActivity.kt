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
import kotlin.random.Random

/** Classic 4x4 sliding number puzzle. Shuffled via random legal moves so it is always solvable. */
class SlidingPuzzleActivity : Activity() {

    private companion object {
        const val SIZE = 4
        const val EMPTY = SIZE * SIZE
    }

    private lateinit var board: IntArray
    private lateinit var cells: Array<Button>
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        board = (1..SIZE * SIZE).toMutableList().also { it[SIZE * SIZE - 1] = EMPTY }.toIntArray()
        shuffle()
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
        render()
    }

    private fun buildGrid(): View {
        cells = Array(SIZE * SIZE) { Button(this) }
        return GridLayout(this).apply {
            columnCount = SIZE
            cells.forEachIndexed { index, button ->
                button.textSize = 24f
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
    }

    private fun onCellTap(index: Int) {
        val target = index + neighborDelta(index)
        if (target in board.indices && board[target] == EMPTY) {
            board[target] = board[index]
            board[index] = EMPTY
        }
        render()
    }

    private fun neighborDelta(index: Int): Int = when {
        index / SIZE > 0 && board[index - SIZE] == EMPTY -> -SIZE
        index / SIZE < SIZE - 1 && board[index + SIZE] == EMPTY -> SIZE
        index % SIZE > 0 && board[index - 1] == EMPTY -> -1
        index % SIZE < SIZE - 1 && board[index + 1] == EMPTY -> 1
        else -> 0
    }

    private fun shuffle() {
        repeat(200) {
            val empty = board.indexOf(EMPTY)
            val options = mutableListOf(empty - SIZE, empty + SIZE, empty - 1, empty + 1)
            if (empty % SIZE == 0) options.remove(empty - 1)
            if (empty % SIZE == SIZE - 1) options.remove(empty + 1)
            val pick = options.filter { it in board.indices }.random(Random(System.nanoTime()))
            board[empty] = board[pick]; board[pick] = EMPTY
        }
    }

    private fun newGameButton(): Button = Button(this).apply {
        text = "New game"
        setOnClickListener { recreate() }
    }

    private fun render() {
        cells.forEachIndexed { index, button ->
            button.text = board[index].let { if (it == EMPTY) "" else it.toString() }
        }
        status.text = if (solved()) "Solved!" else "Slide the tiles into order."
    }

    private fun solved(): Boolean = board.withIndex().all { (i, v) -> v == EMPTY || v == i + 1 }
}
