package zc.jk.btlibrary;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class BluetoothSocketConfig {
    private static final String TAG = "BluetoothSocketConfig";
    private static BluetoothSocketConfig mBtSocketConfig = null;
    public static final int SOCKET_NONE = 0;
    public static final int SOCKET_CONNECTED = 1;
    public static final int FIELD_CONNECTED_THREAD = 0;
    public static final int FIELD_SOCKET_STATE = 1;
    private Map<BluetoothSocket, BluetoothSocketInfo> mBluetoothSocekts = new HashMap();

    private BluetoothSocketConfig() {
    }

    public static BluetoothSocketConfig getInstance() {
        if(mBtSocketConfig == null) {
            Class var0 = BluetoothSocketConfig.class;
            synchronized(BluetoothSocketConfig.class) {
                if(mBtSocketConfig == null) {
                    mBtSocketConfig = new BluetoothSocketConfig();
                }
            }
        }

        return mBtSocketConfig;
    }

    public boolean registerSocket(BluetoothSocket socket, BlueToothStateMachine t, int socketState) {
        Log.d("BluetoothSocketConfig", "------>[registerSocket] start");
        boolean status = true;
        if(socketState == 1) {
            Set socketInfo = this.containSockets(socket.getRemoteDevice().getAddress());

            for(Iterator var7 = socketInfo.iterator(); var7.hasNext(); status = false) {
                BluetoothSocket tmp = (BluetoothSocket)var7.next();
                this.unregisterSocket(tmp);
            }
        }

        BluetoothSocketInfo socketInfo1 = new BluetoothSocketInfo( );
        socketInfo1.setBluetoothSocket(socket);
        socketInfo1.setConnectedThread(t);
        socketInfo1.setSocketState(socketState);
        this.mBluetoothSocekts.put(socket, socketInfo1);
        return status;
    }

    public void updateSocketInfo(BluetoothSocket socket, int field, Object arg) {
        if(this.mBluetoothSocekts.containsKey(socket)) {
            BluetoothSocketInfo socketInfo = this.mBluetoothSocekts.get(socket);
            if(field == 0) {
                BlueToothStateMachine socketState = (BlueToothStateMachine)arg;
                socketInfo.setConnectedThread(socketState);
            } else if(field == 1) {
                int socketState1 = ((Integer)arg).intValue();
                socketInfo.setSocketState(socketState1);
            }

            this.mBluetoothSocekts.put(socket, socketInfo);
        } else {
            Log.e("BluetoothSocketConfig", "------>[updateSocketInfo] Socket doesn\'t exist.");
        }

    }

    public synchronized void unregisterSocket(BluetoothSocket socket) {
        Log.d("BluetoothSocketConfig", "------>[unregisterSocket] start");
        if(this.mBluetoothSocekts.containsKey(socket)) {
            BluetoothSocketInfo socketInfo = this.mBluetoothSocekts.get(socket);
            socketInfo.setConnectedThread(null);
            socketInfo.setSocketState(0);
            socketInfo.setBluetoothSocket(null);
            this.mBluetoothSocekts.remove(socket);
            Log.e("BluetoothSocketConfig", "------>[updateSocketInfo] Remove socket " + socket.getRemoteDevice().getAddress());

            try {
                InputStream e = socket.getInputStream();
                if(e != null) {
                    e.close();
                    Log.w("BluetoothSocketConfig", "------>[disconnectSocket] Close the input stream");
                }

                if(socket != null) {
                    socket.close();
                    Log.w("BluetoothSocketConfig", "------>[disconnectSocket] Close bluetooth socket " + socket.toString() + " ; device name is " + socket.getRemoteDevice().getName());
                }
            } catch (Exception var4) {
                var4.printStackTrace();
            }
        }

    }

    public Set<BluetoothSocket> containSockets(String address) {
        HashSet socketSets = new HashSet();
        Iterator it = this.mBluetoothSocekts.keySet().iterator();

        while(it.hasNext()) {
            BluetoothSocket socket = (BluetoothSocket)it.next();
            if(socket.getRemoteDevice().getAddress().contains(address)) {
                socketSets.add(socket);
            }
        }

        return socketSets;
    }

    public Set<BluetoothSocket> getConnectedSocketList() {
        return this.mBluetoothSocekts.keySet();
    }

    public BlueToothStateMachine getConnectedThread(BluetoothSocket socket) {
        BluetoothSocketInfo socketInfo = this.mBluetoothSocekts.get(socket);
        return socketInfo.getConnectedThread(socket);
    }

    public boolean isSocketConnected(BluetoothSocket socket) {
        return this.mBluetoothSocekts.containsKey(socket);
    }

    private class BluetoothSocketInfo {
        private BluetoothSocket mBluetoothSocket;
        private BlueToothStateMachine mConnectedThread;

        private BluetoothSocketInfo() {
        }

        public BluetoothSocket getBluetoothSocket() {
            return this.mBluetoothSocket;
        }

        public BlueToothStateMachine getConnectedThread(BluetoothSocket socket) {
            return this.mConnectedThread;
        }

        protected void setBluetoothSocket(BluetoothSocket socket) {
            this.mBluetoothSocket = socket;
        }

        protected void setSocketState(int socketState) {
        }

        protected void setConnectedThread(BlueToothStateMachine t) {
            this.mConnectedThread = t;
        }
    }
}