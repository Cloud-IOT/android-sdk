package com.dtston.demo.utils;

import android.content.Context;
import android.os.Build;

import com.dtston.demo.ApplicationManager;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtils {
	
	public static boolean isEmpty(String str) {
        if (str == null || str.trim().length() == 0)
            return true;
        else
            return false;
    }

    private static final ThreadLocal<SimpleDateFormat> threadLocal = new ThreadLocal<SimpleDateFormat>();
    private static final Object object = new Object();

    private static final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS");

    /**
     * 将日期类型转为日期格式的字符串
     * @param date
     * @param pattern
     * @return
     */
    public static String toString(long time, String pattern) {
        String dateString = null;
        Date date = new Date(time);
        if (date != null) {
            try {
                dateString = getDateFormat(pattern).format(date);
            } catch (Exception e) {
            }
        }
        return dateString;
    }

    /**
     * 获取SimpleDateFormat
     * @param pattern 日期格式
     * @return SimpleDateFormat对象
     * @throws RuntimeException 异常：非法日期格式
     */
    private static SimpleDateFormat getDateFormat(String pattern) throws Exception{
        SimpleDateFormat dateFormat = threadLocal.get();
        if(dateFormat == null){
            synchronized (object) {
                if(dateFormat == null){
                    dateFormat = new SimpleDateFormat(pattern);
                    dateFormat.setLenient(true);
                    threadLocal.set(dateFormat);
                }
            }
        }
        dateFormat.applyPattern(pattern);
        return dateFormat;
    }

    public static String getDataTimeString() {
        return formatter.format(new Date());
    }

    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * 获取软件的版本号
     * @param context
     * @return
     */
    public static int getVersionCode(){
        int code=0;
        try {
            Context context= ApplicationManager.getInstance().getApplicationContext();
            code=context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }

    /**
     * 获取软件版本名
     * @param context
     * @return
     */
    public static String getVersionName(){
        String versionName=null;
        try {
            Context context=ApplicationManager.getInstance().getApplicationContext();
            versionName=context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionName;
    }

    public static String getDeviceInfo() {
        String sperator = "; ";
        StringBuffer sbLog = new StringBuffer();

        sbLog.append("Brand: ");
        sbLog.append(Build.BRAND);
        sbLog.append(sperator);
        sbLog.append("Device: ");
        sbLog.append(Build.DEVICE);
        sbLog.append(sperator);
        sbLog.append("Model: ");
        sbLog.append(Build.MODEL);
        sbLog.append(sperator);
        sbLog.append("Id: ");
        sbLog.append(Build.ID);
        sbLog.append(sperator);
        sbLog.append("Product: ");
        sbLog.append(Build.PRODUCT);
        sbLog.append(sperator);
        sbLog.append("SDK: ");
        sbLog.append(Build.VERSION.SDK);
        sbLog.append(sperator);
        sbLog.append("Release: ");
        sbLog.append(Build.VERSION.RELEASE);
        sbLog.append(sperator);
        sbLog.append("Incremental: ");
        sbLog.append(Build.VERSION.INCREMENTAL);
        sbLog.append(sperator);
        sbLog.append("Version code:");
        sbLog.append(StringUtils.getVersionCode());
        sbLog.append(sperator);
        sbLog.append("Version name:");
        sbLog.append(StringUtils.getVersionName());

        return sbLog.toString();
    }
	
}
