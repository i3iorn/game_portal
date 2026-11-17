package com.micro.gameportal

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/** Draws the grid and X/O marks on canvas; forwards taps as cell indices. */
class TicTacToeBoardView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    var onCellTapped: ((Int) -> Unit)? = null

    /** Render as a square using the smaller available dimension. */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val side = minOf(
            View.MeasureSpec.getSize(widthMeasureSpec),
            View.MeasureSpec.getSize(heightMeasureSpec),
        )
        setMeasuredDimension(side, side)
    }
    var game = TicTacToeGame()
        set(value) {
            field = value
            invalidate()
        }

    private val gridPaint = Paint().apply {
        color = 0xFF5B6770.toInt(); strokeWidth = 12f; style = Paint.Style.STROKE
    }
    private val xPaint = paint(0xFF1E88E5.toInt(), 22f)
    private val oPaint = paint(0xFFE53935.toInt(), 22f)
    private val winGlowPaint = Paint().apply {
        color = 0x5543A047; strokeWidth = 60f; style = Paint.Style.STROKE; isAntiAlias = true
    }

    private fun paint(color: Int, width: Float) = Paint().apply {
        this.color = color; this.strokeWidth = width
        style = Paint.Style.STROKE; isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        val side = minOf(width, height).toFloat()
        val third = side / 3f
        val ox = (width - side) / 2f
        val oy = (height - side) / 2f
        drawGrid(canvas, ox, oy, third)
        game.cells.forEachIndexed { index, player -> drawMark(canvas, index, player, ox, oy, third) }
        if (game.winner != TicTacToeGame.EMPTY) drawWinningStroke(canvas, ox, oy, third)
    }

    private fun drawGrid(canvas: Canvas, ox: Float, oy: Float, third: Float) {
        (1..2).forEach { i ->
            canvas.drawLine(ox + i * third, oy + 20f, ox + i * third, oy + third * 3 - 20f, gridPaint)
            canvas.drawLine(ox + 20f, oy + i * third, ox + third * 3 - 20f, oy + i * third, gridPaint)
        }
    }

    private fun drawMark(canvas: Canvas, index: Int, player: Int, ox: Float, oy: Float, third: Float) {
        if (player == TicTacToeGame.EMPTY) return
        val center = cellCenter(index, ox, oy, third)
        val r = third * 0.32f
        if (player == TicTacToeGame.X) {
            canvas.drawLine(center[0] - r, center[1] - r, center[0] + r, center[1] + r, xPaint)
            canvas.drawLine(center[0] + r, center[1] - r, center[0] - r, center[1] + r, xPaint)
        } else {
            canvas.drawCircle(center[0], center[1], r, oPaint)
        }
    }

    private fun drawWinningStroke(canvas: Canvas, ox: Float, oy: Float, third: Float) {
        val line = game.winningLine() ?: return
        val start = cellCenter(line.first(), ox, oy, third)
        val end = cellCenter(line.last(), ox, oy, third)
        canvas.drawLine(start[0], start[1], end[0], end[1], winGlowPaint)
        canvas.drawLine(start[0], start[1], end[0], end[1], if (game.winner == TicTacToeGame.X) xPaint else oPaint)
    }

    private fun cellCenter(index: Int, ox: Float, oy: Float, third: Float) =
        floatArrayOf(ox + (index % 3 + 0.5f) * third, oy + (index / 3 + 0.5f) * third)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) return false
        val side = minOf(width, height).toFloat()
        val third = side / 3f
        val col = ((event.x - (width - side) / 2f) / third).toInt()
        val row = ((event.y - (height - side) / 2f) / third).toInt()
        if (col in 0..2 && row in 0..2) onCellTapped?.invoke(row * 3 + col)
        return true
    }
}
