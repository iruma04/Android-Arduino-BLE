package com.example.esp32ble;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class BtDialogFragment extends DialogFragment implements DialogInterface.OnClickListener{

    private MainActivity m;

    public BtDialogFragment(MainActivity main){
        m = main;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("ペアリング設定は終えていますか？");
        builder.setMessage("端末本体設定によるBluetoothデバイスとのペアリング設定");
        builder.setPositiveButton("はい", this::onClick);
        builder.setNegativeButton("いいえ", this::onClick);
        builder.setNeutralButton("あとで",this::onClick);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which){
            case DialogInterface.BUTTON_POSITIVE:
                //「はい」
                m.positiveCallback();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                //「いいえ」
                startActivityForResult(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS), 0);
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                //「あとで」
                break;
        }
    }
}