package com.darkyen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.TextureData
import com.badlogic.gdx.graphics.glutils.PixmapTextureData
import com.badlogic.gdx.utils.ObjectIntMap

/**
 *
 */
class FountainData {

    var message = ""
        private set

    var messageTimeoutIn = Float.POSITIVE_INFINITY

    val files = Objects<FileHandle>(true, 16)
    val textures = Objects<TextureWithOriginalSize>(true, 16)

    /**
     * Synchronized access!
     *
     * Stores last 32 bits of file handle last modified time
     */
    private val reloaderTimeCache = ObjectIntMap<FileHandle>()

    private val listeners = Objects<Runnable>()

    init {
        listen(false) {
            if (messageTimeoutIn == Float.POSITIVE_INFINITY) {
                messageTimeoutIn = 1f
            }
        }

        if (!ARGS.containsKey("-no-reload")) {
            val reloader = Thread({
                Gdx.app.log(LOG, "Live-reloading initialized")
                while (true) {
                    try {
                        synchronized(reloaderTimeCache) {
                            for (entry in reloaderTimeCache) {
                                val file = entry.key
                                val timestamp = entry.value

                                if (file.exists()) {
                                    val newTimestamp = file.file().lastModified().toInt()

                                    if (timestamp != newTimestamp) {
                                        reloaderTimeCache.put(file, newTimestamp)
                                        Gdx.app.log(LOG, "Reloading "+file.name())
                                        Gdx.app.postRunnable {
                                            reload(file)
                                        }
                                    }
                                }
                            }
                        }

                        Thread.sleep(1000)
                    } catch (e:Exception) {
                        Gdx.app.error(LOG, "Reloader error", e)
                    }
                }
            }, "Image reloader")

            reloader.isDaemon = true
            reloader.start()
        }
    }

    private fun reload(file: FileHandle) {
        val newTexture = createTexture(file)
        if (newTexture == null) {
            message = "Image failed to reload"
            messageTimeoutIn = 5f
            return
        }

        var reloaded = 0
        for (i in 0 until textures.size) {
            if (files[i] == file) {
                textures[i].dispose()
                textures[i] = newTexture
                reloaded += 1
            }
        }

        if (reloaded != 0) {
            changed()

            message = file.name()+" reloaded"
            messageTimeoutIn = 1f
        } else {
            newTexture.dispose()
        }
    }

