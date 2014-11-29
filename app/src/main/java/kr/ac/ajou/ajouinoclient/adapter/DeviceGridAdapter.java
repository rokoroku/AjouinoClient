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
import java.util.Collection;
import java.util.List;

import kr.ac.ajou.ajouinoclient.R;
import kr.ac.ajou.ajouinoclient.model.Device;
import kr.ac.ajou.ajouinoclient.model.DeviceInfo;

/**
 * Created by YoungRok on 2014-11-26.
 */
public class DeviceGridAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<DeviceInfo> mListItems;

    public DeviceGridAdapter(Context context) {
        mContext = context;
        if (context instanceof Activity) {
            setLayoutInflator(((Activity) context).getLayoutInflater());
        }
    }

    public void setLayoutInflator(LayoutInflater inflater) {
        mInflater = inflater;
    }

    public void setListItems(Collection<? extends DeviceInfo> items) {
        mListItems = new ArrayList<>(items);
    }

    public int getCount() {
        return mListItems.size();
    }

    public DeviceInfo getItem(int position) {
        if (position < mListItems.size()) {
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
            convertView = mInflater.inflate(R.layout.item_add_device, null);
        }
        ImageView iconView = (ImageView) convertView.findViewById(R.id.image);
        TextView labelView = (TextView) convertView.findViewById(R.id.label);
        TextView descriptionView = (TextView) convertView.findViewById(R.id.description);

        Integer imageRes = null;
        String label = null;
        String description = null;

        if (position < mListItems.size()) {
            DeviceInfo deviceInfo = getItem(position);
            imageRes = getThumbnailResource(deviceInfo.getType());
            if (deviceInfo.getLabel() != null) {
                label = deviceInfo.getLabel();
            } else {
                label = deviceInfo.getId();
            }
            if (deviceInfo.getType() == null) {
                description = deviceInfo.getAddress();
            }
        } else {
            imageRes = getThumbnailResource("add");
            label = mContext.getString(R.string.action_add_new_device);
        }

        if (imageRes != null) iconView.setImageResource(imageRes);
        if (label != null) labelView.setText(label);
        if (description != null) {
            descriptionView.setVisibility(View.VISIBLE);
            descriptionView.setText(description);
        } else {
            descriptionView.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    // references to our images
    private int getThumbnailResource(String type) {
        if (type == null) return R.drawable.icon_arduino;
        else if (type.equalsIgnoreCase("powerstrip")) return R.drawable.icon_powerstrip;
        else if (type.equalsIgnoreCase("lamp")) return R.drawable.icon_lamp;
        else if (type.equalsIgnoreCase("intercom")) return R.drawable.icon_intercom;
        else return R.drawable.icon_arduino;
    }
}