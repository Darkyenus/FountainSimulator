package com.darkyen;

import com.badlogic.gdx.graphics.Pixmap;

/**
 *
 */
public class BMPLoader {

    private static int r4(final byte[] data, int offset) {
        return (data[offset] & 0xFF) | ((data[offset+1] & 0xFF) << 8) | ((data[offset+2] & 0xFF) << 16) | ((data[offset+3] & 0xFF) << 24);
    }

    private static int r2(final byte[] data, int offset) {
        return (data[offset] & 0xFF) | ((data[offset+1] & 0xFF) << 8);
    }

    private static void checkRange(byte[] data, int index, int size, String section) throws BMPLoaderException {
        if (index < 0 || index + size > data.length) {
            throw new BMPLoaderException("Section '"+section+"' is missing or incomplete");
        }
    }

    private static void checkValue(int current, int expected, String field) throws BMPLoaderException {
        if (current != expected) {
            throw new BMPLoaderException("Invalid "+field+", expected "+expected+" got "+current);
        }
    }

    public static Pixmap load1bppBMP(final byte[] data) throws BMPLoaderException {
        // Main header
        checkRange(data, 0, 14, "header");
        checkValue(r2(data, 0), 'B' | ('M' << 8), "header");

        final int fileSize = r4(data, 2);
        final int bitmapOffset = r4(data, 10);
        checkRange(data, 0, fileSize, "file");

        // DIB header
        checkRange(data, 14, 4, "DIB header size");
        final int dibHeaderSize = r4(data, 14);
        checkRange(data, 14, dibHeaderSize, "DIB header");

        final int width;
        final int heightRaw;
        final int bpp;

        final int colorsInPaletteRaw;

        if (dibHeaderSize == 12) {
            // BITMAP CORE HEADER
            width = r2(data, 18);
            heightRaw = r2(data, 20);
            bpp = r2(data, 24);

            checkValue(r2(data, 22), 1, "color plane count");

            colorsInPaletteRaw = 0;
        } else if (dibHeaderSize >= 40) {
            // BITMAP INFO HEADER or newer
            width = r4(data, 18);
            heightRaw = r4(data, 22);
            checkValue(r2(data, 26), 1, "color plane count");

            bpp = r2(data, 28);

            checkValue(r4(data, 30), 0, "compression");

            colorsInPaletteRaw = r4(data, 46);
        } else {
            throw new BMPLoaderException("Unsupported header of size "+dibHeaderSize);
        }

        final int colorsInPalette = colorsInPaletteRaw == 0 ? 1 << bpp : colorsInPaletteRaw;

        final int height = Math.abs(heightRaw);
        final boolean directionTopToBottom = heightRaw < 0;

        if (bpp != 1) {
            // We load only 1bpp images
            return null;
        }

        // Read palette

        // in RGBA8888 ints
        final int[] palette = new int[colorsInPalette];

        {
            int offset = 14 + dibHeaderSize;
            checkRange(data, offset, colorsInPalette * 4, "palette");

            for (int i = 0; i < colorsInPalette; i++) {
                final int blue = data[offset + i * 4] & 0xFF;
                final int green = data[offset + i * 4] & 0xFF;
                final int red = data[offset + i * 4] & 0xFF;
                final int rgba = (red << 24) | (green << 16) | (blue << 8) | 0xFF;

                palette[i] = rgba;
            }
        }

        // Read data!
        final Pixmap pixmap;
        {
            final int rowSizeBytes = ((bpp * width + 31) / 32) * 4;
            checkRange(data, bitmapOffset, rowSizeBytes * height, "image data");

            pixmap = new Pixmap(width, height, Pixmap.Format.RGB888);
            int offset = bitmapOffset;
            if (directionTopToBottom) {
                for (int y = 0; y < height; y++) {
                    readLine(pixmap, data, offset, y, width, palette, bpp);
                    offset += rowSizeBytes;
                }
            } else {
                for (int y = height - 1; y >= 0; y--) {
                    readLine(pixmap, data, offset, y, width, palette, bpp);
                    offset += rowSizeBytes;
                }
            }
        }

        // Done
        return pixmap;
    }

    private static void readLine(Pixmap pixmap, byte[] data, int offset,
                                 int y, int width,
                                 int[] palette, int bpp) {
        final int mask = (1 << bpp) - 1;

        for (int x = 0; x < width; x++) {
            final int rawBitIndex = x * bpp;
            final int byteIndex = rawBitIndex >> 3;
            final int bitIndex = rawBitIndex & 7;

            final int value = (data[offset + byteIndex] >>> (8 - bitIndex - bpp)) & mask;
            final int color = palette[value];

            pixmap.drawPixel(x, y, color);
        }
    }

    public static class BMPLoaderException extends Exception {
        private BMPLoaderException(String message) {
            super(message);
        }
    }
}
