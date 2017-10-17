package com.darkyen

import com.badlogic.gdx.Game
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.profiling.GLErrorListener
import com.badlogic.gdx.graphics.profiling.GLProfiler
import com.badlogic.gdx.physics.box2d.Box2D
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.ObjectMap
import java.io.File
import java.lang.management.ManagementFactory


/**
 *
 */
object Main : Game() {

    lateinit var batch: Batch
        private set
    lateinit var skin: Skin
        private set
    lateinit var font: BitmapFont
        private set

    override fun create() {
        batch = SpriteBatch()
        assetManager.load("UISkin.json", Skin::class.java)
        assetManager.finishLoading()

        skin = assetManager.get<Skin>("UISkin.json")
        font = skin.getFont("font-ui-small")

        if (ARGS.containsKey("development")) {
            GLProfiler.enable()
            GLProfiler.listener = GLErrorListener.THROWING_LISTENER
        }

        setScreen(MainScreen())
    }
}

val ARGS = ObjectMap<String, String>()

val assetManager = AssetManager(InternalFileHandleResolver())

fun main(args: Array<String>) {
    Box2D.init()
    val c = Lwjgl3ApplicationConfiguration()
    c.setTitle("Fountain Simulator")
    c.useVsync(true)
    c.setResizable(true)
    c.setIdleFPS(30)
    c.setWindowedMode(800, 400)
    c.setWindowSizeLimits(150, 150, 40000, 30000)

    ARGS.ensureCapacity(args.size)
    for (arg in args) {
        val splitIndex = arg.indexOf(':')
        if (splitIndex == -1) {
            ARGS.put(arg, null)
        } else {
            ARGS.put(arg.substring(0, splitIndex), arg.substring(splitIndex + 1))
        }
    }

    try {
        Lwjgl3Application(Main, c)
    } catch (e:ExceptionInInitializerError) {
        if (e.cause is IllegalStateException && (e.cause!!.message ?: "").contains("-XstartOnFirstThread")) {
            // Restart on main thread

            if (ARGS.containsKey("firstThreadRestarted")) {
                System.err.println("Failure to restart")
                throw e
            } else {
                restartOnFirstThread(args)
            }
        } else {
            throw e
        }
    }
}

private fun restartOnFirstThread(args: Array<String>) {
    val command = ArrayList<String>()
    command.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java")
    command.addAll(ManagementFactory.getRuntimeMXBean().inputArguments)
    command.add("-XstartOnFirstThread")
    command.add("-cp")
    command.add(ManagementFactory.getRuntimeMXBean().classPath)
    command.add("com.darkyen.pb009.MainKt")
    command.addAll(args)
    command.add("firstThreadRestarted")

    println("Restarting: "+command)
    val process = ProcessBuilder().command(command).directory(File(".")).inheritIO().start()
    process.waitFor()
    System.exit(process.exitValue())
}