package com.micro.gameportal

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton

/**
 * Shared UI kit. All colors come from the active [Palette] (user-selectable
 * in Settings); buttons use a fixed size scale: 4 width classes x 3 height
 * classes, of which 5 combinations are used:
 *
 *   COMPACT  112dp x 40dp  (intrinsic width, min 112) — small/secondary actions
 *   MEDIUM   168dp x 44dp  — standard actions (New game)
 *   WIDE     232dp x 44dp  — prominent actions with long labels
 *   TALL     168dp x 52dp  — reserved for future emphasis
 *   FILL     match x 52dp  — stretch to row width
 */
object Ui {

    /** The active color palette; defaults to the first preset. */
    fun theme(context: Context): Palette = Palette.current(context)

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
        if (Color.luminance(bg) > 0.5f) Color.DKGRAY else Color.WHITE

    /** Vertically-and-horizontally centered column used by every screen,
     *  padded so content clears the status and gesture bars. */
    fun gameRoot(activity: Activity): LinearLayout = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setBackgroundColor(theme(activity).background)
        val side = dp(activity, 16f)
        setPadding(side, 0, side, 0)
        setOnApplyWindowInsetsListener { view, insets ->
            view.setPadding(side, insets.systemWindowInsetTop + dp(activity, 4f), side, insets.systemWindowInsetBottom)
            insets
        }
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

    /** Fade transition for screen switches; call in onCreate and after finish. */
    fun setupEnter(activity: Activity) {
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    /** Finishes the screen with the matching fade transition. */
    fun back(activity: Activity) {
        activity.finish()
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    /** Slim top row: back arrow + screen title. */
    fun topBar(activity: Activity, title: String, onBack: () -> Unit): View {
        val palette = theme(activity)
        val bar = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(activity, 4f), 0, dp(activity, 4f))
        }
        bar.addView(
            TextView(activity).apply {
                text = "←"
                textSize = 20f
                setTextColor(palette.text)
                gravity = Gravity.CENTER
                background = rounded(activity, palette.surfaceDim, ripple = true)
                layoutParams = LinearLayout.LayoutParams(dp(activity, 44f), dp(activity, 44f))
                setOnClickListener { onBack() }
            },
        )
        bar.addView(
            TextView(activity).apply {
                text = title
                textSize = 18f
                setTextColor(palette.text)
                setPadding(dp(activity, 12f), 0, 0, 0)
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ),
        )
        return bar
    }

    /** One-time hint line; the caller hides it after the first user input. */
    fun hint(activity: Activity, text: String): TextView = TextView(activity).apply {
        this.text = text
        textSize = 13f
        setTextColor(theme(activity).textDim)
        gravity = Gravity.CENTER
        setPadding(0, dp(activity, 4f), 0, dp(activity, 4f))
    }

    /** Big screen title. */
    fun title(activity: Activity, text: String): TextView = TextView(activity).apply {
        this.text = text
        textSize = 26f
        setTextColor(theme(activity).text)
        gravity = Gravity.CENTER
        setPadding(0, dp(activity, 24f), 0, dp(activity, 8f))
    }

    /** Live game status line, centered above the board. */
    fun status(activity: Activity, initial: String = ""): TextView = TextView(activity).apply {
        text = initial
        textSize = 15f
        setTextColor(theme(activity).textDim)
        gravity = Gravity.CENTER
        setPadding(0, dp(activity, 16f), 0, dp(activity, 12f))
    }

    /** Small uppercase section caption for list screens. */
    fun header(activity: Activity, caption: String): TextView = TextView(activity).apply {
        text = caption
        textSize = 12f
        setTextColor(theme(activity).textDim)
        letterSpacing = 0.1f
        setPadding(dp(activity, 8f), dp(activity, 24f), dp(activity, 8f), dp(activity, 4f))
    }

    /** The button size scale (see class doc). */
    enum class ButtonSize(val widthDp: Int?, val heightDp: Int) {
        COMPACT(112, 40), // intrinsic width, min 112
        MEDIUM(168, 44),
        WIDE(232, 44),
        TALL(168, 52),
        FILL(null, 52),
    }

