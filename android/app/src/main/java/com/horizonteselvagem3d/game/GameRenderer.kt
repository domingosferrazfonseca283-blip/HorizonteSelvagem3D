package com.horizonteselvagem3d.game

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

class GameRenderer : GLSurfaceView.Renderer {
    var lookX=0f; var lookY=0f
    private val p=FloatArray(16); private val v=FloatArray(16); private val vp=FloatArray(16); private val m=FloatArray(16)
    private lateinit var fx: FloatProgram; private lateinit var cube: Mesh; private lateinit var ball: Mesh
    private var form=0; private var t=0f
    override fun onSurfaceCreated(gl:GL10?, c:EGLConfig?){ GLES20.glClearColor(.03f,.05f,.08f,1f); GLES20.glEnable(GLES20.GL_DEPTH_TEST); fx=FloatProgram(); cube=Mesh.cube(); ball=Mesh.ball() }
    override fun onSurfaceChanged(gl:GL10?,w:Int,h:Int){ GLES20.glViewport(0,0,w,h); Matrix.perspectiveM(p,0,58f,w.toFloat()/h.coerceAtLeast(1),.1f,60f) }
    override fun onDrawFrame(gl:GL10?){ t+=.016f; GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT); Matrix.setLookAtM(v,0,lookX*2f,2.8f+lookY,8f,0f,1.1f,0f,0f,1f,0f); Matrix.multiplyMM(vp,0,p,0,v,0); world(); hero(); puppy() }
    fun evolve(){ form=(form+1)%3 }
    private fun world(){ box(0f,-.15f,0f,9f,.15f,9f,.08f,.24f,.12f); box(0f,.01f,2f,2.8f,.04f,6f,.32f,.26f,.18f); for(i in -3..3) box(i*1.8f,.35f,-3.5f,.35f,.7f,.35f,.15f,.34f,.18f) }
    private fun hero(){ box(0f,1.25f,.4f,.65f,1.35f,.4f,.14f,.35f,.72f); ball(0f,2.35f,.4f,.4f,.48f,.4f,.48f,.32f,.22f); box(-.5f,1.15f,.4f,.18f,1.1f,.18f,.12f,.25f,.55f); box(.5f,1.15f,.4f,.18f,1.1f,.18f,.12f,.25f,.55f); box(-.23f,.1f,.4f,.22f,.65f,.22f,.07f,.12f,.22f); box(.23f,.1f,.4f,.22f,.65f,.22f,.07f,.12f,.22f) }
    private fun puppy(){ val s=if(form==0)1f else if(form==1)1.3f else 1.65f; val x=1.55f; val b=sin(t*3f)*.04f; ball(x,.65f+b,.25f,.55f*s,.42f*s,.65f*s,.52f,.30f,.10f); ball(x,1.28f*s+b,.25f,.43f*s,.42f*s,.43f*s,.62f,.40f,.18f); box(x-.32f*s,1.62f*s+b,.25f,.14f*s,.32f*s,.18f*s,.45f,.25f,.08f); box(x+.32f*s,1.62f*s+b,.25f,.14f*s,.32f*s,.18f*s,.45f,.25f,.08f); box(x-.32f*s,.25f,.25f,.18f*s,.48f*s,.18f*s,.40f,.22f,.07f); box(x+.32f*s,.25f,.25f,.18f*s,.48f*s,.18f*s,.40f,.22f,.07f); ball(x-.15f*s,1.35f*s+b,.66f,.055f*s,.055f*s,.035f*s,.02f,.02f,.02f); ball(x+.15f*s,1.35f*s+b,.66f,.055f*s,.055f*s,.035f*s,.02f,.02f,.02f); val hx=x+.62f*s; box(hx,.68f+b,.35f,.07f*s,.62f*s,.07f*s,.28f,.14f,.06f); box(hx,1.02f*s+b,.35f,.32f*s,.20f*s,.20f*s,.30f,.30f,.30f); if(form>0) box(x,1.82f*s+b,.25f,.07f,.45f*s,.07f,.86f,.50f,.08f) }
    private fun box(x:Float,y:Float,z:Float,sx:Float,sy:Float,sz:Float,r:Float,g:Float,b:Float){ Matrix.setIdentityM(m,0); Matrix.translateM(m,0,x,y,z); Matrix.scaleM(m,0,sx,sy,sz); draw(cube,m,r,g,b) }
    private fun ball(x:Float,y:Float,z:Float,sx:Float,sy:Float,sz:Float,r:Float,g:Float,b:Float){ Matrix.setIdentityM(m,0); Matrix.translateM(m,0,x,y,z); Matrix.scaleM(m,0,sx,sy,sz); draw(ball,m,r,g,b) }
    private fun draw(q:Mesh,a:FloatArray,r:Float,g:Float,b:Float){ val z=FloatArray(16); Matrix.multiplyMM(z,0,vp,0,a,0); fx.draw(q,z,r,g,b) }
}

