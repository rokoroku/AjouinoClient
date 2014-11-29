package kr.ac.ajou.ajouinoclient;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.ActionClickListener;

import java.util.Collection;

import kr.ac.ajou.ajouinoclient.adapter.DeviceGridAdapter;
import kr.ac.ajou.ajouinoclient.model.Device;
import kr.ac.ajou.ajouinoclient.model.DeviceInfo;
import kr.ac.ajou.ajouinoclient.util.ApiCaller;
import kr.ac.ajou.ajouinoclient.util.Callback;
import kr.ac.ajou.ajouinoclient.util.DeviceManager;

public class DeviceLookupActivity extends ActionBarActivity implements AdapterView.OnItemClickListener, MaterialDialog.SimpleCallback {

    public static final int REQUEST_NEW_DEVICE = 1000;
    private GridView mGridView;
    private ProgressBar mProgressBar;
    private DeviceGridAdapter mGridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_lookup);
        mGridView = (GridView) findViewById(R.id.gridview);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mGridView.setOnItemClickListener(this);
    }


    private void reloadDevices() {
        mGridView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        ApiCaller.getStaticInstance().getAllDevicesOnNetworkAsync(new Callback() {
            @Override
            public void onSuccess(Object result) {
                Collection<DeviceInfo> devices = (Collection<DeviceInfo>) result;
                mGridAdapter.setListItems(devices);
                mGridView.setAdapter(mGridAdapter);
                mGridView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure() {
                mProgressBar.setVisibility(View.GONE);
                Toast.makeText(DeviceLookupActivity.this, "Failed to search devices", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ApiCaller.getStaticInstance() != null) {
            if (mGridAdapter == null) {
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

    private Device selectedDevice;

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        selectedDevice = new Device(mGridAdapter.getItem(i));
        new MaterialDialog.Builder(this)
                .title("Connect to " + selectedDevice.getId())
                .customView(R.layout.dialog_enter_password)
                .positiveText("Connect")
                .negativeText("Cancel")
                .callback(this)
                .show();
    }

    @Override
    public void onPositive(MaterialDialog materialDialog) {
        if (selectedDevice != null) {
            EditText editText = (EditText) materialDialog.getCustomView().findViewById(R.id.editText);
            selectedDevice.setPassword(editText.getText().toString());
            ApiCaller.getStaticInstance().postDeviceAsync(selectedDevice, new Callback() {
                @Override
                public void onSuccess(Object result) {
                    Device device = (Device) result;
                    if (device != null) {
                        device.setPassword(selectedDevice.getPassword());
                        DeviceManager.getInstance().putDevice(device);

                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("deviceId", device.getId());
                        setResult(RESULT_OK,returnIntent);
                        finishActivity(REQUEST_NEW_DEVICE);
                    }
                }

                @Override
                public void onFailure() {
                    Snackbar.with(DeviceLookupActivity.this)
                            .text("Failed to add device")
                            .actionLabel("Close")
                            .actionColor(R.color.accent)
                            .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
                            .show(DeviceLookupActivity.this);
                }
            });
        }
    }

}
