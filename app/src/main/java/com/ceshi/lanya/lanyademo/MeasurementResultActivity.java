package com.ceshi.lanya.lanyademo;

import java.util.ArrayList;
import com.iknet.iknetbluetoothlibrary.BluetoothManager;
import com.iknet.iknetbluetoothlibrary.BluetoothService;
import com.iknet.iknetbluetoothlibrary.MeasurementResult;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/**
 * 显示测量结果
 * @author Administrator
 *
 */
public class MeasurementResultActivity extends Activity{
	private static final String TAG = "MeasurementResultActivity";
	
	private TextView tv_ssy, tv_szy, tv_xl, tv_other;
	
	private MeasurementResult measurementResult;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_measure_result);
		
		measurementResult = (MeasurementResult) getIntent().getSerializableExtra("measure_result");
		initView();
		initReceiver();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	private void initReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothService.ACTION_BLUETOOTH_MEMORY_MEASURE_DATA);
		registerReceiver(receiver, intentFilter);
	}

	private void initView() {
		tv_ssy = (TextView) findViewById(R.id.tv_ssy);
		tv_szy = (TextView) findViewById(R.id.tv_szy);
		tv_xl = (TextView) findViewById(R.id.tv_xl);
		tv_other = (TextView) findViewById(R.id.tv_other);
		
		tv_ssy.setText("收缩压：" + measurementResult.getCheckShrink());
		tv_szy.setText("舒张压：" + measurementResult.getCheckDiastole());
		tv_xl.setText("心率：" + measurementResult.getCheckHeartRate());
		
	}
	
	BroadcastReceiver receiver = new BroadcastReceiver(){

		@SuppressLint("SimpleDateFormat")
		@SuppressWarnings({ "unchecked" })
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(BluetoothService.ACTION_BLUETOOTH_MEMORY_MEASURE_DATA.equals(action)){
				ArrayList<MeasurementResult> measurementResults = (ArrayList<MeasurementResult>) intent.getSerializableExtra("memoryMeasureData");
				if(measurementResults != null){
					StringBuffer sb = new StringBuffer();
					sb.append("记忆数据:" + "\n");
					for(int i=0; i<measurementResults.size(); i++){
						sb.append("创建时间："+measurementResults.get(i).getCreateTimeStr()+"\n")
							.append("收缩压："+measurementResults.get(i).getCheckShrink()+"\n")
							.append("舒张压："+measurementResults.get(i).getCheckDiastole()+"\n")
							.append("心率："+measurementResults.get(i).getCheckHeartRate()+"\n")
							;
					}
					tv_other.setText(sb);
					BluetoothManager.getInstance(MeasurementResultActivity.this).sendData("cc95020302030000");
					Log.v(TAG, "获取到记忆数据应答：cc95020302030000");
				}else{
					tv_other.setText("记忆数据:空");
				}
			}
		}
		
	};
	
	
}
