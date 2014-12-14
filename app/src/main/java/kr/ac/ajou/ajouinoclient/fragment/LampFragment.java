package kr.ac.ajou.ajouinoclient.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;

//import com.larswerkman.holocolorpicker.ColorPicker;
//import com.larswerkman.holocolorpicker.OpacityBar;
//import com.larswerkman.holocolorpicker.SVBar;
//import com.larswerkman.holocolorpicker.SaturationBar;
//import com.larswerkman.holocolorpicker.ValueBar;
import com.chiralcode.colorpicker.ColorPicker;

import java.util.Collections;

import kr.ac.ajou.ajouinoclient.R;
import kr.ac.ajou.ajouinoclient.model.Event;

/**
 * Fragment controls lamp.
 */
public class LampFragment extends DeviceFragment {

    private ColorPicker mColorPicker;
    private SeekBar mBrightnessBar;
    private Button mButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_lamp, container, false);

        mColorPicker = (ColorPicker) rootView.findViewById(R.id.colorPicker);
        mBrightnessBar = (SeekBar) rootView.findViewById(R.id.brightnessBar);
        mButton = (Button) rootView.findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int color = mColorPicker.getColor();

                color &= 0x00FFFFFF;
                color |= (mBrightnessBar.getProgress() << 24);

                setValuesToComponents();

                //String rgbString = "R: " + Color.red(color) + " G: " + Color.green(color) + " B: " + Color.blue(color) + " A: " + mBrightnessBar.getProgress();
                Event event = new Event(mDeviceId, "color", color);
                mListener.onToggleEvent(event);

            }
        });

        return rootView;
    }

    public void setValuesToComponents() {
        // set original value of device
        int value = 0xFFFFFFFF;
        if(mDevice != null) {
            if (mDevice.getValues().get("value") != null) {
                value = mDevice.getValues().get("value");
            }

            // get event if user sent some event after connection
            Event event = null;
            if (mDevice.getEvents() != null && mDevice.getEvents().size() > 0) {
                Collections.sort(mDevice.getEvents());
                event = mDevice.getEvents().get(0);
            }

            // overwrite the value if user send events after connection
            if (event != null && event.getValue() != null) {
                value = event.getValue();
            }

        }
        int color = value |= 0xFF000000;
        int brightness = value >> 24 & 0xFF;

        mColorPicker.setColor(color);
        mBrightnessBar.setProgress(brightness);
    }
}
