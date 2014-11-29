package kr.ac.ajou.ajouinoclient.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;

import com.chiralcode.colorpicker.ColorPicker;

import kr.ac.ajou.ajouinoclient.R;
import kr.ac.ajou.ajouinoclient.adapter.PowerstripOutletAdapter;
import kr.ac.ajou.ajouinoclient.model.Event;

/**
 * Created by YoungRok on 2014-11-27.
 */
public class PowerstripFragment extends DeviceFragment {

    private ListView mListView;
    private PowerstripOutletAdapter mPowerstripOutletAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_powerstrip, container, false);

        mListView = (ListView) rootView.findViewById(R.id.listView);
        mPowerstripOutletAdapter = new PowerstripOutletAdapter(getActivity());
        mPowerstripOutletAdapter.setItem(mDevice);
        mListView.setAdapter(mPowerstripOutletAdapter);

//                Event event = new Event(mDeviceId, "color", color);
//        mListener.onToggleEvent(event);

        return rootView;
    }
}
