package com.example.hrw.btc;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dacer.androidcharts.BarView;
import com.dacer.androidcharts.LineView;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

/*
 * The MIT License (MIT)

 Copyright (c) 2013 Ding Wenhao

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in
 the Software without restriction, including without limitation the rights to
 use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 the Software, and to permit persons to whom the Software is furnished to do so,
 subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private ArrayList<Integer> Fdata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fdata = new ArrayList<Integer>();
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public ArrayList<Integer> getData() {
        return this.Fdata;
    }

    public void setData(int[] tmp) {
        for (int i = 0; i < tmp.length; i++) {
            Fdata.add(tmp[i]);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static int page = 1;
        private TextView btStat, rcMessage;
        private BluetoothAdapter mBluetoothAdapter;
        private BluetoothDevice mDevice;
        private ListView mbtDevices;
        private BluetoothSocket mBluetoothSocket;
        private ObjectOutputStream mObjectOutputStream;
        private InputStream mInputStream;
        private OutputStream mOutputStream;
        private String selectDevice;
        private Thread openConnection;
        private Thread listenData;
        private Thread requestData;
        private Byte seqID;
        private Byte payloadSize;
        private float xd;
        private float xd_av;
        private float xd_sd;
        private float zd;
        private float zd_av;
        private float zd_sd;
        private int count;
        private int[] data;
        private byte[] packet;
        private int GET_HR = 101;
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
                            Log.w("Data","available");
                            Log.w("Header",String.valueOf(packet[0]));
                            if(packet[0] == -91 && packet[1] == -91){//if it is the correct packet
                                rcMessageappend("Packet received:\n");
                                seqID = packet[2];
                                payloadSize = packet[3];
                                xd = get71Var(getBitstoString(packet[4]),getBits(packet[4]));
                                xd_av = get71Var(getBitstoString(packet[5]),getBits(packet[5]));
                                xd_sd = get71Var(getBitstoString(packet[6]),getBits(packet[6]));
                                rcMessageappend("SeqID:" + String.valueOf(seqID) + " Payload size:" + String.valueOf(payloadSize)
                                + " xd:" + String.valueOf(xd) + " xd_av:" + String.valueOf(xd_av) + " xd_sd:" + String.valueOf(xd_sd));
                                zd = get71Var(getBitstoString(packet[7]),getBits(packet[7]));
                                zd_av = get71Var(getBitstoString(packet[8]),getBits(packet[8]));
                                zd_sd = get71Var(getBitstoString(packet[9]),getBits(packet[9]));
                                rcMessageappend(" zd:" + String.valueOf(zd) + " zd_av:" + String.valueOf(zd_av) + " zd_sd:" + String.valueOf(zd_sd));
                                rcMessageappend(" stepR:"+ String.valueOf(getIntValue(packet[10],packet[11])));
                                rcMessageappend(" stepL:"+ String.valueOf(getIntValue(packet[12],packet[13])));
                                rcMessageappend(" lr_ratio:" + String.valueOf(getFloatValue(packet[14],packet[15])));
                                rcMessageappend(" lr_ratio_avg:" + String.valueOf(getFloatValue(packet[16],packet[17])));
                                rcMessageappend(" lr_ratio_sd:" + String.valueOf(getFloatValue(packet[18],packet[19])));
                            }
                        }
                    } catch (IOException e) {
                        rcMessageappend(e.toString() + "\n");
                    }
                }
                rcMessageappend("Connection closed\n");
            }
        };
        /**
         * Request Code GET_HR
         * for getting data from server.
         */
        private Runnable reqData = new Runnable() {
            @Override
            public void run() {
                count = 100;
                while (true) {
                    try {
                        mObjectOutputStream = new ObjectOutputStream(mOutputStream);
                        mObjectOutputStream.writeObject(new int[]{GET_HR, count});
                        rcMessageappend("Request Code = GET_HR, requesting for data......\n");
                        count++;
                        Thread.sleep(10000);
                    } catch (IOException e) {
                        rcMessageappend("Request:" + e.toString() + "\n");
                    } catch (InterruptedException e) {
                        rcMessageappend(e.toString());
                    }
                    break;
                }
            }
        };

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            page = sectionNumber;
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            switch (page) {
                case 1:
                    btStat = (TextView) getView().findViewById(R.id.btStat);
                    rcMessage = (TextView) getView().findViewById(R.id.rcMessage);
                    rcMessage.setMovementMethod(new ScrollingMovementMethod());
                    mbtDevices = (ListView) getView().findViewById(R.id.btDevices);
                    mbtDevices = (ListView) getView().findViewById(R.id.btDevices);
                    data = new int[]{};
                    if (mBluetoothAdapter == null) {
                        Toast.makeText(getActivity(), "not support",
                                Toast.LENGTH_SHORT).show();
                    }
                    if (!mBluetoothAdapter.enable()) {
                        if (!mBluetoothAdapter.isEnabled()) {
                            Intent turnOnIntent = new Intent(
                                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(turnOnIntent, 0);
                        }

                    }
                    btStat.setText("Paired Devices");
                    rcMessage.append("Searching paired device......\n");
                    findBT();
                    break;
                case 2:
                    TextView avgText = (TextView)getActivity().findViewById(R.id.avgText);
                    ArrayList<String> bottom = new ArrayList<String>();
                    bottom.add("1~20");
                    bottom.add("21~40");
                    bottom.add("41~60");
                    bottom.add("61~80");
                    bottom.add("81~100");
                    BarView barView = (BarView) getActivity().findViewById(R.id.bar_view);
                    barView.setBottomTextList(bottom);
                    ArrayList<Integer> avgData = new ArrayList<Integer>();
                    int loop = 0;
                    for(int i = 0;i<5;i++){
                        int temp1 = 0;
                        for(int j = 0;j<20;j++){
                            temp1 += ((MainActivity)getActivity()).getData().get(loop);
                            loop++;
                        }
                        avgData.add(temp1/20);
                        avgText.append("   "+String.valueOf(temp1/20)+"        ");
                    }
                    barView.setDataList(avgData,100);
                    break;
            }
        }

        /**
         * List paired device
         * Set OnItemClickListener to connect
         */
        private void findBT() {
            int mDeviceCount = 0;
            final Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                    .getBondedDevices();
            final String[] mDevices = new String[pairedDevices.size()];
            if (pairedDevices.size() > 0) {
                rcMessage.append("Devices found:\n");
                for (BluetoothDevice device : pairedDevices) {
                    mDevices[mDeviceCount] = device.getName();
                    mDeviceCount++;
                }
                for (int i = 0; i < mDevices.length; i++) {
                    rcMessage.append(mDevices[i] + "\n");
                }
                mbtDevices.setAdapter(new ArrayAdapter<String>(
                        getActivity(),
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1,
                        mDevices
                ));
                mbtDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        selectDevice = ((TextView) view).getText().toString();
                        rcMessage.append(selectDevice + " selected\n");
                        for (BluetoothDevice device : pairedDevices) {
                            if (device.getName().equals(selectDevice)) {
                                mDevice = device;
                                openConnection = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        openBT();
                                    }
                                });
                                openConnection.start();
                                break;
                            }
                        }
                    }
                });
            }
            rcMessage.append("Waiting for selection\n");
        }

        /**
         * Open Bluetooth connection
         * Initialized input and outputstream
         * Start threads
         */
        private void openBT() {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            // Standard
            // SerialPortService
            // ID
            rcMessageappend("UUID set standard serial port service\n");
            try {
                mBluetoothSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
                rcMessageappend("Bluetooth socket initialized\nConnecting......\n");
                mBluetoothSocket.connect();
                rcMessageappend("Device connected\n");
                mInputStream = mBluetoothSocket.getInputStream();
                mOutputStream = mBluetoothSocket.getOutputStream();
                rcMessageappend("InputStream initialized\nwaiting for input\n");
//                rcMessageappend("OutputStream initialized\nwaiting for Output\n");
            } catch (IOException e) {
                rcMessageappend("Connect error:\nDevice not responding\n");
                Log.w("exception", e.toString());
            }
//            requestData = new Thread(reqData);
            listenData = new Thread(lisData);
            if (mInputStream != null && mOutputStream != null) {
                listenData.start();
//                requestData.start();
            }
        }

        private String getStringByScanner(InputStream inputStream) throws IOException {
            return new Scanner(inputStream).useDelimiter("\\A").nextLine();
//            return s.hasNext() ? s.next() : "";
        }
        /**
         *Get byte array data from input stream.
         */
        private void getData() throws IOException, ClassNotFoundException {
            data = getIntArray(mInputStream);
            rcMessageappend("Received Data: ");
            for (int i = 0; i < data.length; i++) {
                rcMessageappend(String.valueOf(data[i]) + ",");
                ((MainActivity) getActivity()).setData(data);
            }
            ((MainActivity) getActivity()).setData(data);
            rcMessageappend("\n");
            rcMessageappend("Total: " + String.valueOf(data.length) + "\n");
            rcMessageappend("Avg = " + String.valueOf(avg(data)) + "\n");
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

        private void rcMessageappend(final String str) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rcMessage.append(str);
                }
            });
//            rcMessage.scrollTo(0,rcMessage.getBottom());
        }

        private float avg(int[] a) {
            float temp = 0;
            for (int i = 0; i < a.length; i++) {
                temp += a[i];
            }
            return temp / a.length;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView;
            switch (page) {
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_main, container, false);
                    break;
                case 2:
                    rootView = inflater.inflate(R.layout.fragment_bt_connect, container, false);
                    break;
                default:
                    rootView = null;
                    break;
            }
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
