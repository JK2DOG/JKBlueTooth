package zc.jk.btlibrary;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by ZhangCheng on 2016/6/14.
 */
public class ClientConnectThread extends Thread {
    private static final String CUSTOM_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    private BluetoothSocket mSocket;
    private BluetoothDevice mDevice;
    private BluetoothAdapter mAdapter;
    private Context mContext;
    private CSocketConnectedCallback mCSocketConnectedCallback;

    private ClientConnectThread(Context context, String adress, CSocketConnectedCallback csocketConnectedCallback) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mDevice = mAdapter.getRemoteDevice(adress);
        BluetoothSocket mbs = null;
        try {
            mbs = mDevice.createRfcommSocketToServiceRecord(UUID.fromString(CUSTOM_UUID));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mSocket = mbs;
        mContext = context;
        mCSocketConnectedCallback = csocketConnectedCallback;
    }


    public void run() {

        if (mAdapter.isDiscovering()) {//是否在搜索
            mAdapter.cancelDiscovery();//关掉搜索
        }
        try {
            if (!mSocket.isConnected()) {
                mSocket.connect();
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                mSocket.close();
            } catch (IOException e1) {
                mCSocketConnectedCallback.internalDone(null, null, e1);
                e1.printStackTrace();
                return;
            }
            mCSocketConnectedCallback.internalDone(null, null, e);
            return;
        }

        mCSocketConnectedCallback.internalDone(mSocket, mDevice, null);
    }

    public void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) {

        }
    }

    public static void startUniqueConnectThread(Context context, String address, CSocketConnectedCallback csocketConnectedCallback) {
        ClientConnectThread thread = null;
        if (thread == null || thread.getState() == State.TERMINATED) {
            thread = new ClientConnectThread(context, address, csocketConnectedCallback);
        }
        //线程一旦被终止，就无法使用start在重新启动。
        if (!thread.isAlive() && thread.getState() == State.NEW) {
            thread.start();
        } else {
            csocketConnectedCallback.internalDone(null, null, new IOException("已有在运行的实例"));
        }

    }

}