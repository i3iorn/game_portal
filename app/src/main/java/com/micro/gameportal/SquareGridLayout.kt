package com.micro.gameportal

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout

/**
 * A GridLayout that always renders as a square sized to the smaller of its
 * two available dimensions — so boards take the full width in portrait and
 * the full height in landscape. Children use weighted specs and a 0 size to
 * share the square equally.
 */
class SquareGridLayout(context: Context) : GridLayout(context) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val availableWidth = View.MeasureSpec.getSize(widthMeasureSpec)
        val availableHeight = View.MeasureSpec.getSize(heightMeasureSpec)
        val side = minOf(availableWidth, availableHeight)
        val squareSpec = View.MeasureSpec.makeMeasureSpec(side, View.MeasureSpec.EXACTLY)
        super.onMeasure(squareSpec, squareSpec)
    }

    /** Layout params for an equally sharing cell at [row]/[col]. */
    fun cellParams(row: Int, col: Int, marginPx: Int): GridLayout.LayoutParams =
        GridLayout.LayoutParams(
            spec(row, 1f),
            spec(col, 1f),
        ).apply {
            width = 0
            height = 0
            setMargins(marginPx, marginPx, marginPx, marginPx)
        }

    companion object {
        /** Layout params that let the square board consume all leftover space
         *  inside the vertical game root (weight-based). */
        fun fillLayoutParams(): LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f,
            )
    }
}
