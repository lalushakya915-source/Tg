package com.tgdownloader.app;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

public final class VideoSaver {
    private VideoSaver() {}

    public static boolean save(Context context, File source, String requestedName) {
        if (!source.exists()) return false;

        String name = cleanName(requestedName);
        if (!name.toLowerCase().endsWith(".mp4")) name += ".mp4";

        ContentResolver resolver = context.getContentResolver();

        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.DISPLAY_NAME, name);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/TGDownloader");
        values.put(MediaStore.Video.Media.IS_PENDING, 1);

        Uri uri = resolver.insert(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            values
        );
        if (uri == null) return false;

        try (FileInputStream in = new FileInputStream(source);
             OutputStream out = resolver.openOutputStream(uri)) {

            if (out == null) throw new Exception("Output stream unavailable");

            byte[] buffer = new byte[1024 * 1024];
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }

            ContentValues done = new ContentValues();
            done.put(MediaStore.Video.Media.IS_PENDING, 0);
            resolver.update(uri, done, null, null);
            return true;

        } catch (Exception e) {
            resolver.delete(uri, null, null);
            return false;
        }
    }

    private static String cleanName(String name) {
        if (name == null || name.trim().isEmpty()) return "video.mp4";
        return name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }
}
