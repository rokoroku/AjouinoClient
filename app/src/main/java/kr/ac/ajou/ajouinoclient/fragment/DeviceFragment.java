package kr.ac.ajou.ajouinoclient.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kr.ac.ajou.ajouinoclient.R;
import kr.ac.ajou.ajouinoclient.model.Device;
import kr.ac.ajou.ajouinoclient.model.Event;
import kr.ac.ajou.ajouinoclient.persistent.DeviceManager;

/**
 * A fragment representing a list of Items.
 */
public class DeviceFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "deviceId";

    protected String mDeviceId;
    protected Device mDevice;
    protected OnDeviceFragmentInteractionListener mListener;

    // TODO: Rename and change types of parameters
    public static DeviceFragment newInstance(String deviceId) {

        Device device = DeviceManager.getInstance().getDevice(deviceId);

        String deviceType = "";
        if(device != null) deviceType = device.getType();

        DeviceFragment fragment = null;
        if(deviceType.equals(Device.TYPE_LAMP)) fragment = new LampFragment();
        else if(deviceType.equals(Device.TYPE_POWERSTRIP)) fragment = new PowerstripFragment();
        else if(deviceType.equals(Device.TYPE_INTERCOM)) fragment = new IntercomFragment();
        else fragment = new DeviceFragment();

        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, deviceId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DeviceFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mDeviceId = getArguments().getString(ARG_PARAM1);
            mDevice = DeviceManager.getInstance().getDevice(mDeviceId);
            if(mListener != null) mListener.onDeviceFragmentAttached(mDevice);
        }

        // TODO: Change Adapter to display your content
//        setListAdapter(new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
//                android.R.layout.simple_list_item_1, android.R.id.text1, DummyContent.ITEMS));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_device, container, false);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDeviceFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnDeviceFragmentInteractionListener {
        public void onDeviceFragmentAttached(Device device);

        public void onToggleEvent(Event event);

        public void onRemoveDevice(Device device);
    }

}
