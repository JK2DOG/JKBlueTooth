package zc.jk.btlibrary;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BlueToothStateMachine implements Runnable {
    private static final String TAG = "BlueToothStateMachine";
    private static final int PRE_CODE1 = 0;
    private static final int PRE_CODE2 = 1;
    private static final int VER_CODE = 2;
    private static final int LEN_CODE = 3;
    private static final int FLAG_CODE = 4;
    private static final int SFLAG_CODE = 5;
    private static final int DATA_CODE = 6;
    private static final int CS_CODE = 7;
    private static final byte HEADER_CODE1 = -86;
    private static final byte HEADER_CODE2 = -107;
    private Thread thread = null;
    private BluetoothSocketConfig socketConfig;
    private BluetoothSocket mmSocket;
    private Handler mHandler;
    private InputStream inStream;
    private OutputStream outStream;
    private SparseArray<IHandler> handlerMap = new SparseArray();
    private int status = 0;
    private int dataLength = 0;
    private int flag = 0;
    private int sflag = 0;
    private byte[] dataBuff = null;
    private byte[] dataBuffBefore = null;
    private boolean isDataSame = false;
    private int readTimeout = 0;

    public BlueToothStateMachine(BluetoothSocketConfig socketConfig, BluetoothSocket mmSocket, Handler mHandler) {
        try {
            this.thread = new Thread(this, mmSocket.getRemoteDevice().toString());
            this.socketConfig = socketConfig;
            this.mmSocket = mmSocket;
            this.mHandler = mHandler;
            this.inStream = mmSocket.getInputStream();
            this.outStream = mmSocket.getOutputStream();
            this.initReadHeader1();
            this.initPreCode2();
            this.initVerCode();
            this.initLenCode();
            this.initFlagCode();
            this.initSFlagCode();
            this.initDataCode();
            this.initCSCode();
            Log.i("BluetoothConnModel", "------>[ConnectedThread] Constructure: Set up bluetooth socket i/o stream");
        } catch (IOException var5) {
            Log.e("BluetoothConnModel", "------>[ConnectedThread] temp sockets not created", var5);
        }

    }

    private void initReadHeader1() {
        this.handlerMap.append(0, new IHandler() {
            public void handler() {
                try {
                    if (BlueToothStateMachine.this.inStream.available() >= 1) {
                        byte[] e = new byte[1];
                        BlueToothStateMachine.this.inStream.read(e);
                        if (-86 == e[0]) {
                            BlueToothStateMachine.this.status = 1;
                        }
                    } else {
                        Thread.sleep(10L);
                    }
                } catch (IOException var2) {
                    BlueToothStateMachine.this.status = 0;
                    var2.printStackTrace();
                } catch (InterruptedException var3) {
                    BlueToothStateMachine.this.status = 0;
                    var3.printStackTrace();
                }

            }
        });
    }

    private void initPreCode2() {
        this.handlerMap.append(1, new IHandler() {
            public void handler() {
                try {
                    if (BlueToothStateMachine.this.inStream.available() >= 1) {
                        byte[] e = new byte[1];
                        BlueToothStateMachine.this.inStream.read(e);
                        if (-107 == e[0]) {
                            BlueToothStateMachine.this.status = 2;
                        }
                    }
                } catch (IOException var2) {
                    BlueToothStateMachine.this.status = 0;
                    var2.printStackTrace();
                }

            }
        });
    }

    private void initVerCode() {
        this.handlerMap.append(2, new IHandler() {
            public void handler() {
                try {
                    if (BlueToothStateMachine.this.inStream.available() >= 1) {
                        byte[] e = new byte[1];
                        BlueToothStateMachine.this.inStream.read(e);
                        BlueToothStateMachine.this.status = 3;
                    }
                } catch (IOException var2) {
                    BlueToothStateMachine.this.status = 0;
                    var2.printStackTrace();
                }

            }
        });
    }

    private void initLenCode() {
        this.handlerMap.append(3, new IHandler() {
            public void handler() {
                try {
                    if (BlueToothStateMachine.this.inStream.available() >= 1) {
                        byte[] e = new byte[1];
                        BlueToothStateMachine.this.inStream.read(e);
                        BlueToothStateMachine.this.dataLength = e[0] & 255;
                        BlueToothStateMachine.this.status = 4;
                    }
                } catch (IOException var2) {
                    BlueToothStateMachine.this.status = 0;
                    var2.printStackTrace();
                }

            }
        });
    }

    private void initFlagCode() {
        this.handlerMap.append(4, new IHandler() {
            public void handler() {
                try {
                    if (BlueToothStateMachine.this.inStream.available() >= 1) {
                        byte[] e = new byte[1];
                        BlueToothStateMachine.this.inStream.read(e);
                        BlueToothStateMachine.this.dataLength = BlueToothStateMachine.this.dataLength - 1;
                        BlueToothStateMachine.this.flag = e[0] & 255;
                        BlueToothStateMachine.this.status = 5;
                    }
                } catch (IOException var2) {
                    BlueToothStateMachine.this.status = 0;
                    var2.printStackTrace();
                }

            }
        });
    }

    private void initSFlagCode() {
        this.handlerMap.append(5, new IHandler() {
            public void handler() {
                try {
                    if (BlueToothStateMachine.this.inStream.available() >= 1) {
                        byte[] e = new byte[1];
                        BlueToothStateMachine.this.inStream.read(e);
                        BlueToothStateMachine.this.dataLength = BlueToothStateMachine.this.dataLength - 1;
                        BlueToothStateMachine.this.sflag = e[0] & 255;
                        BlueToothStateMachine.this.status = 6;
                        if (BlueToothStateMachine.this.flag == 2) {
                        }
                    }
                } catch (IOException var2) {
                    BlueToothStateMachine.this.status = 0;
                    var2.printStackTrace();
                }
            }
        });
    }

    private void initDataCode() {
        this.handlerMap.append(6, new IHandler() {
            public void handler() {
                try {
                    if (BlueToothStateMachine.this.dataLength > 0) {
                        if (BlueToothStateMachine.this.inStream.available() >= 1) {
                            BlueToothStateMachine.this.dataBuff = new byte[BlueToothStateMachine.this.inStream.available() - 1];
                            BlueToothStateMachine.this.inStream.read(BlueToothStateMachine.this.dataBuff);
                            if (BlueToothStateMachine.this.dataBuffBefore != null) {
                                BlueToothStateMachine.this.isDataSame = BlueToothStateMachine.this.dataBuffBefore.equals(BlueToothStateMachine.this.dataBuff);
                            }

                            BlueToothStateMachine.this.dataBuffBefore = BlueToothStateMachine.this.dataBuff;

                        }else {
                            BlueToothStateMachine.this.status = 0;
                        }
                    } else {
                        BlueToothStateMachine.this.status = 0;
                    }

                    BlueToothStateMachine.this.status = 7;
                } catch (IOException var2) {
                    BlueToothStateMachine.this.status = 0;
                    var2.printStackTrace();
                }

            }
        });
    }

    private void initCSCode() {
        this.handlerMap.append(7, new IHandler() {
            public void handler() {
                if (!BlueToothStateMachine.this.isDataSame) {
                    try {
                        if (BlueToothStateMachine.this.inStream.available() >= 1) {
                            byte[] e = new byte[1];
                            BlueToothStateMachine.this.inStream.read(e);
                            BlueToothStateMachine.this.mHandler.obtainMessage(2, BlueToothStateMachine.this.flag, BlueToothStateMachine.this.sflag, BlueToothStateMachine.this.dataBuff).sendToTarget();
                        }
                    } catch (IOException var5) {
                        var5.printStackTrace();
                    } finally {
                        BlueToothStateMachine.this.status = 0;
                    }

                }
            }
        });
    }

    public void run() {
        while (this.socketConfig.isSocketConnected(this.mmSocket)) {
            IHandler handler = this.handlerMap.get(this.status);
            if (handler != null) {
                handler.handler();
            }
        }

    }

    public boolean write(byte[] buff) {
        try {
            this.outStream.write(buff);
            return true;
        } catch (IOException var3) {
            Log.i("BlueToothStateMachine", "------写入错误的原因----->" + var3.getMessage());
            var3.printStackTrace();
            return false;
        }
    }

    public void start() {
        this.thread.start();
    }

    public void setReadTimeout(int millis) {
        this.readTimeout = millis;
    }

    public int getReadTimeout() {
        return this.readTimeout;
    }
}
