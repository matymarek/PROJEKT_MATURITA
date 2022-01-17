package com.example.javaapp1.MessageBoxes;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

import android.Manifest;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

public class MessageBoxBackgroundLocationAPI30 extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Poloha na pozadí");
        builder.setMessage("Pro trasování i na pozadí povolte polohu na pozadí(vždy).");
        builder.setPositiveButton("OK", (dialog, id) -> {
            if( ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PERMISSION_GRANTED &&
                    Build.VERSION.SDK_INT >= 29) {
                String[] permision = new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION};
                ActivityCompat.requestPermissions(getActivity(), permision, PERMISSION_GRANTED);
                while (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PERMISSION_GRANTED) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).setNegativeButton("Zrušit", (dialog, id) -> {
        });
        return builder.create();
    }
}
