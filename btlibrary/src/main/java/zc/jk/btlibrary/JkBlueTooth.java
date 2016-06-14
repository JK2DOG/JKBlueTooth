package zc.jk.btlibrary;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;

/**
 * Created by ZhangCheng on 2016/6/14.
 */
public class JkBlueTooth {

    private static BluetoothAdapter mBtAdapter;
    private BluetoothSocket mBtSocket;
    protected static Handler mHandler;

    //搜索并连接蓝牙设备
    public static void connectBluetooth() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {

        }


    }


}
