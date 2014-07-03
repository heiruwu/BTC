package com.example.hrw.btc;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;


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
        InputStream mInputStream;
        String selectDevice;
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
            mbtDevices = (ListView)getView().findViewById(R.id.btDevices);
            mbtDevices = (ListView)getView().findViewById(R.id.btDevices);
            if (mBluetoothAdapter == null) {
                Toast.makeText(getActivity(), "not fucking support",
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
                                openBT();
                                break;
                            }
                        }
                    }
                });
            }
            rcMessage.append("Waiting for selection\n");
        }
        void openBT(){
            rcMessage.append("Connecting......\n");
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard
            // SerialPortService
            // ID
            try {
                mBluetoothSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
                mBluetoothSocket.connect();
                rcMessage.append("Device connected\n");
                mInputStream = mBluetoothSocket.getInputStream();
                rcMessage.append("InputStream initialized\nwaiting for input\n");
            }catch(IOException e){
                rcMessage.append("Connect error:\nDevices not responding\n");
            }
//          beginListenForData();
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
