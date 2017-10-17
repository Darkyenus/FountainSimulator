package com.darkyen.util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.BufferUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 *
 */
class AutoReloadShaderProgram(val vertexShader:FileHandle, val fragmentShader:FileHandle) : ShaderProgram(vertexShader, fragmentShader) {

    private var keepReloading = true
    init {
        if (!isCompiled) {
            throw IllegalArgumentException("Error compiling shader: " + log)
        }

        Thread({
            val vertexFile = vertexShader.file().canonicalFile
            val fragmentFile = fragmentShader.file().canonicalFile
            var vertexModified = vertexFile.lastModified()
            var fragmentModified = fragmentFile.lastModified()

            while (keepReloading) {
                val vMod = vertexFile.lastModified()
                val fMod = fragmentFile.lastModified()

                if (vMod != vertexModified || fragmentModified != fMod) {
                    vertexModified = vMod
                    fragmentModified = fMod

                    Gdx.app.postRunnable {
                        reload()
                    }
                }
                Thread.sleep(2000)
            }

        }, "AutoReloadShaderProgram").apply {
            isDaemon = true
            start()
        }
    }

    private var vertexShaderHandle by ReflectionMember<Int>(ShaderProgram::class.java, "vertexShaderHandle")
    private var fragmentShaderHandle by ReflectionMember<Int>(ShaderProgram::class.java, "fragmentShaderHandle")
    private var program by ReflectionMember<Int>(ShaderProgram::class.java, "program")

    fun reload() {
        try {
            // Load new
            val vertexShaderSource = vertexShader.readString()
            val fragmentShaderSource = fragmentShader.readString()

            val newVertexShaderHandle = loadShader(GL20.GL_VERTEX_SHADER, vertexShaderSource)
            val newFragmentShaderHandle = loadShader(GL20.GL_FRAGMENT_SHADER, fragmentShaderSource)
            val newProgram = linkProgram(createProgram(), newVertexShaderHandle, newFragmentShaderHandle)


            // Delete old
            val gl = Gdx.gl20
            gl.glUseProgram(0)
            gl.glDeleteShader(vertexShaderHandle)
            gl.glDeleteShader(fragmentShaderHandle)
            gl.glDeleteProgram(program)

            vertexShaderHandle = newVertexShaderHandle
            fragmentShaderHandle = newFragmentShaderHandle
            program = newProgram

            println("Shader reloaded")
        } catch (e:Throwable) {
            println("Shader failed to reload")
            e.printStackTrace()
        }
    }

    private fun loadShader(type: Int, source: String): Int {
        val gl = Gdx.gl20
        val intbuf = BufferUtils.newIntBuffer(1)

        val shader = gl.glCreateShader(type)
        if (shader == 0) error("Failed to create shader program")

        gl.glShaderSource(shader, source)
        gl.glCompileShader(shader)
        gl.glGetShaderiv(shader, GL20.GL_COMPILE_STATUS, intbuf)

        val compiled = intbuf.get(0)
        if (compiled == 0) {
            val infoLog = gl.glGetShaderInfoLog(shader)
            error("Failed to compile shader program:\n"+(if (type == GL20.GL_VERTEX_SHADER) "Vertex shader\n" else "Fragment shader:\n")+infoLog)
        }

        return shader
    }

    private fun linkProgram(program: Int, vertexShader:Int, fragmentShader:Int): Int {
        val gl = Gdx.gl20
        if (program == -1) error("Invalid program")

        gl.glAttachShader(program, vertexShader)
        gl.glAttachShader(program, fragmentShader)
        gl.glLinkProgram(program)

        val tmp = ByteBuffer.allocateDirect(4)
        tmp.order(ByteOrder.nativeOrder())
        val intbuf = tmp.asIntBuffer()

        gl.glGetProgramiv(program, GL20.GL_LINK_STATUS, intbuf)
        val linked = intbuf.get(0)
        if (linked == 0) {
            error("Failed to link program: "+Gdx.gl20.glGetProgramInfoLog(program))
        }

        return program
    }

    override fun dispose() {
        super.dispose()
        keepReloading = false
    }
}