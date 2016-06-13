package zc.jk.btlibrary;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class BluetoothManager extends ContextWrapper {
    private static final String TAG = "BluetoothManager";
    private static BluetoothManager instance;
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
    private Context mContext;
    private List<BluetoothDevice> deviceList = new ArrayList();
    private MyReceiver myReceiver;
    private OnBTMeasureListener mOnBTMeasureListener;
    private int onDiscoveryFinishedCount = 0;

    public static BluetoothManager getInstance(Context context) {
        if (instance == null) {
            instance = new BluetoothManager(context);
        }

        return instance;
    }

    private BluetoothManager(Context context) {
        super(context);
        this.mContext = context;
    }

    public void initSDK() {
        this.initReceiver();
    }

    public void startBTAffair(OnBTMeasureListener onBTMeasureListener) {
        if (!this._bluetooth.enable()) {
            Toast.makeText(mContext, "请打开手机蓝牙！", 0);
        } else {
            Log.v("BluetoothManager", "启动蓝牙事务");
            this.mOnBTMeasureListener = onBTMeasureListener;
            if (TextUtils.isEmpty(BluetoothService.getConnectedAddr())) {
                this.doDiscovery();
            } else {
                this.mOnBTMeasureListener.onConnected(true, this._bluetooth.getRemoteDevice(BluetoothService.getConnectedAddr()));
//                this.startMeasure();
            }

        }
    }

    public void stopMeasureAndUnregister() {
        this.unregisterReceiver(this.myReceiver);
        this.stopMeasure();
    }

    public boolean startMeasure() {
        if (TextUtils.isEmpty(BluetoothService.getConnectedAddr())) {
            return false;
        } else {
            BluetoothManager.this.sendData("cc95020301020002");
            Log.v("BluetoothManager", "发送启动测量指令：cc95020301020002");
            return true;
        }
    }

    public void stopMeasure() {
        if (TextUtils.isEmpty(BluetoothService.getConnectedAddr())) {
            Toast.makeText(mContext, "未连接蓝牙设备！", 0);
        } else {
            this.sendData("cc95020301030003");
            Log.v("BluetoothManager", "发送停止测量指令：cc95020301030003");
        }
    }

    //APP是否与设备连接
    public boolean isConnectBT() {
        return !TextUtils.isEmpty(BluetoothService.getConnectedAddr());
    }

    public void closeBT() {
        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                BluetoothManager.this.unregisterReceiver(BluetoothManager.this.myReceiver);
                if (BluetoothManager.this._bluetooth.isEnabled()) {
                    BluetoothManager.this._bluetooth.disable();
                }

            }
        }, 100L);
    }

    public void sendData(String dataStr) {
        byte[] data = hex2byte(dataStr.getBytes());
        Intent intent = new Intent("com.action.ACTION_BLUETOOTH_DATA_WRITE");
        intent.putExtra("com.action.ACTION_BLUETOOTH_DATA_EXTRA_BYTEARRAY", data);
        this.sendBroadcast(intent);
    }

    private void initReceiver() {
        this.myReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter("com.action.ACTION_BLUETOOTH_CONNECT");
        filter.addAction("com.action.ACTION_BLUETOOTH_CONNECT2");
        filter.addAction("com.action.ACTION_BLUETOOTH_TIME_SETUP");
        filter.addAction("com.action.ACTION_BLUETOOTH_DATA_READ");
        filter.addAction("com.action.ACTION_BLUETOOTH_RUNNING");
        filter.addAction("com.action.ACTION_BLUETOOTH_POWER");
        filter.addAction("com.action.ACTION_ERROR_MEASURE");
        filter.addAction("com.action.ACTION_BLUETOOTH_MEMORY_MEASURE_DATA");
        filter.addAction("android.bluetooth.device.action.FOUND");
        filter.addAction("android.bluetooth.adapter.action.DISCOVERY_FINISHED");
        filter.addAction("android.bluetooth.device.action.ACL_DISCONNECTED");
        this.registerReceiver(this.myReceiver, filter);
    }

    private void connectToBT(String addr) {
        Log.v("BluetoothManager", "开始连接蓝牙：" + addr);
        Intent intent2;
        if (APPUtil.isServiceRunning("com.bona.rueiguangkangtai.service.BluetoothService", this.mContext)) {
            intent2 = new Intent("com.action.ACTION_CONNECT_TO");
            intent2.putExtra("addr", addr);
            this.mContext.sendBroadcast(intent2);
        } else {
            intent2 = new Intent(this.mContext.getApplicationContext(), BluetoothService.class);
            intent2.putExtra("PREFS_BLUETOOTH_PRE_ADDR_STRING", addr);
            this.mContext.startService(intent2);
        }

    }

    private void postDataReadAction(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        MeasurementResult result = (MeasurementResult) bundle.getSerializable("result");
        this.mOnBTMeasureListener.onMeasureResult(result);
    }

    //蓝牙搜索
    public void doDiscovery() {
        this.onDiscoveryFinishedCount = 0;
        this.deviceList.clear();
        if (!this._bluetooth.isEnabled()) {
            Toast.makeText(mContext, "蓝牙连接中断", 0);
        } else {
            if (this._bluetooth.isDiscovering()) {
                this._bluetooth.cancelDiscovery();
            }

            this._bluetooth.startDiscovery();
            Log.v("BluetoothManager", "开始搜索");
        }
    }


    //退出所有
    public void exitAll() {
        this.unregisterReceiver(this.myReceiver);
        this.stopMeasure();
        if (this.mOnBTMeasureListener != null) {
            this.mOnBTMeasureListener = null;
        }

    }

    private boolean check(String address) {
        int count = this.deviceList.size();

        for (int i = 0; i < count; ++i) {
            if (this.deviceList.get(i).getAddress().equals(address)) {
                return false;
            }
        }

        return true;
    }

    private static byte[] hex2byte(byte[] b) {
        if (b.length % 2 != 0) {
            System.out.println("ERROR: 转化失败  le= " + b.length + " b:" + b.toString());
            return null;
        } else {
            byte[] b2 = new byte[b.length / 2];

            for (int n = 0; n < b.length; n += 2) {
                String item = new String(b, n, 2);
                b2[n / 2] = (byte) Integer.parseInt(item, 16);
            }

            Object b1 = null;
            return b2;
        }
    }

    private class MyReceiver extends BroadcastReceiver {
        private MyReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean device;
            if ("com.action.ACTION_BLUETOOTH_CONNECT".equals(action)) {
                device = intent.getBooleanExtra("com.action.ACTION_BLUETOOTH_CONNECT_EXTRA_BOOLEAN", false);
                if (device) {
                    BluetoothManager.this.sendData("cc95020301010001");
                    Log.v("BluetoothManager", "发送连接血压计指令：cc95020301010001");
                }
            } else if ("com.action.ACTION_BLUETOOTH_CONNECT2".equals(action)) {
                device = intent.getBooleanExtra("com.action.ACTION_BLUETOOTH_CONNECT_EXTRA_BOOLEAN", false);
                if (BluetoothManager.this.deviceList.size() > 0) {
                    BluetoothManager.this.mOnBTMeasureListener.onConnected(device, BluetoothManager.this.deviceList.get(0));
                    if (device) {
                        this.sendTimeSetupCommand();//校时
                    }
                }

            } else if ("com.action.ACTION_BLUETOOTH_TIME_SETUP".equals(action)) {
                BluetoothManager.this.sendData("cc95020304040001");
                Log.v("BluetoothManager", "发送查询电量指令：cc95020304040001");
            } else if ("com.action.ACTION_ERROR_MEASURE".equals(action)) {
                BluetoothManager.this.sendData("cc95020301030003");
                Log.v("BluetoothManager", "发送停止测量指令：cc95020301030003");
                int device1 = intent.getIntExtra("errorCode", 2);
                BluetoothManager.this.mOnBTMeasureListener.onMeasureError(device1);
            } else {
                String device1;
                if ("com.action.ACTION_BLUETOOTH_POWER".equals(action)) {
                    device1 = intent.getStringExtra("power");
                    mOnBTMeasureListener.onPower(device1);
                    sendData("cc95020304040001");
                    Log.v("BluetoothManager", "发送查询电量指令：cc95020304040001");

                } else if ("com.action.ACTION_BLUETOOTH_RUNNING".equals(action)) {
                    device1 = intent.getStringExtra("running");
                    BluetoothManager.this.mOnBTMeasureListener.onRunning(device1);
                } else if ("com.action.ACTION_BLUETOOTH_DATA_READ".equals(action)) {
                    BluetoothManager.this.postDataReadAction(context, intent);
                    BluetoothManager.this.sendData("cc95020301060006");
                    Log.v("BluetoothManager", "发送测量结果应答：cc95020301060006");
                } else if ("com.action.ACTION_BLUETOOTH_MEMORY_MEASURE_DATA".equals(action)) {
                    ArrayList device2 = (ArrayList) intent.getSerializableExtra("memoryMeasureData");
                    BluetoothManager.this.mOnBTMeasureListener.onMemoryMeasureData(device2);
                    BluetoothManager.this.sendData("cc95020302030000");
                    Log.v("BluetoothManager", "获取到记忆数据应答：cc95020302030000");
                } else {
                    BluetoothDevice device3;
                    if ("android.bluetooth.device.action.FOUND".equals(action)) {
                        device3 = intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                        String str = device3.getName() + "\n" + device3.getAddress();
                        Log.v("BluetoothManager", "搜索到的设备：" + str);
                        String arg = str.substring(0, 3);
                        if (BluetoothManager.this.check(device3.getAddress()) && (arg.equals("RBP") || arg.equals("MTK"))) {
                            BluetoothManager.this.deviceList.add(device3);
                            if (BluetoothManager.this._bluetooth.isDiscovering()) {
                                BluetoothManager.this._bluetooth.cancelDiscovery();
                            }
                        }
                    } else if ("android.bluetooth.adapter.action.DISCOVERY_FINISHED".equals(action)) {
                        BluetoothManager.this.onDiscoveryFinishedCount = BluetoothManager.this.onDiscoveryFinishedCount + 1;
                        if (BluetoothManager.this.onDiscoveryFinishedCount == 1) {
                            Log.v("BluetoothManager", "搜索完成-搜索到设备数量：" + BluetoothManager.this.deviceList.size());
                            BluetoothManager.this.mOnBTMeasureListener.onFoundFinish(BluetoothManager.this.deviceList);
                            if (BluetoothManager.this.deviceList.size() > 0) {
                                BluetoothManager.this.connectToBT(BluetoothManager.this.deviceList.get(0).getAddress());
                            }
                        }
                    } else if (action.equals("android.bluetooth.device.action.ACL_DISCONNECTED")) {
                        device3 = intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                        BluetoothManager.this.mOnBTMeasureListener.onDisconnected(device3);
                        BluetoothManager.this.deviceList.clear();
                    }
                }
            }

        }

        private void sendTimeSetupCommand() {
            String year = Integer.toHexString(DateUtil.getYear() - 2000);
            String month = Integer.toHexString(DateUtil.getMonth());
            String day = Integer.toHexString(DateUtil.getCurrentMonthDay());
            String hour = Integer.toHexString(DateUtil.getHour());
            String minute = Integer.toHexString(DateUtil.getMinute());
            String second = Integer.toHexString(DateUtil.getSecond());
            int checkCode = 11 ^ Integer.parseInt(year, 16) ^ Integer.parseInt(month, 16) ^ Integer.parseInt(day, 16) ^ Integer.parseInt(hour, 16) ^ Integer.parseInt(minute, 16) ^ Integer.parseInt(second, 16);
            month = month.length() == 1 ? "0" + month : month;
            day = day.length() == 1 ? "0" + day : day;
            hour = hour.length() == 1 ? "0" + hour : hour;
            minute = minute.length() == 1 ? "0" + minute : minute;
            second = second.length() == 1 ? "0" + second : second;
            String checkCodeStr = Integer.toHexString(checkCode);
            checkCodeStr = checkCodeStr.length() == 1 ? "0" + checkCodeStr : checkCodeStr;
            BluetoothManager.this.sendData("cc9502080302" + year + month + day + hour + minute + second + checkCodeStr);
            Log.v("BluetoothManager", "发送“设置时间”指令");
        }
    }

    public interface OnBTMeasureListener {
        void onFoundFinish(List<BluetoothDevice> var1);

        void onConnected(boolean var1, BluetoothDevice var2);

        void onPower(String var1);

        void onRunning(String var1);

        void onMeasureError(int var1);

        void onMeasureResult(MeasurementResult var1);

        void onDisconnected(BluetoothDevice var1);

        void onMemoryMeasureData(ArrayList<MeasurementResult> var1);
    }
}
