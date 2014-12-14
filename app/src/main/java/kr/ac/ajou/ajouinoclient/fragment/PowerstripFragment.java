package kr.ac.ajou.ajouinoclient.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.nispok.snackbar.Snackbar;

import kr.ac.ajou.ajouinoclient.R;
import kr.ac.ajou.ajouinoclient.adapter.PowerstripOutletAdapter;
import kr.ac.ajou.ajouinoclient.model.Event;

/**
 * Fragment controls power-strip.
 */
public class PowerstripFragment extends DeviceFragment implements AdapterView.OnItemClickListener, PowerstripOutletAdapter.OnValueChangedListener {

    private ListView mListView;
    private PowerstripOutletAdapter mPowerstripOutletAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_listview, container, false);

        mListView = (ListView) rootView.findViewById(R.id.listView);
        mPowerstripOutletAdapter = new PowerstripOutletAdapter(getActivity());

        mPowerstripOutletAdapter.setItem(mDevice);
        mPowerstripOutletAdapter.setValueChangedListener(this);
        mListView.setAdapter(mPowerstripOutletAdapter);
        mListView.setOnItemClickListener(this);
        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        mPowerstripOutletAdapter.toggleSwitch(position);
    }

    @Override
    public void onValuedChanged() {
        Log.d(this.toString(), "onValueChanged");
        Snackbar.with(getActivity())
                .text(String.format("value %d", mPowerstripOutletAdapter.getValue()))
                .show(getActivity());

        Event event = new Event(mDeviceId, Event.TYPE_POWER, mPowerstripOutletAdapter.getValue());
        mListener.onToggleEvent(event);

    }
}
