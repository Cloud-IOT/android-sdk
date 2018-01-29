package com.dtston.demo.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dtston.demo.ApplicationManager;
import com.dtston.demo.R;
import com.dtston.demo.common.Constans;
import com.dtston.demo.db.DeviceTable;
import com.dtston.demo.thirdmodify.ShuiShengHelper;
import com.dtston.demo.utils.InputMethodUtils;
import com.dtston.demo.utils.StringUtils;
import com.dtston.demo.utils.ToastUtils;
import com.dtston.dtcloud.DeviceManager;
import com.dtston.dtcloud.push.DTDeviceState;
import com.dtston.dtcloud.push.DTIDeviceMessageSourceCallback;
import com.dtston.dtcloud.push.DTIDeviceStateCallback;
import com.dtston.dtcloud.push.DTIOperateCallback;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.dtston.demo.common.Constans.isTestGprs;

public class DeviceControlForShuiShengActivity extends BaseActivity implements DTIDeviceMessageSourceCallback, DTIDeviceStateCallback {
    private static final String TAG                    = "DeviceControlActivity";
    public static final  String EXTRA_GPRS_HTTP_FLAG   = "extra_gprs_http";
    public static final  int    WHAT_INTERVAL_SEND_CMD = 1;
    private View     mVRoot;
    private View     mVBack;
    private TextView mVTitle;
    private View     mVControlRoot;
    private View     mVNonControl;
    private TextView mVState;
    private EditText mVType;
    private EditText mVCmd;
    private TextView mVSend;
    private TextView mVResult;
    private View     mVClear;
    private TextView shuishengReset;
    private View     interval_root;
    private EditText time_interval;

    private Button button_open_machine;
    private Button button_close_machine;

    private DeviceTable mCurrentDevice;

