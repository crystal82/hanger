package com.uascent.jz.ua420r.timer;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

/**
 * Created by maxiao on 2017/7/3.
 */

public class Utils {

    public static String getCurrentTimeZone() {
        TimeZone tz = TimeZone.getDefault();
        return createGmtOffsetString(true, true, tz.getRawOffset());
    }

    public static String createGmtOffsetString(boolean includeGmt,
                                               boolean includeMinuteSeparator, int offsetMillis) {
        int offsetMinutes = offsetMillis / 60000;
        char sign = '+';
        if (offsetMinutes < 0) {
            sign = '-';
            offsetMinutes = -offsetMinutes;
        }
       /* StringBuilder builder = new StringBuilder(9);
        if (includeGmt) {
            builder.append("GMT");
        }
        builder.append(sign);*/
        //appendNumber(builder, 2, offsetMinutes / 60);
        Log.e("TAG", "1-" + offsetMinutes / 60);
        /*if (includeMinuteSeparator) {
            builder.append(':');
        }
        appendNumber(builder, 2, offsetMinutes % 60);
        Log.e("TAG","2-"+offsetMinutes / 60);*/
        if ((offsetMinutes / 60) < 10)
            return "0" + offsetMinutes / 60;
        else
            return "" + offsetMinutes / 60;
    }

    private static void appendNumber(StringBuilder builder, int count, int value) {
        String string = Integer.toString(value);
        for (int i = 0; i < count - string.length(); i++) {
            builder.append('0');
        }
        builder.append(string);
        Log.e("TAG", "3-" + string);
    }


    public static String geTimeId() {

        StringBuilder builder = new StringBuilder();
        int max = 2;
        int min = 0;
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            int s = random.nextInt(max) % (max - min + 1) + min;
            builder.append(s);
        }
        System.out.println(builder.toString());
        return binaryString2hexString(builder.toString());
    }

    public static String getID() {
        String id = "00";
        boolean isRepeat = false;
        for (int i = 0; i < 256; i++) {
            id = geTimeId();
            for (Timer t : Constant.list) {
                if (t.getTimerId().equals(id)) {
                    isRepeat = true;
                    break;
                }
            }
            if (!isRepeat) {
                break;
            }
        }
        Log.e("TAG", "id=" + id);
        return id;
    }

    /**
     * 得到与当前时间的相对时间  若为负数则加一天
     *
     * @return
     */
    public static long getRelativeTime(String setTimer) {
        Date nowTime = new Date(System.currentTimeMillis());

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        //Date setDate=format.parse("18:00");
        String tempTimer = format.format(nowTime).substring(0, 10) + " " + setTimer;
        Log.d("testTimeUtil", nowTime.getTime() + "");
        try {
            long delay = (format.parse(tempTimer).getTime() - nowTime.getTime()) / 1000;
            if (delay <= 0)
                delay = 24 * 60 * 60 + delay;
            Log.e("TAG", "delay=" + delay);
            return delay;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;

    }

    /**
     * 得到与当前时间的相对时间  若为负数则加一天
     *
     * @return
     */
    public static long getRelativeTime2(String setTimer, int n) {
        Date nowTime = new Date(System.currentTimeMillis());

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        //Date setDate=format.parse("18:00");
        String tempTimer = format.format(nowTime).substring(0, 10) + " " + setTimer;
        Log.d("testTimeUtil", nowTime.getTime() + "");
        try {
            long delay = (format.parse(tempTimer).getTime() - nowTime.getTime()) / 1000;
            if (delay <= 0) {
                delay = 24 * 60 * 60 + delay;
                if (n != 1)
                    delay = delay + (n - 1) * 24 * 3600;
            } else
                delay = delay + n * 24 * 3600;
            Log.e("TAG", "delay=" + delay);
            return delay;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;

    }

    public static long getRelative(String setTimer) {
        Date nowTime = new Date(System.currentTimeMillis());

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        //Date setDate=format.parse("18:00");
        String tempTimer = format.format(nowTime).substring(0, 10) + " " + setTimer;
        Log.d("testTimeUtil", nowTime.getTime() + "");
        try {
            long delay = (format.parse(tempTimer).getTime() - nowTime.getTime()) / 1000;
            if (delay <= 0) {
                return -1;
            }
            return delay;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;

    }

    /**
     * 十进制转换为十六进制
     *
     * @return String 十六进制字符串
     * @throws
     * @Title: convert10To16
     * @Description:
     * @param：[source:十进制数字]
     * @user： wangzg
     * @Date：2014-9-5
     */
    public static String convert10To16(int source) {
        String result = Integer.toHexString(source);
        return result;
    }

    /**
     * 将16进制 转换成10进制
     *
     * @param str
     * @return
     */
    public static String print10(String str) {

        StringBuffer buff = new StringBuffer();
        String array[] = str.split(" ");
        for (int i = 0; i < array.length; i++) {
            int num = Integer.parseInt(array[i], 16);
            buff.append(String.valueOf((char) num));
        }
        return buff.toString();
    }


    /*
     * 将时间戳转换为时间
     */
    public static String stampToDate(long s) {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

        Date date = new Date(s);
        res = simpleDateFormat.format(date);
        return res;
    }

    public static String binaryString2hexString(String bString) {
        if (bString == null || bString.equals("") || bString.length() % 8 != 0)
            return null;
        StringBuffer tmp = new StringBuffer();
        int iTmp = 0;
        for (int i = 0; i < bString.length(); i += 4) {
            iTmp = 0;
            for (int j = 0; j < 4; j++) {
                iTmp += Integer.parseInt(bString.substring(i + j, i + j + 1)) << (4 - j - 1);
            }
            tmp.append(Integer.toHexString(iTmp));
        }
        return tmp.toString();
    }
}
