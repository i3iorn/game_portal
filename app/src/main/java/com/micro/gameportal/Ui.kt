package com.micro.gameportal

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/** Shared UI helpers: consistent root layout, centered grids, buttons, status lines. */
object Ui {

    /** Vertically-and-horizontally centered column used by every game screen. */
    fun gameRoot(activity: Activity): LinearLayout = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
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

    fun status(activity: Activity, initial: String = ""): TextView = TextView(activity).apply {
        textSize = 18f
        setPadding(0, 24, 0, 24)
        text = initial
    }

    fun button(activity: Activity, label: String, onClick: () -> Unit): Button = Button(activity).apply {
        text = label
        setOnClickListener { onClick() }
    }

    fun addCentered(root: LinearLayout, view: View) {
        root.addView(centered(view))
    }
}
