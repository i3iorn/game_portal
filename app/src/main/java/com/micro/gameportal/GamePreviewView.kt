package com.micro.gameportal

import android.content.Context
import android.graphics.Canvas
import android.view.View

/** Small square canvas view used as a game preview icon on the selection screen. */
class GamePreviewView(context: Context, val draw: (Canvas, Float) -> Unit) : View(context) {

    override fun onDraw(canvas: Canvas) {
        draw(canvas, minOf(width, height).toFloat())
    }
}
