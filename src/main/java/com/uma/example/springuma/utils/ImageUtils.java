package com.uma.example.springuma.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Utility helpers to compress and decompress image byte arrays.
 * Methods now propagate IOExceptions instead of silently swallowing errors.
 */
public class ImageUtils {

    public static byte[] compressImage(byte[] data) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setLevel(Deflater.BEST_COMPRESSION);
        deflater.setInput(data);
        deflater.finish();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
            byte[] tmp = new byte[4 * 1024];
            while (!deflater.finished()) {
                int size = deflater.deflate(tmp);
                outputStream.write(tmp, 0, size);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw e;
        } finally {
            deflater.end();
        }
    }

    public static byte[] decompressImage(byte[] data) throws IOException {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
            byte[] tmp = new byte[4 * 1024];
            while (!inflater.finished()) {
                int count;
                try {
                    count = inflater.inflate(tmp);
                } catch (Exception e) {
                    throw new IOException("Failed to decompress image", e);
                }
                outputStream.write(tmp, 0, count);
            }
            return outputStream.toByteArray();
        } finally {
            inflater.end();
        }
    }

}
