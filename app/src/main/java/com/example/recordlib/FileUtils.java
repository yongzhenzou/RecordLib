package com.example.recordlib;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Create by liupeiquan on 2018/10/17
 */
public class FileUtils {
    /**
     * 获取应用的本地缓存路径
     * @param context
     * @param uniqueName
     * @return
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * 创建并获取录音的 文件路径
     * @param context
     * @return
     */
    public static File getVoiceFile(Context context){
        File file = null;
        try {
            file = FileUtils.getDiskCacheDir(context, "voice");
            if (!file.exists()) {
                file.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String name = UUID.randomUUID().toString();
        String nameFormate=name.trim();
        File voiceFile = new File(file,makeVoiceAmrSuffix(context,nameFormate));
        return voiceFile;
    }

    public static String makeVoiceAmrSuffix(Context context,String fileName){
        if(TextUtils.isEmpty(fileName)){
            return "";
        }
        StringBuilder voiceNameBuilder=new StringBuilder();
        voiceNameBuilder.append(fileName).append(".amr");
        return voiceNameBuilder.toString();
    }

    /**
     * 保存bitmap对象到本地文件，图片格式为jpg,并将保存路径
     *
     * @param context
     * @param bitmap
     */
    public static File saveBitmapToFile(Context context, Bitmap bitmap) {
        File file = FileUtils.getDiskCacheDir(context, "image");
        if (!file.exists())
            file.mkdirs();
        String name = UUID.randomUUID().toString();
        File mapPath = new File(file, name + ".jpg"); // 图片路径
        FileOutputStream fileOutputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(mapPath);
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bufferedOutputStream);
            bufferedOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (bufferedOutputStream != null) {
                    bufferedOutputStream.close();
                }
            } catch (Exception e) {

            }

        }
        return mapPath;
    }
}
