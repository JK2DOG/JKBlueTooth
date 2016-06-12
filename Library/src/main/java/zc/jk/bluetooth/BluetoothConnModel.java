package zc.jk.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;


@SuppressLint({"NewApi"})
public class BluetoothConnModel {
    private static final boolean D = true;
    static final String TAG = "BluetoothConnModel";
    private static final String NAME = "BluetoothConn";
    public static final UUID CUSTOM_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final String MONITOR_OUTPUT_NAME = "output.txt";
    private final BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
    private final Handler mHandler;
    private ServerSocketThread mServerSocketThread;
    private BluetoothSocketConfig mSocketConfig = null;
    private FileOutputStream mOutputFile;
    private boolean mMonitor = false;
    private int mTxBytes = 0;
    private int mRxBytes = 0;
    private BluetoothDevice mBluetoothDevice;

    public static int mConnectFlag = 1;//全局静态值连接判断


    public int getTxBytes() {
        return this.mTxBytes;
    }

    public int getRxBytes() {
        return this.mRxBytes;
    }

    public boolean getFileMonitor() {
        return this.mMonitor;
    }

    public void startFileMonitor(boolean b) {
        Log.d("BluetoothConnModel", "startFileMonitor " + b);
        this.mMonitor = b;
        if (this.mMonitor) {
            File root = Environment.getExternalStorageDirectory();

            try {
                this.mOutputFile = new FileOutputStream(root + "/" + "output.txt", false);
            } catch (Exception var5) {
                Log.e("BluetoothConnModel", "new FileOutputStream fail", var5);
            }
        } else {
            try {
                this.mOutputFile.close();
            } catch (Exception var4) {
                ;
            }
        }

    }

    public BluetoothConnModel(Context context, Handler handler) {
        this.mHandler = handler;
        this.mSocketConfig = BluetoothSocketConfig.getInstance();
    }

    public synchronized void startSession() {
        Log.d("BluetoothConnModel", "---->[startSession] ServerSocketThread start...");
        if (this.mServerSocketThread == null) {
            Log.v("BluetoothConnModel", "---->[startSession] mServerSocketThread is dead");
            this.mServerSocketThread = new ServerSocketThread();
            this.mServerSocketThread.start();
        } else {
            Log.v("BluetoothConnModel", "---->[startSession] mServerSocketThread is alive : " + this);
        }

    }


    public synchronized void connectTo(BluetoothDevice device) {
        this.mBluetoothDevice = device;
        Log.d("BluetoothConnModel", "---->[connectTo] ClientSocketThread start...");
        try {
            SocketThread mSocketThread = new SocketThread(device);
            mSocketThread.run();
        } catch (Exception ex) {
            Log.d("BluetoothConnModel", "---->[connectTo] Run thread failed.");
            ex.printStackTrace();
        }
    }

    public synchronized void connected(BluetoothSocket socket) {
        Log.v("BluetoothConnModel", "成功连接设备：" + this.mBluetoothDevice.getName() + "," + this.mBluetoothDevice.getAddress());
        this.mHandler.obtainMessage(7, -1, -1, "0").sendToTarget();
        BlueToothStateMachine connectedThread = new BlueToothStateMachine(this.mSocketConfig, socket, this.mHandler);
        if (!this.mSocketConfig.registerSocket(socket, connectedThread, 1)) {
            this.mHandler.obtainMessage(6, -1, -1, "Device link back again!").sendToTarget();
        }

        Log.e("BluetoothConnModel", "---->[connected] connectedThread hashcode = " + connectedThread.toString());
        connectedThread.start();
    }

    public void writeToSocket(BluetoothSocket socket, byte[] data) {
        Log.d("BluetoothConnModel", "---->writeToDevice start...");
        BlueToothStateMachine connectedThread = this.mSocketConfig.getConnectedThread(socket);
        Log.e("BluetoothConnModel", "---->[writeToDevice] connectedThread hashcode = " + connectedThread.toString());
        if (this.mSocketConfig.isSocketConnected(socket)) {
            Log.w("BluetoothConnModel", "---->[writeToDevice] The socket is alived.");
            boolean flag = connectedThread.write(data);
            Log.v("BluetoothConnModel", "-----指令写入情况---->" + flag);
        } else {
            Log.w("BluetoothConnModel", "---->[writeToDevice] The socket has been closed.");
        }

    }

    public void writeToAllSockets(byte[] data) {
        Log.d("BluetoothConnModel", "---->writeToAllDevices start...");
        Iterator var3 = this.mSocketConfig.getConnectedSocketList().iterator();

        while (var3.hasNext()) {
            BluetoothSocket socket = (BluetoothSocket) var3.next();
            synchronized (this) {
                this.writeToSocket(socket, data);
                Log.e("BluetoothConnModel", "---->[writeToAllDevices] currentTimeMillis: " + System.currentTimeMillis());
            }
        }

    }

    public void disconnectServerSocket() {
        Log.d("BluetoothConnModel", "---->[disconnectServerSocket]---->");
        if (this.mServerSocketThread != null) {
            this.mServerSocketThread.disconnect();
            this.mServerSocketThread = null;
            Log.w("BluetoothConnModel", "---->[disconnectServerSocket] NULL mServerSocketThread");
        }

    }

