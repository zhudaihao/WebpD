package com.example.administrator.webpdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.google.webp.libwebp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * 兼容4.2以下webp
 * 注意打包的动态库只支持arm
 */
public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("webp");
    }

    private ImageView imageView,imageView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.iv);
        imageView2 = findViewById(R.id.iv2);

        //获取读写权限
        checkPermission();

        /**
         * 编码
         */
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.t2);//png图片
        Bitmap bitmap2 = encodeWebp(bitmap, "cs");
        imageView.setImageBitmap(bitmap2);

        /**
         * 解码
         */
        //Bitmap转换为inputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.tp);//webp图片
        bm.compress(Bitmap.CompressFormat.WEBP, 100, baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());

        //通过id获取inputStream
//        @SuppressLint("ResourceType") InputStream is = getResources().openRawResource(R.mipmap.tp);//webp图片

        Bitmap bitmap1 = decodeWebp(is);


        imageView2.setImageBitmap(bitmap1);


    }


    /**
     * 获取读写权限
     */
    public void checkPermission() {
        boolean isGranted = true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //如果没有写sd卡权限
                isGranted = false;
            }
            if (this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                isGranted = false;
            }
            Log.i("cbs", "isGranted == " + isGranted);
            if (!isGranted) {
                this.requestPermissions(
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission
                                .ACCESS_FINE_LOCATION,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        102);
            }
        }

    }


    /**
     * 编码（把其他格式图片转换为webp）
     *
     * @param bitmap
     */
    private Bitmap encodeWebp(Bitmap bitmap, String name) {
        //转换后的图片存储路径
        String path = Environment.getExternalStorageDirectory() + "/" + name + ".webp";
        //获取bitmap 宽高
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        //获得bitmap中的 ARGB 数据 nio
        ByteBuffer buffer = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(buffer);
        //编码 获得 webp格式文件数据  4 *width
        byte[] bytes = libwebp.WebPEncodeRGBA(buffer.array(), width, height, width * 4, 75);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            fos.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != fos) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //获取转换后的图片
        bitmap = BitmapFactory.decodeFile(path);

        return bitmap;
    }

    /**
     * 解码（把编码后的webp图片转换为Bitmap）
     * <p>
     *
     * @return
     */
    private Bitmap decodeWebp(InputStream is) {
        byte[] bytes = stream2Bytes(is);
        //将webp格式的数据转成 argb
        int[] width = new int[1];
        int[] height = new int[1];
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] argb = libwebp.WebPDecodeARGB(bytes, bytes.length, width, height);
        //将argb byte数组转成 int数组
        int[] pixels = new int[argb.length / 4];
        ByteBuffer.wrap(argb).asIntBuffer().get(pixels);
        //获得bitmap
        Bitmap bitmap = Bitmap.createBitmap(pixels, width[0], height[0], Bitmap.Config.ARGB_8888);
        return bitmap;
    }

    byte[] stream2Bytes(InputStream is) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int len;
        try {
            while ((len = is.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bos.toByteArray();
    }
}
