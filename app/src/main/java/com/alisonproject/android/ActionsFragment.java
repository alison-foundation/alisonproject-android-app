package com.alisonproject.android;

import android.animation.ArgbEvaluator;
import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alisonproject.android.BlutetoothHelpers.BluetoothConnManager;
import com.alisonproject.android.BlutetoothHelpers.MessageConstants;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ActionsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ActionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ActionsFragment extends Fragment  {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "isRecording";

    // TODO: Rename and change types of parameters

    private TextView responseDisplay;
    private TextView startRecordTextView;
    private TextView timerTextView;
    private ImageButton startRecord;
    private boolean isRecording = false;
    ValueAnimator colorAnimation;
    //MediaPlayer mediaPlayerStart;
    //MediaPlayer mediaPlayerStop;

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
     * @param isRecording Parameter 1.
     * @return A new instance of fragment ActionsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ActionsFragment newInstance(boolean isRecording) {
        ActionsFragment fragment = new ActionsFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM1, isRecording);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * start => commencer
     * stop => finir
     * save | tag | color
     * python -m alison params...
     * @param savedInstanceState
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isRecording = getArguments().getBoolean(ARG_PARAM1);
        }

        BluetoothConnManager.setUIhandler(conHandler);
       // mediaPlayerStart = MediaPlayer.create(getContext(), R.raw.start_record);
       // mediaPlayerStop = MediaPlayer.create(getContext(), R.raw.stop_record);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_actions, container, false);

        if(view instanceof ConstraintLayout){
            responseDisplay = view.findViewById(R.id.response_text);
            startRecordTextView = view.findViewById(R.id.recordSoundTextView);
            timerTextView = view.findViewById(R.id.timerTextView);
            timerTextView.setText("00:00");
            startRecord = view.findViewById(R.id.recordSoundBtn);

            startRecord.setOnClickListener( listener -> {
                if(!isRecording)
                    startRecording(view);
                else
                    stopRecording(view);
            });
        }

        return view;
    }

    private void startRecording(View view){
        isRecording = true;
        BluetoothConnManager.getInstance().send("start");
        startRecord.setImageDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.ic_mic_stop_record_128dp));

        //animate record button
        int colorFrom = 255;
        int colorTo = 20;
        colorAnimation = ValueAnimator.ofObject(new IntEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(3000); // milliseconds
        colorAnimation.setRepeatMode(ValueAnimator.REVERSE);
        colorAnimation.setRepeatCount(ValueAnimator.INFINITE);
        colorAnimation.addUpdateListener( animator -> startRecord.getDrawable().setAlpha((int) animator.getAnimatedValue()));
        colorAnimation.start();

        //play sound
        /*if(mediaPlayerStop.isPlaying()){
            mediaPlayerStop.stop();
            mediaPlayerStop.release();
            mediaPlayerStop = MediaPlayer.create(getContext(), R.raw.stop_record);
        }*/
        startRecordTextView.setText("Recording...");
        new CountDownTimer(3000, 100) {

            public void onTick(long millisUntilFinished) {
                Log.i("##milliseconds", ""  + millisUntilFinished);
                float result = (float)(millisUntilFinished) / 1000.0f;
                String str = ""  + Math.round( (float)(millisUntilFinished - Math.round(result)) / 10.0f);
                if(str.length() > 2)
                    str = str.substring(1);
                timerTextView.setText(Math.round(result) + ":" + str);
            }
    
            public void onFinish() {
                timerTextView.setText("03:00");
                stopRecording(view);
            }
        }.start();

        //mediaPlayerStart.start();
    }

    private void stopRecording(View view){
        isRecording = false;
        BluetoothConnManager.getInstance().send("stop");
        startRecord.setImageDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.ic_mic_start_record_24dp));
        colorAnimation.cancel();

        //play sound
        /*if(mediaPlayerStart.isPlaying()){
            mediaPlayerStart.stop();
            mediaPlayerStart.release();
            mediaPlayerStart = MediaPlayer.create(getContext(), R.raw.start_record);
        }*/
        startRecordTextView.setText("Record");
        /*mediaPlayerStop.start();
        mediaPlayerStop.setOnCompletionListener(l -> {
            if(l.isPlaying()){
                l.stop();
                l.release();
            }});
        */
        SaveSoundFragment confirmDialog = new SaveSoundFragment();
        confirmDialog.show(Objects.requireNonNull(getFragmentManager()), "saveSoundFragment");
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
