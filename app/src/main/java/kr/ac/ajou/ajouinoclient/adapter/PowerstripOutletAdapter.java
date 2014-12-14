package kr.ac.ajou.ajouinoclient.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Collections;

import kr.ac.ajou.ajouinoclient.R;
import kr.ac.ajou.ajouinoclient.model.Device;
import kr.ac.ajou.ajouinoclient.model.Event;

/**
 * Adapter Class for power-strip's each port
 */
public class PowerstripOutletAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private SparseBooleanArray mListItems;

    private OnValueChangedListener mValueChangedListener;

    public PowerstripOutletAdapter(Context context) {
        mContext = context;
        if (context instanceof Activity) {
            setLayoutInflator(((Activity) context).getLayoutInflater());
        }
    }

    public void setLayoutInflator(LayoutInflater inflater) {
        mInflater = inflater;
    }

    public void setItem(Device device) {

        Integer ports = device.getValues().get("ports");
        if(ports == null) ports = 0;

        mListItems = new SparseBooleanArray();

        // set original value of device
        int value = 0;
        if(device.getValues().get("value") != null) {
            value = device.getValues().get("value");
        }

        // get event if user sent some event after connection
        Event event = null;
        if(device.getEvents() != null && device.getEvents().size() > 0) {
            Collections.sort(device.getEvents());
            event = device.getEvents().get(0);
        }

        // overwrite the value if user send events after connection
        if(event != null && event.getValue() != null) {
            value = event.getValue();
        }

        // set values to the adapter's boolean array
        for(int i=0; i<ports; i++) {
            mListItems.put(i, (value & 1) == 1);
            value = value >> 1;
        }
    }

    public int getCount() {
        return mListItems.size();
    }

    public Boolean getItem(int position) {
        if (position < mListItems.size()) {
            return mListItems.get(position);
        } else {
            return null;
        }
    }

    public int getValue() {
        int value = 0;
        for(int i=getCount(); i>=0; i--) {
            if(mListItems.get(i)) value += 1;
            value = value << 1;
        }
        return value >> 1;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {  // if it's not recycled, initialize some attributes
            convertView = mInflater.inflate(R.layout.item_powerstrip_switch_card, null);
        }


        TextView labelView = (TextView) convertView.findViewById(R.id.label);
        TextView descriptionView = (TextView) convertView.findViewById(R.id.description);
        Switch switchView = (Switch) convertView.findViewById(R.id.power_switch);

        String label = null;
        String description = null;

        if (position < mListItems.size()) {

            label = "port " + position;
            description = String.valueOf(getItem(position));
            switchView.setChecked(getItem(position));
            switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(mListItems.get(position) != b) {
                        toggleSwitch(position);
                    }
                }
            });
            switchView.setFocusable(false);
        }

        if (label != null) labelView.setText(label);
        if (description != null) descriptionView.setText(description);

        return convertView;
    }

    public void toggleSwitch(int position) {
        if (position < mListItems.size()) {
            mListItems.put(position, !mListItems.get(position));
            notifyDataSetChanged();
            if(mValueChangedListener != null) {
                mValueChangedListener.onValuedChanged();
            }
        }
    }

    public void setValueChangedListener(OnValueChangedListener mValueChangedListener) {
        this.mValueChangedListener = mValueChangedListener;
    }

    public interface OnValueChangedListener {
        public void onValuedChanged();
    }
}