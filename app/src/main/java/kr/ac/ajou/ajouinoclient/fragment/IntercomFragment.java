package kr.ac.ajou.ajouinoclient.fragment;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nispok.snackbar.Snackbar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import kr.ac.ajou.ajouinoclient.R;
import kr.ac.ajou.ajouinoclient.adapter.IntercomGuestAdapter;
import kr.ac.ajou.ajouinoclient.model.Event;
import kr.ac.ajou.ajouinoclient.service.GcmIntentService;
import kr.ac.ajou.ajouinoclient.util.ApiCaller;
import kr.ac.ajou.ajouinoclient.util.Callback;
import kr.ac.ajou.ajouinoclient.util.ImageDownloader;

/**
 * Fragment controls intercom.
 */
public class IntercomFragment extends DeviceFragment implements AdapterView.OnItemClickListener, GcmIntentService.onNewGcmMessageListener {

    private ListView mListView;
    private IntercomGuestAdapter mIntercomGuestAdapter;
    private Handler mHandler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_listview, container, false);

        mListView = (ListView) rootView.findViewById(R.id.listView);
        mIntercomGuestAdapter = new IntercomGuestAdapter(getActivity());
        mIntercomGuestAdapter.setItem(mDevice);
        mListView.setAdapter(mIntercomGuestAdapter);
        mListView.setOnItemClickListener(this);

        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        final Event event = mIntercomGuestAdapter.getItem(i);
        if (event != null) {
            final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                    .title(event.getType())
                    .customView(R.layout.dialog_layout_guest)
                    .positiveText("OK")
                    .negativeText("DELETE")
                    .callback(new MaterialDialog.Callback() {
                        @Override
                        public void onNegative(MaterialDialog materialDialog) {
                            ApiCaller.getStaticInstance().removeEventAsync(event, new Callback() {
                                @Override
                                public void onSuccess(Object result) {
                                    Snackbar.with(getActivity())
                                            .text("Event Removed.")
                                            .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
                                            .show(getActivity());
                                    mDevice.getEvents().remove(event);
                                    mIntercomGuestAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onFailure() {
                                    Snackbar.with(getActivity())
                                            .text("Failed to remove the event.")
                                            .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
                                            .show(getActivity());
                                }
                            });
                        }

                        @Override
                        public void onPositive(MaterialDialog materialDialog) {
                            //do nothing
                        }
                    })
                    .build();

            // get image drawable inside the list
            Drawable drawable = ((ImageView)view.findViewById(R.id.imageView)).getDrawable();

            // find custom dialog view component reference
            ImageView imageView = (ImageView) dialog.getCustomView().findViewById(R.id.imageView);
            TextView descriptionView = (TextView) dialog.getCustomView().findViewById(R.id.textView);

            // setting up custom dialog view component
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            imageView.setImageDrawable(drawable);
            descriptionView.setText(dateFormat.format(event.getTimestamp()));

            dialog.show();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        GcmIntentService.addOnNewGcmMessageListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        GcmIntentService.removeOnNewGcmMessageListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(mIntercomGuestAdapter != null) mIntercomGuestAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNewEvent(Event event) {
        if(mHandler != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mIntercomGuestAdapter != null) mIntercomGuestAdapter.notifyDataSetChanged();
                }
            });
        }
    }
}
