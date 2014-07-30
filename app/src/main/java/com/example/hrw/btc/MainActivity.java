package com.example.hrw.btc;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cengalabs.flatui.FlatUI;
import com.dacer.androidcharts.BarView;
import com.dacer.androidcharts.LineView;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    private InputStream inputStream;
    private BluetoothSocket bluetoothSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getActionBar().setBackgroundDrawable(FlatUI.getActionBarDrawable(this,FlatUI.DARK, false));
        getSupportActionBar().setBackgroundDrawable(FlatUI.getActionBarDrawable(this,FlatUI.DARK, false));
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
        Fragment fragment = null;
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch(position) {
            case 0:
                fragmentManager.beginTransaction()
                    .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                    .commit();
                break;
            case 1:
                if(bluetoothSocket.isConnected()) {
                    fragment = new DatadisplayFragment();
                    fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
                }else{
                    Toast.makeText(this,"Please connect to the device first",Toast.LENGTH_LONG).show();
                }
        }
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

    public BluetoothSocket getBluetoothSocket(){
        return this.bluetoothSocket;
    }
    public void storeBluetoothSocket(BluetoothSocket bluetoothSocket){
        this.bluetoothSocket = bluetoothSocket;
    }
    public void storeInputStream(InputStream inputStream){
        this.inputStream = inputStream;
    }
    public InputStream getInputStream(){
        return this.inputStream;
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
        private int[] data;
        private int count;
        private int GET_HR = 101;

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
                                        try {
                                            openBT();
                                        } catch (NoSuchMethodException e) {
                                            e.printStackTrace();
                                        } catch (InvocationTargetException e) {
                                            e.printStackTrace();
                                        } catch (IllegalAccessException e) {
                                            e.printStackTrace();
                                        }
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
        private void openBT() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
//            UUID uid = mDevice.getUuids()[0].getUuid();
            Method m = mDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
            // Standard
            // SerialPortService
            // ID
            rcMessageappend("UUID set standard serial port service(deprecated\nreplace by getUUIDs from mDevice )\n");
            try {
                mBluetoothSocket = (BluetoothSocket) m.invoke(mDevice, 1);
                rcMessageappend("Bluetooth socket initialized\nConnecting......\n");
                mBluetoothSocket.connect();
                rcMessageappend("Device connected\n");
                mInputStream = mBluetoothSocket.getInputStream();
                mOutputStream = mBluetoothSocket.getOutputStream();
                rcMessageappend("InputStream initialized\n");
//                rcMessageappend("OutputStream initialized\nwaiting for Output\n");
            } catch (IOException e) {
                rcMessageappend("Connect error:\nDevice not responding\n");
                Log.w("exception", e.toString());
                return;
            }
            try {
                ((MainActivity) getActivity()).storeInputStream(mInputStream);
                rcMessageappend("mInputStream stored success,");
                ((MainActivity) getActivity()).storeBluetoothSocket(mBluetoothSocket);
                rcMessageappend("mBluetoothSocket stored success, ready to go\n");
            }catch(IOError e){
                rcMessageappend("Over class method corrupted");
            }
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

        private int[] getIntArray(InputStream inputStream) throws IOException, ClassNotFoundException {
            return (int[]) new ObjectInputStream(inputStream).readObject();
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
