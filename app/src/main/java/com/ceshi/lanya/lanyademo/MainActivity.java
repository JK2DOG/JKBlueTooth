package com.ceshi.lanya.lanyademo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import zc.jk.btlibrary.BluetoothLog;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    public static final UUID CUSTOM_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final String CELIANG = "cc95020301020002";//测量


    private BluetoothAdapter mBluetoothAdapter;
    public static final int REQUEST_ENABLE_BT = 0;

    private Button bt;
    private TextView bt_found;
    private ListView bt_list;
    private BtAdapter mAdapter;
    private List<BluetoothDevice> mList = new ArrayList<>();

    private BluetoothDevice device;

    private BluetoothSocket mClientSocket;//客户端发送数据
    private OutputStream os;

    private AcceptThread at;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        //判断是否支持BLE设备
//        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
//            Log.e(LanYaTag.Tag, "此设备不支持BLE");
//            Toast.makeText(this, "此设备不支持BLE", Toast.LENGTH_SHORT).show();
//            //初始化蓝牙适配器
//            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//            mBluetoothAdapter = bluetoothManager.getAdapter();//获得蓝牙BLE
//            //打开蓝牙
//            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
//                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//            }
//        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//获取默认的蓝牙适配器，整个系统使用同一个适配器，如果返回null则设备不支持蓝牙
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {//检查蓝牙是否打开
                Intent mBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(mBTIntent, REQUEST_ENABLE_BT);
            }
        } else {
            Toast.makeText(this, "此设备不支持蓝牙!", 1).show();
        }
//        // 获取已经配对的设备
//        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
//                .getBondedDevices();
//        List<BluetoothDevice> list = new ArrayList<BluetoothDevice>(pairedDevices);
        initView();
        initBroadcast();
        mAdapter = new BtAdapter(MainActivity.this, mList);
        bt_list.setAdapter(mAdapter);
        //启动服务
        at = new AcceptThread();
        at.start();

//        if (list.size() > 0)


    }

    //init广播
    private void initBroadcast() {
        IntentFilter inif = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, inif);
        // 搜索完成的广播
        inif = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        // 注册广播
        registerReceiver(receiver, inif);
    }

    private void initView() {
        bt = (Button) findViewById(R.id.bt);
        bt_found = (TextView) findViewById(R.id.bt_found);
        bt_list = (ListView) findViewById(R.id.bt_list);
        bt_list.setOnItemClickListener(this);
        bt.setOnClickListener(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "打开成功", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt:
                mList.clear();
                // 判断是否在搜索,如果在搜索，就取消搜索
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                // 开始搜索
                mBluetoothAdapter.startDiscovery();
                break;
        }
    }


    // 广播接收器
    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            // 收到的广播类型
            String action = intent.getAction();
            // 发现设备的广播
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 从intent中获取设备
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 判断是否配对过
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    // 添加到列表
                    bt_found.append(device.getName() + ":"
                            + device.getAddress() + "\n");
                    String str = device.getName() + "\n" + device.getAddress();
                    Log.e(LanYaTag.Tag, "搜索到的设备：" + str);
                    mList.add(device);
                    mAdapter.notifyDataSetChanged();
                }


                // 搜索完成
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {
                setTitle("搜索完成！");
                mAdapter.notifyDataSetChanged();
            }

        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // 先获得蓝牙的地址和设备名
        BluetoothDevice bd = mAdapter.getItem(position);
        // 单独解析地址
        String address = bd.getAddress();

        // 主动连接蓝牙
        try {
            // 判断是否在搜索,如果在搜索，就取消搜索
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            try {
                // 判断是否可以获得
                if (device == null) {
                    // 获得远程设备
                    device = mBluetoothAdapter.getRemoteDevice(address);
                }
                // 开始连接
                if (mClientSocket == null) {
                    mClientSocket = device
                            .createRfcommSocketToServiceRecord(CUSTOM_UUID);
                    BluetoothLog.e("开始连接");
                    // 连接
                    mClientSocket.connect();
                    // 获得输出流
                    os = mClientSocket.getOutputStream();

                }
            } catch (Exception e) {
                // TODO: handle exception
            }
            // 如果成功获得输出流
            if (os != null) {
                BluetoothLog.e("连接成功");
                os.write(hex2byte(CELIANG.getBytes()));
                BluetoothLog.e("发出信息");

            }

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    // 服务端，需要监听客户端的线程类
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Toast.makeText(MainActivity.this, String.valueOf(msg.obj),
                    Toast.LENGTH_SHORT).show();
            super.handleMessage(msg);
        }
    };

    // 线程服务类
    private class AcceptThread extends Thread {
        private BluetoothServerSocket serverSocket;
        private BluetoothSocket socket;
        // 输入 输出流
        private OutputStream mos;
        private InputStream is;

        public AcceptThread() {
            try {
                serverSocket = mBluetoothAdapter
                        .listenUsingInsecureRfcommWithServiceRecord("ZHANG", CUSTOM_UUID);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            // 截获客户端的蓝牙消息
            try {
                socket = serverSocket.accept(); // 如果阻塞了，就会一直停留在这里
                is = socket.getInputStream();
                mos = socket.getOutputStream();
                // 不断接收请求,如果客户端没有发送的话还是会阻塞
                while (true) {
                    // 每次只发送128个字节
                    byte[] buffer = new byte[128];
                    // 读取
                    int count = is.read();
                    // 如果读取到了，我们就发送刚才的那个Toast
                    Message msg = new Message();
                    msg.obj = new String(buffer, 0, count, "utf-8");
                    handler.sendMessage(msg);
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
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

}
