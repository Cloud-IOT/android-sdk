package com.dtston.demo.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.dtston.demo.ApplicationManager;
import com.dtston.demo.R;
import com.dtston.demo.dao.DeviceDao;
import com.dtston.demo.db.DeviceTable;
import com.dtston.demo.db.UserTable;
import com.dtston.demo.dialog.ChoiceListDialog;
import com.dtston.demo.dialog.NetworkProgressDialog;
import com.dtston.demo.ui.LanDeviceListActivity;
import com.dtston.demo.utils.ToastUtils;
import com.dtston.dtcloud.DeviceManager;
import com.dtston.dtcloud.push.DTConnectedDevice;
import com.dtston.dtcloud.push.DTIDeviceConnectCallback;

import java.util.List;

public class LanDeviceAdapter extends BaseAdapter implements AdapterView.OnItemClickListener{

    private List<DTConnectedDevice> mDeviceList;
    private Activity mContext;

    private ChoiceListDialog mChoiceListDialog;
    private DeviceTable mLongClickDevice;
    private NetworkProgressDialog mVProgressDialog;

    public LanDeviceAdapter(Context context, List<DTConnectedDevice> deviceList) {
        this.mContext = (Activity) context;
        this.mDeviceList = deviceList;
    }

    public void setDeviceList(List<DTConnectedDevice> deviceList) {
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_device_lan, null);
            holder = new Holder();
            holder.mac = (TextView) convertView.findViewById(R.id.tv_mac);
            holder.icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            holder.productId = (TextView) convertView.findViewById(R.id.tv_product_id);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        DTConnectedDevice device = mDeviceList.get(position);
        holder.mac.setText(mContext.getResources().getString(R.string.mac_label) +device.getMac());
        holder.productId.setText(mContext.getResources().getString(R.string.product_id_label) +device.getType());
        return convertView;
    }

    public void showDeviceNameEditDlg(final DTConnectedDevice dtConnectedDevice) {
        final EditText et = new EditText(mContext);

        new AlertDialog.Builder(mContext).setTitle("输入设备名")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(et)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        final String deviceName = et.getText().toString();
                        if (deviceName.equals("")) {
                            ToastUtils.showToast("设备名不能为空！");
                        }
                        else {
//                            DeviceManager.bindDeviceBySearch(dtConnectedDevice.getMac(), Integer.valueOf(dtConnectedDevice.getType()), deviceName, new DTIDeviceConnectCallback() {
//                                @Override
//                                public void onSuccess(DTConnectedDevice connectedDevice) {
//                                    DeviceTable device = new DeviceTable();
//                                    UserTable currentUser = ApplicationManager.getInstance().getCurrentUser();
//                                    device.setMac(connectedDevice.getMac());
//                                    device.setDeviceName(deviceName);
//                                    device.setUid(currentUser.getUid());
//                                    device.setType(Integer.valueOf(connectedDevice.getType()));
//                                    device.setDataId(connectedDevice.getDataId());
//                                    DeviceDao.getInstance().insert(device);
//                                    LanDeviceListActivity activity = (LanDeviceListActivity) mContext;
//                                    activity.finish();
//                                }
//
//                                @Override
//                                public void onFail(int errcode, String errmsg) {
//                                    ToastUtils.showToast(errmsg);
//                                }
//                            });
                            DeviceManager.bindDeviceBySearch(dtConnectedDevice.getMac(), Integer.valueOf(dtConnectedDevice.getType()), deviceName, new DTIDeviceConnectCallback() {
                                @Override
                                public void onSuccess(DTConnectedDevice connectedDevice) {
                                    DeviceTable device = new DeviceTable();
                                    UserTable currentUser = ApplicationManager.getInstance().getCurrentUser();
                                    device.setMac(dtConnectedDevice.getMac());
                                    device.setDeviceName(deviceName);
                                    device.setUid(currentUser.getUid());
                                    device.setType(Integer.valueOf(dtConnectedDevice.getType()));
                                    device.setDataId("");
                                    DeviceDao.getInstance().insert(device);
                                    LanDeviceListActivity activity = (LanDeviceListActivity) mContext;
                                    activity.finish();
                                }

                                @Override
                                public void onFail(int errcode, String errmsg) {
                                    ToastUtils.showToast("绑定失败");
                                }
                            });
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    class Holder {
        TextView mac;
        ImageView icon;
        TextView productId;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = null;
        DTConnectedDevice device = mDeviceList.get(position - 1);
        showDeviceNameEditDlg(device);
    }
}
