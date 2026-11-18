package com.micro.gameportal

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * Canvas-drawn Connect Four board (7x6). Tap a column to drop a piece.
 * The board keeps its 7:6 aspect and sizes to the smaller available dimension.
 */
class ConnectFourBoardView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    companion object {
        private const val RED = 0xFFE53935.toInt()
        private const val YELLOW = 0xFFFFD54F.toInt()
        private const val HOLE = 0xFF32281C.toInt()
        private const val WIN_RING = 0xFFFFFFFF.toInt()
    }

    var onColumnTapped: ((Int) -> Unit)? = null
    var game = ConnectFourGame()
        set(value) {
            field = value
            invalidate()
        }

    private val holePaint = Paint().apply { color = HOLE; isAntiAlias = true }
    private val redPaint = Paint().apply { color = RED; isAntiAlias = true }
    private val yellowPaint = Paint().apply { color = YELLOW; isAntiAlias = true }
    private val winPaint = Paint().apply {
        color = WIN_RING; style = Paint.Style.STROKE; strokeWidth = 8f; isAntiAlias = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val availableWidth = View.MeasureSpec.getSize(widthMeasureSpec)
        val availableHeight = View.MeasureSpec.getSize(heightMeasureSpec)
        val width = minOf(availableWidth, (availableHeight.toFloat() * game.cols / game.rows).toInt())
        val height = width * game.rows / game.cols
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cell = width.toFloat() / game.cols
        val pieceRadius = cell * 0.38f
        game.cells.forEachIndexed { index, player ->
            val col = index % game.cols
            val row = index / game.cols
            val cx = col * cell + cell / 2
            val cy = row * cell + cell / 2
            canvas.drawCircle(cx, cy, cell * 0.44f, holePaint)
            when (player) {
                ConnectFourGame.RED -> canvas.drawCircle(cx, cy, pieceRadius, redPaint)
                ConnectFourGame.YELLOW -> canvas.drawCircle(cx, cy, pieceRadius, yellowPaint)
            }
            if (index in game.winningCells) canvas.drawCircle(cx, cy, pieceRadius, winPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) return false
        val col = (event.x / (width.toFloat() / game.cols)).toInt()
        if (col in 0 until game.cols) onColumnTapped?.invoke(col)
        return true
    }
}
