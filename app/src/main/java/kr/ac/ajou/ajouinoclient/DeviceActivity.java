package kr.ac.ajou.ajouinoclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.ActionClickListener;

import kr.ac.ajou.ajouinoclient.fragments.DeviceFragment;
import kr.ac.ajou.ajouinoclient.fragments.NavigationDrawerFragment;
import kr.ac.ajou.ajouinoclient.model.Device;
import kr.ac.ajou.ajouinoclient.model.Event;
import kr.ac.ajou.ajouinoclient.util.ApiCaller;
import kr.ac.ajou.ajouinoclient.util.Callback;


public class DeviceActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        DeviceFragment.OnDeviceFragmentInteractionListener {

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
        setContentView(R.layout.activity_device);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        Intent intent = getIntent();
        if(intent != null) {
            String param = intent.getStringExtra("deviceId");
            if(param != null) {
                replaceDeviceFragment(param);
            }
        }
    }


    @Override
    public void onNavigationDrawerItemSelected(String param) {
        // update the main content by replacing fragments
        if(param != null) replaceDeviceFragment(param);
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
            getMenuInflater().inflate(R.menu.device, menu);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDeviceFragmentAttached(Device device) {
        if(device != null) mTitle = device.getLabel();
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
                        .actionColor(R.color.accent)
                        .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
                        .show(DeviceActivity.this);
            }
        });

    }

    @Override
    public void onRemoveDevice(Device device) {
        Snackbar.with(DeviceActivity.this)
                .text("Device removed")
                .actionLabel("Undo")
                .actionColor(R.color.accent)
                .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
                .show(DeviceActivity.this);
    }
}
