package zc.jk.bluetooth;


import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

@SuppressLint({"NewApi"})
public class BluetoothService extends Service {
    private static final String TAG = "BluetoothService";
    public static final String ACTION_BLUETOOTH_DATA_WRITE = "com.action.ACTION_BLUETOOTH_DATA_WRITE";
    public static final String ACTION_MESSAGE_TOAST = "com.action.ACTION_MESSAGE_TOAST";
    public static final String ACTION_BLUETOOTH_DATA_EXTRA_BYTEARRAY = "com.action.ACTION_BLUETOOTH_DATA_EXTRA_BYTEARRAY";
    public static final String ACTION_BLUETOOTH_DATA_READ = "com.action.ACTION_BLUETOOTH_DATA_READ";
    public static final String ACTION_ERROR_MEASURE = "com.action.ACTION_ERROR_MEASURE";
    public static final String ACTION_BLUETOOTH_RUNNING = "com.action.ACTION_BLUETOOTH_RUNNING";
    public static final String ACTION_BLUETOOTH_CONNECT = "com.action.ACTION_BLUETOOTH_CONNECT";
    public static final String ACTION_BLUETOOTH_CONNECT_EXTRA_BOOLEAN = "com.action.ACTION_BLUETOOTH_CONNECT_EXTRA_BOOLEAN";
    public static final String ACTION_BLUETOOTH_CONNECT2 = "com.action.ACTION_BLUETOOTH_CONNECT2";
    public static final String ACTION_BLUETOOTH_TIME_SETUP = "com.action.ACTION_BLUETOOTH_TIME_SETUP";
    public static final String ACTION_BLUETOOTH_POWER = "com.action.ACTION_BLUETOOTH_POWER";
    public static final String ACTION_CONNECT_TO = "com.action.ACTION_CONNECT_TO";
    public static final String ACTION_BLUETOOTH_MEMORY_MEASURE_DATA = "com.action.ACTION_BLUETOOTH_MEMORY_MEASURE_DATA";
    private static String connectedBTAddress = null;
    private String mAddr = "";
    public static boolean isSimulator = false;
    public static boolean enable = true;
    public static boolean enableBTDialog = true;
    public static InputStream is;
    private BluetoothDevice remoteDevice;
    public static final String REQUEST_ECHO_ACTION = "REQUEST_ECHO_ACTION";
    public static final String TOAST = "toast";
    public static final String GET_SERIVICE_STATUS_EVENT = "GET_SERIVICE_STATUS_EVENT";
    public static final String MONITOR_STATUS = "MONITOR_STATUS";
    public static final String TX_BYTES = "TX_BYTES";
    public static final String RX_BYTES = "RX_BYTES";
    private MessageReceiver mBtMsgReceiver;
    private BluetoothConnModel mBluetoothConnModel = null;
    private MessageHandler msgHandler;
    public static boolean isConnect = false;
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_ALERT_DIALOG = 6;
    public static final int MESSAGE_CONNECTED = 7;
    public static final int MESSAGE_SEND_MEMORY_DATA = 8;
    public static final int MSG_MODE_SEND_DATA = 0;
    public static final int MSG_MODE_SEND_STRING = 1;
    public static final int MSG_MODE_SEND_FILE = 2;

    public BluetoothService() {
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        Log.v("BluetoothService", "onCreate");
        this.initPhoneStateListener();
        this.msgHandler = new MessageHandler();
        this.mBtMsgReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter("com.action.ACTION_BLUETOOTH_DATA_WRITE");
        filter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        filter.addAction("android.bluetooth.device.action.ACL_DISCONNECTED");
        filter.addAction("com.action.ACTION_CONNECT_TO");
        this.registerReceiver(this.mBtMsgReceiver, filter);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        int ret = super.onStartCommand(intent, flags, startId);
        if (intent == null) {
            this.stopSelf();
            return ret;
        } else {
            String addr = intent.getStringExtra("PREFS_BLUETOOTH_PRE_ADDR_STRING");
            if (!TextUtils.isEmpty(addr)) {
                this.mAddr = addr;
            }

            if (this.mBluetoothConnModel == null) {
                this.mBluetoothConnModel = new BluetoothConnModel(this, this.msgHandler);
            }

            if (!TextUtils.isEmpty(this.mAddr)) {
                this.connectTo(this.mAddr);
            }

            return 2;
        }
    }

