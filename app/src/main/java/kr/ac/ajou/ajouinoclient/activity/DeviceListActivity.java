package kr.ac.ajou.ajouinoclient.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.melnykov.fab.FloatingActionButton;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.ActionClickListener;

import java.util.ArrayList;
import java.util.Collection;

import kr.ac.ajou.ajouinoclient.R;
import kr.ac.ajou.ajouinoclient.adapter.DeviceGridAdapter;
import kr.ac.ajou.ajouinoclient.model.Device;
import kr.ac.ajou.ajouinoclient.model.DeviceInfo;
import kr.ac.ajou.ajouinoclient.util.ApiCaller;
import kr.ac.ajou.ajouinoclient.util.Callback;
import kr.ac.ajou.ajouinoclient.persistent.DeviceManager;

/**
 * Activity for showing list of devices
 */
public class DeviceListActivity extends ActionBarActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, ActionMode.Callback {

    public static final int RESULT_REMOVE = 1010;

    private GridView mGridView;
    private ProgressBar mProgressBar;
    private FloatingActionButton mFloatingActionButton;
    private DeviceGridAdapter mGridAdapter;
    private ActionMode mActionMode;

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
                Intent intent = new Intent(DeviceListActivity.this, DeviceLookupActivity.class);
                startActivityForResult(intent, DeviceLookupActivity.REQUEST_NEW_DEVICE);
            }
        });
        mGridView.setOnItemClickListener(this);
        mGridView.setOnItemLongClickListener(this);
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
    protected void onResume() {
        super.onResume();
        if (mGridAdapter != null) mGridAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (requestCode == DeviceLookupActivity.REQUEST_NEW_DEVICE) {
            if (resultCode == RESULT_OK) {
                Collection<Device> devices = DeviceManager.getInstance().getDevices();
                mGridAdapter.setListItems(devices);
                mGridAdapter.notifyDataSetChanged();
                Snackbar.with(DeviceListActivity.this)
                        .text(String.format("%s added", data.getStringExtra("deviceId")))
                        .actionColor(R.color.accent)
                        .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                        .show(DeviceListActivity.this);
            }
        }
        if (resultCode == DeviceListActivity.RESULT_REMOVE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mGridView.setClickable(false);
            mGridView.setLongClickable(false);

            String deviceId = data.getStringExtra("deviceId");
            if(deviceId != null) {
                Device device = DeviceManager.getInstance().getDevice(deviceId);
                removeDevice(device);
            }
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
        if (id == R.id.action_logout) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Device device = (Device) mGridAdapter.getItem(i);
        if (device != null) {
            Intent intent = new Intent(this, DeviceActivity.class);
            intent.putExtra(DeviceActivity.PARAM_DEVICE_ID, device.getId());
            startActivityForResult(intent, 0);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        mGridAdapter.toggleSelection(i);
        boolean hasCheckedItems = mGridAdapter.getSelectedCount() > 0;

        if (hasCheckedItems && mActionMode == null)
            // there are some selected items, start the actionMode
            mActionMode = startSupportActionMode(this);
        else if (!hasCheckedItems && mActionMode != null)
            // there no selected items, finish the actionMode
            mActionMode.finish();

        if (mActionMode != null)
            mActionMode.setTitle(String.valueOf(mGridAdapter
                    .getSelectedCount()) + " selected");
        return true;
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        actionMode.getMenuInflater().inflate(R.menu.context_menu_device_list, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_delete:
                // retrieve selected items and delete them out
                SparseBooleanArray selected = mGridAdapter.getSelectedIds();
                for (int i = (selected.size() - 1); i >= 0; i--) {
                    if (selected.valueAt(i)) {
                        DeviceInfo selectedItem = mGridAdapter.getItem(selected.keyAt(i));
                        removeDevice(selectedItem);
                    }
                }
                actionMode.finish(); // Action picked, so close the CAB
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        mGridAdapter.removeSelection();
        mActionMode = null;
    }

    private void removeDevice(final DeviceInfo deviceInfo) {
        mProgressBar.setVisibility(View.VISIBLE);
        mGridView.setClickable(false);
        mGridView.setLongClickable(false);
        ApiCaller.getStaticInstance().removeDeviceAsync(deviceInfo, new Callback() {
            @Override
            public void onSuccess(Object result) {
                mProgressBar.setVisibility(View.GONE);
                mGridView.setLongClickable(true);
                mGridView.setClickable(true);

                Device device = DeviceManager.getInstance().removeDevice(deviceInfo.getId());
                mGridAdapter.setListItems(DeviceManager.getInstance().getDevices());
                mGridAdapter.notifyDataSetChanged();
                onRemoveDevice(device);
            }

            @Override
            public void onFailure() {
                mProgressBar.setVisibility(View.GONE);
                mGridView.setLongClickable(true);
                mGridView.setClickable(true);
                Snackbar.with(DeviceListActivity.this)
                        .text("Failed to remove device")
                        .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
                        .show(DeviceListActivity.this);
            }
        });
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

        ApiCaller.getStaticInstance().getDevicesAsync(new Callback() {
            @Override
            public void onSuccess(Object result) {

                if (!loadingSnackbar.isDismissed()) {
                    loadingSnackbar.dismiss();
                }

                Collection<Device> devices = (Collection<Device>) result;
                if (devices == null) {
                    devices = new ArrayList<Device>();
                    Snackbar.with(DeviceListActivity.this)
                            .text("No devices are registered.")
                            .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
                            .show(DeviceListActivity.this);
                }

                // dummy powerstrip device for mockup
//                Device tempDevice = new Device("ps", Device.TYPE_POWERSTRIP, "192.1.1.1", "Arduino Powerstrip");
//                Map<String, Integer> values = new HashMap<>();
//                values.put("ports", 2);
//                List<Event> events = new ArrayList<>();
//                events.add(new Event(tempDevice.getId(), Event.TYPE_POWER, 0b11));
//                tempDevice.setValues(values);
//                tempDevice.setEvents(events);
//                devices.add(tempDevice);

                // dummy intercom device for mockup
//                tempDevice = new Device("intercom", Device.TYPE_INTERCOM, "192.1.1.2", "Android Intercom");
//                events = new ArrayList<>();
//                events.add(new Event(tempDevice.getId(), "guest", R.drawable.pic1));
//                events.add(new Event(tempDevice.getId(), "guest", R.drawable.pic2));
//                tempDevice.setEvents(events);
//                devices.add(tempDevice);

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
                Snackbar.with(DeviceListActivity.this)
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
                        .show(DeviceListActivity.this);
            }
        });
    }

    private void onRemoveDevice(final Device device) {
        Snackbar.with(this)
                .text("Device removed")
                .actionLabel("Undo")
                .actionColorResource(R.color.accent)
                .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
                .actionListener(new ActionClickListener() {
                    @Override
                    public void onActionClicked() {
                        mProgressBar.setVisibility(View.VISIBLE);
                        mGridView.setClickable(false);
                        ApiCaller.getStaticInstance().postDeviceAsync(device, new Callback() {
                            @Override
                            public void onSuccess(Object result) {
                                mProgressBar.setVisibility(View.GONE);
                                mGridView.setClickable(true);
                                Device resultDevice = (Device) result;
                                if (resultDevice != null) {
                                    resultDevice.setPassword(device.getPassword());
                                    DeviceManager.getInstance().putDevice(resultDevice);

                                    Intent returnIntent = new Intent();
                                    returnIntent.putExtra("deviceId", resultDevice.getId());
                                    setResult(RESULT_OK, returnIntent);
                                    onActivityResult(DeviceLookupActivity.REQUEST_NEW_DEVICE, RESULT_OK, returnIntent);

                                } else {
                                    Snackbar.with(DeviceListActivity.this)
                                            .text("Failed to add device")
                                            .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
                                            .show(DeviceListActivity.this);
                                }
                            }

                            @Override
                            public void onFailure() {
                                mProgressBar.setVisibility(View.GONE);
                                mGridView.setClickable(true);
                                Snackbar.with(DeviceListActivity.this)
                                        .text("Failed to add device")
                                        .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
                                        .show(DeviceListActivity.this);
                            }
                        });
                    }
                })
                .show(this);
    }
}
