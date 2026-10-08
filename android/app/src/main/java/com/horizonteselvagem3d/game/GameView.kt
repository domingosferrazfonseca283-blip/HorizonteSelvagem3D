package com.horizonteselvagem3d.game

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent

class GameView(context: Context) : GLSurfaceView(context) {
    private val renderer = GameRenderer()
    private var lastX = 0f
    private var lastY = 0f

    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
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
            MotionEvent.ACTION_UP -> {
                if (e.x > width * .78f && e.y < height * .28f) renderer.evolve()
            }
        }
        return true
    }
}
