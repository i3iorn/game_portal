package com.micro.gameportal

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.GridLayout
import android.widget.TextView

/** 2048: swipe to slide and merge tiles. Score and best score shown live. */
class Game2048Activity : Activity() {

    private companion object {
        const val STATE_BOARD = "board"
        const val STATE_SCORE = "score"
        const val STATE_STATUS = "status"
        const val KEY_BEST_SCORE = "best_score_v2"
    }

    private lateinit var game: Game2048
    private lateinit var tiles: Array<TextView>
    private lateinit var status: TextView
    private val scores by lazy { ScoreStore(this, "game2048") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        game = Game2048()
        val root = Ui.gameRoot(this)
        status = Ui.status(this)
        val grid = buildGrid()
        grid.setOnTouchListener(SwipeListener { direction -> onSwipe(direction) })
        root.addView(status)
        Ui.addCentered(root, grid)
        root.addView(Ui.button(this, "New game") {
            game.newGame()
            render()
        })
        setContentView(root)
        restoreOrStart(savedInstanceState)
        render()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray(STATE_BOARD, game.board)
        outState.putInt(STATE_SCORE, game.score)
        outState.putInt(STATE_STATUS, game.status)
    }

    private fun restoreOrStart(saved: Bundle?) {
        val board = saved?.getIntArray(STATE_BOARD)
        if (board != null) {
            game.restore(board, saved.getInt(STATE_SCORE), saved.getInt(STATE_STATUS), board.any { it >= Game2048.WIN_TILE })
        }
    }

    private fun buildGrid(): GridLayout {
        tiles = Array(Game2048.CELLS) { TextView(this) }
        return GridLayout(this).apply {
            columnCount = Game2048.SIZE
            tiles.forEach { tile ->
                tile.textSize = 22f
                tile.gravity = Gravity.CENTER
                addView(
                    tile,
                    GridLayout.LayoutParams().apply {
                        width = 165; height = 165
                        setMargins(4, 4, 4, 4)
                    },
                )
            }
        }
    }

    private fun onSwipe(direction: Game2048.Direction) {
        if (!game.move(direction)) return
        scores.submit(KEY_BEST_SCORE, game.score.toLong(), lowerIsBetter = false)
        render()
    }

    private fun bestScore(): Long = scores.best(KEY_BEST_SCORE) ?: 0L

    private fun render() {
        tiles.forEachIndexed { index, tile ->
            val v = game.board[index]
            tile.text = if (v == 0) "" else v.toString()
            tile.setBackgroundColor(if (v == 0) 0xFF3A3226.toInt() else tileColor(v))
            tile.setTextColor(if (v >= 8) Color.WHITE else Color.DKGRAY)
        }
        status.text = when {
            game.status == Game2048.LOST -> "No moves left! Score: ${game.score}  (best ${bestScore()})"
            else -> "Score: ${game.score}  (best ${bestScore()})" +
                if (game.status == Game2048.WIN) "  — 2048! Keep going" else ""
        }
    }

    private fun tileColor(v: Int): Int = when {
        v >= 1024 -> 0xFFFFB300.toInt()
        v >= 512 -> 0xFFFB8C00.toInt()
        v >= 256 -> 0xFFF4511E.toInt()
        v >= 128 -> 0xFFE53935.toInt()
        v >= 64 -> 0xFF8E24AA.toInt()
        v >= 32 -> 0xFF3949AB.toInt()
        v >= 16 -> 0xFF1E88E5.toInt()
        v >= 8 -> 0xFF00897B.toInt()
        v >= 4 -> 0xFF7CB342.toInt()
        else -> 0xFFC0CA33.toInt()
    }
}

/** Translates swipes on any view into [Game2048.Direction] callbacks. */
class SwipeListener(private val onSwipe: (Game2048.Direction) -> Unit) : View.OnTouchListener {

    private companion object {
        const val SWIPE_MIN_PX = 60f
    }

    private var downX = 0f
    private var downY = 0f

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> { downX = event.x; downY = event.y }
            MotionEvent.ACTION_UP -> {
                val dx = event.x - downX
                val dy = event.y - downY
                when {
                    maxOf(kotlin.math.abs(dx), kotlin.math.abs(dy)) < SWIPE_MIN_PX -> return false
                    kotlin.math.abs(dx) > kotlin.math.abs(dy) ->
                        onSwipe(if (dx > 0) Game2048.Direction.RIGHT else Game2048.Direction.LEFT)
                    else -> onSwipe(if (dy > 0) Game2048.Direction.DOWN else Game2048.Direction.UP)
                }
                view.performClick()
            }
        }
        return true
    }
}
