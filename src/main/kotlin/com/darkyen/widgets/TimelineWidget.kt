package com.darkyen.widgets

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.scenes.scene2d.utils.DragListener
import com.badlogic.gdx.utils.Align

/**
 *
 */
class TimelineWidget(skin:Skin) : Widget() {

    private val white = skin.getRegion("white")
    private val font = skin.getFont("font-ui-small").newFontCache().apply {
        setColor(0.94f, 0.94f, 0.94f, 1f)
    }

    var now = 0f
        set(value) {
            val range = to - from

            if (range <= 0) {
                field = from
            } else {
                var base = (value - from) % range
                if (base < 0f) {
                    base += range
                }
                field = from + base
            }
        }

    var totalTime = 0f
        set(value) {
            field = value
            from = 0f
            to = value
        }

    var from = 0f
        set(value) {
            field = MathUtils.clamp(value, 0f, totalTime)
        }
    var to = 0f
        set(value) {
            field = MathUtils.clamp(value, 0f, totalTime)
        }

    var paused = false
    var pausedForDragging = false

    private fun xToTime(x:Float):Float {
        return MathUtils.clamp((x / this.width) * totalTime, 0f, totalTime)
    }

    init {
        addListener(object : InputListener() {

            var dragging = false

            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (button == Input.Buttons.LEFT) {
                    dragging = true
                    pausedForDragging = true
                    return true
                }
                return false
            }

            override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                if (dragging) {
                    update(x)
                }
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                if (dragging && button == Input.Buttons.LEFT) {
                    update(x)
                    dragging = false
                    pausedForDragging = false
                }
            }

            private fun update(x: Float) {
                if (totalTime <= 0f) {
                    return
                }

                val time = xToTime(x)
                if (time !in from..to) {
                    from = 0f
                    to = totalTime
                }
                now = time
            }
        })

        addListener(object:DragListener() {

            init {
                button = Input.Buttons.RIGHT
            }

            var dragStartTime = 0f

            override fun dragStart(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                dragStartTime = xToTime(x)
            }

            private fun updateDrag(x:Float) {
                val secondTime = xToTime(x)

                val newFrom = Math.min(dragStartTime, secondTime)
                val newTo = Math.max(dragStartTime, secondTime)

                this@TimelineWidget.from = newFrom
                this@TimelineWidget.to = newTo
            }

            override fun dragStop(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                updateDrag(x)
            }

            override fun drag(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                updateDrag(x)
            }
        })
    }

    override fun act(delta: Float) {
        super.act(delta)

        if (!paused && !pausedForDragging && totalTime > 0f) {
            now += delta
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        validate()

        val x = x
        val y = y
        val w = width
        val h = height

        batch.setColor(BACKGROUND_COLOR)
        batch.draw(white, x, y, w, h)

        val textXPad = font.font.lineHeight / 2f

        if (totalTime > 0f) {
            val fromX = x + (from / totalTime) * w
            val nowX = x + (now / totalTime) * w
            val toX = x + (to / totalTime) * w

            if (from > 0f) {
                batch.setColor(DULL_BAR_COLOR)
                batch.draw(white, x, y, fromX - x, h)
            }

            batch.setColor(BAR_COLOR)
            batch.draw(white, fromX, y, nowX - fromX, h)

            if (to < totalTime) {
                batch.setColor(FUTURE_BAR_COLOR)
                batch.draw(white, nowX, y, toX - nowX, h)
            }

            font.setText(timeToString(now), x + textXPad, y + h/2f + font.font.capHeight/2f, w - textXPad*2f, Align.left, false)
            font.draw(batch)
        }

        font.setText(timeToString(totalTime), x + textXPad, y + h/2f + font.font.capHeight/2f, w - textXPad*2f, Align.right, false)
        font.draw(batch)
    }

    private companion object {

        private val BACKGROUND_COLOR = Color.toFloatBits(63, 63, 63, 255)
        private val DULL_BAR_COLOR = Color.toFloatBits(34, 127, 63, 255)
        private val BAR_COLOR = Color.toFloatBits(53, 158, 30, 255)
        private val FUTURE_BAR_COLOR = Color.toFloatBits(173, 181, 119, 255)

        private fun timeToString(time:Float):CharSequence {
            val sb = StringBuilder()

            sb.append(time.toInt() / 60)
            sb.append(":")
            val seconds = time.toInt() % 60
            if (seconds < 10) {
                sb.append('0')
            }
            sb.append(seconds)
            sb.append('.')
            val millis = Math.round((time * 1000) % 1000)
            if (millis < 10) {
                sb.append("00")
            } else if (millis < 100) {
                sb.append('0')
            }
            sb.append(millis)

            return sb
        }
    }
}