package com.darkyen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.glutils.PixmapTextureData

/**
 *
 */
class FountainData {

    var defaultData = true
        private set

    val files = Objects<FileHandle>(true, 16)
    val textures = Objects<Texture>(true, 16)

    fun add(file: FileHandle) {
        defaultData = false
        val alreadyAt = files.indexOf(file, false)
        files.add(file)
        if (alreadyAt >= 0) {
            textures.add(textures[alreadyAt])
        } else {
            textures.add(createTexture(file))
        }
    }

    fun getHeight(index: Int):Float {
        val texture = textures[index]
        return (1024f / texture.width) * texture.height
    }

    fun get(index:Int):Texture {
        return textures[index]
    }

    fun remove(index:Int) {
        defaultData = false
        files.removeIndex(index)
        val removedTexture = textures.removeIndex(index)
        if (!textures.contains(removedTexture, true)) {
            removedTexture.dispose()
        }
    }

    fun count():Int = textures.size

    val totalHeight:Float
        get() {
            var totalHeight = 0f
            for (i in 0 until count()) {
                totalHeight += getHeight(i)
            }
            return totalHeight
        }

    val cycleTime:Float
        get() = totalHeight * FOUNTAIN_TIME_PER_DROPLET

    init {
        add(Gdx.files.internal("fountain-default-image.png"))
        add(Gdx.files.internal("fountain-default-transition.png"))
        defaultData = true
    }

    private fun sanitizePixmap(pixmap: Pixmap, maxWidth:Int = 1024):Pixmap {
        val p:Pixmap

        // Resize
        p = if (pixmap.width > maxWidth) {
            val scaledPixmap = Pixmap(maxWidth, Math.round(pixmap.height.toFloat() * maxWidth.toFloat() / pixmap.width.toFloat()), pixmap.format)

            println("Resizing from " + pixmap.width + "x" + pixmap.height + " to " + scaledPixmap.width + "x" + scaledPixmap.height)

            Pixmap.setBlending(Pixmap.Blending.None)
            Pixmap.setFilter(Pixmap.Filter.NearestNeighbour)
            scaledPixmap.drawPixmap(pixmap, 0, 0, pixmap.width, pixmap.height, 0, 0, scaledPixmap.width, scaledPixmap.height)

            pixmap.dispose()
            scaledPixmap
        } else {
            pixmap
        }

        val width = p.width
        val height = p.height

        var and = -1
        var or = 0

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pix = p.getPixel(x, y)

                and = and and pix
                or = or or pix
            }
        }

        val aR = (and ushr 24) and 0xFF
        val aG = (and ushr 16) and 0xFF
        val aB = (and ushr 8) and 0xFF
        val aA = (and ushr 0) and 0xFF

        val oR = (or ushr 24) and 0xFF
        val oG = (or ushr 16) and 0xFF
        val oB = (or ushr 8) and 0xFF
        val oA = (or ushr 0) and 0xFF

        val result = Pixmap(width, height, Pixmap.Format.RGB888)

        if (aA != oA) {
            // Data is in alpha
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val a = p.getPixel(x, y) and 0xFF
                    result.drawPixel(x, y, (a shl 24) or (a shl 16) or (a shl 8) or 0xFF)
                }
            }
        } else {
            // Data is in rgb
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val rgba = p.getPixel(x, y) or (0xFF)
                    result.drawPixel(x, y, rgba)
                }
            }
        }

        p.dispose()
        return result
    }

    private fun createTexture(file: FileHandle):Texture {
        var pixmap:Pixmap? = null

        if (file.extension().equals("bmp", ignoreCase = true)) {
            // we will have to load it ourselves, maybe
            try {
                val bmp = BMPLoader.load1bppBMP(file.readBytes())
                if (bmp != null) {
                    pixmap = bmp
                }
            } catch (b:BMPLoader.BMPLoaderException) {
                System.err.println("Failed to load BMP: "+b)
            }
        }

        if (pixmap == null) {
            pixmap = Pixmap(file)
        }

        pixmap = sanitizePixmap(pixmap)

        val texture = Texture(PixmapTextureData(pixmap, null, false, true))
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
        return texture
    }

    companion object {

        val FOUNTAIN_WIDTH = 1024
        val FOUNTAIN_HEIGHT = 280

        //val FOUNTAIN_HEIGHT_METERS = 3.4f

        //val FOUNTAIN_VERTICAL_DROPLETS = 244.4f

        /** Seconds */
        val FOUNTAIN_FALL_TIME = 0.83333f

        // Data measured experimentally, slightly more accurate version of FOUNTAIN_FALL_TIME / FOUNTAIN_VERTICAL_DROPLETS
        val FOUNTAIN_TIME_PER_DROPLET = 22.4f / 7056f
    }
}