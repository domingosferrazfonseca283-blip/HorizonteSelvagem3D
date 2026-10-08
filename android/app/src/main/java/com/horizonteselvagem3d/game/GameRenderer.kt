package com.horizonteselvagem3d.game

import android.content.SharedPreferences
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
import kotlin.math.sqrt

class GameRenderer : GLSurfaceView.Renderer {
    var lookX = 0f
    var lookY = 0f
    private val p = FloatArray(16)
    private val v = FloatArray(16)
    private val vp = FloatArray(16)
    private val m = FloatArray(16)
    private lateinit var fx: FloatProgram
    private lateinit var cube: Mesh
    private lateinit var ball: Mesh
    private var form = 0
    private var t = 0f
    private var px = 0f
    private var pz = 7f
    private var eggCare = 0
    private var eggFound = false
    private var aurino = false
    private var mysticaBonded = false
    private var riverVisited = false
    private var mountainVisited = false
    private var missionStage = 0
    private var mission = "Explore o vale rural e encontre Mística."

    override fun onSurfaceCreated(gl: GL10?, c: EGLConfig?) {
        GLES20.glClearColor(.03f, .05f, .08f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        fx = FloatProgram()
        cube = Mesh.cube()
        ball = Mesh.ball()
    }

    override fun onSurfaceChanged(gl: GL10?, w: Int, h: Int) {
        GLES20.glViewport(0, 0, w, h)
        Matrix.perspectiveM(p, 0, 58f, w.toFloat() / h.coerceAtLeast(1), .1f, 60f)
    }

    override fun onDrawFrame(gl: GL10?) {
        t += .016f
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        Matrix.setLookAtM(v, 0, px + lookX * 2f, 2.8f + lookY, pz + 8f, px, 1.1f, pz, 0f, 1f, 0f)
        Matrix.multiplyMM(vp, 0, p, 0, v, 0)
        world()
        hero()
        mystica()
        if (eggFound && !aurino) egg()
        if (aurino) aurino()
    }

    fun evolve() = action()

    fun action() {
        if (aurino) {
            mission = "Capítulo 1 concluído. Aurino está com você. A luz azul aguarda nas montanhas."
            return
        }

        val eggDistance = distance(px, pz, 7f, -35f)
        val mysticaDistance = distance(px, pz, 1.55f, 0f)

        if (!mysticaBonded && mysticaDistance < 5f) {
            mysticaBonded = true
            missionStage = 1
            mission = "Mística confiou em você. Agora investigue o rio."
            return
        }

        val npc = nearestNpc()
        if (npc != null && npc.distance < 4f) {
            mission = when (npc.name) {
                "Dona Rosa" -> if (missionStage >= 1) "Dona Rosa: a margem está estranha. Procure o brilho perto da água."
                    else "Dona Rosa: o rio anda deixando as criaturas inquietas. Primeiro encontre Mística."
                "Mateus" -> if (missionStage >= 2) "Mateus: a luz veio das montanhas. Siga a estrada azul."
                    else "Mateus: vi uma luz azul atrás das montanhas. Primeiro investigue o rio."
                else -> if (missionStage >= 3) "Joana: Mística e você já estão no mesmo caminho. Proteja o ovo."
                    else "Joana: não force Mística. Caminhe com ela e deixe a confiança crescer."
            }
            return
        }

        if (eggDistance < 5f) {
            if (missionStage < 3) {
                mission = "O ovo está aqui, mas algo ainda falta. Explore o rio e as montanhas."
                return
            }
            if (!eggFound) {
                eggFound = true
                missionStage = 4
                mission = "Você encontrou um ovo misterioso. Cuide dele."
            } else {
                eggCare++
                mission = "Você cuidou do ovo: $eggCare/3"
                if (eggCare >= 3) {
                    aurino = true
                    missionStage = 5
                    mission = "Aurino nasceu. A luz azul pulsa atrás das montanhas."
                }
            }
            return
        }

        mission = when (missionStage) {
            0 -> "Encontre Mística no vilarejo."
            1 -> "Objetivo: investigue a margem do rio."
            2 -> "Objetivo: siga a estrada até as montanhas."
            3 -> "Objetivo: procure o ovo misterioso."
            4 -> "Objetivo: cuide do ovo até ele despertar."
            else -> "Explore o vale e procure a próxima pista."
        }
    }
    fun missionText(): String = mission

    fun move(dx: Float, dz: Float) {
        px = (px + dx).coerceIn(-18f, 18f)
        pz = (pz + dz).coerceIn(-70f, 18f)

        if (mysticaBonded && missionStage == 1 && distance(px, pz, 14f, -32f) < 7f) {
            riverVisited = true
            missionStage = 2
            mission = "A água emite um brilho azul. Agora siga para as montanhas."
        }

        if (riverVisited && missionStage == 2 && distance(px, pz, 10f, -55f) < 9f) {
            mountainVisited = true
            missionStage = 3
            mission = "Você encontrou a origem do sinal. Volte e procure o ovo."
        }

        if (missionStage >= 3 && !eggFound && distance(px, pz, 7f, -35f) < 5f) {
            mission = "Há algo escondido aqui. Use AÇÃO para investigar."
        }
    }
    fun saveState(prefs: SharedPreferences) {
        prefs.edit()
            .putBoolean("mysticaBonded", mysticaBonded)
            .putBoolean("eggFound", eggFound)
            .putBoolean("aurino", aurino)
            .putBoolean("riverVisited", riverVisited)
            .putBoolean("mountainVisited", mountainVisited)
            .putInt("missionStage", missionStage)
            .putInt("eggCare", eggCare)
            .apply()
    }

    fun loadState(prefs: SharedPreferences) {
        mysticaBonded = prefs.getBoolean("mysticaBonded", false)
        eggFound = prefs.getBoolean("eggFound", false)
        aurino = prefs.getBoolean("aurino", false)
        riverVisited = prefs.getBoolean("riverVisited", false)
        mountainVisited = prefs.getBoolean("mountainVisited", false)
        missionStage = prefs.getInt("missionStage", if (aurino) 5 else if (eggFound) 4 else if (mysticaBonded) 1 else 0)
        eggCare = prefs.getInt("eggCare", 0)
        mission = when {
            aurino -> "Capítulo 1 concluído. Aurino está com você. A luz azul aguarda nas montanhas."
            eggFound -> "O ovo misterioso está com você. Continue cuidando dele."
            mysticaBonded -> when (missionStage) { 1 -> "Objetivo: investigue a margem do rio."; 2 -> "Objetivo: siga a estrada até as montanhas."; else -> "Mística está com você. Procure a próxima pista." }
            else -> "Explore o vale rural e encontre Mística."
        }
    }

    private data class Npc(val name: String, val x: Float, val z: Float, val distance: Float)

    private fun nearestNpc(): Npc? {
        val npcs = listOf(
            Npc("Dona Rosa", -7f, -12f, distance(px, pz, -7f, -12f)),
            Npc("Mateus", 10f, -24f, distance(px, pz, 10f, -24f)),
            Npc("Joana", -5f, -30f, distance(px, pz, -5f, -30f))
        )
        return npcs.minByOrNull { it.distance }
    }

    private fun distance(x1: Float, z1: Float, x2: Float, z2: Float): Float =
        sqrt((x1 - x2) * (x1 - x2) + (z1 - z2) * (z1 - z2))

    private fun world() {
        box(0f, -.15f, -25f, 24f, .15f, 70f, .08f, .24f, .12f)
        box(0f, .01f, -30f, 3.2f, .04f, 60f, .32f, .26f, .18f)
        box(-9f, 1.4f, -14f, 3.2f, 1.4f, 2.6f, .55f, .43f, .30f)
        box(-9f, 3.4f, -14f, 3.7f, .7f, 3.1f, .34f, .22f, .14f)
        box(8f, 1.2f, -20f, 2.8f, 1.2f, 2.3f, .58f, .47f, .34f)
        box(8f, 3.0f, -20f, 3.3f, .65f, 2.8f, .32f, .20f, .12f)
        box(14f, .02f, -32f, 3.2f, .03f, 35f, .16f, .36f, .48f)

        for (i in -8..8) {
            val z = -4f - i * 4.2f
            box(-15f, 1.6f, z, .35f, 1.6f, .35f, .25f, .13f, .06f)
            ball(-15f, 3.4f, z, 1.5f, 1.2f, 1.5f, .10f, .28f, .16f)
            box(17f, 1.4f, z - 1.5f, .32f, 1.4f, .32f, .25f, .13f, .06f)
            ball(17f, 3.0f, z - 1.5f, 1.4f, 1.1f, 1.4f, .10f, .28f, .16f)
        }

        box(0f, .01f, 2f, 2.8f, .04f, 6f, .32f, .26f, .18f)
        for (i in -3..3) box(i * 1.8f, .35f, -3.5f, .35f, .7f, .35f, .15f, .34f, .18f)

        // Pequenos marcos visuais para orientar o jogador até as três conversas.
        beacon(-7f, -12f, .72f, .55f, .18f, .42f)
        beacon(10f, -24f, .72f, .55f, .34f, .16f)
        beacon(-5f, -30f, .72f, .25f, .48f, .68f)

        npc(-7f, -12f, .55f, .42f, .24f)
        npc(10f, -24f, .24f, .42f, .55f)
        npc(-5f, -30f, .42f, .28f, .62f)

        val pulse = .8f + .2f * sin(t * 4f)
        box(4f, .65f, -1.8f, .22f, .75f * pulse, .22f, .20f, .65f, .95f)

        // Silhuetas das montanhas: um alvo visual para a segunda etapa da missão.
        box(-11f, 5.5f, -61f, 8f, 5.5f, 1.8f, .12f, .20f, .18f)
        box(0f, 7.5f, -64f, 10f, 7.5f, 2.0f, .10f, .17f, .16f)
        box(12f, 5f, -60f, 7f, 5f, 1.8f, .13f, .21f, .18f)
    }

    private fun beacon(x: Float, z: Float, r: Float, g: Float, b: Float) {
        val pulse = .8f + .2f * sin(t * 4f)
        box(x, .35f, z, .08f, .35f * pulse, .08f, r, g, b)
        ball(x, 1.0f * pulse, z, .16f, .16f, .16f, r, g, b)
    }

    private fun npc(x: Float, z: Float, r: Float, g: Float, b: Float) {
        val bob = sin(t * 2f + x) * .015f
        box(x, .95f + bob, z, .38f, .9f, .30f, r, g, b)
        ball(x, 1.85f + bob, z, .30f, .34f, .30f, .72f, .56f, .40f)
        box(x - .23f, .18f, z, .13f, .45f, .13f, r * .75f, g * .75f, b * .75f)
        box(x + .23f, .18f, z, .13f, .45f, .13f, r * .75f, g * .75f, b * .75f)
    }

    private fun egg() {
        val pulse = 1f + sin(t * 3f) * .06f
        ball(7f, .75f * pulse, -35f, .55f, .75f * pulse, .55f, .78f, .68f, .45f)
        box(7f, .78f, -35f, .62f, .04f, .62f, .28f, .55f, .64f)
    }

    private fun aurino() {
        val bob = sin(t * 3f) * .08f
        val x = px + 1.6f
        val z = pz - 1.8f
        ball(x, .75f + bob, z, .55f, .48f, .50f, .37f, .48f, .56f)
        ball(x, 1.32f + bob, z, .42f, .40f, .42f, .72f, .64f, .46f)
        box(x + .58f, .55f + bob, z, .10f, .48f, .10f, .48f, .34f, .20f)
        box(x, 1.78f + bob, z, .16f, .36f, .16f, .56f, .72f, .68f)
    }

    private fun hero() {
        box(px, 1.25f, pz, .65f, 1.35f, .4f, .14f, .35f, .72f)
        ball(px, 2.35f, pz, .4f, .48f, .4f, .48f, .32f, .22f)
        box(px - .5f, 1.15f, pz, .18f, 1.1f, .18f, .12f, .25f, .55f)
        box(px + .5f, 1.15f, pz, .18f, 1.1f, .18f, .12f, .25f, .55f)
        box(px - .23f, .1f, pz, .22f, .65f, .22f, .07f, .12f, .22f)
        box(px + .23f, .1f, pz, .22f, .65f, .22f, .07f, .12f, .22f)
    }

    private fun mystica() {
        val s = when (form) { 0 -> 1f; 1 -> 1.3f; else -> 1.65f }
        val x = if (mysticaBonded) px + 1.7f else 1.55f
        val z = if (mysticaBonded) pz - 1.8f else .25f
        val bob = sin(t * 3f) * .06f
        ball(x, .72f * s + bob, z, .58f * s, .48f * s, .62f * s, .18f, .28f, .62f)
        ball(x, 1.32f * s + bob, z, .46f * s, .43f * s, .46f * s, .28f, .42f, .78f)
        ball(x, 1.28f * s + bob, z + .42f, .12f * s, .12f * s, .08f * s, .75f, .90f, 1f)
        box(x - .30f * s, 1.72f * s + bob, z, .13f * s, .38f * s, .13f * s, .45f, .18f, .80f)
        box(x + .28f * s, 1.78f * s + bob, z, .10f * s, .48f * s, .12f * s, .18f, .75f, .95f)
        box(x - .38f * s, .25f, z, .16f * s, .48f * s, .16f * s, .12f, .20f, .48f)
        box(x + .38f * s, .25f, z, .16f * s, .48f * s, .16f * s, .12f, .20f, .48f)
        box(x - .36f * s, 1f * s + bob, z + .30f, .15f * s, .16f * s, .22f * s, .16f, .32f, .72f)
        box(x + .36f * s, 1f * s + bob, z + .30f, .15f * s, .16f * s, .22f * s, .16f, .32f, .72f)
        ball(x - .15f * s, 1.38f * s + bob, z + .41f, .055f * s, .055f * s, .035f * s, .98f, .98f, .78f)
        ball(x + .15f * s, 1.38f * s + bob, z + .41f, .055f * s, .055f * s, .035f * s, .98f, .98f, .78f)
        box(x + .62f * s, .85f * s + bob, z - .10f, .10f * s, .28f * s, .10f * s, .32f, .65f, .95f)
        box(x + .82f * s, 1.05f * s + bob, z - .15f, .09f * s, .24f * s, .09f * s, .45f, .30f, .95f)
        box(x + 1f * s, 1.25f * s + bob, z - .20f, .08f * s, .20f * s, .08f * s, .72f, .30f, .92f)
        val hx = x - .70f * s
        box(hx, .85f * s + bob, z + .30f, .07f * s, .68f * s, .07f * s, .30f, .16f, .08f)
        box(hx, 1.22f * s + bob, z + .30f, .34f * s, .20f * s, .20f * s, .42f, .45f, .55f)
        if (form > 0) {
            box(x - .62f * s, 1.40f * s + bob, z, .08f * s, .32f * s, .08f * s, .72f, .30f, .95f)
            box(x + .62f * s, 1.40f * s + bob, z, .08f * s, .32f * s, .08f * s, .72f, .30f, .95f)
        }
        if (form == 2) {
            box(x, 2.05f * s + bob, z, .10f * s, .55f * s, .10f * s, .82f, .48f, .95f)
            box(x - .72f * s, 1.55f * s + bob, z - .05f, .08f * s, .38f * s, .08f * s, .20f, .70f, 1f)
            box(x + .72f * s, 1.55f * s + bob, z - .05f, .08f * s, .38f * s, .08f * s, .20f, .70f, 1f)
        }
    }

    private fun box(x: Float, y: Float, z: Float, sx: Float, sy: Float, sz: Float, r: Float, g: Float, b: Float) {
        Matrix.setIdentityM(m, 0)
        Matrix.translateM(m, 0, x, y, z)
        Matrix.scaleM(m, 0, sx, sy, sz)
        draw(cube, m, r, g, b)
    }

    private fun ball(x: Float, y: Float, z: Float, sx: Float, sy: Float, sz: Float, r: Float, g: Float, b: Float) {
        Matrix.setIdentityM(m, 0)
        Matrix.translateM(m, 0, x, y, z)
        Matrix.scaleM(m, 0, sx, sy, sz)
        draw(ball, m, r, g, b)
    }

    private fun draw(mesh: Mesh, transform: FloatArray, r: Float, g: Float, b: Float) {
        val z = FloatArray(16)
        Matrix.multiplyMM(z, 0, vp, 0, transform, 0)
        fx.draw(mesh, z, r, g, b)
    }
}

private class Mesh(private val data: FloatBuffer, val count: Int) {
    companion object {
        private fun buf(a: FloatArray): FloatBuffer = ByteBuffer.allocateDirect(a.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(a); position(0) }
        fun cube(): Mesh {
            val a = floatArrayOf(
                -1f,-1f,1f, 1f,-1f,1f, 1f,1f,1f, -1f,1f,1f,
                -1f,-1f,-1f, -1f,1f,-1f, 1f,1f,-1f, 1f,-1f,-1f,
                -1f,1f,-1f, -1f,1f,1f, 1f,1f,1f, 1f,1f,-1f,
                -1f,-1f,-1f, 1f,-1f,-1f, 1f,-1f,1f, -1f,-1f,1f,
                1f,-1f,-1f, 1f,1f,-1f, 1f,1f,1f, 1f,-1f,1f,
                -1f,-1f,-1f, -1f,-1f,1f, -1f,1f,1f, -1f,1f,-1f
            )
            return Mesh(buf(a), 24)
        }
        fun ball(): Mesh {
            val a = ArrayList<Float>()
            val rings = 8
            val segments = 12
            for (r in 0 until rings) {
                val a0 = Math.PI * r / rings - Math.PI / 2
                val a1 = Math.PI * (r + 1) / rings - Math.PI / 2
                for (s in 0..segments) {
                    val q = 2 * Math.PI * s / segments
                    a.add((cos(a0) * cos(q)).toFloat()); a.add(sin(a0).toFloat()); a.add((cos(a0) * sin(q)).toFloat())
                    a.add((cos(a1) * cos(q)).toFloat()); a.add(sin(a1).toFloat()); a.add((cos(a1) * sin(q)).toFloat())
                }
            }
            return Mesh(buf(a.toFloatArray()), a.size / 3)
        }
    }
    fun bind(f: FloatProgram) { GLES20.glEnableVertexAttribArray(f.a); GLES20.glVertexAttribPointer(f.a, 3, GLES20.GL_FLOAT, false, 0, data) }
}

private class FloatProgram {
    private val q: Int
    val a: Int
    private val mm: Int
    private val cc: Int
    init {
        val vs = shader(GLES20.GL_VERTEX_SHADER, "attribute vec3 p; uniform mat4 m; void main(){ gl_Position=m*vec4(p,1.0); }")
        val fs = shader(GLES20.GL_FRAGMENT_SHADER, "precision mediump float; uniform vec4 c; void main(){ gl_FragColor=c; }")
        q = GLES20.glCreateProgram()
        GLES20.glAttachShader(q, vs); GLES20.glAttachShader(q, fs); GLES20.glLinkProgram(q)
        a = GLES20.glGetAttribLocation(q, "p"); mm = GLES20.glGetUniformLocation(q, "m"); cc = GLES20.glGetUniformLocation(q, "c")
    }
    private fun shader(type: Int, source: String): Int { val s = GLES20.glCreateShader(type); GLES20.glShaderSource(s, source); GLES20.glCompileShader(s); return s }
    fun draw(x: Mesh, m: FloatArray, r: Float, g: Float, b: Float) { GLES20.glUseProgram(q); GLES20.glUniformMatrix4fv(mm,1,false,m,0); GLES20.glUniform4f(cc,r,g,b,1f); x.bind(this); GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,x.count); GLES20.glDisableVertexAttribArray(a) }
}
