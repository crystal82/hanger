package com.uascent.jz.ua420r.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 作者：HWQ on 2017/5/9 11:09
 * 描述：
 */

public class MyUtils {
    public interface DialogAble {
        void onDataSet(View layout, AlertDialog dialog);
    }
    public static void setDrawable(Context context, View view, int drawableId) {
        Resources resources = context.getResources();
        view.setBackgroundDrawable(resources.getDrawable(drawableId));
    }
    //显示dialog
    public static void showDialog(Activity activity, int layoutId, int viewGroupId, DialogAble dialogAble) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(layoutId,
                                       (ViewGroup) activity.findViewById(viewGroupId));
        AlertDialog dialog = new AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_LIGHT).show();
        dialogAble.onDataSet(layout, dialog);//设置数据
        dialog.getWindow().setContentView(layout);
    }

    /**
     * 批量设置OnClickListener
     *
     * @param listener 监听器
     * @param views    可变参数view
     */
    public static void setOnClick(View.OnClickListener listener, View... views) {
        for (View view : views) {
            view.setOnClickListener(listener);
        }
    }

    public static int[] bytesToInts(byte[] bytes) {
        int[] ints = new int[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            ints[i] = bytes[i] & 0xFF;
        }
        return ints;
    }
}
