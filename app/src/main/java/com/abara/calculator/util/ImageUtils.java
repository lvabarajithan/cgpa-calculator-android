/*
 * Copyright (C) 2016 Abarajithan Lv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.abara.calculator.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by abara on 8/30/2015.
 *
 * Utility class for Image.
 */
public class ImageUtils {

    // Snippets currently not needed
    public static Bitmap loadImageFromLocal(Context context) {

        Bitmap image;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.parse(Environment.getExternalStorageDirectory().toString() + PreferenceIds.IMAGE_LOCATION_PATH);
            mediaScanIntent.setData(contentUri);
            context.sendBroadcast(mediaScanIntent);
        } else {
            context.sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://"
                            + Environment.getExternalStorageDirectory())));
        }

        try {
            File file = new File(Environment.getExternalStorageDirectory() + PreferenceIds.IMAGE_LOCATION_PATH);
            image = BitmapFactory.decodeFile(file.getAbsolutePath());
        } catch (Exception e) {
            image = null;
        }
        return image;
    }

    // Snippets currently not needed
    public static void saveImageToLocal(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + PreferenceIds.IMAGE_LOCATION_DIR);
        myDir.mkdirs();
        String fname = PreferenceIds.IMAGE_NAME_FORMAT;
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 50, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void deleteImageFromLocal() {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + PreferenceIds.IMAGE_LOCATION_DIR);
        myDir.mkdirs();
        String fname = PreferenceIds.IMAGE_NAME_FORMAT;
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
    }

    public static void initMediaScanner(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.parse(Environment.getExternalStorageDirectory().toString() + PreferenceIds.IMAGE_LOCATION_PATH);
            mediaScanIntent.setData(contentUri);
            context.sendBroadcast(mediaScanIntent);
        } else {
            context.sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://"
                            + Environment.getExternalStorageDirectory())));
        }
    }

}
