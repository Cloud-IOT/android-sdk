package com.dtston.demo.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dtston.demo.ApplicationManager;
import com.dtston.demo.R;
import com.dtston.demo.common.Constans;
import com.dtston.demo.dao.DeviceDao;
import com.dtston.demo.db.DeviceTable;
import com.dtston.demo.dialog.ChoiceListDialog;
import com.dtston.demo.dialog.ConfirmDialogWithoutTitle;
import com.dtston.demo.dialog.NetworkProgressDialog;
import com.dtston.demo.ui.DeviceConnectionActivity;
import com.dtston.demo.ui.DeviceControlActivity;
import com.dtston.demo.ui.DeviceControlForShuiShengActivity;
import com.dtston.demo.utils.StringUtils;
import com.dtston.demo.utils.ToastUtils;
import com.dtston.dtcloud.DeviceManager;
import com.dtston.dtcloud.push.DTDeviceState;
import com.dtston.dtcloud.push.DTFirmwareUpgradeResult;
import com.dtston.dtcloud.push.DTIOperateCallback;
import com.dtston.dtcloud.push.DTProtocolVersion;

import java.util.ArrayList;
import java.util.List;

import static com.dtston.demo.common.Constans.isTestGprs;

public class DeviceManagerAdapter extends BaseAdapter implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener {

    private List<DeviceTable> mDeviceList;
    private Activity          mContex;

    private List<String> mMenuList = new ArrayList<String>();
    private ChoiceListDialog      mChoiceListDialog;
    private DeviceTable           mLongClickDevice;
    private NetworkProgressDialog mVProgressDialog;

    public DeviceManagerAdapter(Context context, List<DeviceTable> deviceTables) {
        this.mContex = (Activity) context;
        this.mDeviceList = deviceTables;
        mMenuList.add("删除并解绑设备");
//        mMenuList.add("固件升级");//放到发送指令中去了
    }

    public void setDeviceList(List<DeviceTable> deviceList) {
        mDeviceList = deviceList;
    }

    @Override
    public int getCount() {
        return mDeviceList == null ? 0 : mDeviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContex).inflate(R.layout.adapter_device_manager, null);
            holder = new Holder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.switchIcon = (ImageView) convertView.findViewById(R.id.switch_icon);
            holder.switchState = (TextView) convertView.findViewById(R.id.switch_state);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        DeviceTable device = mDeviceList.get(position);
        holder.name.setText(device.getDeviceName());

        DTDeviceState deviceState = DeviceManager.getDevicesState(device.getMac());
        if (device.getMac().startsWith("-1")) {
            holder.switchState.setText("未配对连接");
            holder.switchState.setTextColor(Color.parseColor("#757980"));
            holder.switchIcon.setImageResource(R.drawable.switch_tag_offline);
        } else if (deviceState != null && deviceState.isOnline()) {
            holder.switchState.setText("在线");
            holder.switchState.setTextColor(Color.parseColor("#317ff5"));
            holder.switchIcon.setImageResource(R.drawable.switch_tag_online);
        } else {
            holder.switchState.setText("离线");
            holder.switchState.setTextColor(Color.parseColor("#757980"));
            holder.switchIcon.setImageResource(R.drawable.switch_tag_offline);
        }

