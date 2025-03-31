package com.example.z.utils;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.FileUtils;
import android.provider.OpenableColumns;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ImgUtil {
    // 主压缩方法（默认64KB限制）
    public static String compressToBase64(String filePath) throws IOException {
        return compressToBase64(filePath, 64 * 1024);
    }

    // 完整参数版压缩方法
    private static String compressToBase64(String filePath, int maxSizeBytes) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // 计算初始采样率
        options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight);
        options.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        if (bitmap == null) {
            throw new IOException("Failed to decode image");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int quality = 85;
        Bitmap currentBitmap = bitmap;

        try {
            while (quality >= 30) {
                outputStream.reset();
                currentBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);

                if (outputStream.size() <= maxSizeBytes) {
                    break;
                }

                // 质量压缩效果不足时调整尺寸
                if (quality <= 50) {
                    currentBitmap = resizeBitmap(currentBitmap, 0.75f);
                    quality = 85; // 重置质量参数
                } else {
                    quality -= 10;
                }
            }

            // 最终检查尺寸限制
            if (outputStream.size() > maxSizeBytes) {
                throw new IOException("Compression failed to meet size target");
            }

            return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP);
        } finally {
            bitmap.recycle();
            if (currentBitmap != bitmap) {
                currentBitmap.recycle();
            }
            outputStream.close();
        }
    }

    // 尺寸缩放方法
    private static Bitmap resizeBitmap(Bitmap src, float scaleFactor) {
        int newWidth = (int) (src.getWidth() * scaleFactor);
        int newHeight = (int) (src.getHeight() * scaleFactor);
        return Bitmap.createScaledBitmap(src, newWidth, newHeight, true);
    }

    // 内存优化采样率计算
    private static int calculateInSampleSize(int width, int height) {
        int longSide = Math.max(width, height);

        if (longSide > 4096) return 8;
        if (longSide > 2048) return 4;
        if (longSide > 1024) return 2;
        return 1;
    }


    public static Bitmap base64ToBitmap(String base64Str) {
        try {
            // 去除可能的URI前缀（如："data:image/png;base64,"）
            String pureBase64 = base64Str.substring(base64Str.indexOf(",") + 1);

            byte[] decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT);
            InputStream inputStream = new ByteArrayInputStream(decodedBytes);

            // 内存优化配置
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565; // 减少内存占用
            options.inSampleSize = calculateSampleSize(decodedBytes.length);

            return BitmapFactory.decodeStream(inputStream, null, options);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 自动设置图片到ImageView
    public static void displayBase64Image(String base64, ImageView imageView) {
        Bitmap bitmap = base64ToBitmap(base64);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {

        }
    }

    // 内存优化计算采样率
    private static int calculateSampleSize(int byteSize) {
        // 按字节大小自动计算（假设目标内存控制在2MB以内）
        int targetBytes = 2 * 1024 * 1024;
        int sampleSize = 1;

        while (byteSize / (sampleSize * sampleSize) > targetBytes) {
            sampleSize *= 2;
        }
        return sampleSize;
    }

    @SuppressLint("Range")
    private static String getFileName(Context context, Uri uri) {
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        return "not a content";
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static String createTempFile(Context context, Uri uri) throws IOException {
        try (InputStream is = context.getContentResolver().openInputStream(uri)) {
            File tempFile = new File(context.getExternalCacheDir(), getFileName(context, uri));
            FileUtils.copy(is, new FileOutputStream(tempFile));
            return tempFile.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}