package com.micro.gameportal

import android.graphics.Canvas
import android.graphics.Paint

/** Canvas drawing routines for the home-screen preview icons. */
object GamePreviewDraw {

    private val GRAY = 0xFF5B6770.toInt()
    private val BLUE = 0xFF1E88E5.toInt()
    private val RED = 0xFFE53935.toInt()
    private val GREEN = 0xFF43A047.toInt()
    private val ORANGE = 0xFFFB8C00.toInt()
    private val YELLOW = 0xFFFFD54F.toInt()

    private fun stroke(canvas: Canvas, width: Float, color: Int = GRAY): Paint = Paint().apply {
        this.color = color; strokeWidth = width; style = Paint.Style.STROKE
    }

    private fun fill(color: Int): Paint = Paint().apply { this.color = color }

    fun ticTacToe(canvas: Canvas, s: Float) {
        val line = stroke(canvas, s * 0.04f)
        canvas.drawLine(s * 0.5f, s * 0.15f, s * 0.5f, s * 0.85f, line)
        canvas.drawLine(s * 0.15f, s * 0.5f, s * 0.85f, s * 0.5f, line)
        val x = stroke(canvas, s * 0.07f, BLUE).apply {
            style = Paint.Style.STROKE; isAntiAlias = true
        }
        canvas.drawLine(s * 0.62f, s * 0.62f, s * 0.82f, s * 0.82f, x)
        canvas.drawLine(s * 0.82f, s * 0.62f, s * 0.62f, s * 0.82f, x)
        canvas.drawCircle(s * 0.27f, s * 0.27f, s * 0.11f, x.apply { color = RED })
    }

    fun puzzle(canvas: Canvas, s: Float) {
        val paint = stroke(canvas, s * 0.03f)
        (1..2).forEach { i ->
            canvas.drawLine(s * 0.2f + i * s * 0.2f, s * 0.2f, s * 0.2f + i * s * 0.2f, s * 0.8f, paint)
            canvas.drawLine(s * 0.2f, s * 0.2f + i * s * 0.2f, s * 0.8f, s * 0.2f + i * s * 0.2f, paint)
        }
        canvas.drawRect(s * 0.2f, s * 0.6f, s * 0.4f, s * 0.8f, fill(BLUE))
    }

    fun memory(canvas: Canvas, s: Float) {
        val paint = stroke(canvas, s * 0.03f)
        val tile = s * 0.2f
        (0..1).forEach { r ->
            (0..1).forEach { c ->
                val l = s * 0.1f + c * tile * 1.2f
                val t = s * 0.25f + r * tile * 1.2f
                canvas.drawRect(l, t, l + tile, t + tile, paint)
            }
        }
        canvas.drawRect(s * 0.1f, s * 0.25f, s * 0.3f, s * 0.45f, fill(GREEN))
    }

    fun game2048(canvas: Canvas, s: Float) {
        val paint = stroke(canvas, s * 0.02f)
        (1..3).forEach { i ->
            canvas.drawLine(s * 0.15f + i * s * 0.175f, s * 0.15f, s * 0.15f + i * s * 0.175f, s * 0.85f, paint)
            canvas.drawLine(s * 0.15f, s * 0.15f + i * s * 0.175f, s * 0.85f, s * 0.15f + i * s * 0.175f, paint)
        }
        canvas.drawRect(s * 0.15f, s * 0.675f, s * 0.325f, s * 0.85f, fill(ORANGE))
        canvas.drawRect(s * 0.5f, s * 0.15f, s * 0.675f, s * 0.325f, fill(BLUE))
    }

    fun minesweeper(canvas: Canvas, s: Float) {
        val paint = stroke(canvas, s * 0.03f)
        (1..2).forEach { i ->
            canvas.drawLine(s * 0.2f + i * s * 0.2f, s * 0.2f, s * 0.2f + i * s * 0.2f, s * 0.8f, paint)
            canvas.drawLine(s * 0.2f, s * 0.2f + i * s * 0.2f, s * 0.8f, s * 0.2f + i * s * 0.2f, paint)
        }
        canvas.drawCircle(s * 0.7f, s * 0.7f, s * 0.07f, fill(RED))
        canvas.drawCircle(s * 0.3f, s * 0.3f, s * 0.07f, fill(GREEN))
    }

    fun lightsOut(canvas: Canvas, s: Float) {
        val off = stroke(canvas, s * 0.03f)
        val cell = s * 0.16f
        (0..3).forEach { r ->
            (0..3).forEach { c ->
                val l = s * 0.18f + c * cell * 1.1f
                val t = s * 0.18f + r * cell * 1.1f
                if ((r + c) % 3 == 0) canvas.drawRect(l, t, l + cell, t + cell, fill(YELLOW))
                else canvas.drawRect(l, t, l + cell, t + cell, off)
            }
        }
    }

    fun connectFour(canvas: Canvas, s: Float) {
        val paint = stroke(canvas, s * 0.03f)
        (1..2).forEach { i ->
            canvas.drawLine(s * 0.2f + i * s * 0.2f, s * 0.2f, s * 0.2f + i * s * 0.2f, s * 0.8f, paint)
            canvas.drawLine(s * 0.2f, s * 0.2f + i * s * 0.2f, s * 0.8f, s * 0.2f + i * s * 0.2f, paint)
        }
        canvas.drawCircle(s * 0.3f, s * 0.7f, s * 0.07f, fill(RED))
        canvas.drawCircle(s * 0.7f, s * 0.5f, s * 0.07f, fill(YELLOW))
    }
}
