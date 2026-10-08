package com.horizonteselvagem3d.game

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.TextView
import android.opengl.GLSurfaceView

class GameView(context: Context) : FrameLayout(context) {
    private val surface = GLSurfaceView(context)
    private val renderer = GameRenderer()
    private val prefs = context.getSharedPreferences("horizonte_selvagem_progress", Context.MODE_PRIVATE)
    private var lastX = 0f
    private var lastY = 0f
    private val mission = TextView(context)

    init {
        surface.setEGLContextClientVersion(2)
        surface.setRenderer(renderer)
        surface.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        addView(surface, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        renderer.loadState(prefs)

        mission.setTextColor(Color.WHITE)
        mission.setTextSize(16f)
        mission.setPadding(24, 18, 24, 18)
        mission.setBackgroundColor(0x99000000.toInt())
        mission.text = renderer.missionText()
        val missionParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        missionParams.gravity = Gravity.TOP
        addView(mission, missionParams)

        val action = TextView(context)
        action.text = "AÇÃO"
        action.gravity = Gravity.CENTER
        action.setTextColor(Color.WHITE)
        action.setTextSize(15f)
        action.setBackgroundColor(0xcc304b42.toInt())
        action.setPadding(30, 18, 30, 18)
        action.setOnClickListener {
            renderer.action()
            renderer.saveState(prefs)
            mission.text = renderer.missionText()
        }
        val actionParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        actionParams.gravity = Gravity.TOP or Gravity.END
        actionParams.setMargins(0, 80, 24, 0)
        addView(action, actionParams)

        surface.setOnTouchListener { _, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = e.x
                    lastY = e.y
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = e.x - lastX
                    val dy = e.y - lastY
                    if (lastX < width * .35f) {
                        renderer.move(-dx / width * 2.2f, -dy / height * 3.5f)
                    } else {
                        renderer.lookX = (e.x / width.toFloat() - .5f) * 2f
                        renderer.lookY = (e.y / height.toFloat() - .5f) * 2f
                    }
                    lastX = e.x
                    lastY = e.y
                }
            }
            true
        }
    }
}
