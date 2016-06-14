package zc.jk.btlibrary;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

/**
 * Created by ZhangCheng on 2016/6/14.
 */
public abstract class BlueToothCallBack {
    /**
     * 连接成功或失败后调用
     *
     * @param socket 获得的socket
     * @param device 连接的设备
     * @param e 错误
     */
    public abstract void connected(BluetoothSocket socket, BluetoothDevice device, Exception e);

    /**
     * 断开连接后调用
     */
    public abstract void disconnected();




}
