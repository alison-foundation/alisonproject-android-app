package com.alisonproject.android;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;

import java.util.Objects;

public class SaveSoundFragment extends DialogFragment {

    public interface SaveSoundDialogListener{
        public void onDialogPositiveClick(SaveSoundFragment dialog, String tag);
        public void onDialogNegativeClick(SaveSoundFragment dialog);
    }

    SaveSoundDialogListener listener;

    private ConstraintLayout layout;
    EditText tagIput;
    Button openColorPickerBtn;
    ColorPicker colorPicker;
    public int selectedColor;
    public String tag = "";

    public void openColorPicker(View listener){
        colorPicker.show();
        colorPicker.enableAutoClose();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (SaveSoundDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getActivity().toString()
                    + " must implement SaveSoundDialogListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        colorPicker= new ColorPicker(Objects.requireNonNull(getActivity()), 20 , 100, 200);
        colorPicker.setCallback(color -> {
            selectedColor = color;
            layout.setBackgroundColor(selectedColor);
            Log.d("##selectedcolor", Integer.toHexString(selectedColor));
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);


        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
//        builder.setTitle("Sound informations");
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        layout = (ConstraintLayout) inflater.inflate(R.layout.confirm_dialog_fragment, null);
        tagIput = layout.findViewById(R.id.tagInput);
        openColorPickerBtn = layout.findViewById(R.id.selectColorButton);
        openColorPickerBtn.setOnClickListener( listener -> openColorPicker(listener));

        builder.setView(layout)
                // Add action buttons
                .setPositiveButton(R.string.save, (dialog, which) -> listener.onDialogPositiveClick(this, tagIput.getText().toString()))
                .setNegativeButton(R.string.cancel, (dialog, which) -> listener.onDialogNegativeClick(this));
        return builder.create();

    }
}