    /**
     * The one button style: rounded, ripple, 14sp caps label.
     * [quiet] renders a subdued variant for secondary actions.
     */
    fun button(
        activity: Activity,
        label: String,
        size: ButtonSize = ButtonSize.MEDIUM,
        quiet: Boolean = false,
        onClick: () -> Unit,
    ): MaterialButton = MaterialButton(activity).apply {
        text = label
        textSize = 14f
        isAllCaps = true
        minHeight = dp(activity, size.heightDp.toFloat())
        setPadding(dp(activity, 20f), 0, dp(activity, 20f), 0)
        val palette = theme(activity)
        if (quiet) {
            background = rounded(activity, palette.surfaceDim, ripple = true)
            setTextColor(palette.textDim)
        } else {
            background = rounded(activity, palette.primary, ripple = true)
            setTextColor(textColorFor(palette.primary))
        }
        layoutParams = LinearLayout.LayoutParams(
            when {
                size.widthDp != null -> dp(activity, size.widthDp.toFloat())
                size == ButtonSize.FILL -> ViewGroup.LayoutParams.MATCH_PARENT
                else -> ViewGroup.LayoutParams.WRAP_CONTENT
            },
            dp(activity, size.heightDp.toFloat()),
        )
        if (size == ButtonSize.COMPACT) minimumWidth = dp(activity, 112f)
        setOnClickListener { onClick() }
    }

    /** Card-style container for home screen rows. */
    fun card(activity: Activity, content: View): View = LinearLayout(activity).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        background = rounded(activity, theme(activity).surface, radiusDp = 14f, ripple = true)
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

/** A selectable color palette. */
data class Palette(
    val name: String,
    val background: Int,
    val surface: Int,
    val surfaceDim: Int,
    val primary: Int,
    val text: Int,
    val textDim: Int,
    val accent: Int,
) {
    companion object {
        val PRESETS = listOf(
            Palette(
                "Dark Brown",
                background = 0xFF2E2118.toInt(),
                surface = 0xFF3A2F22.toInt(),
                surfaceDim = 0xFF32281C.toInt(),
                primary = 0xFF8A7A5E.toInt(),
                text = 0xFFEDE4D3.toInt(),
                textDim = 0xFFB9AB93.toInt(),
                accent = 0xFFFFD54F.toInt(),
            ),
            Palette(
                "Slate",
                background = 0xFF1C2024.toInt(),
                surface = 0xFF262C33.toInt(),
                surfaceDim = 0xFF21262C.toInt(),
                primary = 0xFF6B7A8C.toInt(),
                text = 0xFFE3E8EE.toInt(),
                textDim = 0xFF9AA7B5.toInt(),
                accent = 0xFF7FD1C0.toInt(),
            ),
            Palette(
                "Ocean",
                background = 0xFF122430.toInt(),
                surface = 0xFF1B3140.toInt(),
                surfaceDim = 0xFF172A36.toInt(),
                primary = 0xFF4E8EA8.toInt(),
                text = 0xFFE0EEF4.toInt(),
                textDim = 0xFF9BB9C6.toInt(),
                accent = 0xFF9AD1D4.toInt(),
            ),
            Palette(
                "Forest",
                background = 0xFF17211A.toInt(),
                surface = 0xFF22301F.toInt(),
                surfaceDim = 0xFF1D291B.toInt(),
                primary = 0xFF6E8B5E.toInt(),
                text = 0xFFE6EEDF.toInt(),
                textDim = 0xFFA3B79A.toInt(),
                accent = 0xFFC9E265.toInt(),
            ),
            Palette(
                "Plum",
                background = 0xFF231826.toInt(),
                surface = 0xFF31223A.toInt(),
                surfaceDim = 0xFF2A1E32.toInt(),
                primary = 0xFF8E6B9E.toInt(),
                text = 0xFFF0E6F2.toInt(),
                textDim = 0xFFB5A4C0.toInt(),
                accent = 0xFFF2A2C0.toInt(),
            ),
            Palette(
                "Daylight",
                background = 0xFFF4EEE2.toInt(),
                surface = 0xFFE7DECC.toInt(),
                surfaceDim = 0xFFDED3BE.toInt(),
                primary = 0xFF7C6A4E.toInt(),
                text = 0xFF2E2118.toInt(),
                textDim = 0xFF7A6A52.toInt(),
                accent = 0xFFD98324.toInt(),
            ),
            Palette(
                "Paper",
                background = 0xFFF2F4F6.toInt(),
                surface = 0xFFE2E7EC.toInt(),
                surfaceDim = 0xFFD8DEE5.toInt(),
                primary = 0xFF52677E.toInt(),
                text = 0xFF1C2024.toInt(),
                textDim = 0xFF5F6B78.toInt(),
                accent = 0xFF2C8C7D.toInt(),
            ),
        )

        fun byIndex(index: Int): Palette = PRESETS[index.coerceIn(0, PRESETS.lastIndex)]

        // Process-wide cache; invalidated when the user picks another theme.
        @Volatile
        private var cached: Pair<Int, Palette>? = null

        fun current(context: Context): Palette {
            val index = context.getSharedPreferences("game_settings", Context.MODE_PRIVATE)
                .getInt("color_theme", 0)
            val cache = cached
            if (cache != null && cache.first == index) return cache.second
            val palette = byIndex(index)
            cached = index to palette
            return palette
        }
    }
}
