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
    private lateinit var cylinder: Mesh
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
    private var chapter2Stage = 0
    private var redFieldVisited = false
    private var springVisited = false
    private var lumeaMet = false
    private var lumeaBonded = false
    private var chapter3Stage = 0
    private var blueFragments = 0
    private var waterValleyVisited = false
    private var mission = "Explore o vale rural e encontre Mística."

    override fun onSurfaceCreated(gl: GL10?, c: EGLConfig?) {
        GLES20.glClearColor(.03f, .05f, .08f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        fx = FloatProgram()
        cube = Mesh.cube()
        ball = Mesh.ball()
        cylinder = Mesh.cylinder()
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
        farmAnimals()
        mystica()
        if (eggFound && !aurino) egg()
        if (aurino) aurino()
        if (lumeaBonded) lumea()
    }

    fun evolve() = action()

    fun action() {
        if (aurino && chapter2Stage == 0) {
            chapter2Stage = 1
            mission = "Capítulo 2: atravesse a passagem azul e explore os Campos Vermelhos."
            return
        }
        if (chapter2Stage >= 5 && chapter3Stage == 0) {
            chapter3Stage = 1
            mission = "Capítulo 3: entre no Vale das Águas e procure os Fragmentos de Horizonte."
            return
        }
        if (chapter3Stage >= 4) {
            mission = "Capítulo 3 concluído. O caminho para a Serra do Horizonte foi revelado."
            return
        }

        val eggDistance = distance(px, pz, 7f, -35f)
        val oldMillDistance = distance(px, pz, -9f, -82f)
        val springDistance = distance(px, pz, -4f, -108f)
        val lumeaDistance = distance(px, pz, 10f, -92f)
        val mysticaDistance = distance(px, pz, 1.55f, 0f)
        val waterValleyDistance = distance(px, pz, 0f, -130f)
        val fragment1Distance = distance(px, pz, -10f, -136f)
        val fragment2Distance = distance(px, pz, 9f, -142f)
        val fragment3Distance = distance(px, pz, -6f, -150f)
        val shrineDistance = distance(px, pz, 4f, -154f)

        if (chapter3Stage >= 1 && chapter3Stage < 4) {
            if (!waterValleyVisited && waterValleyDistance < 9f) {
                waterValleyVisited = true
                mission = "O Vale das Águas guarda três fragmentos. Explore as margens."
                return
            }
            if (waterValleyVisited && blueFragments < 1 && fragment1Distance < 6f) {
                blueFragments = 1
                mission = "Fragmento de Horizonte encontrado: 1/3."
                return
            }
            if (blueFragments == 1 && fragment2Distance < 6f) {
                blueFragments = 2
                mission = "Segundo Fragmento de Horizonte encontrado: 2/3."
                return
            }
            if (blueFragments == 2 && fragment3Distance < 6f) {
                blueFragments = 3
                mission = "Terceiro Fragmento de Horizonte encontrado: 3/3. Volte ao santuário."
                return
            }
            if (blueFragments >= 3 && shrineDistance < 7f) {
                chapter3Stage = 4
                mission = "Os três fragmentos ativaram o santuário. Capítulo 3 concluído."
                return
            }
        }

        if (chapter2Stage >= 1 && chapter2Stage < 5) {
            if (oldMillDistance < 6f && chapter2Stage == 1) {
                chapter2Stage = 2
                mission = "A passagem termina num moinho antigo. Procure o campo vermelho ao norte."
                return
            }
            if (chapter2Stage == 2 && redFieldVisited) {
                chapter2Stage = 3
                mission = "O campo reage à presença de Aurino. Encontre a criatura que está observando de longe."
                return
            }
            if (chapter2Stage == 3 && lumeaDistance < 5f && !lumeaMet) {
                lumeaMet = true
                mission = "Uma criatura chamada Lumea observa você. Aproxime-se novamente para formar confiança."
                return
            }
            if (chapter2Stage == 3 && lumeaMet && lumeaDistance < 5f) {
                lumeaBonded = true
                chapter2Stage = 4
                mission = "Lumea escolheu caminhar ao seu lado. Agora encontre a nascente."
                return
            }
            if (chapter2Stage == 4 && springDistance < 7f) {
                springVisited = true
                chapter2Stage = 5
                mission = "A nascente estabilizou a luz azul. Capítulo 2 concluído."
                return
            }
        }

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
        pz = (pz + dz).coerceIn(-158f, 18f)

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

        if (aurino && chapter2Stage == 1 && distance(px, pz, 0f, -72f) < 10f) {
            mission = "A passagem azul está logo adiante. Explore a nova região."
        }
        if (chapter2Stage == 2 && distance(px, pz, 3f, -76f) < 9f) {
            redFieldVisited = true
            mission = "Os Campos Vermelhos respondem à energia de Aurino. Volte ao moinho."
        }
        if (chapter2Stage == 3 && distance(px, pz, 10f, -92f) < 5f) {
            mission = "Há uma criatura observando entre as plantações. Use AÇÃO."
        }
        if (chapter2Stage == 4 && distance(px, pz, -4f, -108f) < 7f) {
            mission = "A nascente está diante de você. Use AÇÃO."
        }
        if (chapter3Stage == 1 && distance(px, pz, 0f, -130f) < 9f) {
            mission = "O Vale das Águas começa aqui. Use AÇÃO para investigar."
        }
        if (chapter3Stage >= 1 && blueFragments < 1 && distance(px, pz, -10f, -136f) < 6f) {
            mission = "Há um brilho entre as pedras. Use AÇÃO para coletar o fragmento."
        }
        if (chapter3Stage >= 1 && blueFragments == 1 && distance(px, pz, 9f, -142f) < 6f) {
            mission = "Outro fragmento está perto da margem. Use AÇÃO."
        }
        if (chapter3Stage >= 1 && blueFragments == 2 && distance(px, pz, -6f, -150f) < 6f) {
            mission = "O último fragmento está diante de você. Use AÇÃO."
        }
        if (blueFragments >= 3 && distance(px, pz, 4f, -154f) < 7f) {
            mission = "O santuário está pronto. Use AÇÃO."
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
            .putInt("chapter2Stage", chapter2Stage)
            .putBoolean("redFieldVisited", redFieldVisited)
            .putBoolean("springVisited", springVisited)
            .putBoolean("lumeaMet", lumeaMet)
            .putBoolean("lumeaBonded", lumeaBonded)
            .putInt("chapter3Stage", chapter3Stage)
            .putInt("blueFragments", blueFragments)
            .putBoolean("waterValleyVisited", waterValleyVisited)
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
        chapter2Stage = prefs.getInt("chapter2Stage", 0)
        redFieldVisited = prefs.getBoolean("redFieldVisited", false)
        springVisited = prefs.getBoolean("springVisited", false)
        lumeaMet = prefs.getBoolean("lumeaMet", false)
        lumeaBonded = prefs.getBoolean("lumeaBonded", false)
        chapter3Stage = prefs.getInt("chapter3Stage", 0)
        blueFragments = prefs.getInt("blueFragments", 0)
        waterValleyVisited = prefs.getBoolean("waterValleyVisited", false)
        mission = when {
            chapter3Stage >= 4 -> "Capítulo 3 concluído. O caminho para a Serra do Horizonte foi revelado."
            chapter3Stage >= 1 -> "Capítulo 3: Fragmentos de Horizonte $blueFragments/3."
            chapter2Stage >= 5 -> "Capítulo 2 concluído. Uma nova trilha se abre além dos campos."
            aurino -> "Capítulo 2: atravesse a passagem azul e explore os Campos Vermelhos."
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
        beacon(-7f, -12f, .72f, .55f, .18f)
        beacon(10f, -24f, .72f, .55f, .34f)
        beacon(-5f, -30f, .72f, .25f, .48f)

        npc(-7f, -12f, .55f, .42f, .24f)
        npc(10f, -24f, .24f, .42f, .55f)
        npc(-5f, -30f, .42f, .28f, .62f)

        val pulse = .8f + .2f * sin(t * 4f)
        box(4f, .65f, -1.8f, .22f, .75f * pulse, .22f, .20f, .65f, .95f)

        // Silhuetas das montanhas: um alvo visual para a segunda etapa da missão.
        box(-11f, 5.5f, -61f, 8f, 5.5f, 1.8f, .12f, .20f, .18f)
        box(0f, 7.5f, -64f, 10f, 7.5f, 2.0f, .10f, .17f, .16f)
        box(12f, 5f, -60f, 7f, 5f, 1.8f, .13f, .21f, .18f)

        // Capítulo 2: Campos Vermelhos e a nascente azul.
        box(0f, -.12f, -94f, 24f, .12f, 48f, .26f, .18f, .10f)
        box(3f, .02f, -76f, 3.2f, .04f, 18f, .48f, .28f, .12f)
        box(-9f, 1.2f, -82f, 3.4f, 1.2f, 2.8f, .42f, .28f, .16f)
        box(-9f, 3.0f, -82f, 3.8f, .65f, 3.0f, .30f, .18f, .10f)
        for (i in -4..4) {
            val z = -78f - i * 4.8f
            box(7f, .28f, z, 1.6f, .28f, .18f, .58f, .30f, .10f)
            box(9.2f, .32f, z + 1.1f, 1.8f, .32f, .18f, .64f, .34f, .12f)
        }
        beacon(-9f, -82f, .72f, .32f, .12f)
        beacon(3f, -76f, .72f, .18f, .10f)
        beacon(-4f, -108f, .18f, .55f, .82f)
        npc(4f, -78f, .58f, .38f, .20f)

        if ((lumeaMet || chapter2Stage >= 3) && !lumeaBonded) lumea()

        box(-10f, 2.8f, -114f, 7f, 2.8f, 1.6f, .10f, .16f, .18f)
        box(8f, 3.6f, -116f, 8f, 3.6f, 1.8f, .08f, .13f, .16f)

        // Capítulo 3: Vale das Águas, pontos de coleta e santuário.
        box(0f, -.12f, -141f, 22f, .12f, 42f, .12f, .28f, .34f)
        box(0f, .02f, -130f, 2.8f, .04f, 9f, .10f, .46f, .58f)
        box(-10f, .35f, -136f, .34f, .35f, .34f, .20f, .72f, .92f)
        box(9f, .35f, -142f, .34f, .35f, .34f, .20f, .72f, .92f)
        box(-6f, .35f, -150f, .34f, .35f, .34f, .20f, .72f, .92f)
        beacon(0f, -130f, .18f, .55f, .82f)
        beacon(-10f, -136f, .18f, .72f, .92f)
        beacon(9f, -142f, .18f, .72f, .92f)
        beacon(-6f, -150f, .18f, .72f, .92f)
        beacon(4f, -154f, .60f, .34f, .82f)
        box(4f, .65f, -154f, 1.4f, .65f, 1.4f, .16f, .22f, .32f)
        box(4f, 1.55f, -154f, .25f, .9f, .25f, .20f, .62f, .82f)
    }


    private fun lumea() {
        val bob = sin(t * 2.8f) * .08f
        val x = if (lumeaBonded) px - 1.7f else 10f
        val z = if (lumeaBonded) pz - 2.0f else -92f
        ball(x, .65f + bob, z, .52f, .38f, .58f, .62f, .30f, .16f)
        ball(x, 1.12f + bob, z - .05f, .38f, .34f, .40f, .78f, .48f, .22f)
        box(x - .34f, 1.48f + bob, z, .10f, .28f, .10f, .72f, .28f, .12f)
        box(x + .34f, 1.48f + bob, z, .10f, .28f, .10f, .72f, .28f, .12f)
        ball(x - .14f, 1.18f + bob, z - .38f, .055f, .055f, .04f, .95f, .90f, .62f)
        ball(x + .14f, 1.18f + bob, z - .38f, .055f, .055f, .04f, .95f, .90f, .62f)
        box(x + .62f, .55f + bob, z + .08f, .12f, .16f, .40f, .76f, .38f, .14f)
    }

    private fun beacon(x: Float, z: Float, r: Float, g: Float, b: Float) {
        val pulse = .8f + .2f * sin(t * 4f)
        box(x, .35f, z, .08f, .35f * pulse, .08f, r, g, b)
        ball(x, 1.0f * pulse, z, .16f, .16f, .16f, r, g, b)
    }

    private fun npc(x: Float, z: Float, r: Float, g: Float, b: Float) {
        val bob = sin(t * 2f + x) * .02f
        cylinder(x, 1.15f + bob, z, .40f, 1.05f, .28f, r, g, b)
        cylinder(x, 1.78f + bob, z, .14f, .22f, .14f, .74f, .57f, .42f)
        ball(x, 2.18f + bob, z, .30f, .36f, .28f, .68f, .52f, .38f)
        cylinder(x - .48f, 1.18f + bob, z, .11f, .88f, .11f, r * .78f, g * .78f, b * .78f)
        cylinder(x + .48f, 1.18f - bob, z, .11f, .88f, .11f, r * .78f, g * .78f, b * .78f)
        cylinder(x - .17f, .42f, z, .14f, .72f, .14f, .12f, .14f, .16f)
        cylinder(x + .17f, .42f, z, .14f, .72f, .14f, .12f, .14f, .16f)
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
        // Kael: silhueta humana mais natural, com tronco, pescoço, cabeça, braços e pernas separados.
        val sway = sin(t * 2.2f) * .035f
        cylinder(px, 1.42f, pz, .48f, 1.25f, .34f, .12f, .31f, .62f)
        cylinder(px, 2.05f, pz, .16f, .24f, .16f, .76f, .56f, .38f)
        ball(px, 2.43f + sway, pz, .34f, .40f, .31f, .58f, .42f, .28f)
        ball(px, 2.67f + sway, pz + .02f, .29f, .14f, .27f, .08f, .055f, .045f)
        cylinder(px - .58f, 1.46f, pz + sway, .13f, 1.05f, .13f, .10f, .26f, .52f)
        cylinder(px + .58f, 1.46f, pz - sway, .13f, 1.05f, .13f, .10f, .26f, .52f)
        cylinder(px - .22f, .38f, pz, .17f, .72f, .17f, .07f, .11f, .20f)
        cylinder(px + .22f, .38f, pz, .17f, .72f, .17f, .07f, .11f, .20f)
        box(px - .22f, .04f, pz + .08f, .22f, .08f, .38f, .045f, .06f, .07f)
        box(px + .22f, .04f, pz + .08f, .22f, .08f, .38f, .045f, .06f, .07f)
    }

    private fun farmAnimals() {
        // Animais reais do cenário rural: modelos simples, mas com proporções reconhecíveis.
        cow(-13f, -18f, 1f)
        cow(13f, -27f, 1.08f)
        horse(-11f, -42f, .95f)
        horse(14f, -50f, 1.05f)
    }

    private fun cow(x: Float, z: Float, s: Float) {
        val bob = sin(t * 1.5f + x) * .025f
        box(x, .92f + bob, z, .92f*s, .62f*s, .48f*s, .70f, .66f, .56f)
        ball(x + .85f*s, 1.18f + bob, z, .38f*s, .34f*s, .34f*s, .70f, .66f, .56f)
        box(x + 1.15f*s, 1.20f + bob, z, .10f*s, .08f*s, .22f*s, .16f, .10f, .07f)
        box(x + .68f*s, 1.46f + bob, z - .25f*s, .10f*s, .20f*s, .08f*s, .16f, .10f, .07f)
        box(x + .68f*s, 1.46f + bob, z + .25f*s, .10f*s, .20f*s, .08f*s, .16f, .10f, .07f)
        for (i in -1..1 step 2) {
            cylinder(x + .52f*s, .35f, z + i*.25f*s, .11f*s, .62f*s, .11f*s, .28f, .24f, .20f)
            cylinder(x - .55f*s, .35f, z + i*.25f*s, .11f*s, .62f*s, .11f*s, .28f, .24f, .20f)
        }
    }

    private fun horse(x: Float, z: Float, s: Float) {
        val bob = sin(t * 1.8f + z) * .025f
        box(x, 1.18f + bob, z, 1.05f*s, .70f*s, .38f*s, .34f, .22f, .12f)
        cylinder(x + .82f*s, 1.58f + bob, z, .24f*s, .95f*s, .24f*s, .38f, .25f, .14f)
        ball(x + 1.00f*s, 2.15f + bob, z, .34f*s, .28f*s, .28f*s, .38f, .25f, .14f)
        box(x + 1.16f*s, 2.28f + bob, z - .14f*s, .08f*s, .22f*s, .08f*s, .18f, .12f, .08f)
        box(x + 1.16f*s, 2.28f + bob, z + .14f*s, .08f*s, .22f*s, .08f*s, .18f, .12f, .08f)
        for (i in -1..1 step 2) {
            cylinder(x + .62f*s, .43f, z + i*.20f*s, .10f*s, .88f*s, .10f*s, .22f, .14f, .09f)
            cylinder(x - .62f*s, .43f, z + i*.20f*s, .10f*s, .88f*s, .10f*s, .22f, .14f, .09f)
        }
        cylinder(x - 1.0f*s, 1.18f + bob, z, .08f*s, .72f*s, .08f*s, .30f, .16f, .09f)
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
        fun cylinder(): Mesh {
            val a = ArrayList<Float>()
            val segments = 12
            for (s in 0 until segments) {
                val q0 = 2 * Math.PI * s / segments
                val q1 = 2 * Math.PI * (s + 1) / segments
                a.add(cos(q0).toFloat()); a.add(-1f); a.add(sin(q0).toFloat())
                a.add(cos(q1).toFloat()); a.add(-1f); a.add(sin(q1).toFloat())
                a.add(cos(q0).toFloat()); a.add(1f); a.add(sin(q0).toFloat())
                a.add(cos(q1).toFloat()); a.add(1f); a.add(sin(q1).toFloat())
            }
            return Mesh(buf(a.toFloatArray()), a.size / 3)
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
