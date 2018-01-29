package com.dtston.demo.thirdmodify;

import com.dtston.dtcloud.DeviceManager;
import com.dtston.dtcloud.push.DTIOperateCallback;

/**
 * Created by Administrator on 2017/6/30.
 */

public class ShuiShengHelper {

    public static void resetFilter(String mac) {
        String msgType = "108D";

        //7个滤芯 前面三个数据一样 后面四个数据一样
        StringBuffer uartData = new StringBuffer();
        //7个滤芯
        uartData.append("07");
        //前3个
        uartData.append("0000000010e01770");
        uartData.append("0000000010e01770");
        uartData.append("0000000010e01770");
        //后4个
        uartData.append("0000000021c02ee0");
        uartData.append("0000000021c02ee0");
        uartData.append("0000000021c02ee0");
        uartData.append("0000000021c02ee0");

        DeviceManager.sendMessage(mac, msgType, uartData.toString(), new DTIOperateCallback() {
            @Override
            public void onSuccess(Object var1, int var2) {
                System.out.println("success=" + var2);
            }

            @Override
            public void onFail(Object var1, int error, String var3) {
                System.out.println("error=" + error);
            }
        });
    }
}
