package com.example.javaapp1.MessageBoxes;

import android.app.Dialog;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class MessageBoxLocation extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Chyba");
        builder.setMessage("Nepodařilo se určit polohu zařízení.");
        builder.setPositiveButton("OK", (dialog, id) -> {
        });
        return builder.create();
    }
}