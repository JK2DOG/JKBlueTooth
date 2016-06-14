package zc.jk.btlibrary;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Looper;

import java.io.IOException;

/**
 * Created by ZhangCheng on 2016/6/14.
 */
public abstract class CSocketConnectedCallback {

    public abstract void done(BluetoothSocket socket, BluetoothDevice device, IOException e);

    public void internalDone(final BluetoothSocket socket, final BluetoothDevice device, final IOException e) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            if (!JkBlueTooth.mHandler.post(new Runnable() {
                @Override
                public void run() {
                    done(socket, device, e);
                }
            })) {

            }
        } else {
            this.done(socket, device, e);
        }
    }
}
