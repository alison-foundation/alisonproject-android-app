package com.alisonproject.android;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alisonproject.android.BlutetoothHelpers.BluetoothConnManager;
import com.alisonproject.android.BlutetoothHelpers.MessageConstants;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ActionsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ActionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ActionsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView responseDisplay;
    private ImageButton startRecord;
    private ImageButton setNotification;
    private ImageButton saveRecord;
    private ImageButton dropRecord;

    private OnFragmentInteractionListener mListener;

    private Handler conHandler = new Handler(msg -> {

        switch (msg.what){
            case MessageConstants.MESSAGE_READ:
                //show spinner
                responseDisplay.setText((String)msg.obj);
                break;
            case MessageConstants.DEVICE_DISCONNECTED:
                mListener.onFragmentInteraction(MessageConstants.DEVICE_DISCONNECTED, "");
                break;
            default:
                Log.d("##ActionFragment", "Undefined handler message constant [" + msg.what + "]");
                return false;
        }
        return true;
    });

    public ActionsFragment() {
        // Required empty public conBasestructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ActionsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ActionsFragment newInstance(String param1, String param2) {
        ActionsFragment fragment = new ActionsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        BluetoothConnManager.setUIhandler(conHandler);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_actions, container, false);

        if(view instanceof ConstraintLayout){
            startRecord = view.findViewById(R.id.recordSoundBtn);
            setNotification = view.findViewById(R.id.notifBtn);
            saveRecord = view.findViewById(R.id.saveRecordBtn);
            dropRecord = view.findViewById(R.id.dropRecordBtn);

            responseDisplay = view.findViewById(R.id.response_text);

            startRecord.setOnClickListener( listener -> BluetoothConnManager.getInstance().send("listen sound | 1"));
            setNotification.setOnClickListener( listener -> BluetoothConnManager.getInstance().send("set led color | blue"));
            saveRecord.setOnClickListener( listener -> BluetoothConnManager.getInstance().send("save sound"));
            dropRecord.setOnClickListener( listener -> BluetoothConnManager.getInstance().send("drop last sound"));
        }

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(String text) {
        if (mListener != null) {
            mListener.onFragmentInteraction(MessageConstants.MESSAGE_TOAST,text);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
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
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(int what, String text);
    }
}
