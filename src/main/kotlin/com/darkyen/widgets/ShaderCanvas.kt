package com.darkyen.widgets

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.math.MathUtils.ceil
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Disposable
import com.darkyen.Main
import org.lwjgl.system.MathUtil

/**
 *
 */
@Suppress("LeakingThis")
abstract class ShaderCanvas(beginSubSampled:Boolean) : Table(Main.skin), Disposable {

    private val canvas = FrameBufferCanvas(this)
    private val optionsTable = Table(Main.skin)
    protected val input = InputMultiplexer()

    var mouseX:Int = 0
        private set
    var mouseY:Int = 0
        private set

    var screenWidth:Int = 1
        private set

    var screenHeight:Int = 1
        private set

    var factor:Float = 1f

    val screenMesh = Mesh(Mesh.VertexDataType.VertexBufferObject, true, 4, 4, VertexAttribute(VertexAttributes.Usage.Position, 2, "in_position"))

    init {
        screenMesh.setIndices(shortArrayOf(
                0, 1, 2, 3
        ))
        screenMesh.setVertices(floatArrayOf(
                -1f, 1f,
                1f, 1f,
                1f, -1f,
                -1f, -1f
        ))

        add(canvas).grow().row()
        isTransform = false

        optionsTable.defaults().pad(10f)
        optionsTable.background = Main.skin.newDrawable("white", Color.GRAY)

        add(optionsTable).growX().row()

        newSelectBox(*FBFactor.values(), initiallySelected = if (beginSubSampled) FBFactor.SUB2.ordinal else FBFactor.ONE.ordinal) { newFactor ->
            factor = newFactor.factor
            canvas.sizeChanged()
        }
    }

    fun <SelectItem>newSelectBox(vararg items:SelectItem, initiallySelected:Int = 0, changed:((SelectItem) -> Unit)? = null):() -> SelectItem {
        val selectBox = SelectBox<SelectItem>(Main.skin)
        selectBox.setItems(*items)
        selectBox.selectedIndex = initiallySelected
        if (changed != null) {
            selectBox.addListener(object : ChangeListener() {
                var selectedBefore = selectBox.selected

                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    val selectedNow = selectBox.selected
                    if (selectedNow != selectedBefore) {
                        selectedBefore = selectedNow
                        changed(selectedNow)
                    }
                }
            })
            changed(selectBox.selected)
        }

        optionsTable.add(selectBox)

        return { selectBox.selected }
    }

    abstract fun render()

    private class FrameBufferCanvas(val parent: ShaderCanvas) : Widget() {
        var framebuffer:FrameBuffer? = null

        init {
            addListener(object : InputListener() {
                override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                    parent.mouseX = x.toInt()
                    parent.mouseY = y.toInt()
                    parent.input.touchUp(x.toInt(), y.toInt(), pointer, button)
                }

                override fun mouseMoved(event: InputEvent?, x: Float, y: Float): Boolean {
                    parent.mouseX = x.toInt()
                    parent.mouseY = y.toInt()
                    return parent.input.mouseMoved(x.toInt(), y.toInt())
                }

                override fun keyTyped(event: InputEvent?, character: Char): Boolean {
                    return parent.input.keyTyped(character)
                }

                override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                    parent.mouseX = x.toInt()
                    parent.mouseY = y.toInt()
                    return parent.input.touchDown(x.toInt(), y.toInt(), pointer, button)
                }

                override fun scrolled(event: InputEvent?, x: Float, y: Float, amount: Int): Boolean {
                    return parent.input.scrolled(amount)
                }

                override fun keyUp(event: InputEvent?, keycode: Int): Boolean {
                    return parent.input.keyUp(keycode)
                }

                override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                    parent.mouseX = x.toInt()
                    parent.mouseY = y.toInt()
                    parent.input.touchDragged(x.toInt(), y.toInt(), pointer)
                }

                override fun keyDown(event: InputEvent?, keycode: Int): Boolean {
                    return parent.input.keyDown(keycode)
                }
            })
        }

        var framebufferDrawnWidth = 1
        var framebufferDrawnHeight = 1
        var framebufferDrawnU = 1f
        var framebufferDrawnV = 1f

        override fun draw(batch: Batch, parentAlpha: Float) {
            validate()
            stage.keyboardFocus = this
            stage.scrollFocus = this

            val width = ceil(width)
            val height = ceil(height)

            val density = (Gdx.graphics.backBufferWidth / Gdx.graphics.width) * parent.factor

            var framebuffer:FrameBuffer? = framebuffer
            if (framebuffer == null) {
                val drawnWidth = maxOf((width * density).toInt(), 1)
                val drawnHeight = maxOf((height * density).toInt(), 1)
                val fbWidth = MathUtil.mathRoundPoT(drawnWidth)
                val fbHeight = MathUtil.mathRoundPoT(drawnHeight)

                framebuffer = FrameBuffer(Pixmap.Format.RGBA8888, fbWidth, fbHeight, true, false)
                this.framebuffer = framebuffer
                parent.screenWidth = width
                parent.screenHeight = height

                framebufferDrawnWidth = drawnWidth
                framebufferDrawnHeight = drawnHeight
                framebufferDrawnU = drawnWidth.toFloat() / fbWidth.toFloat()
                framebufferDrawnV = drawnHeight.toFloat() / fbHeight.toFloat()
            }

            batch.end()
            framebuffer.bind()
            Gdx.gl20.glViewport(0, 0, framebufferDrawnWidth, framebufferDrawnHeight)
            parent.render()
            framebuffer.end()
            batch.begin()

            batch.color = Color.WHITE

            val fbTexture = framebuffer.colorBufferTexture
            batch.draw(fbTexture, this.x, this.y, this.width, this.height, 0f, 0f, framebufferDrawnU, framebufferDrawnV)

            batch.color = Color.WHITE
            Main.font.draw(batch, "FPS: "+Gdx.graphics.framesPerSecond, this.x + 10f, this.y + 10f)
        }

        override public fun sizeChanged() {
            super.sizeChanged()
            framebuffer?.dispose()
            framebuffer = null
        }
    }

    override fun dispose() {
        canvas.framebuffer?.dispose()
        screenMesh.dispose()
    }

    private enum class FBFactor(val factor:Float, val displayName:String) {
        SUB8(1f/8f, "Subsample 8x"),
        SUB4(1f/4f, "Subsample 4x"),
        SUB2(1f/2f, "Subsample 2x"),
        ONE(1f, "1:1"),
        SUP2(2f, "Supersample 2x"),
        SUP4(4f, "Supersample 4x");

        override fun toString(): String = displayName
    }
}