package cn.edu.ccibe.alst.uitls;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;

public class BitmapUtil {
    public static void bitmapToImage(Bitmap bitmap, File file, Bitmap.CompressFormat format) {
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(format, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
