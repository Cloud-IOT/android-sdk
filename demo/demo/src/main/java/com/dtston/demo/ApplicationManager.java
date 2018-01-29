package com.dtston.demo;

import android.app.Application;

import com.dtston.demo.dao.UserDao;
import com.dtston.demo.db.DataHelper;
import com.dtston.demo.db.DeviceTable;
import com.dtston.demo.db.UserTable;
import com.dtston.demo.utils.Debugger;
import com.dtston.dtcloud.DtCloudManager;
import com.dtston.dtcloud.push.DTIOperateCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationManager extends Application {
	
	private static ApplicationManager mRef;
	
	private UserTable mCurrentUser = null;
	private DeviceTable mCurrentControlDevice = null;

	private List<DeviceTable> mDeviceList;
	
	private boolean isUserMessageNotify = false;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mRef = this;
		Thread.setDefaultUncaughtExceptionHandler(AppUncaughtExceptionHandler.getInstance());
		DataHelper.getInstance(this);

		initCurrentUser();

		DtCloudManager.setDebug(true);
		DtCloudManager.setTestEnvironment(true);
		DtCloudManager.setAppInfoEnterpriseId("110");
		DtCloudManager.init(this, new DTIOperateCallback() {
			@Override
			public void onSuccess(Object var1, int var2) {
				System.out.println("初始化成功");
			}

			@Override
			public void onFail(Object errmsg, int errcode, String var3) {
				System.out.println("初始化错误：" + errmsg + ",errcode=" + errcode);
			}
		});

		//测试多进程下 dtcloud初始化情况
//		startService(new Intent(this, MultiProgressService.class));
	}
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		Debugger.logD("Application", "onLowMemory");
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		Debugger.logD("Application", "onTerminate");
	}

	private void initCurrentUser() {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put(UserTable.TYPE, UserTable.TYPE_CURRENT_USER);
		UserTable currentUser = UserDao.getInstance().getCurrentUser();
		ApplicationManager.getInstance().setCurrentUser(currentUser);
	}
	
	public static ApplicationManager getInstance() {
		return mRef;
	}

	public UserTable getCurrentUser() {
		return mCurrentUser;
	}

	public void setCurrentUser(UserTable currentUser) {
		this.mCurrentUser = currentUser;
	}

	public DeviceTable getCurrentControlDevice() {
		return mCurrentControlDevice;
	}

	public void setCurrentControlDevice(DeviceTable currentControlDevice) {
		this.mCurrentControlDevice = currentControlDevice;
	}

	public List<DeviceTable> getDeviceList() {
		if (mDeviceList == null) {
			mDeviceList = new ArrayList<DeviceTable>();
		}
		return mDeviceList;
	}

	public void setDeviceList(List<DeviceTable> deviceList) {
		this.mDeviceList = deviceList;
	}
	
	public void addDeviceList(DeviceTable device) {
		if (mDeviceList == null) {
			mDeviceList = new ArrayList<DeviceTable>();
		}
		this.mDeviceList.add(device);
	}

}