    public void disconnectSocketFromAddress(String address) {
        Set socketSets = this.mSocketConfig.containSockets(address);
        Iterator var4 = socketSets.iterator();

        while (var4.hasNext()) {
            BluetoothSocket socket = (BluetoothSocket) var4.next();
            this.disconnectSocket(socket);
        }

    }

    public synchronized void disconnectSocket(BluetoothSocket socket) {
        Log.w("BluetoothConnModel", "---->[disconnectSocket]---->" + socket.toString() + " ; device name is " + socket.getRemoteDevice().getName());
        if (!this.mSocketConfig.isSocketConnected(socket)) {
            Log.w("BluetoothConnModel", "---->[disconnectSocket] mSocketConfig doesn\'t contain the socket: " + socket.toString() + " ; device name is " + socket.getRemoteDevice().getName());
        } else {
            Log.d("BluetoothConnModel", socket.getRemoteDevice().getName() + " connection was disconnected!");
            this.mSocketConfig.unregisterSocket(socket);
        }
    }

    public void terminated() {
        Log.w("BluetoothConnModel", "---->[terminated]--------------");
        this.disconnectServerSocket();
        Iterator var2 = this.mSocketConfig.getConnectedSocketList().iterator();

        while (var2.hasNext()) {
            BluetoothSocket socket = (BluetoothSocket) var2.next();
            Log.w("BluetoothConnModel", "[terminated] Left Socket(s): " + this.mSocketConfig.getConnectedSocketList().size());
            this.disconnectSocket(socket);
        }

        Log.w("BluetoothConnModel", "---->[terminated] Final Left Socket(s): " + this.mSocketConfig.getConnectedSocketList().size());
    }

    private void notifyUiFromToast(String str) {
        Message msg = this.mHandler.obtainMessage(5);
        Bundle bundle = new Bundle();
        bundle.putString("toast", str);
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
    }

    private static boolean shouldUseFixChannel() {
        if (Build.VERSION.RELEASE.startsWith("4.0.")) {
            if (Build.MANUFACTURER.equals("samsung")) {
                return true;
            }

            if (Build.MANUFACTURER.equals("HTC")) {
                return true;
            }
        }

        return Build.VERSION.RELEASE.startsWith("4.1.") && Build.MANUFACTURER.equals("samsung") ? true : Build.MANUFACTURER.equals("Xiaomi") && Build.VERSION.RELEASE.equals("2.3.5");
    }

    private class ServerSocketThread implements Runnable {
        private BluetoothServerSocket mmServerSocket = null;
        private Thread thread = null;
        private boolean isServerSocketValid = false;

        @SuppressLint({"NewApi"})
        public ServerSocketThread() {
            this.thread = new Thread(this);
            BluetoothServerSocket serverSocket = null;

            try {
                Log.v("BluetoothConnModel", "---->[ServerSocketThread] Enter the listen server socket");
                serverSocket = BluetoothConnModel.this.mAdapter.listenUsingInsecureRfcommWithServiceRecord("BluetoothConn", BluetoothConnModel.CUSTOM_UUID);
                Log.v("BluetoothConnModel", "---->[ServerSocketThread] serverSocket hash code = " + serverSocket.hashCode());
                this.isServerSocketValid = true;
            } catch (IOException var4) {
                Log.e("BluetoothConnModel", "---->[ServerSocketThread] Constructure: listen() failed", var4);
                var4.printStackTrace();
                BluetoothConnModel.this.notifyUiFromToast("Listen failed. Restart application again");
                this.isServerSocketValid = false;
                BluetoothConnModel.this.mServerSocketThread = null;
            }

            this.mmServerSocket = serverSocket;
            String serverSocketName = this.mmServerSocket.toString();
            Log.v("BluetoothConnModel", "---->[ServerSocketThread] serverSocket name = " + serverSocketName);
        }

        public void start() {
            this.thread.start();
        }

        public void run() {
            Log.d("BluetoothConnModel", "---->BEGIN ServerSocketThread " + this);
            BluetoothSocket socket = null;

            while (this.isServerSocketValid) {
                try {
                    Log.v("BluetoothConnModel", "---->[ServerSocketThread] Enter while loop");
                    Log.v("BluetoothConnModel", "---->[ServerSocketThread] serverSocket hash code = " + this.mmServerSocket.hashCode());
                    socket = this.mmServerSocket.accept();
                    Log.v("BluetoothConnModel", "---->[ServerSocketThread] Got client socket");
                } catch (IOException var4) {
                    Log.e("BluetoothConnModel", "---->accept() failed", var4);
                    break;
                }

                if (socket != null) {
                    BluetoothConnModel e = BluetoothConnModel.this;
                    synchronized (BluetoothConnModel.this) {
                        Log.v("BluetoothConnModel", "---->[ServerSocketThread] " + socket.getRemoteDevice() + " is connected.");
                        BluetoothConnModel.this.connected(socket);
                        BluetoothConnModel.this.disconnectServerSocket();
                        break;
                    }
                }
            }

            Log.v("BluetoothConnModel", "---->[ServerSocketThread] break from while");
            BluetoothConnModel.this.startSession();
        }

