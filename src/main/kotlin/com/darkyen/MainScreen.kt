package com.darkyen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.darkyen.widgets.DataLineWidget
import com.darkyen.widgets.FountainWidget
import com.darkyen.widgets.TimelineWidget

/**
 *
 */
class MainScreen : ScreenAdapter() {

    val viewport = ScreenViewport()
    val stage = Stage(viewport)

    init {
        val skin = Main.skin
        val root = Table(skin)
        root.setFillParent(true)
        stage.addActor(root)

        val pauseButton = Button(skin.getDrawable("pause-button"),
                skin.newDrawable("pause-button", 0.8f, 0.8f, 0.8f, 1f),
                skin.getDrawable("play-button"))

        val timeline = TimelineWidget(skin)
        val fountain = FountainWidget(timeline, skin)

        pauseButton.addListener(object :ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                timeline.paused = pauseButton.isChecked
            }
        })

        root.addListener(object : InputListener() {
            override fun keyDown(event: InputEvent?, keycode: Int): Boolean {
                if (keycode == Input.Keys.SPACE) {
                    pauseButton.isChecked = !pauseButton.isChecked
                    return true
                }
                return false
            }
        })
        stage.keyboardFocus = root

        root.add(fountain).grow().colspan(2).row()

        root.add()
        val dataLineWidget = DataLineWidget(skin, fountain)
        root.add(dataLineWidget).height(20f).fill().expandX().pad(5f).padBottom(2f).row()

        root.add(pauseButton).fill().size(32f).pad(5f).padTop(Value.zero)
        root.add(timeline).fill().expandX().pad(5f).padTop(Value.zero)



        (Gdx.graphics as Lwjgl3Graphics).window.windowListener = object : Lwjgl3WindowListener {
            override fun closeRequested(): Boolean {
                return true
            }

            override fun filesDropped(files: Array<out String>) {
                for (file in files) {
                    val handle = Gdx.files.absolute(file)
                    fountain.data.add(handle)
                }
                dataLineWidget.refreshChildren()
            }

            override fun maximized(isMaximized: Boolean) {
            }

            override fun focusLost() {
            }

            override fun focusGained() {
            }

            override fun refreshRequested() {
            }

            override fun iconified(isIconified: Boolean) {
            }
        }
    }

    override fun show() {
        Gdx.input.inputProcessor = stage
        Gdx.gl.glEnable(GL20.GL_BLEND)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

}