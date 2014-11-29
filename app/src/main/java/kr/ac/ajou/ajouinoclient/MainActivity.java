package kr.ac.ajou.ajouinoclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.ActionClickListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import kr.ac.ajou.ajouinoclient.adapter.DeviceGridAdapter;
import kr.ac.ajou.ajouinoclient.model.Device;
import kr.ac.ajou.ajouinoclient.model.DeviceInfo;
import kr.ac.ajou.ajouinoclient.util.ApiCaller;
import kr.ac.ajou.ajouinoclient.util.Callback;
import kr.ac.ajou.ajouinoclient.util.DeviceManager;


public class MainActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

    private GridView mGridView;
    private ProgressBar mProgressBar;
    private FloatingActionButton mFloatingActionButton;
    private DeviceGridAdapter mGridAdapter;

    private void reloadDevices() {
        mGridView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        ApiCaller.getStaticInstance().getDevicesAsync(new Callback() {
            @Override
            public void onSuccess(Object result) {
                Collection<Device> devices = (Collection<Device>) result;

                // dummy powerstrip device for mockup
                Device tempDevice = new Device("ps", Device.TYPE_POWERSTRIP, "192.1.1.1", "Arduino Powerstrip");
                Map<String, Integer> values = new HashMap<String, Integer>();
                values.put("port1", 1);
                values.put("port2", 2);
                tempDevice.setValues(values);
                devices.add(tempDevice);

                DeviceManager.getInstance().putDevices(devices);
                mGridAdapter.setListItems(devices);
                mGridAdapter.notifyDataSetChanged();
                mGridView.setAdapter(mGridAdapter);
                mGridView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure() {
                mProgressBar.setVisibility(View.GONE);
                Collection<Device> devices = DeviceManager.getInstance().getDevices();
                if (devices != null) {
                    mGridAdapter.setListItems(devices);
                    mGridView.setAdapter(mGridAdapter);
                    mGridView.setVisibility(View.VISIBLE);
                }
                Snackbar.with(MainActivity.this)
                        .text("Failed to load devices")
                        .actionLabel("Retry")
                        .actionListener(new ActionClickListener() {
                            @Override
                            public void onActionClicked() {
                                reloadDevices();
                            }
                        })
                        .actionColor(R.color.accent)
                        .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
                        .show(MainActivity.this);
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGridView = (GridView) findViewById(R.id.gridview);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DeviceLookupActivity.class);
                startActivityForResult(intent, DeviceLookupActivity.REQUEST_NEW_DEVICE);
            }
        });
        mGridView.setOnItemClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(ApiCaller.getStaticInstance() != null) {
            if(mGridAdapter == null) {
                mGridAdapter = new DeviceGridAdapter(this);
                reloadDevices();
            }
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            reloadDevices();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if(requestCode == DeviceLookupActivity.REQUEST_NEW_DEVICE) {
            if(resultCode == RESULT_OK) {
                Collection<Device> devices = DeviceManager.getInstance().getDevices();
                mGridAdapter.setListItems(devices);
                mGridAdapter.notifyDataSetChanged();
                Snackbar.with(MainActivity.this)
                        .text(String.format("%s added", data.getStringExtra("deviceId")))
                        .actionLabel("Close")
                        .actionColor(R.color.accent)
                        .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                        .show(MainActivity.this);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Device device = (Device) mGridAdapter.getItem(i);
        if(device != null) {
            Intent intent = new Intent(this, DeviceActivity.class);
            intent.putExtra("deviceId", device.getId());
            startActivity(intent);
        }
    }
}
