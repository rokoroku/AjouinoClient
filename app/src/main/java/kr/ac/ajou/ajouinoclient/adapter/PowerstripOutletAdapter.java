package kr.ac.ajou.ajouinoclient.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.ac.ajou.ajouinoclient.R;
import kr.ac.ajou.ajouinoclient.model.Device;
import kr.ac.ajou.ajouinoclient.model.DeviceInfo;

/**
 * Created by YoungRok on 2014-11-26.
 */
public class PowerstripOutletAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private Map<String, Integer> mItems;
    private List<Map.Entry<String, Integer>> mListItems;

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
        mItems = device.getValues();
        mListItems = new ArrayList<>(mItems.entrySet());
    }

    public int getCount() {
        return mItems.size();
    }

    public Map.Entry<String, Integer> getItem(int position) {
        if (position < mItems.size()) {
            return mListItems.get(position);
        } else {
            return null;
        }
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {  // if it's not recycled, initialize some attributes
            convertView = mInflater.inflate(R.layout.item_powerstrip_swtich, null);
        }

        TextView labelView = (TextView) convertView.findViewById(R.id.label);
        TextView descriptionView = (TextView) convertView.findViewById(R.id.description);

        String label = null;
        String description = null;

        if (position < mItems.size()) {

            Map.Entry<String, Integer> entry = getItem(position);
            label = entry.getKey();
            description = String.valueOf(entry.getValue());

        }

        if (label != null) labelView.setText(label);
        if (description != null) descriptionView.setText(description);

        return convertView;
    }

}