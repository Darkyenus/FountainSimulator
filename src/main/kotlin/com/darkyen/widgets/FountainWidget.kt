package com.darkyen.widgets

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.darkyen.FountainData
import com.darkyen.Main
import com.darkyen.util.AutoReloadShaderProgram

/**
 *
 */
class FountainWidget(val timeline: TimelineWidget, skin: Skin) : Widget() {

    private val font = skin.getFont("font-ui-big").newFontCache().apply {
        setColor(0.97f, 0.97f, 0.97f, 1f)
    }
    private val white = skin.getRegion("white")


    val data = FountainData()

    init {
        refreshTimeline()
    }

    fun refreshTimeline() {
        timeline.totalTime = data.cycleTime
    }

    private fun renderFountainBackground(batch: Batch, x:Float, y:Float, w:Float, h:Float) {
        val bg = Background.NIGHT
        val bgTexture = bg.texture

        val xScale = w / bg.w.toFloat()
        val yScale = h / bg.h.toFloat()

        val bgX = x - bg.x * xScale
        val bgY = y - (bgTexture.regionHeight - bg.y - bg.h) * yScale
        val bgW = bgTexture.regionWidth * xScale
        val bgH = bgTexture.regionHeight * yScale

        batch.setColor(1f, 1f, 1f, 1f)
        batch.draw(bgTexture, bgX, bgY, bgW, bgH)
    }

    private fun renderFountain(batch: Batch, time:Float, x:Float, y:Float, w:Float, h:Float) {
        val shader = fountainShader

        batch.shader = shader
        val oldBlendSrcFunc = batch.blendSrcFunc
        val oldBlendDstFunc = batch.blendDstFunc
        batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE)
        batch.setColor(1f, 1f, 1f, 1f)

        val normalizedAcceleration = 2f/(FountainData.FOUNTAIN_FALL_TIME * FountainData.FOUNTAIN_FALL_TIME)

        shader.setUniformf("time", time)
        shader.setUniformf("acceleration", normalizedAcceleration)
        shader.setUniformf("totalTextureHeight", data.cycleTime)
        shader.setUniformi("colorTexture", 1)

        val waterColor = fountainColorTexture
        waterColor.texture.bind(1)
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0)
        shader.setUniformf("colorTextureCoords",
                (waterColor.u + waterColor.u2) * 0.5f,
                waterColor.v,
                waterColor.v2)

        var textureOffset = 0f
        val textureOffsetLocation = shader.getUniformLocation("textureOffset")
        val textureHeightLocation = shader.getUniformLocation("textureHeight")

        for (i in 0 until data.count()) {
            val textureHeight = data.getHeight(i) * FountainData.FOUNTAIN_TIME_PER_DROPLET
            shader.setUniformf(textureOffsetLocation, textureOffset)
            shader.setUniformf(textureHeightLocation, textureHeight)

            batch.draw(data.get(i), x, y, w, h)
            batch.flush()

            textureOffset += textureHeight
        }

        batch.shader = null
        batch.setBlendFunction(oldBlendSrcFunc, oldBlendDstFunc)
    }

    private fun renderDefaultDataInfo(batch: Batch) {
        batch.setColor(0.5f, 0.5f, 0.5f, 0.4f)
        batch.draw(white, x, y, width, height)

        font.setText("Drag your pattern image here", x, y + height/2f + font.font.capHeight/2f, width, Align.center, true)
        font.draw(batch)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        validate()

        val padding = 25f
        val padding2 = padding + padding

        val dimensions = Scaling.fit.apply(
                FountainData.FOUNTAIN_WIDTH.toFloat(),
                FountainData.FOUNTAIN_HEIGHT.toFloat(),
                width - padding2,
                height - padding2)

        val x = padding + x + (width - padding2 - dimensions.x)/2f
        val y = padding + y + (height - padding2 - dimensions.y)/2f
        val w = dimensions.x
        val h = dimensions.y

        renderFountainBackground(batch, x, y, w, h)
        renderFountain(batch, timeline.now, x, y, w, h)

        if (data.defaultData) {
            renderDefaultDataInfo(batch)
        }
    }

    private companion object {
        val fountainColorTexture by lazy(LazyThreadSafetyMode.NONE) {
            Main.skin.getRegion("water-color")
        }

        val fountainShader by lazy(LazyThreadSafetyMode.NONE) {
            AutoReloadShaderProgram(Gdx.files.internal("shaders/fountain.vert"), Gdx.files.internal("shaders/fountain.frag"))
        }
    }

    /**
     * @param x from left
     * @param y from top
     */
    enum class Background(textureName:String, val x:Int, val y:Int, val w:Int, val h:Int) {
        NIGHT("background-night", 280, 228, 1356, 448);

        val texture: TextureRegion by lazy(LazyThreadSafetyMode.NONE) { Main.skin.getRegion(textureName) }
    }
}