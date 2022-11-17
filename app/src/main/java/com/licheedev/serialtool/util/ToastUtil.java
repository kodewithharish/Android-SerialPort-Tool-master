package com.licheedev.serialtool.util;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.licheedev.serialtool.R;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Toast工具类
 */
public class ToastUtil {

    private static WeakReference<Toast> mToastRef = null;

    private static final String _ROOT_FOLDER_NAME_ = "smartNav20";
    private static final String _ROOT_SYS = "/sdcard";
    public static   File _SMART_NAV_ROOT = new File(_ROOT_SYS + File.separator + _ROOT_FOLDER_NAME_);
    public static File _SMART_NAV_ROOT_MAPS = new File(_ROOT_SYS + File.separator + _ROOT_FOLDER_NAME_ + File.separator + "maps");
    public static File _SMART_NAV_ROOT_DATABASE = new File(_ROOT_SYS + File.separator + _ROOT_FOLDER_NAME_ + File.separator + "database");
    private static File file, file2,file3;


    /**
     * 自定义Toast
     */
    public static void showOne(Context context, String text) {
        Toast toast;
        if (mToastRef != null && (toast = mToastRef.get()) != null) {
            toast.setDuration(Toast.LENGTH_SHORT);
            TextView tv = (TextView) toast.getView().findViewById(R.id.tv_toast_text);
            tv.setText(text);
        } else {

            toast = new Toast(context);

            LayoutInflater inflate =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflate.inflate(R.layout.custom_toast, null);
            TextView tv = (TextView) view.findViewById(R.id.tv_toast_text);
            tv.setText(text);
            toast.setView(view);
            toast.setDuration(Toast.LENGTH_SHORT);
            
            mToastRef = new WeakReference<>(toast);
        }

        toast.show();
    }

    /**
     * 自定义Toast
     */
    public static void showOne(Context context, int resid) {
        showOne(context, context.getResources().getString(resid));
    }

    public static void show(Context context, String text) {
        Toast toast = new Toast(context);
        LayoutInflater inflate =
            (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflate.inflate(R.layout.custom_toast, null);
        TextView tv = (TextView) view.findViewById(R.id.tv_toast_text);
        tv.setText(text);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void show(Context context, int resid) {
        show(context, context.getResources().getString(resid));
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void recordLog1(String str, String file_name) {

        if (file2 == null) {
            file2 = new File(_SMART_NAV_ROOT, File.separator + "mapSheet" + File.separator + file_name+".txt");
            // If file doesn't exists, then create it
            if (!file2.exists()) {
                try {
                    file2.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            Files.write(file2.toPath(), Arrays.asList(ConvertSystemTimetoime( System.currentTimeMillis())+ "  :  " + str), StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void recordLog2(String str, String file_name) {

        if (file3 == null) {
            file3 = new File(_SMART_NAV_ROOT, File.separator + "mapSheet" + File.separator + file_name+".txt");
            // If file doesn't exists, then create it
            if (!file3.exists()) {
                try {
                    file3.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            Files.write(file3.toPath(), Arrays.asList(ConvertSystemTimetoime( System.currentTimeMillis())+ "  :  " + str), StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String ConvertSystemTimetoime(long time)
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss:SSS Z");
        Date date = new Date(time);
        String time1 = simpleDateFormat.format(date);
        return time1;
    }
}
