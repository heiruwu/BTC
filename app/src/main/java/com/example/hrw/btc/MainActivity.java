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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.util.Set;
import java.util.UUID;
import java.util.Scanner;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        TextView btStat,rcMessage;
        BluetoothAdapter mBluetoothAdapter;
        BluetoothDevice mDevice;
        ListView mbtDevices;
        BluetoothSocket mBluetoothSocket;
        ObjectOutputStream mObjectOutputStream;
        InputStream mInputStream;
        OutputStream mOutputStream;
        String selectDevice;
        Thread openConnection;
        Thread listenData;
        Thread requestData;
        String tmp;
        int count;
        int[] data;
        int GET_HR = 101;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            btStat = (TextView)getView().findViewById(R.id.btStat);
            rcMessage = (TextView)getView().findViewById(R.id.rcMessage);
            rcMessage.setMovementMethod(new ScrollingMovementMethod());
            mbtDevices = (ListView)getView().findViewById(R.id.btDevices);
            mbtDevices = (ListView)getView().findViewById(R.id.btDevices);
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
        }

        void findBT() {
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
                for (int i = 0;i<mDevices.length;i++){
                    rcMessage.append(mDevices[i]+"\n");
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
                        rcMessage.append(selectDevice+" selected\n");
                        for (BluetoothDevice device : pairedDevices){
                            if(device.getName().equals(selectDevice)){
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
        void openBT(){
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard
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
            }catch(IOException e){
                rcMessageappend("Connect error:\nDevice not responding\n");
                Log.w("exception",e.toString());
            }
            requestData = new Thread(reqData);
            listenData = new Thread(lisData);
            if(mInputStream != null && mOutputStream != null) {
                listenData.start();
                requestData.start();
            }
        }

        Runnable lisData = new Runnable() {
            @Override
            public void run() {
                while(mBluetoothSocket.isConnected()) {
                    try {
                        if (mInputStream.available() > 0) {
//                            rcMessageappend("Received message: " + getStringByScanner(mInputStream) + "\n"); not used for now
                            data = getIntArray(mInputStream);
                            rcMessageappend("Received Data: ");
                            for (int i = 0;i<count;i++){
                                rcMessageappend(String.valueOf(data[i])+",");
                            }
                            rcMessageappend("\n");
                            rcMessageappend("Total: "+String.valueOf(count)+"\n");
                            rcMessageappend("Avg = "+String.valueOf(avg(data))+"\n");
                        }
                    }catch (IOException e){
                        rcMessageappend(e.toString()+"\n");
                    }catch (ClassNotFoundException e){
                        rcMessageappend(e.toString()+"\n");
                    }
                }
                rcMessageappend("Connection closed\n");
            }
        };

        /**
         * Request Code GET_HR
         * for getting data from server
         */
        Runnable reqData = new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        count = 60;
                        mObjectOutputStream = new ObjectOutputStream(mOutputStream);
                        mObjectOutputStream.writeObject(new int[]{GET_HR,count});
                        rcMessageappend("Request Code = GET_HR, requesting for data......\n");
                        Thread.sleep(10000);
                    } catch (IOException e) {
                        rcMessageappend("Request:" + e.toString() + "\n");
                    } catch (InterruptedException e){
                        rcMessageappend(e.toString());
                    }
                }
            }
        };
        public String getStringByScanner (InputStream inputStream) throws IOException{
            return new Scanner(inputStream).useDelimiter("\\A").nextLine();
//            return s.hasNext() ? s.next() : "";
        }
        public int[] getIntArray(InputStream inputStream) throws IOException,ClassNotFoundException{
            return (int[]) new ObjectInputStream(inputStream).readObject();
        }

        void rcMessageappend(final String str){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rcMessage.append(str);
                }
            });
//            rcMessage.scrollTo(0,rcMessage.getBottom());
        }

        private int avg(int[] a){
            int temp = 0;
            for(int i = 0;i<a.length;i++){
                temp += a[i];
            }
            return temp/a.length;
        }



        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
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
