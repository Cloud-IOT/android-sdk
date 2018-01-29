package com.dtston.demo.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;

import com.dtston.demo.ApplicationManager;
import com.dtston.demo.R;
import com.dtston.demo.adapter.DeviceManagerAdapter;
import com.dtston.demo.dao.DeviceDao;
import com.dtston.demo.dao.UserDao;
import com.dtston.demo.db.DeviceTable;
import com.dtston.demo.db.UserTable;
import com.dtston.demo.dialog.ChoiceListDialog;
import com.dtston.demo.dialog.ConfirmDialogWithoutTitle;
import com.dtston.demo.utils.Activitystack;
import com.dtston.demo.utils.Debugger;
import com.dtston.demo.utils.SharedPreferencesUtils;
import com.dtston.demo.utils.ToastUtils;
import com.dtston.demo.view.PullDownListView;
import com.dtston.dtcloud.DeviceManager;
import com.dtston.dtcloud.UserManager;
import com.dtston.dtcloud.push.DTDeviceState;
import com.dtston.dtcloud.push.DTIDeviceStateCallback;
import com.dtston.dtcloud.push.DTIOperateCallback;
import com.dtston.dtcloud.result.UserDevicesResult;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/12.
 */
public class MainActivity extends BaseActivity implements DTIDeviceStateCallback,
        PullDownListView.OnPullDownListener {

    public static final String EXTRA_MAC = "mac";
    public static final String EXTRA_TYPE = "dev_type";
    private final String TAG = this.getClass().getSimpleName();

    private View mVAdd;
    private View mVLeftMenu;
    private PullDownListView mVDeviceList;
    private LinearLayout mVEmptyView;
    private DeviceManagerAdapter mAdapter;

    private Button mVAddDevice;
    private List<DeviceTable> mDeviceList = new ArrayList<>();
    private ChoiceListDialog mChoiceListDialog;
    private List<String> mAddDeviceMethods = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_manager);
        initViews();
        initEvents();
        initData();
        onRefresh();
        DeviceManager.registerDeviceStateCallback(this);
    }

    protected void initViews() {
        mVAdd = findViewById(R.id.ivAdd);
        mVLeftMenu = findViewById(R.id.leftMenu);
        mVDeviceList = (PullDownListView) findViewById(R.id.deviceList);

        mVEmptyView = (LinearLayout) findViewById(R.id.empty);
        mVAddDevice = (Button) mVEmptyView.findViewById(R.id.addDevice);
        LinearLayout loadingView = (LinearLayout) findViewById(R.id.loading);
        mVDeviceList.setEmptyView(loadingView);

        mVDeviceList.setPullLoadEnable(false);
        mVDeviceList.setPullRefreshEnable(true);
        mVDeviceList.setOnPullDownListener(this);
    }

    protected void initEvents() {
        mVAdd.setOnClickListener(this);
        mVLeftMenu.setOnClickListener(this);
        mVAddDevice.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivAdd:
            case R.id.addDevice:
                showAddDeviceDialog();
                break;
            case R.id.leftMenu:
                ConfirmDialogWithoutTitle confirmDialogWithoutTitle = new ConfirmDialogWithoutTitle(this, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        quit();
                    }
                });
                confirmDialogWithoutTitle.setContent("是否退出账号");
                confirmDialogWithoutTitle.show();
            default:
                break;
        }
    }

    private void initData() {
        mAddDeviceMethods.add("本地配网添加");
        mAddDeviceMethods.add("本地搜索添加");
        mAdapter = new DeviceManagerAdapter(this, mDeviceList);
        mVDeviceList.setAdapter(mAdapter);
        mVDeviceList.setOnItemClickListener(mAdapter);
        mVDeviceList.setOnItemLongClickListener(mAdapter);
    }

    public void onResume() {
        super.onResume();
        loadDevices();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void addDeviceByMatch() {
        Intent intent = new Intent(this, DeviceConnectionActivity.class);
        startActivity(intent);
    }

    private void addDeviceBySearch() {
        Intent intent = new Intent(this, LanDeviceListActivity.class);
        startActivity(intent);
    }

    private void showAddDeviceDialog() {
        AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                switch (position) {
                    case 0:
                        addDeviceByMatch();
                        break;
                    case 1:
                        addDeviceBySearch();
                        break;
                    default:
                        break;
                }
            }
        };
        mChoiceListDialog = new ChoiceListDialog(this, mAddDeviceMethods, onItemClickListener);
        ;
        mChoiceListDialog.setTitle("添加设备");
        mChoiceListDialog.show();
    }


    //从服务器获取用户绑定的设备，并保存到数据库
    private void postGetBindingDevice() {
        DeviceManager.getBindDevices(new DTIOperateCallback<UserDevicesResult>() {
            @Override
            public void onSuccess(UserDevicesResult var1, int var2) {
                if (isFinishing()) {
                    return;
                }
                if (var1.getErrcode() == 0) {
                    DeviceTable deviceTable = null;
                    UserTable currentUser = ApplicationManager.getInstance().getCurrentUser();
                    String uid = currentUser.getUid();
                    JSONObject deviceInfo;
                    String name;
                    String mac;
                    String type;
                    String deviceId;
                    String dataId;
                    List<UserDevicesResult.DataBean> data = var1.getData();
                    int count = data.size();
                    for (int i = 0; i < count; i++) {
                        name = data.get(i).getName();
                        mac = data.get(i).getMac();
                        type = data.get(i).getType();
                        deviceId = data.get(i).getDevice_id();
                        dataId = data.get(i).getId();

                        boolean exist = true;
                        deviceTable = DeviceDao.getInstance().getDeviceByMac(uid, mac);
                        if (deviceTable == null) {
                            deviceTable = new DeviceTable();
                            deviceTable.setMac(mac);
                            exist = false;
                        }
                        deviceTable.setBind(DeviceTable.BIND_HAS);
                        deviceTable.setUid(currentUser.getUid());
                        deviceTable.setType(Integer.valueOf(type));
                        deviceTable.setDeviceId(deviceId);
                        deviceTable.setDeviceName(name);
                        deviceTable.setDataId(dataId);
                        if (exist) {
                            int update = DeviceDao.getInstance().update(deviceTable);
                            Debugger.logD(TAG, "deviceTable update id is " + update);
                        } else {
                            int insert = DeviceDao.getInstance().insert(deviceTable);
                            Debugger.logD(TAG, "deviceTable insert id is " + insert);
                        }
                    }
                } else if (var1.getErrcode() == 400011) {
                    ToastUtils.showToast("没有绑定设备");
                }

                loadDevices();
            }

            @Override
            public void onFail(Object var1, int var2, String var3) {
                String msg = null;
//                if (var1 instanceof NoConnectionError) {
//                    msg = "刷新失败，请检查网络是否可用";
//                } else if (var1 instanceof TimeoutError) {
//                    msg = "刷新失败，请检查网络是否可用";
//                } else {
//                    msg = "刷新失败,网络繁忙,请稍候再试";
//                }
                ToastUtils.showToast(var1.toString());
                loadDevices();
            }
        });
    }

    //从数据库获取设备，并显示
    private void loadDevices() {
        UserTable currentUser = ApplicationManager.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getUid() == null) {
            finish();
            return;
        }
        List<DeviceTable> deviceTables = DeviceDao.getInstance().getDeviceByUid(currentUser.getUid());
        if (deviceTables != null) {
            mDeviceList.clear();
            mDeviceList.addAll(deviceTables);
        }
        mAdapter.notifyDataSetChanged();
        mVDeviceList.setEmptyView(mVEmptyView);
        mVDeviceList.stopRefresh();

        ApplicationManager.getInstance().setDeviceList(mDeviceList);
    }

    @Override
    public void onDestroy() {
        DeviceManager.unregisterDeviceStateCallback(this);
        super.onDestroy();
    }

    @Override
    public void onDeviceOnlineNotice(DTDeviceState deviceState) {
        // TODO Auto-generated method stub
        Log.d(TAG, "onDeviceOnlineNotice: mac:"+deviceState.getMac()+" state:"+deviceState.isOnline());
        stateRefresh();
    }

    @Override
    public void onDeviceOfflineNotice(DTDeviceState deviceState) {
        // TODO Auto-generated method stub
        Log.d(TAG, "onDeviceOfflineNotice: mac:"+deviceState.getMac()+" state:"+deviceState.isOnline());
        stateRefresh();
    }

    private void stateRefresh(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    public void onRefresh() {
        postGetBindingDevice();
    }

    @Override
    public void onLoadMore() {
        // TODO Auto-generated method stub
    }

    //退出
    private void quit() {
        UserTable currentUser = ApplicationManager.getInstance().getCurrentUser();
        currentUser.setType(UserTable.TYPE_OTHER_USER);
        int update = UserDao.getInstance().update(currentUser);
        Debugger.logD("quit", "quit update id is " + update);

        UserManager.logoutUser();

        SharedPreferencesUtils.editExitUserName(this, currentUser.getUserName());
        ApplicationManager.getInstance().setCurrentUser(null);
        Activitystack.finishAll();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

}
