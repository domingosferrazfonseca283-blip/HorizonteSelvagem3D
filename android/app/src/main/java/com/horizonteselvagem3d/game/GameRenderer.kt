package com.horizonteselvagem3d.game

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GameRenderer : GLSurfaceView.Renderer {
    var lookX = 0f; var lookY = 0f
    private val mvp = FloatArray(16); private val proj = FloatArray(16); private val view = FloatArray(16); private val model = FloatArray(16)
    private lateinit var program: FloatProgram
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) { GLES20.glClearColor(.04f,.07f,.10f,1f); program = FloatProgram() }
    override fun onSurfaceChanged(gl: GL10?, w: Int, h: Int) { GLES20.glViewport(0,0,w,h); Matrix.frustumM(proj,0,-1f,1f,-h.toFloat()/w, h.toFloat()/w,1f,50f) }
    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT); GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        Matrix.setLookAtM(view,0,0f,2.5f,7f,0f,1f,0f,0f,1f,0f)
        drawGround(); drawCharacter(); drawCreature()
    }
    private fun drawGround(){ Matrix.setIdentityM(model,0); Matrix.scaleM(model,0,8f,.1f,8f); draw(model,.10f,.28f,.16f) }
    private fun drawCharacter(){ Matrix.setIdentityM(model,0); Matrix.translateM(model,0,0f,1f,0f); Matrix.scaleM(model,0,.65f,1.1f,.45f); draw(model,.18f,.45f,.82f) }
    private fun drawCreature(){ Matrix.setIdentityM(model,0); Matrix.translateM(model,0,2f,.65f,-1.5f); Matrix.scaleM(model,0,.7f,.55f,.9f); draw(model,.82f,.55f,.16f) }
    private fun draw(m:FloatArray,r:Float,g:Float,b:Float){ Matrix.multiplyMM(mvp,0,view,0,m,0); Matrix.multiplyMM(mvp,0,proj,0,mvp,0); program.draw(mvp,r,g,b) }
}

private class FloatProgram {
    private val v: Int; private val a: Int; private val u: Int
    private val verts: FloatBuffer = ByteBuffer.allocateDirect(12*4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(floatArrayOf(-1f,0f,-1f,1f,0f,-1f,-1f,0f,1f,1f,0f,1f)); position(0) }
    init { val vs="attribute vec3 p; uniform mat4 m; void main(){gl_Position=m*vec4(p,1.0);}"; val fs="precision mediump float; uniform vec4 c; void main(){gl_FragColor=c;}"; val pv=GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER); GLES20.glShaderSource(pv,vs); GLES20.glCompileShader(pv); val pf=GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER); GLES20.glShaderSource(pf,fs); GLES20.glCompileShader(pf); v=GLES20.glCreateProgram(); GLES20.glAttachShader(v,pv); GLES20.glAttachShader(v,pf); GLES20.glLinkProgram(v); a=GLES20.glGetAttribLocation(v,"p"); u=GLES20.glGetUniformLocation(v,"m") }
    fun draw(m:FloatArray,r:Float,g:Float,b:Float){ GLES20.glUseProgram(v); GLES20.glUniformMatrix4fv(u,1,false,m,0); val c=GLES20.glGetUniformLocation(v,"c"); GLES20.glUniform4f(c,r,g,b,1f); GLES20.glEnableVertexAttribArray(a); GLES20.glVertexAttribPointer(a,3,GLES20.GL_FLOAT,false,0,verts); GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4); GLES20.glDisableVertexAttribArray(a) }
}
