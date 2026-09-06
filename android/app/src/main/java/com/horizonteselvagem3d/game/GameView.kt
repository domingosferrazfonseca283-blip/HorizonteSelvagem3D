package com.horizonteselvagem3d.game

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent

class GameView(context: Context) : GLSurfaceView(context) {
    private val renderer = GameRenderer()
    init { setEGLContextClientVersion(2); setRenderer(renderer); renderMode = RENDERMODE_CONTINUOUSLY }
    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.action == MotionEvent.ACTION_DOWN || e.action == MotionEvent.ACTION_MOVE) {
            renderer.lookX = (e.x / width.toFloat() - .5f) * 2f
            renderer.lookY = (e.y / height.toFloat() - .5f) * 2f
        }
        return true
    }
}
