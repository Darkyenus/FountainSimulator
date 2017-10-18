package com.darkyen.widgets

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.darkyen.FountainData
import com.darkyen.Objects

/**
 *
 */
class DataLineWidget(private val skin: Skin, private val data: FountainData) : WidgetGroup() {

    private val moveLeftStyle = Button.ButtonStyle(
            skin.getDrawable("move-left"),
            skin.newDrawable("move-left", 0.9f, 0.9f, 0.9f, 1f),
            skin.newDrawable("move-left", 0.8f, 0.8f, 0.8f, 1f)
    )

    private val moveRightStyle = Button.ButtonStyle(
            skin.getDrawable("move-right"),
            skin.newDrawable("move-right", 0.9f, 0.9f, 0.9f, 1f),
            skin.newDrawable("move-right", 0.8f, 0.8f, 0.8f, 1f)
    )

    private val removeStyle = Button.ButtonStyle(
            skin.getDrawable("remove"),
            skin.newDrawable("remove", 0.9f, 0.9f, 0.9f, 1f),
            skin.newDrawable("remove", 0.8f, 0.8f, 0.8f, 1f)
    )

    fun refreshChildren() {
        while (children.size < data.count()) {
            addActor(DataWidget(0, 0f))
        }
        while (children.size > data.count()) {
            children[children.size - 1].remove()
        }

        val totalTime = data.totalHeight

        @Suppress("UNCHECKED_CAST")
        (children as Objects<DataWidget>).forEachIndexed { index, actor ->
            actor.tooltip.hide()
            actor.removeListener(actor.tooltip)
            actor.index = index
            actor.label.setText(data.files[index].nameWithoutExtension())
            val height = data.getHeight(index)
            actor.weight = height / totalTime

            val problems = StringBuilder()
            val width = data.textures[index].originalWidth
            if (width != FountainData.FOUNTAIN_RESOLUTION_WIDTH) {
                problems.append("Width is ").append(width).append(", preferred width is ").append(FountainData.FOUNTAIN_RESOLUTION_WIDTH).append('\n')
            }
            if (height > FountainData.FOUNTAIN_RESOLUTION_MAX_HEIGHT) {
                // OpenGL will probably reject such image anyway...
                problems.append("Height is ").append(height).append(", which is more than supported maximum of ").append(FountainData.FOUNTAIN_RESOLUTION_MAX_HEIGHT).append('\n')
            }

            if (problems.isEmpty()) {
                actor.background = dataWidgetBackgroundOK
            } else {
                actor.background = dataWidgetBackgroundNotOK
                problems.setLength(problems.length - 1)
                actor.tooltipLabel.setText(problems)
                actor.addListener(actor.tooltip)
            }
        }

        invalidate()
    }

    override fun layout() {
        val padding = 2
        val availableWidth = width - (children.size - 1) * padding

        var x = 0f
        for (child in children) {
            val w = (child as DataWidget).weight * availableWidth
            child.setBounds(x, 0f, w, height)

            x += w + padding
        }
    }

    private fun moveLeft(index: Int) {
        val to = (index - 1 + data.count()) % data.count()

        data.files.swap(index, to)
        data.textures.swap(index, to)
        refreshChildren()
    }

    private fun moveRight(index: Int) {
        val to = (index + 1) % data.count()

        data.files.swap(index, to)
        data.textures.swap(index, to)
        refreshChildren()
    }

    private fun remove(index: Int) {
        data.remove(index)
        refreshChildren()
    }

    private val dataWidgetBackgroundOK = skin.newDrawable("white", 0.5f, 0.5f, 0.5f, 1f)
    private val dataWidgetBackgroundNotOK = skin.newDrawable("white", 0.6f, 0.4f, 0.4f, 1f)

    init {
        data.listen {
            refreshChildren()
        }
    }

    private inner class DataWidget(var index:Int, var weight:Float) : Table(this@DataLineWidget.skin) {

        val label = Label("", skin, "font-ui-small", Color(0.94f, 0.94f, 0.94f, 1f))

        val tooltipLabel = Label("", skin, "font-ui-small", Color(0.94f, 0.94f, 0.94f, 1f))
        val tooltip = Tooltip<Label>(tooltipLabel)

        init {
            background = dataWidgetBackgroundOK
            tooltip.container.background = dataWidgetBackgroundOK
            tooltip.container.pad(5f)
            tooltip.setInstant(true)

            val moveLeft = Button(moveLeftStyle)
            moveLeft.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    moveLeft.isChecked = false
                    moveLeft(index)
                }
            })
            val moveRight = Button(moveRightStyle)
            moveRight.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    moveRight.isChecked = false
                    moveRight(index)
                }
            })

            val remove = Button(removeStyle)
            remove.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    remove.isChecked = false
                    remove(index)
                }
            })

            label.setEllipsis(true)
            label.setWrap(true)

            defaults().pad(2f)

            add(moveLeft)
            add(moveRight)
            add(label).grow()
            add(remove)
        }
    }
}