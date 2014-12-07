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

import kr.ac.ajou.ajouinoclient.R;
import kr.ac.ajou.ajouinoclient.model.Event;

/**
 * Created by YoungRok on 2014-11-27.
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

                //String rgbString = "R: " + Color.red(color) + " G: " + Color.green(color) + " B: " + Color.blue(color) + " A: " + mBrightnessBar.getProgress();
                Event event = new Event(mDeviceId, "color", color);
                mListener.onToggleEvent(event);

            }
        });

        return rootView;
    }
}
