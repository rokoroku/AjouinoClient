package kr.ac.ajou.ajouinoclient.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.ActionClickListener;

import kr.ac.ajou.ajouinoclient.R;
import kr.ac.ajou.ajouinoclient.fragment.DeviceFragment;
import kr.ac.ajou.ajouinoclient.fragment.NavigationDrawerFragment;
import kr.ac.ajou.ajouinoclient.model.Device;
import kr.ac.ajou.ajouinoclient.model.Event;
import kr.ac.ajou.ajouinoclient.persistent.DeviceManager;
import kr.ac.ajou.ajouinoclient.util.ApiCaller;
import kr.ac.ajou.ajouinoclient.util.Callback;

/**
 * Activity for showing device's detailed fragment
 */
public class DeviceActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        DeviceFragment.OnDeviceFragmentInteractionListener {

    public static final String PARAM_DEVICE_ID = "deviceId";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private Device mDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        Intent intent = getIntent();
        if (intent != null) {
            String param = intent.getStringExtra(PARAM_DEVICE_ID);
            if (param != null) {
                replaceDeviceFragment(param);
            }
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(String param) {
        // update the main content by replacing fragments
        if (param != null) replaceDeviceFragment(param);
    }

    public void replaceDeviceFragment(String deviceId) {
        DeviceFragment deviceFragment = DeviceFragment.newInstance(deviceId);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, deviceFragment)
                .commit();
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
            getMenuInflater().inflate(R.menu.menu_device, menu);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("deviceId", mDevice.getId());
            setResult(DeviceListActivity.RESULT_REMOVE, returnIntent);
            finish();
            return true;

        } else if (id == R.id.action_refresh) {
            if (mDevice != null) {
                ApiCaller.getStaticInstance().getDeviceAsync(mDevice.getId(), new Callback() {
                    @Override
                    public void onSuccess(Object result) {
                        Device device = (Device) result;
                        if (device != null) {
                            DeviceManager.getInstance().putDevice(device);
                            replaceDeviceFragment(device.getId());

                            Snackbar.with(DeviceActivity.this)
                                    .text("Contents are updated.")
                                    .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                                    .show(DeviceActivity.this);
                        } else {
                            Snackbar.with(DeviceActivity.this)
                                    .text("Failed to update content.")
                                    .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                                    .show(DeviceActivity.this);
                        }
                    }

                    @Override
                    public void onFailure() {
                        Snackbar.with(DeviceActivity.this)
                                .text("Failed to refresh.")
                                .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                                .show(DeviceActivity.this);
                    }
                });
            }
            return true;

        } else if (id == R.id.action_edit_label) {
            if (mDevice != null) {
                changeLabel(mDevice);
            }
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private void changeLabel(final Device device) {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Change Label")
                .customView(R.layout.dialog_layout_edittext)
                .positiveText("Submit")
                .negativeText("Cancel")
                .callback(new MaterialDialog.SimpleCallback() {
                    @Override
                    public void onPositive(final MaterialDialog materialDialog) {
                        String label = ((EditText) materialDialog.getCustomView().findViewById(R.id.editText)).getText().toString();
                        if (label != null && !label.isEmpty()) {
                            mDevice.setLabel(label);
                            ApiCaller.getStaticInstance().postDeviceAsync(mDevice, new Callback() {
                                @Override
                                public void onSuccess(Object result) {
                                    Snackbar.with(DeviceActivity.this)
                                            .text("Label changed")
                                            .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
                                            .show(DeviceActivity.this);

                                }

                                @Override
                                public void onFailure() {
                                    Snackbar.with(DeviceActivity.this)
                                            .text("Failed to change label")
                                            .actionLabel("RETRY")
                                            .actionColorResource(R.color.accent)
                                            .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
                                            .actionListener(new ActionClickListener() {
                                                @Override
                                                public void onActionClicked() {
                                                    onPositive(materialDialog);
                                                }
                                            })
                                            .show(DeviceActivity.this);
                                }
                            });

                            mTitle = label;
                            restoreActionBar();
                        }
                    }
                })
                .build();

        EditText editText = (EditText) dialog.getCustomView().findViewById(R.id.editText);
        editText.setHint(R.string.hint_edit_label);
        editText.setText(mDevice.getLabel());
        editText.setEnabled(true);

        dialog.show();
    }

    @Override
    public void onDeviceFragmentAttached(Device device) {
        if (device != null) {
            mTitle = device.getLabel();
            mDevice = device;
        }
    }

    @Override
    public void onToggleEvent(final Event event) {
        ApiCaller.getStaticInstance().postEventAsync(event, new Callback() {
            @Override
            public void onSuccess(Object result) {
                Snackbar.with(DeviceActivity.this)
                        .text("Event sent")
                        .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                        .show(DeviceActivity.this);
            }

            @Override
            public void onFailure() {
                Snackbar.with(DeviceActivity.this)
                        .text("Failed to send an event")
                        .actionLabel("Retry")
                        .actionListener(new ActionClickListener() {
                            @Override
                            public void onActionClicked() {
                                onToggleEvent(event);
                            }
                        })
                        .actionColorResource(R.color.accent)
                        .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
                        .show(DeviceActivity.this);
            }
        });

    }

    @Override
    public void onRemoveDevice(final Device device) {
        Snackbar.with(this)
                .text("Device removed")
                .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
                .show(this);
    }
}