    public void onDestroy() {
        Log.v("BluetoothService", "onDestroy");
        isConnect = false;
        this.sendConnectBroadcast(false, "onDestroy");
        this.unregisterReceiver(this.mBtMsgReceiver);
        if (this.mBluetoothConnModel != null) {
            this.mBluetoothConnModel.terminated();
        }

        this.mBluetoothConnModel = null;
        super.onDestroy();
    }

    public static String getConnectedAddr() {
        return connectedBTAddress;
    }

    public void postBTConnectState(boolean connect, String msg) {
        if (connect) {
            if (connect != isConnect) {
                this.sendConnectBroadcast(true, msg);
            }
        } else if (connect != isConnect) {
            String sysLanguage = Locale.getDefault().getLanguage();
            String str = "";
            if (sysLanguage.equals("en")) {
                str = "Device disconnected!";
            } else {
                str = "蓝牙设备已经断开连接!";
            }
            Toast.makeText(this,str,0);
            this.sendConnectBroadcast(false, msg);
        }

        isConnect = connect;
    }

    private void connectTo(final String deviceAddress) {
        Log.v("BluetoothService", "connectTo");
        (new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                BluetoothService.this.remoteDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
                BluetoothService.this.mBluetoothConnModel.connectTo(BluetoothService.this.remoteDevice);
            }
        })).start();
    }

    private void postStateChangeAction(Context context, Intent intent) {
        int currentState = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -2147483648);
        switch (currentState) {
            case 10:
                try {
                    this.terminatedAllSockets();
                } catch (Exception var5) {
                    var5.printStackTrace();
                }
            case 11:
            case 12:
            case 13:
            default:
        }
    }

    private void postDataWrite(Intent intent) {
        byte[] data = intent.getExtras().getByteArray("com.action.ACTION_BLUETOOTH_DATA_EXTRA_BYTEARRAY");
        if (data != null) {
            if (data != null && data.length >= 1) {
                this.mBluetoothConnModel.writeToAllSockets(data);
            }
        }
    }

    private void disconnectTo(String address) {
        if (this.mBluetoothConnModel == null) {
            this.mBluetoothConnModel = new BluetoothConnModel(this, this.msgHandler);
        }

        this.mBluetoothConnModel.disconnectSocketFromAddress(address);
    }

    private void terminatedAllSockets() {
        this.mBluetoothConnModel.terminated();
        this.mBluetoothConnModel = null;
    }

    private void postBTMsg(int flag, int sflag, byte[] data) {
        int ans;
        if (flag == 4 && sflag == 4) {
            ans = ((data[0] & 255) << 8) + (data[1] & 255);
            this.sendPowerBroadcast(ans);
        } else if (flag == 1 && sflag == 6) {
            Log.v("BluetoothService", "测量完成");
            this.getResult(data);
        } else if (flag == 1 && sflag == 5) {
            if (data.length < 2) {
                return;
            }

            ans = ((data[0] & 255) << 8) + (data[1] & 255);
            this.sendRunningBroadcast(ans);
        } else if (flag == 1 && sflag == 7) {
            ans = data[0] & 255;
            Log.v("BluetoothService", "测量错误码：" + ans);
            this.sendErrorBroadcast(ans);
        } else if (flag == 1 && sflag == 1) {
            ans = data[0] & 255;
            if (ans == 0) {
                connectedBTAddress = this.mAddr;
                this.sendConnect2Broadcast(true);
            } else {
                connectedBTAddress = null;
                this.sendConnect2Broadcast(false);
            }
        } else if (flag == 3 && sflag == 2) {
            this.sendTimeSetupBroadcast();
        } else if (flag == 2 && sflag == 3) {
            this.resolveMemoryMeasureDate(data);
        }

    }

    private void resolveMemoryMeasureDate(byte[] data) {
        int count = data.length > 0 ? data[0] & 255 : 0;
        Log.v("BluetoothService", "记忆数据的条数：" + count);
        if (data.length >= count * 10 + 1) {
            ArrayList measurementResults = new ArrayList();

            for (int i = 0; i < count; ++i) {
                MeasurementResult result = new MeasurementResult();
                int year = (data[i * 10 + 1] & 255) + 2000;
                int month = data[i * 10 + 2] & 255;
                int day = data[i * 10 + 3] & 255;
                int hour = data[i * 10 + 4] & 255;
                int minute = data[i * 10 + 5] & 255;

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month - 1, day, hour, minute);
                long resultTime = calendar.getTimeInMillis()/1000;
                Log.e("ZC","获取到的时间戳：" + calendar.getTimeInMillis()/1000);
//                String createTimeStr = String.format("%04d%02d%02d%02d%02d", year, month, day, hour, minute);
                // String createTimeStr = String.valueOf((data[i * 10 + 1] & 255) + 2000) + String.valueOf(data[i * 10 + 2] & 255) + (data[i * 10 + 3] & 255) + (data[i * 10 + 4] & 255) + (data[i * 10 + 5] & 255);
                result.setCreateTime(resultTime);

                int llow = ((data[i * 10 + 6] & 255) << 8) + (data[i * 10 + 7] & 255);
                result.setCheckShrink(llow);
                int intszdatal = ((data[i * 10 + 8] & 255) << 8) + (data[i * 10 + 9] & 255);
                result.setCheckDiastole(intszdatal);
                int heartRate = data[i * 10 + 10] & 255;
                result.setCheckHeartRate(heartRate);
                measurementResults.add(result);
            }

            this.sendMemoryMeasureDataBroadcast(measurementResults);
        }
    }

    private void getResult(byte[] data) {
        MeasurementResult result = new MeasurementResult();
        int llow = ((data[5] & 255) << 8) + (data[6] & 255);
        result.setCheckShrink(llow);
        int intszdatal = ((data[7] & 255) << 8) + (data[8] & 255);
        result.setCheckDiastole(intszdatal);
        int heartRate = data[9] & 255;
        result.setCheckHeartRate(heartRate);
        result.setCreateTime(Calendar.getInstance().getTimeInMillis()/1000);
        Bundle bundle = new Bundle();
        bundle.putSerializable("result", result);
        Log.v("BluetoothService", "测量结果-收缩压:" + result.getCheckShrink());
        Intent intent = new Intent("com.action.ACTION_BLUETOOTH_DATA_READ");
        intent.putExtras(bundle);
        this.sendBroadcast(intent);
    }

    private void sendMemoryMeasureDataBroadcast(final ArrayList<MeasurementResult> measurementResults) {
        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent("com.action.ACTION_BLUETOOTH_MEMORY_MEASURE_DATA");
                intent.putExtra("memoryMeasureData", measurementResults);
                BluetoothService.this.sendBroadcast(intent);
            }
        }, 3000L);
    }

    private void sendTimeSetupBroadcast() {
        Intent intent = new Intent("com.action.ACTION_BLUETOOTH_TIME_SETUP");
        this.sendBroadcast(intent);
    }

    private void sendErrorBroadcast(int errorCode) {
        Intent intent = new Intent("com.action.ACTION_ERROR_MEASURE");
        intent.putExtra("errorCode", errorCode);
        this.sendBroadcast(intent);
    }

    private void sendRunningBroadcast(int running) {
        Intent intent = new Intent("com.action.ACTION_BLUETOOTH_RUNNING");
        intent.putExtra("running", String.valueOf(running));
        this.sendBroadcast(intent);
    }

    private void sendPowerBroadcast(int power) {
        Intent intent = new Intent("com.action.ACTION_BLUETOOTH_POWER");
        intent.putExtra("power", String.valueOf(power));
        this.sendBroadcast(intent);
    }

    private void sendConnectBroadcast(boolean connect, String msg) {
        Intent intent = new Intent("com.action.ACTION_BLUETOOTH_CONNECT");
        intent.putExtra("com.action.ACTION_BLUETOOTH_CONNECT_EXTRA_BOOLEAN", connect);
        this.sendBroadcast(intent);
    }

    private void sendConnect2Broadcast(boolean connect) {
        Intent intent = new Intent("com.action.ACTION_BLUETOOTH_CONNECT2");
        intent.putExtra("com.action.ACTION_BLUETOOTH_CONNECT_EXTRA_BOOLEAN", connect);
        this.sendBroadcast(intent);
    }

    private void initPhoneStateListener() {
        TelephonyManager tpm = (TelephonyManager) this.getSystemService("phone");
        tpm.listen(new MyPhoneStateListener(), 32);
    }

    @SuppressLint({"HandlerLeak"})
    private class MessageHandler extends Handler {
        private MessageHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                case 4:
                case 6:
                default:
                    break;
                case 2:
                    int flag = msg.arg1;
                    int sflag = msg.arg2;
                    byte[] dataBuff = (byte[]) msg.obj;
                    BluetoothService.this.postBTMsg(flag, sflag, dataBuff);
                    break;
                case 3:
                    Intent intent2 = (Intent) msg.obj;
                    BluetoothService.this.postDataWrite(intent2);
                    break;
                case 5:
                    Intent intent = new Intent("com.action.ACTION_MESSAGE_TOAST");
                    BluetoothService.this.getApplicationContext().sendBroadcast(intent);
                    break;
                case 7:
                    String tmp = (String) msg.obj;
                    BluetoothService.this.postBTConnectState(true, tmp);
            }

        }
    }

    public class MessageReceiver extends BroadcastReceiver {
        public MessageReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("com.action.ACTION_BLUETOOTH_DATA_WRITE")) {
                BluetoothService.this.msgHandler.sendMessageDelayed(BluetoothService.this.msgHandler.obtainMessage(3, intent), 500L);
            } else if (action.equals("android.bluetooth.adapter.action.STATE_CHANGED")) {
                BluetoothService.this.postStateChangeAction(context, intent);
            } else if (action.equals("android.bluetooth.device.action.ACL_DISCONNECTED")) {
                BluetoothDevice device = intent.getExtras().getParcelable("android.bluetooth.device.extra.DEVICE");
                BluetoothService.this.disconnectTo(device.getAddress());
                Log.v("BluetoothService", "与" + device.getName() + "连接中断");
                BluetoothService.this.postBTConnectState(false, "android.bluetooth.device.action.ACL_DISCONNECTED");
                BluetoothService.connectedBTAddress = null;
            } else if (action.equals("com.action.ACTION_CONNECT_TO")) {
                if (BluetoothService.this.mBluetoothConnModel == null) {
                    BluetoothService.this.mBluetoothConnModel = new BluetoothConnModel(BluetoothService.this, BluetoothService.this.msgHandler);
                }

                BluetoothService.this.mAddr = intent.getStringExtra("PREFS_BLUETOOTH_PRE_ADDR_STRING");
                if (!TextUtils.isEmpty(BluetoothService.this.mAddr)) {
                    BluetoothService.this.connectTo(BluetoothService.this.mAddr);
                }
            }

        }
    }

    class MyPhoneStateListener extends PhoneStateListener {
        MyPhoneStateListener() {
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case 0:
                default:
                    break;
                case 1:
                    Intent intent = new Intent("com.action.PHONE_IS_COMING");
                    BluetoothService.this.sendBroadcast(intent);
                    break;
                case 2:
                    BluetoothService.enable = false;
            }

        }
    }
}
