package com.example.esp32ble;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class gameDialog extends DialogFragment implements DialogInterface.OnClickListener {

    private GamePage g;

    public gameDialog(GamePage game){ g = game; }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("胸の前でセットしてください");
        builder.setPositiveButton("しました",this::onClick);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which){
            case DialogInterface.BUTTON_POSITIVE:
                g.zeroPoint();
                break;
        }
    }
}
