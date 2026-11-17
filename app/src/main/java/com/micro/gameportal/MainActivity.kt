package com.micro.gameportal

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/** Home screen listing the available puzzle games. */
class MainActivity : Activity() {

    private data class GameEntry(val title: String, val target: Class<out Activity>)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }
        root.addView(titleView())
        listOf(
            GameEntry("15 Puzzle", SlidingPuzzleActivity::class.java),
            GameEntry("Memory Match", MemoryActivity::class.java),
        ).forEach { root.addButtonFor(it) }
        setContentView(root)
    }

    private fun titleView(): TextView = TextView(this).apply {
        text = "Game Portal"
        textSize = 28f
        gravity = Gravity.CENTER
        setPadding(0, 48, 0, 64)
    }

    private fun LinearLayout.addButtonFor(entry: GameEntry) {
        val button = Button(this@MainActivity).apply {
            text = entry.title
            setOnClickListener { startActivity(Intent(context, entry.target)) }
        }
        addView(button, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { setMargins(64, 16, 64, 16) })
    }
}