        public void disconnect() {
            Log.d("BluetoothConnModel", "---->[ServerSocketThread] disconnect " + this);

            try {
                Log.v("BluetoothConnModel", "---->[ServerSocketThread] disconnect serverSocket name = " + this.mmServerSocket.toString());
                this.mmServerSocket.close();
                Log.v("BluetoothConnModel", "---->[ServerSocketThread] mmServerSocket is closed.");
            } catch (IOException var2) {
                Log.e("BluetoothConnModel", "---->close() of server failed", var2);
            }

        }
    }

    private class SocketThread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private BluetoothSocket tmp = null;

        @SuppressLint({"NewApi"})
        public SocketThread(BluetoothDevice device) {
            Log.v("BluetoothConnModel", "---->[SocketThread] Enter these server sockets");
            this.mmDevice = device;

            try {
                this.tmp = device.createInsecureRfcommSocketToServiceRecord(BluetoothConnModel.CUSTOM_UUID);
                Log.v("BluetoothConnModel", "默认UUID:" + BluetoothConnModel.CUSTOM_UUID.toString());
                Log.v("BluetoothConnModel", "---->[SocketThread] Constructure: Get a BluetoothSocket for a connection, create Rfcomm");
            } catch (Exception var4) {
                Log.e("BluetoothConnModel", "---->create() failed", var4);
            }

            this.mmSocket = this.tmp;
        }

        public void run() {
            Log.d("BluetoothConnModel", "---->BEGIN SocketThread" + this);
            BluetoothConnModel.this.mAdapter.cancelDiscovery();

            try {
                this.mmSocket.connect();
                Log.v("BluetoothConnModel", "---->[SocketThread] Return a successful connection");
            } catch (Exception var13) {
                Log.e("BluetoothConnModel", "---->[SocketThread] Connection failed", var13);
                var13.printStackTrace();

                try {
                    Log.v("BluetoothConnModel", "-----使用远程设备端UUID连接--->");
                    ParcelUuid[] e2 = this.mmDevice.getUuids();
                    UUID clazz1 = e2[0].getUuid();
                    Log.v("BluetoothConnModel", "远程设备端UUID:" + clazz1.toString());
                    this.mmSocket.close();
                    this.mmSocket = this.mmDevice.createInsecureRfcommSocketToServiceRecord(clazz1);
                    this.mmSocket.connect();
                } catch (Exception var11) {
                    var11.printStackTrace();
                    Class clazz = this.mmDevice.getClass();
                    Class[] paramTypes = new Class[]{Integer.TYPE};

                    try {
                        Log.v("BluetoothConnModel", "-----尝试反射连接--->");
                        Method e1 = clazz.getMethod("createInsecureRfcommSocket", paramTypes);
                        Object[] e31 = new Object[]{Integer.valueOf(1)};
                        this.mmSocket.close();
                        if (BluetoothConnModel.shouldUseFixChannel()) {
                            this.mmSocket = (BluetoothSocket) e1.invoke(this.mmDevice, Integer.valueOf(6));
                        } else {
                            this.mmSocket = (BluetoothSocket) e1.invoke(this.mmDevice, e31);
                        }

                        this.mmSocket.connect();
                    } catch (Exception var10) {
                        Log.v("BluetoothConnModel", "-----反射失败--->" + var10.getMessage());

                        try {
                            Log.v("BluetoothConnModel", "-----尝试第二种反射连接--->");
                            Method e3 = clazz.getMethod("createScoSocket", new Class[0]);
                            this.mmSocket.close();
                            this.mmSocket = (BluetoothSocket) e3.invoke(this.mmDevice, new Object[0]);
                            this.mmSocket.connect();
                        } catch (Exception var9) {
                            Log.v("BluetoothConnModel", "-----反射2失败--->" + var9.getMessage());
                            try {
                                this.mmSocket.close();
                                initConnectSecond();
                                Log.v("BluetoothConnModel", "---->[SocketThread] Connect fail, close the client socket");
                            } catch (IOException var8) {
                                Log.e("BluetoothConnModel", "---->unable to close() socket during connection failure", var8);
                            }
                        }
                    }
                }

                return;
            }

            BluetoothConnModel e = BluetoothConnModel.this;
            synchronized (BluetoothConnModel.this) {
                BluetoothConnModel.this.connected(this.mmSocket);
                Log.v("BluetoothConnModel", "---->[SocketThread] " + this.mmDevice + " is connected.");
            }

            Log.v("BluetoothConnModel", "---->END mConnectThread");
        }
    }


    /**
     * 尝试第二次连接
     */
    private void initConnectSecond() {
        if (mConnectFlag < 2) {//反射两次后跳出
            mConnectFlag++;
            SocketThread mSocketThread = new SocketThread(this.mBluetoothDevice);
            mSocketThread.run();
        } else {//两次完整的尝试连接失败后发送一个Event
            EventBus.getDefault().post(new BPEvent());
        }
    }
}