package com.example.myapplication.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class BitmapUtil {
    private final static String TAG = "BitmapUtil";

    // 把位图数据保存到指定路径的图片文件
    public static void saveImage(String path, Bitmap bitmap) {
        // 根据指定的文件路径构建文件输出流对象
        try (FileOutputStream fos = new FileOutputStream(path)) {
            // 把位图数据压缩到文件输出流中
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 获得旋转角度之后的位图对象
    public static Bitmap getRotateBitmap(Bitmap bitmap, float rotateDegree) {
        Matrix matrix = new Matrix(); // 创建操作图片用的矩阵对象
        matrix.postRotate(rotateDegree); // 执行图片的旋转动作
        // 创建并返回旋转后的位图对象
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, false);
    }

    // 获得比例缩放之后的位图对象
    public static Bitmap getScaleBitmap(Bitmap bitmap, double scaleRatio) {
        int new_width = (int) (bitmap.getWidth() * scaleRatio);
        int new_height = (int) (bitmap.getHeight() * scaleRatio);
        // 创建并返回缩放后的位图对象
        return Bitmap.createScaledBitmap(bitmap, new_width, new_height, false);
    }

    // 获得自动缩小后的位图对象
    public static Bitmap getAutoZoomImage(Context ctx, Uri uri) {
        Log.d(TAG, "getAutoZoomImage uri="+uri.toString());
        Bitmap zoomBitmap = null;
        // 打开指定uri获得输入流对象
        try (InputStream is = ctx.getContentResolver().openInputStream(uri)) {
            // 从输入流解码得到原始的位图对象
            Bitmap originBitmap = BitmapFactory.decodeStream(is);
            int ratio = originBitmap.getWidth()/2000+1;
            // 获得比例缩放之后的位图对象
            zoomBitmap = BitmapUtil.getScaleBitmap(originBitmap, 1.0/ratio);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return zoomBitmap;
    }

    // 获得自动缩小后的图片路径
    public static String getAutoZoomPath(Context ctx, Uri uri) {
        Log.d(TAG, "getAutoZoomPath uri="+uri.toString());
        String imagePath = String.format("%s/%s.jpg",
                ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString(),
                DateUtil.getNowDateTime());
        // 打开指定uri获得输入流对象
        try (InputStream is = ctx.getContentResolver().openInputStream(uri)) {
            // 从输入流解码得到原始的位图对象
            Bitmap originBitmap = BitmapFactory.decodeStream(is);
            int ratio = originBitmap.getWidth()/1000+1;
            // 获得比例缩放之后的位图对象
            Bitmap zoomBitmap = getScaleBitmap(originBitmap, 1.0/ratio);
            saveImage(imagePath, zoomBitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imagePath;
    }

    public static Bitmap drawFaceFrame(Rect location, Bitmap bitmap) {
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((location.right - location.left) / 50);
        canvas.drawRect(location, paint); // 画人脸相框
        return bitmap;
    }

    public static Uri getPureUri(Uri originUri) {
        Uri uri = originUri;
        String path = uri.toString();
        if (path.contains("providers")) {
            String imageId;
            if (path.contains("%3A")) { // %3A为斜杆“/”的转义符
                imageId = path.substring(path.lastIndexOf("%3A")+3);
            } else {
                imageId = path.substring(path.lastIndexOf("/")+1);
            }
            uri = Uri.parse("content://media/external/images/media/"+imageId);
        }
        return uri;
    }

    public static void viewAlbum(Context ctx, String imagePath) {
        File imgFile = new File(imagePath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri imgUri = FileProvider.getUriForFile(ctx, ctx.getPackageName() + ".provider", imgFile);
        Log.d(TAG, "image uri:" + imgUri.toString());
        intent.setDataAndType(imgUri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        ctx.startActivity(intent);
    }

    // 通知相册来了张新图片
    public static void notifyPhotoAlbum(Context ctx, String filePath) {
        try {
            String fileName = filePath.substring(filePath.lastIndexOf("/")+1);
            MediaStore.Images.Media.insertImage(ctx.getContentResolver(),
                    filePath, fileName, null);
            Uri uri = Uri.parse("file://" + filePath);
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
            ctx.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
