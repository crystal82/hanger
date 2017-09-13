package com.uascent.jz.ua420r.utils;

import android.text.TextUtils;
import android.util.Log;

/**
 * 打印LOG工具类
 * Created by zsg on 2016/9/12.
 */
public class Lg {
    private static boolean sDebug = true;
    private static String  sTag   = "JzMalePrj";

    public static void init(boolean debug, String tag) {
        Lg.sDebug = debug;
        Lg.sTag = tag;
    }

    private static String getFinalTag(String tag) {
        if (!TextUtils.isEmpty(tag)) {
            return tag;
        }
        return sTag;
    }

    public static void d(String msg) {
        if (!sDebug) return;

        StackTraceElement targetStackTraceElement = getTargetStackTraceElement();
        Log.d(sTag, "(" + targetStackTraceElement.getFileName() + ":"
                + targetStackTraceElement.getLineNumber() + ")" + "   " + msg);
    }

    public static void d(String tag, String msg) {
        if (!sDebug) return;

        String            finalTag                = getFinalTag(tag);
        StackTraceElement targetStackTraceElement = getTargetStackTraceElement();
        Log.d(finalTag, "(" + targetStackTraceElement.getFileName() + ":"
                + targetStackTraceElement.getLineNumber() + ")" + "   " + msg);
    }

    public static void e(String msg) {
        if (!sDebug) return;

        StackTraceElement targetStackTraceElement = getTargetStackTraceElement();
        Log.e(sTag, "(" + targetStackTraceElement.getFileName() + ":"
                + targetStackTraceElement.getLineNumber() + ")" + "   " + msg);
    }

    public static void e(String tag, String msg) {
        if (!sDebug) return;

        String            finalTag                = getFinalTag(tag);
        StackTraceElement targetStackTraceElement = getTargetStackTraceElement();
        Log.e(finalTag, "(" + targetStackTraceElement.getFileName() + ":"
                + targetStackTraceElement.getLineNumber() + ")" + "   " + msg);
    }

    public static void i(String tag, String msg) {
        if (!sDebug) return;

        String            finalTag                = getFinalTag(tag);
        StackTraceElement targetStackTraceElement = getTargetStackTraceElement();
        Log.i(finalTag, "(" + targetStackTraceElement.getFileName() + ":"
                + targetStackTraceElement.getLineNumber() + ")" + "   " + msg);
    }


    //得到上一个栈帧
    private static StackTraceElement getTargetStackTraceElement() {
        // find the target invoked method
        StackTraceElement   targetStackTrace = null;
        boolean             shouldTrace      = false;
        StackTraceElement[] stackTrace       = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            boolean isLogMethod = stackTraceElement.getClassName().equals(Lg.class.getName());
            if (shouldTrace && !isLogMethod) {
                targetStackTrace = stackTraceElement;
                break;
            }
            shouldTrace = isLogMethod;
        }
        return targetStackTrace;
    }
}
