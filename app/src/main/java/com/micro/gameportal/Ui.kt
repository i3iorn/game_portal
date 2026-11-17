package com.micro.gameportal

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Shared UI kit: colors, rounded/rippled backgrounds, and consistently sized
 * text and buttons so every screen follows the same visual language.
 */
object Ui {

    // Palette (matches the tile colors used across games).
    const val BG = 0xFF2E2118.toInt()
    const val SURFACE = 0xFF3A2F22.toInt()
    const val SURFACE_DIM = 0xFF32281C.toInt()
    const val PRIMARY = 0xFF8A7A5E.toInt()
    const val TEXT = 0xFFEDE4D3.toInt()
    const val TEXT_DIM = 0xFFB9AB93.toInt()
    const val ACCENT = 0xFFFFD54F.toInt()

    fun dp(context: Context, value: Float): Int =
        (value * context.resources.displayMetrics.density + 0.5f).toInt()

    /** Rounded, ripple-enabled background for buttons, cells and cards. */
    fun rounded(context: Context, color: Int, radiusDp: Float = 10f, ripple: Boolean = true): android.graphics.drawable.Drawable {
        val shape = GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(context, radiusDp).toFloat()
        }
        return if (ripple) RippleDrawable(ColorStateList.valueOf(0x40FFFFFF), shape, null) else shape
    }

    private fun textColorFor(bg: Int): Int =
        if (Color.luminance(bg) > 0.5f) Color.DKGRAY else TEXT

    /** Vertically-and-horizontally centered column used by every screen. */
    fun gameRoot(activity: Activity): LinearLayout = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setPadding(dp(activity, 16f), 0, dp(activity, 16f), 0)
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
    }

    /** Wraps a grid/button so it centers horizontally inside the root column. */
    fun centered(view: View): View = LinearLayout(view.context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        addView(
            view,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT),
        )
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
    }

    fun addCentered(root: LinearLayout, view: View) {
        root.addView(centered(view))
    }

    /** Big screen title. */
    fun title(activity: Activity, text: String): TextView = TextView(activity).apply {
        this.text = text
        textSize = 26f
        setTextColor(TEXT)
        gravity = Gravity.CENTER
        setPadding(0, dp(activity, 24f), 0, dp(activity, 8f))
    }

    /** Live game status line, centered above the board. */
    fun status(activity: Activity, initial: String = ""): TextView = TextView(activity).apply {
        text = initial
        textSize = 15f
        setTextColor(TEXT_DIM)
        gravity = Gravity.CENTER
        setPadding(0, dp(activity, 16f), 0, dp(activity, 12f))
    }

    /** Small uppercase section caption for list screens. */
    fun header(activity: Activity, caption: String): TextView = TextView(activity).apply {
        text = caption
        textSize = 12f
        setTextColor(TEXT_DIM)
        letterSpacing = 0.1f
        setPadding(dp(activity, 8f), dp(activity, 24f), dp(activity, 8f), dp(activity, 4f))
    }

    /**
     * The one button style: 48dp tall, rounded, ripple, 14sp label.
     * [quiet] renders a subdued variant for secondary actions (Back, Undo...).
     */
    fun button(activity: Activity, label: String, quiet: Boolean = false, onClick: () -> Unit): Button =
        Button(activity).apply {
            text = label
            textSize = 14f
            isAllCaps = true
            minWidth = dp(activity, 88f)
            minHeight = dp(activity, 48f)
            setPadding(dp(activity, 20f), 0, dp(activity, 20f), 0)
            if (quiet) {
                background = rounded(activity, SURFACE_DIM, ripple = true)
                setTextColor(TEXT_DIM)
            } else {
                background = rounded(activity, PRIMARY, ripple = true)
                setTextColor(textColorFor(PRIMARY))
            }
            setOnClickListener { onClick() }
        }

    /** Card-style container for home screen rows. */
    fun card(activity: Activity, content: View): View = LinearLayout(activity).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        background = rounded(activity, SURFACE, radiusDp = 14f, ripple = true)
        setPadding(dp(activity, 12f), dp(activity, 8f), dp(activity, 12f), dp(activity, 8f))
        addView(
            content,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT),
        )
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ).apply { topMargin = dp(activity, 8f) }
    }

    /** Styled grid cell background (used by board games instead of raw colors). */
    fun cell(view: View, color: Int, radiusDp: Float = 8f) {
        view.background = rounded(view.context, color, radiusDp, ripple = false)
    }
}
