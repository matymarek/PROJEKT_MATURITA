package com.example.javaapp1.MessageBoxes;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;

import com.example.javaapp1.SettingsActivity;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class MessageBoxNewColor extends DialogFragment {
    String[] colors = {"červená", "zelená", "modrá", "černá"};
    int input;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Vyber is novou barvu trasy");
        builder.setItems(colors, (dialog, which) -> {
            Toast.makeText(getContext(), "Byla vybrána barva: "+colors[which], Toast.LENGTH_LONG).show();
            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getContext().openFileOutput("config.txt", Context.MODE_PRIVATE));
                outputStreamWriter.write(""+colors[which]);
                outputStreamWriter.close();
            }
            catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        });
        builder.setNegativeButton("Zrušit", (dialog, id) -> {});
        return builder.create();
    }
}