private class Mesh(private val data:FloatBuffer,val count:Int){
    companion object{
        private fun buf(a:FloatArray)=ByteBuffer.allocateDirect(a.size*4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply{put(a);position(0)}
        fun cube()=Mesh(buf(floatArrayOf(-1f,-1f,1f,1f,-1f,1f,1f,1f,1f,-1f,1f,1f,1f,-1f,-1f,-1f,-1f,-1f,-1f,1f,-1f,1f,1f,-1f,1f,1f,1f,1f,1f,-1f,1f,-1f,-1f,-1f,-1f,-1f,-1f,1f,1f,-1f,1f,1f,-1f,1f,-1f,-1f,-1f,-1f,-1f,1f,-1f,1f,1f,-1f,1f,-1f,1f,1f,1f,-1f,-1f,-1f,1f,-1f,-1f,1f,-1f,1f,1f,1f,1f)),24)
        fun ball() : Mesh { val a=ArrayList<Float>(); val rs=8; val ss=12; for(r in 0 until rs){ val a0=Math.PI*r/rs-Math.PI/2; val a1=Math.PI*(r+1)/rs-Math.PI/2; for(s in 0..ss){ val q=2*Math.PI*s/ss; a.add((cos(a0)*cos(q)).toFloat());a.add(sin(a0).toFloat());a.add((cos(a0)*sin(q)).toFloat());a.add((cos(a1)*cos(q)).toFloat());a.add(sin(a1).toFloat());a.add((cos(a1)*sin(q)).toFloat()) } }; return Mesh(buf(a.toFloatArray()),a.size/3) }
    }
    fun bind(f:FloatProgram){ GLES20.glEnableVertexAttribArray(f.a); GLES20.glVertexAttribPointer(f.a,3,GLES20.GL_FLOAT,false,0,data) }
}
private class FloatProgram{
    private val q:Int; val a:Int; private val mm:Int; private val cc:Int
    init{ fun sh(t:Int,s:String)=GLES20.glCreateShader(t).also{GLES20.glShaderSource(it,s);GLES20.glCompileShader(it)}; val vs=sh(GLES20.GL_VERTEX_SHADER,"attribute vec3 p;uniform mat4 m;void main(){gl_Position=m*vec4(p,1.0);}"); val fs=sh(GLES20.GL_FRAGMENT_SHADER,"precision mediump float;uniform vec4 c;void main(){gl_FragColor=c;}"); q=GLES20.glCreateProgram().also{GLES20.glAttachShader(it,vs);GLES20.glAttachShader(it,fs);GLES20.glLinkProgram(it)}; a=GLES20.glGetAttribLocation(q,"p");mm=GLES20.glGetUniformLocation(q,"m");cc=GLES20.glGetUniformLocation(q,"c") }
    fun draw(x:Mesh,m:FloatArray,r:Float,g:Float,b:Float){GLES20.glUseProgram(q);GLES20.glUniformMatrix4fv(mm,1,false,m,0);GLES20.glUniform4f(cc,r,g,b,1f);x.bind(this);GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,x.count);GLES20.glDisableVertexAttribArray(a)}
}