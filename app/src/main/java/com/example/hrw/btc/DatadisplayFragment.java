package com.example.hrw.btc;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.cengalabs.flatui.FlatUI;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Scanner;

/*
Copyright 2014 CengaLabs.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

/**
 * Created by hrw on 2014/7/21.
 */
public class DatadisplayFragment extends Fragment {
    private BluetoothSocket mBluetoothSocket;
    private InputStream mInputStream;
    private Thread listenData;
    private TextView stepR,stepL,tvxd,tvxd_av,tvxd_sd,tvzd,tvzd_av,tvzd_sd,lr_ratio,lr_ratio_av,lr_ratio_sd;
    private Byte seqID;
    private Byte payloadSize;
    private float xd;
    private float xd_av;
    private float xd_sd;
    private float zd;
    private float zd_av;
    private float zd_sd;
    private int count;
    private byte[] packet;
    private int GET_HR = 101;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_datadisplay,container,false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        stepR = (TextView)getView().findViewById(R.id.stepR);
        stepL = (TextView)getView().findViewById(R.id.stepL);
        tvxd = (TextView)getView().findViewById(R.id.xd);
        tvxd_av = (TextView)getView().findViewById(R.id.xd_av);
        tvxd_sd = (TextView)getView().findViewById(R.id.xd_sd);
        tvzd = (TextView)getView().findViewById(R.id.zd);
        tvzd_av = (TextView)getView().findViewById(R.id.zd_av);
        tvzd_sd = (TextView)getView().findViewById(R.id.zd_sd);
        lr_ratio = (TextView)getView().findViewById(R.id.lr_ratio);
        lr_ratio_av = (TextView)getView().findViewById(R.id.lr_ratio_av);
        lr_ratio_sd = (TextView)getView().findViewById(R.id.lr_ratio_sd);
        mBluetoothSocket = ((MainActivity)getActivity()).getBluetoothSocket();
        FlatUI.initDefaultValues(getActivity());
        FlatUI.setDefaultTheme(FlatUI.DEEP);
        mInputStream = ((MainActivity)getActivity()).getInputStream();
        listenData = new Thread(lisData);
        if (mInputStream != null) {
            listenData.start();
        }
    }

    /**
     * Listening for incoming data
     * and calculate it's average.
     */
    private Runnable lisData = new Runnable() {
        @Override
        public void run() {
            while (mBluetoothSocket.isConnected()) {
                try {
                    if (mInputStream.available() == 23) {
                        packet = new byte[23];
                        int useless;
                        useless = mInputStream.read(packet);
                        Log.w("Data", "available");
                        Log.w("Header",String.valueOf(packet[0]));
                        if(packet[0] == -91 && packet[1] == -91){//if it is the correct packet
                            seqID = packet[2];
                            payloadSize = packet[3];
                            tvxd.setText(String.valueOf(get71Var(getBitstoString(packet[4]),getBits(packet[4]))));
                            tvxd_av.setText(String.valueOf(get71Var(getBitstoString(packet[5]),getBits(packet[5]))));
                            tvxd_sd.setText(String.valueOf(get71Var(getBitstoString(packet[6]),getBits(packet[6]))));
                            tvzd.setText(String.valueOf(get71Var(getBitstoString(packet[7]),getBits(packet[7]))));
                            tvzd_av.setText(String.valueOf(get71Var(getBitstoString(packet[8]),getBits(packet[8]))));
                            tvzd_sd.setText(String.valueOf(get71Var(getBitstoString(packet[9]),getBits(packet[9]))));
                            stepR.setText(String.valueOf(getIntValue(packet[10],packet[11])));
                            stepL.setText(String.valueOf(getIntValue(packet[12],packet[13])));
                            lr_ratio.setText(String.valueOf(getFloatValue(packet[14],packet[15])));
                            lr_ratio_av.setText(String.valueOf(getFloatValue(packet[16],packet[17])));
                            lr_ratio_sd.setText(String.valueOf(getFloatValue(packet[18],packet[19])));
                        }
                    }
                } catch (IOException e) {
//                    rcMessageappend(e.toString() + "\n");
                }
            }
//            rcMessageappend("Connection closed\n");
        }
    };

    private String getStringByScanner(InputStream inputStream) throws IOException {
        return new Scanner(inputStream).useDelimiter("\\A").nextLine();
//            return s.hasNext() ? s.next() : "";
    }

    /**
     * Transfer inputstream to int array.
     */
    private int[] getIntArray(InputStream inputStream) throws IOException, ClassNotFoundException {
        return (int[]) new ObjectInputStream(inputStream).readObject();
    }

    /**
     * Transfer a byte to byte array containing all 8 bits.
     * @param b
     * @return Byte[]
     */
    private Byte[] getBits(byte b){
        Byte[] bits = new Byte[8];
        for (int i = 7; i >= 0; i--) {
            bits[i] = (byte)(b & 1);
//              packet[5] = (byte) (packet[5] >> 1);
            b >>= 1;
        }
        return  bits;
    }

    /**
     * Transfer a byte to String containing all 8 bits.
     * @param b
     * @return String
     */
    private String getBitstoString(byte b){
        return ""
                + (byte) ((b >> 7) & 0x1) + (byte) ((b >> 6) & 0x1)
                + (byte) ((b >> 5) & 0x1) + (byte) ((b >> 4) & 0x1)
                + (byte) ((b >> 3) & 0x1) + (byte) ((b >> 2) & 0x1)
                + (byte) ((b >> 1) & 0x1) + (byte) ((b >> 0) & 0x1);
    }

    /**
     * Get values of 7.1 formatted packet.
     * @param string a String containing 8 bit
     * @param bytes a Byte array containing 8 bit
     * @return int
     */
    private float get71Var(String string, Byte[] bytes){
        float temp;
        if(bytes[0] == 0){
            Log.w("Binary String: ", string );
            temp = Integer.parseInt(string.substring(1,7),2);
            Log.w("parse",String.valueOf(temp));
            if (bytes[7] == 0){
                return temp;
            }else{
                return  temp + (float) 0.5;
            }
        }else{
            temp = Integer.parseInt(string.substring(1,7),2) - 128;
            if (bytes[7] == 0){
                return temp;
            }else{
                return  temp - (float) 0.5;
            }
        }
    }

    private int getIntValue(Byte L,Byte H){
        String temp = getBitstoString(H);
        temp += getBitstoString(L);
        return Integer.parseInt(temp,2);
    }

    private float getFloatValue(Byte L,Byte H){
        float tempint;
        String temp = getBitstoString(H);
        temp += getBitstoString(L);
        tempint = Integer.parseInt(temp.substring(0,14) , 2);
        if(Integer.valueOf(temp.substring(14,15)) == 0){
            if(Integer.valueOf(temp.substring(15,16)) == 0){
                return tempint;
            }else{
                return tempint + (float)0.24;
            }
        }else{
            if(Integer.valueOf(temp.substring(15,16)) == 0){
                return tempint + (float) 0.49;
            }else{
                return tempint + (float) 0.99;
            }
        }
    }

    private Byte[] getByteArray(InputStream inputStream) throws IOException, ClassNotFoundException {
        return (Byte []) new ObjectInputStream(inputStream).readObject();
    }
    private Byte[] getPacketToData(InputStream inputStream) throws IOException, ClassNotFoundException {
        Byte[] b;
        b = getByteArray(inputStream);
        return b;
    }

}
