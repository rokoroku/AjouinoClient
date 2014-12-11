package kr.ac.ajou.ajouinoclient.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

import kr.ac.ajou.ajouinoclient.R;
import kr.ac.ajou.ajouinoclient.model.Device;
import kr.ac.ajou.ajouinoclient.model.Event;
import kr.ac.ajou.ajouinoclient.util.ApiCaller;
import kr.ac.ajou.ajouinoclient.util.ImageDownloader;

/**
 * Created by YoungRok on 2014-11-26.
 */
public class IntercomGuestAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<Event> mItems;
    private static ImageDownloader mImageDownloader = null;

    public IntercomGuestAdapter(Context context) {
        mContext = context;
        if (context instanceof Activity) {
            setLayoutInflator(((Activity) context).getLayoutInflater());
        }
    }

    public void setLayoutInflator(LayoutInflater inflater) {
        mInflater = inflater;
    }

    public void setItem(Device device) {
        mItems = device.getEvents();
        Collections.sort(mItems);
    }

    public int getCount() {
        return mItems.size();
    }

    public Event getItem(int position) {
        if (position < mItems.size()) {
            return mItems.get(position);
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
            convertView = mInflater.inflate(R.layout.item_intercom_guest_card, null);
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
        TextView labelView = (TextView) convertView.findViewById(R.id.label);
        TextView descriptionView = (TextView) convertView.findViewById(R.id.description);

        String imageResUrl = null;
        String label = null;
        String description = null;

        if (position < mItems.size()) {

            Event entry = getItem(position);

            imageResUrl = ApiCaller.getStaticInstance().getHostAddress() + "event/image/" + entry.getDeviceID() + "_" + entry.getTimestamp().getTime();
            label = entry.getType();
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH시 mm분");
            description = dateFormat.format(entry.getTimestamp());
        }

        if (imageView != null && imageResUrl != null) {
            if(mImageDownloader == null) mImageDownloader = new ImageDownloader();
            mImageDownloader.download(imageResUrl, imageView);
        }
        if (labelView != null && label != null) labelView.setText(label);
        if (descriptionView != null && description != null) descriptionView.setText(description);

        return convertView;
    }

}