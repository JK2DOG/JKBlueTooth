package com.ceshi.lanya.lanyademo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


/**
 * 蓝牙连接与测量
 *
 * @author Administrator
 */
public class BluetoothConnMeasureActivity extends Activity {
    private final int REQUEST_ENABLE_BT = 0x01;

    private View imgAnim;
    private BluetoothAdapter mBTAdapter = BluetoothAdapter.getDefaultAdapter();
    private TextView tv_connect_state, tv_turgoscope_power, tv_heart;
    private Button btn_stop_measure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth);
        initView();
        setBluetooth();
    }

    public void initView() {
        imgAnim = findViewById(R.id.imgAnim);
        tv_connect_state = (TextView) findViewById(R.id.tv_connect_state);
        tv_turgoscope_power = (TextView) findViewById(R.id.tv_turgoscope_power);
        tv_heart = (TextView) findViewById(R.id.tv_heart);
        btn_stop_measure = (Button) findViewById(R.id.btn_stop_measure);

    }

    /**
     * 设置蓝牙信息 ：如果蓝牙可用，则打开蓝牙； 如果蓝牙不可用，则进行提示
     */
    private void setBluetooth() {

        if (mBTAdapter == null) {
            Toast.makeText(this, "本机没有找到蓝牙硬件或驱动！", Toast.LENGTH_LONG).show();
            return;
        }

        if (!mBTAdapter.isEnabled()) {
            //提醒用户打开蓝牙
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "蓝牙已打开", Toast.LENGTH_LONG);
                //TODO:血压计的搜索连接以及后续操作
            } else {
                Toast.makeText(this, "蓝牙打开失败", Toast.LENGTH_LONG);
            }
        }
    }
}