    private StringBuffer     mResultBuffer = new StringBuffer();
    private SimpleDateFormat mFormatter    = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");
    private MyHandler sendIntervalHandler;
    private int       interval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control_shuisheng_root);
        DeviceManager.registerDeviceStateCallback(this);
        DeviceManager.registerDeviceMessageCallback(this);
        initViews();
        initEvents();

        mCurrentDevice = ApplicationManager.getInstance().getCurrentControlDevice();
        if (mCurrentDevice == null) return;
        mVTitle.setText("(" + mCurrentDevice.getMac() + ")");

        sendIntervalHandler = new MyHandler(this);
    }

    @Override
    protected void initViews() {
        mVRoot = findViewById(R.id.root);
        mVBack = findViewById(R.id.back);
        mVTitle = (TextView) findViewById(R.id.title);
        mVControlRoot = findViewById(R.id.control_root);
        mVNonControl = findViewById(R.id.nonControl);
        mVState = (TextView) findViewById(R.id.state);
        mVType = (EditText) findViewById(R.id.msg_type);
        mVCmd = (EditText) findViewById(R.id.cmd);
        mVSend = (TextView) findViewById(R.id.send);
        mVResult = (TextView) findViewById(R.id.result);
        mVClear = findViewById(R.id.clear);
        button_open_machine = (Button) findViewById(R.id.button_open_machine);
        button_close_machine = (Button) findViewById(R.id.button_close_machine);
        shuishengReset = (TextView) findViewById(R.id.shui_sheng_reset_filter);
        if (Constans.isTestShuiSheng) {
            shuishengReset.setVisibility(View.VISIBLE);
        }
        initIntervalSendCmd();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DeviceManager.unregisterDeviceStateCallback(this);
        DeviceManager.unregisterDeviceMessageCallback(this);
    }

    @Override
    protected void initEvents() {
        InputMethodUtils.hideInputMethodWindow(this, mVRoot);
        mVBack.setOnClickListener(this);
        mVSend.setOnClickListener(this);
        mVClear.setOnClickListener(this);
        shuishengReset.setOnClickListener(this);
        button_open_machine.setOnClickListener(this);
        button_close_machine.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCurrentDevice == null) mCurrentDevice = new DeviceTable();
        DTDeviceState currentDeviceState = DeviceManager.getDevicesState(mCurrentDevice.getMac());
        if ((currentDeviceState != null && currentDeviceState.isOnline()) || isTestGprs) {
            mVControlRoot.setVisibility(View.VISIBLE);
            mVNonControl.setVisibility(View.GONE);
        } else {
            mVControlRoot.setVisibility(View.GONE);
            mVNonControl.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.send:
                checkEdittextState();
                break;
            case R.id.clear:
                clear();
                break;
            case R.id.shui_sheng_reset_filter:
                ShuiShengHelper.resetFilter(mCurrentDevice.getMac());
                break;
            case R.id.button_open_machine:
                sendToShuiShengPower("01");
                break;
            case R.id.button_close_machine:
                sendToShuiShengPower("00");
                break;
            default:
                break;
        }
    }

    private void clear() {
        mResultBuffer.delete(0, mResultBuffer.length());
        mVResult.setText("");
    }

    private void initIntervalSendCmd() {
        if (Constans.isIntervalSend) {
            interval_root = findViewById(R.id.interval_root);
            time_interval = (EditText) findViewById(R.id.time_interval);
            interval_root.setVisibility(View.VISIBLE);
        }
    }

    private void sendToShuiShengPower(String body){
        DeviceManager.sendMessage(mCurrentDevice.getMac(), "1080", body, new DTIOperateCallback() {
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

    private void sendInterval() {


    }

    private void checkEdittextState() {
        String msgType  = mVType.getText().toString();
        String uartData = mVCmd.getText().toString();
        if (uartData.length() % 2 != 0) {
            ToastUtils.showToast("16进制字符必须为双数");
            return;
        }
//        msgType = "2258";
//        uartData = "1212121212";
        if (mCurrentDevice == null) mCurrentDevice = new DeviceTable();
//        mCurrentDevice.setMac("123456789012345");
        if (StringUtils.isEmpty(msgType) || msgType.length() != 4) {
            ToastUtils.showToast("功能码长度为4");
            return;
        }
        if (Constans.isIntervalSend) {
            String intervalStr = time_interval.getText().toString();
            if (TextUtils.isEmpty(intervalStr)) {
                ToastUtils.showToast("间隔时间不能为空");
                return;
            }
            if ("发送".equals(mVSend.getText())) {
                try {
                    interval = Integer.parseInt(intervalStr);
                    if (interval != 0) {
                        sendIntervalHandler.sendEmptyMessageDelayed(WHAT_INTERVAL_SEND_CMD, interval);
                        mVSend.setText("停止发送");
                    }
                    send();

                } catch (Exception e) {
                    ToastUtils.showToast("请输入数字");
                }
            } else {
                mVSend.setText("发送");
                sendIntervalHandler.removeMessages(WHAT_INTERVAL_SEND_CMD);
            }
        } else {
            send();
        }

    }

    private void send() {
        Log.d(TAG, "send() called");
        String msgType  = mVType.getText().toString();
        String uartData = mVCmd.getText().toString();
        String mac      = mCurrentDevice.getMac();
        if (getIntent().getBooleanExtra(EXTRA_GPRS_HTTP_FLAG, false) || isTestGprs ) {//|| mac.length() == 15
            DeviceManager.sendGprsHttpMessage(mCurrentDevice.getMac(), msgType, uartData, new DTIOperateCallback() {
                @Override
                public void onSuccess(Object var1, int var2) {
                    Log.d(TAG, "onSuccess() called with: var1 = [" + var1 + "], var2 = [" + var2 + "]");
                    ToastUtils.showToast("发送成功");
                }

                @Override
                public void onFail(Object var1, int error, String var3) {
                    Log.d(TAG, "onFail() called with: var1 = [" + var1 + "], error = [" + error + "], var3 = [" + var3 + "]");
                    ToastUtils.showToast(var1.toString());
                }
            });
        } else {
            DeviceManager.sendMessage(mCurrentDevice.getMac(), msgType, uartData, new DTIOperateCallback() {
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

    public void receiveData(final String data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mResultBuffer.length() > 0) {
                    mResultBuffer.append("\n");
                }
                String strTime = mFormatter.format(new Date());
//				mResultBuffer.append(strTime + "  ");
                mResultBuffer.append(data);
                mVResult.setText(mResultBuffer.toString());
            }
        });
    }

    public void deviceOffline() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isTestGprs) return;
                mVControlRoot.setVisibility(View.GONE);
                mVNonControl.setVisibility(View.VISIBLE);
                mVState.setText("设备离线了");
            }
        });
    }

    public void deviceOnline() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mVControlRoot.setVisibility(View.VISIBLE);
                mVNonControl.setVisibility(View.GONE);
                mResultBuffer.delete(0, mResultBuffer.length());
                mVResult.setText("");
            }
        });
    }


    @Override
    public void onMessageReceive(String mac, String msgType, byte[] msgBody) {
        if (mCurrentDevice.getMac().equals(mac)) {
            String msg = StringUtils.bytesToHexString(msgBody);
            receiveData(msgType + " : " + msg);
        }
    }

    @Override
    public void onMessageReceive(String mac, String msgType, byte[] msgBody, String msgSource) {

    }

    @Override
    public void onDeviceOnlineNotice(DTDeviceState deviceState) {
        deviceOnline();
    }

    @Override
    public void onDeviceOfflineNotice(DTDeviceState deviceState) {
        deviceOffline();
    }

    private static class MyHandler extends Handler {
        private WeakReference<DeviceControlForShuiShengActivity> paretant;

        public MyHandler(DeviceControlForShuiShengActivity activity) {
            paretant = new WeakReference<DeviceControlForShuiShengActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            DeviceControlForShuiShengActivity parentActivity = paretant.get();
            if (parentActivity != null && !parentActivity.isFinishing()) {
                switch (msg.what) {
                    case WHAT_INTERVAL_SEND_CMD:
                        parentActivity.send();
                        sendEmptyMessageDelayed(WHAT_INTERVAL_SEND_CMD, parentActivity.interval);
                        break;
                }
            }
        }
    }

}
