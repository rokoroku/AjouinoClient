package kr.ac.ajou.ajouinoclient.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.ActionClickListener;

import java.util.Collection;
import java.util.Iterator;

import kr.ac.ajou.ajouinoclient.R;
import kr.ac.ajou.ajouinoclient.adapter.DeviceGridAdapter;
import kr.ac.ajou.ajouinoclient.model.Device;
import kr.ac.ajou.ajouinoclient.model.DeviceInfo;
import kr.ac.ajou.ajouinoclient.util.ApiCaller;
import kr.ac.ajou.ajouinoclient.util.Callback;
import kr.ac.ajou.ajouinoclient.persistent.DeviceManager;

public class DeviceLookupActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

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
        getMenuInflater().inflate(R.menu.menu_device_list, menu);
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
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        final Device selectedDevice = new Device(mGridAdapter.getItem(i));
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Connect to " + selectedDevice.getId())
                .customView(R.layout.dialog_layout_edittext)
                .positiveText("Connect")
                .negativeText("Cancel")
                .callback(new MaterialDialog.SimpleCallback() {
                    @Override
                    public void onPositive(MaterialDialog materialDialog) {
                        if (selectedDevice != null) {
                            EditText editText = (EditText) materialDialog.getCustomView().findViewById(R.id.editText);
                            selectedDevice.setPassword(editText.getText().toString());
                            mProgressBar.setVisibility(View.VISIBLE);
                            mGridView.setClickable(false);
                            ApiCaller.getStaticInstance().postDeviceAsync(selectedDevice, new Callback() {
                                @Override
                                public void onSuccess(Object result) {
                                    Device device = (Device) result;
                                    if (device != null) {
                                        device.setPassword(selectedDevice.getPassword());
                                        DeviceManager.getInstance().putDevice(device);

                                        Intent returnIntent = new Intent();
                                        returnIntent.putExtra("deviceId", device.getId());
                                        setResult(RESULT_OK, returnIntent);
                                        finishActivity(REQUEST_NEW_DEVICE);
                                        finish();
                                    } else {
                                        mProgressBar.setVisibility(View.GONE);
                                        mGridView.setClickable(true);
                                        Snackbar.with(DeviceLookupActivity.this)
                                                .text("Failed to add device")
                                                .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
                                                .show(DeviceLookupActivity.this);
                                    }
                                }

                                @Override
                                public void onFailure() {
                                    mProgressBar.setVisibility(View.GONE);
                                    mGridView.setClickable(true);
                                    Snackbar.with(DeviceLookupActivity.this)
                                            .text("Failed to add device")
                                            .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
                                            .show(DeviceLookupActivity.this);
                                }
                            });
                        }
                    }

                })
                .build();

        EditText editText = (EditText) dialog.getCustomView().findViewById(R.id.editText);
        editText.setHint(R.string.string_password);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        dialog.show();
    }

    private void reloadDevices() {
        mGridView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);

        final Snackbar loadingSnackbar = Snackbar.with(this)
                .text("Loading...")
                .duration(10000)
                .animation(false);
        loadingSnackbar.show(this);
        loadingSnackbar.animation(true);

        ApiCaller.getStaticInstance().getAllDevicesOnNetworkAsync(new Callback() {
            @Override
            public void onSuccess(Object result) {

                if (!loadingSnackbar.isDismissed()) {
                    loadingSnackbar.dismiss();
                }

                Collection<DeviceInfo> devices = (Collection<DeviceInfo>) result;
                Iterator<DeviceInfo> iterator = devices.iterator();
                while (iterator.hasNext()) {
                    DeviceInfo deviceInfo = iterator.next();
                    if (DeviceManager.getInstance().getDevice(deviceInfo.getId()) != null) {
                        iterator.remove();
                    }
                }
                mGridAdapter.setListItems(devices);
                mGridView.setAdapter(mGridAdapter);
                mGridView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);

                if (mGridAdapter.getCount() == 0) {
                    Snackbar.with(DeviceLookupActivity.this)
                            .text("No devices are found")
                            .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
                            .show(DeviceLookupActivity.this);
                }
            }

            @Override
            public void onFailure() {
                mProgressBar.setVisibility(View.GONE);
                Snackbar.with(DeviceLookupActivity.this)
                        .text("Failed to search devices")
                        .actionLabel("Retry")
                        .actionListener(new ActionClickListener() {
                            @Override
                            public void onActionClicked() {
                                reloadDevices();
                            }
                        })
                        .actionColorResource(R.color.accent)
                        .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
                        .show(DeviceLookupActivity.this);
            }
        });
    }

}
