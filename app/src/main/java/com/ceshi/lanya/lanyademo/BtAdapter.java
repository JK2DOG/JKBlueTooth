package com.ceshi.lanya.lanyademo;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by JK2DOG on 2016/5/24.
 */
public class BtAdapter extends BaseAdapter {
    private Context mContext;
    private List<BluetoothDevice> mList;
    private LayoutInflater mLayoutInflater;

    public BtAdapter(Context mContext, List<BluetoothDevice> mList) {
        this.mContext = mContext;
        this.mList = mList;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public BluetoothDevice getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHodler mHodler;
        if (convertView == null) {
            mHodler = new ViewHodler();
            convertView = mLayoutInflater.inflate(R.layout.bt_list, null);
            mHodler.name = (TextView) convertView.findViewById(R.id.name);
            mHodler.adress = (TextView) convertView.findViewById(R.id.adress);
            convertView.setTag(mHodler);
        } else {
            mHodler = (ViewHodler) convertView.getTag();
        }
        mHodler.name.setText(mList.get(position).getName());
        mHodler.adress.setText(mList.get(position).getAddress());
        return convertView;
    }


    private static final class ViewHodler {
        TextView name;
        TextView adress;
    }
}