    fun add(file: FileHandle) {
        val alreadyAt = files.indexOf(file, false)
        if (alreadyAt >= 0) {
            files.add(file)
            textures.add(textures[alreadyAt])

            changed()
        } else {
            val texture = createTexture(file)
            if (texture == null) {
                message = "Image failed to load"
                messageTimeoutIn = 5f
            } else {
                files.add(file)
                textures.add(texture)

                synchronized(reloaderTimeCache) {
                    reloaderTimeCache.put(file, file.file().lastModified().toInt())
                }

                changed()
            }
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
        val file = files.removeIndex(index)
        val removedTexture = textures.removeIndex(index)
        if (!textures.contains(removedTexture, true)) {
            removedTexture.dispose()

            synchronized(reloaderTimeCache) {
                reloaderTimeCache.remove(file, 0)
            }
        }

        changed()
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

        message = "Drag your pattern image here"
        messageTimeoutIn = Float.POSITIVE_INFINITY
    }

    private fun sanitizePixmap(pixmap: Pixmap, maxWidth:Int = 1024):Pixmap {
        val p:Pixmap

        // Resize
        p = if (pixmap.width > maxWidth) {
            val scaledPixmap = Pixmap(maxWidth, Math.round(pixmap.height.toFloat() * maxWidth.toFloat() / pixmap.width.toFloat()), pixmap.format)

            Gdx.app.debug(LOG, "Resizing from ${toString(pixmap)} to ${toString(scaledPixmap)}")

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

        //val aR = (and ushr 24) and 0xFF
        //val aG = (and ushr 16) and 0xFF
        //val aB = (and ushr 8) and 0xFF
        val aA = (and ushr 0) and 0xFF

        //val oR = (or ushr 24) and 0xFF
        //val oG = (or ushr 16) and 0xFF
        //val oB = (or ushr 8) and 0xFF
        val oA = (or ushr 0) and 0xFF

        val result = Pixmap(width, height, Pixmap.Format.RGB888)

        if (aA != oA) {
            // Data is in alpha
            Gdx.app.debug(LOG, "${toString(p)} has data in alpha")
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val a = p.getPixel(x, y) and 0xFF
                    result.drawPixel(x, y, (a shl 24) or (a shl 16) or (a shl 8) or 0xFF)
                }
            }
        } else {
            // Data is in rgb
            Gdx.app.debug(LOG, "${toString(p)} has data in RGB")
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val rgba = p.getPixel(x, y) or (0xFF)
                    result.drawPixel(x, y, rgba)
                }
            }
        }

        Gdx.app.debug(LOG, "will dispose ${toString(p)}")
        p.dispose()
        Gdx.app.debug(LOG, "${toString(pixmap)} sanitized to ${toString(result)}")
        return result
    }

    private fun createTexture(file: FileHandle):TextureWithOriginalSize? {
        Gdx.app.debug(LOG, "Creating texture for "+file)
        try {
            var pixmap:Pixmap? = null

            if (file.extension().equals("bmp", ignoreCase = true)) {
                // we will have to load it ourselves, maybe
                try {
                    Gdx.app.debug(LOG, "trying to load $file as bmp")
                    val bmp = BMPLoader.load1bppBMP(file.readBytes())
                    if (bmp != null) {
                        pixmap = bmp
                    }
                } catch (b:BMPLoader.BMPLoaderException) {
                    Gdx.app.error(LOG, "Failed to load BMP", b)
                }
            }

            if (pixmap == null) {
                Gdx.app.debug(LOG, "creating default pixmap for $file")
                pixmap = Pixmap(file)
            }

            val originalWidth = pixmap.width
            val originalHeight = pixmap.height

            Gdx.app.debug(LOG, "sanitizing ${toString(pixmap)}")
            pixmap = sanitizePixmap(pixmap)

            Gdx.app.debug(LOG, "creating texture from ${toString(pixmap)}")
            val texture = TextureWithOriginalSize(
                    PixmapTextureData(pixmap, null, false, true),
                    originalWidth, originalHeight)
            texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
            Gdx.app.debug(LOG, "texture ${toString(texture)} created")
            return texture
        } catch (e:Exception) {
            Gdx.app.error(LOG, "Failed to load texture from "+file, e)
            return null
        }
    }

    companion object {

        private val LOG = "FountainData"

        val FOUNTAIN_WIDTH = 1024
        val FOUNTAIN_HEIGHT = 338

        val FOUNTAIN_RESOLUTION_WIDTH = 1024
        val FOUNTAIN_RESOLUTION_MAX_HEIGHT = 65536

        private val FOUNTAIN_HEIGHT_METERS = 4f

        /** Seconds */
        val FOUNTAIN_FALL_TIME = Math.sqrt(FOUNTAIN_HEIGHT_METERS/ (0.5 * 9.81)).toFloat()

        // Time it takes to "print" out single pixel, in seconds
        val FOUNTAIN_TIME_PER_DROPLET = 28.27f / 4350f
    }

    fun listen(runImmediately:Boolean = true, r:() -> Unit) {
        synchronized(listeners) {
            listeners.add(Runnable { r() })
        }

        if (runImmediately) {
            r()
        }
    }

    private fun changed() {
        synchronized(listeners) {
            for (listener in listeners) {
                listener.run()
            }
        }
    }

    class TextureWithOriginalSize(data:TextureData, val originalWidth:Int, val originalHeight:Int) : Texture(data)
}