        return convertView;
    }

    class Holder {
        TextView  name;
        ImageView switchIcon;
        TextView  switchState;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent      intent = null;
        DeviceTable device = mDeviceList.get(position - 1);
        ApplicationManager.getInstance().setCurrentControlDevice(device);
        String mac = device.getMac();
        if (TextUtils.isEmpty(device.getMac())
                || mac.startsWith("-1")) {
            intent = new Intent(mContex, DeviceConnectionActivity.class);
            mContex.startActivity(intent);
        } else {
            if (Constans.isTestShuiSheng) {
                intent = new Intent(mContex, DeviceControlForShuiShengActivity.class);
            } else {
                intent = new Intent(mContex, DeviceControlActivity.class);
            }

//            intent.putExtra(DeviceControlActivity.EXTRA_GPRS_HTTP_FLAG,true);
            mContex.startActivity(intent);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        mLongClickDevice = mDeviceList.get(position - 1);
        showMenuDialog();
        return true;
    }

    private void showMenuDialog() {
        AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position == 0) {
                    delete(mLongClickDevice);
                } else if (position == 1) {//固件升级
                    if (isTestGprs) {
                        firmwarmGprsUpgrade(mLongClickDevice);
                    } else {
                        firmwareCheck(mLongClickDevice);
                    }

                }
            }
        };
        mChoiceListDialog = new ChoiceListDialog(mContex, mMenuList, onItemClickListener);
        ;
        mChoiceListDialog.setTitle("菜单");
        mChoiceListDialog.show();
    }

    private void showProgressDialog(String text) {
        closeProgressDialog();
        mVProgressDialog = new NetworkProgressDialog(mContex, false, false);
        mVProgressDialog.setProgressText(text);
        mVProgressDialog.show();
    }

    private void closeProgressDialog() {
        if (null != mVProgressDialog && mVProgressDialog.isShowing()) {
            mVProgressDialog.cancel();
        }
        mVProgressDialog = null;
    }

    private void delete(final DeviceTable deviceTable) {
        String dataId = deviceTable.getDataId();
        if (StringUtils.isEmpty(dataId) || "0".equals(dataId)) {
            DeviceDao.getInstance().delete(deviceTable);
            mDeviceList.remove(deviceTable);
            notifyDataSetChanged();
            ToastUtils.showToast("删除成功");
        } else {
            showProgressDialog("正在解绑");
            DeviceManager.unbindDevice(deviceTable.getMac(), dataId, new DTIOperateCallback() {
                @Override
                public void onSuccess(Object var1, int var2) {
                    closeProgressDialog();
                    DeviceDao.getInstance().delete(deviceTable);
                    mDeviceList.remove(deviceTable);
                    notifyDataSetChanged();
                    ToastUtils.showToast("删除解绑成功");
                }

                @Override
                public void onFail(Object var1, int var2, String var3) {
                    closeProgressDialog();
                    ToastUtils.showToast("删除解绑失败");
                }
            });
        }
    }

    //固件版本检测
    private void firmwareCheck(final DeviceTable deviceTable) {
        showProgressDialog("正在检查固件版本");
        DeviceManager.firmwareUpgrade(deviceTable.getMac(), DTFirmwareUpgradeResult.TYPE_CHECK_VERSION, DTProtocolVersion.TYPE_THIRD,
                new DTIOperateCallback<DTFirmwareUpgradeResult>() {
                    @Override
                    public void onSuccess(final DTFirmwareUpgradeResult upgradeResult, int i) {
                        closeProgressDialog();
                        if (DTFirmwareUpgradeResult.RESULT_NO_NEW_VERSION == upgradeResult.getResult()) {
                            ToastUtils.showToast("已是最新固件版本");
                        } else if (DTFirmwareUpgradeResult.RESULT_HAS_NEW_VERSION == upgradeResult.getResult()) {
                            showFirmwareUpgradeDialog(deviceTable);
                        }
                    }

                    @Override
                    public void onFail(Object o, int i, String s) {
                        closeProgressDialog();
                        ToastUtils.showToast("固件版本检查失败: " + o.toString());
                    }
                });
    }

    private void firmwareGprsCheck(final DeviceTable deviceTable) {
        showProgressDialog("正在检查固件版本");
        DeviceManager.gprsFirmwareUpgrade(deviceTable.getMac(), DTFirmwareUpgradeResult.TYPE_CHECK_VERSION,
                new DTIOperateCallback<DTFirmwareUpgradeResult>() {
                    @Override
                    public void onSuccess(final DTFirmwareUpgradeResult upgradeResult, int i) {
                        closeProgressDialog();
                        if (DTFirmwareUpgradeResult.RESULT_NO_NEW_VERSION == upgradeResult.getResult()) {
                            ToastUtils.showToast("已是最新固件版本");
                        } else if (DTFirmwareUpgradeResult.RESULT_HAS_NEW_VERSION == upgradeResult.getResult()) {
                            showFirmwareGprsUpgradeDialog(deviceTable);
                        }
                    }

                    @Override
                    public void onFail(Object o, int i, String s) {
                        closeProgressDialog();
                        ToastUtils.showToast("固件版本检查失败: " + o.toString());
                    }
                });
    }

    private void showFirmwareUpgradeDialog(final DeviceTable deviceTable) {
        ConfirmDialogWithoutTitle dialog = new ConfirmDialogWithoutTitle(mContex, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showProgressDialog("正在下载固件");
                firmwarmUpgrade(deviceTable);
            }
        });
        dialog.setContent("是否更新固件");
        dialog.show();
    }

    private void showFirmwareGprsUpgradeDialog(final DeviceTable deviceTable) {
        ConfirmDialogWithoutTitle dialog = new ConfirmDialogWithoutTitle(mContex, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showProgressDialog("正在下载固件");
                firmwarmGprsUpgrade(deviceTable);
            }
        });
        dialog.setContent("是否更新固件");
        dialog.show();
    }

    //固件升级
    private void firmwarmUpgrade(DeviceTable deviceTable) {
        DeviceManager.firmwareUpgrade(deviceTable.getMac(), DTFirmwareUpgradeResult.TYPE_UPGRADE, DTProtocolVersion.TYPE_THIRD,
                new DTIOperateCallback<DTFirmwareUpgradeResult>() {
                    @Override
                    public void onSuccess(final DTFirmwareUpgradeResult upgradeResult, int i) {
                        if (DTFirmwareUpgradeResult.RESULT_DOWNLOAD_OK == upgradeResult.getResult()) {
                            closeProgressDialog();
                            ToastUtils.showToast("固件升级成功");
                        } else if (DTFirmwareUpgradeResult.RESULT_DOWNLOAD_FAILED == upgradeResult.getResult()) {
                            closeProgressDialog();
                            ToastUtils.showToast("固件升级失败");
                        } else if (DTFirmwareUpgradeResult.RESULT_DOWNLOAD_START == upgradeResult.getResult()
                                || DTFirmwareUpgradeResult.RESULT_DOWNLOADING == upgradeResult.getResult()) {
                            ToastUtils.showToast("正在下载固件");
                        }
                    }

                    @Override
                    public void onFail(Object o, int i, String s) {
                        closeProgressDialog();
                        ToastUtils.showToast("固件升级失败: " + s);
                    }
                });
    }

    private void firmwarmGprsUpgrade(DeviceTable deviceTable) {
        DeviceManager.gprsFirmwareUpgrade(deviceTable.getMac(), DTFirmwareUpgradeResult.TYPE_UPGRADE,
                new DTIOperateCallback<DTFirmwareUpgradeResult>() {
                    @Override
                    public void onSuccess(final DTFirmwareUpgradeResult upgradeResult, int i) {
                        if (DTFirmwareUpgradeResult.RESULT_DOWNLOAD_OK == upgradeResult.getResult()) {
                            closeProgressDialog();
                            ToastUtils.showToast("固件升级成功");
                        } else if (DTFirmwareUpgradeResult.RESULT_DOWNLOAD_FAILED == upgradeResult.getResult()) {
                            closeProgressDialog();
                            ToastUtils.showToast("固件升级失败");
                        } else if (DTFirmwareUpgradeResult.RESULT_DOWNLOAD_START == upgradeResult.getResult()
                                || DTFirmwareUpgradeResult.RESULT_DOWNLOADING == upgradeResult.getResult()) {
                            ToastUtils.showToast("正在下载固件");
                        }
                    }

                    @Override
                    public void onFail(Object o, int i, String s) {
                        closeProgressDialog();
                        ToastUtils.showToast("固件升级失败: " + s);
                    }
                });
    }

}
