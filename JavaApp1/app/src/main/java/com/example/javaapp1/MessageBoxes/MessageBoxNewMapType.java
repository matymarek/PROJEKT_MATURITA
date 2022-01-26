package com.example.javaapp1.MessageBoxes;

import android.app.Dialog;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MessageBoxNewMapType extends DialogFragment {
    String[] mapTypes = {"normální", "turistická", "satelitní", "hybridní"};
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Vyber si nový typ mapy");
        builder.setItems(mapTypes, (dialog, which) -> {
            try {
                InputStream inputStream = getContext().openFileInput("config.txt");
                String json;
                if (inputStream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString;
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((receiveString = bufferedReader.readLine()) != null) {
                        stringBuilder.append("\n").append(receiveString);
                    }
                    inputStream.close();
                    json = stringBuilder.toString();
                }
                else{
                    json = new JSONObject("{\"barva\":\"červená\",\"autosave\":\"1\",\"mapType\":\"turistická\"}").toString();
                }
                JSONObject reader = new JSONObject(json);
                reader.put("mapType", mapTypes[which]);
                Toast.makeText(getContext(), "Byl vybrán typ: "+mapTypes[which], Toast.LENGTH_LONG).show();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getContext().openFileOutput("config.txt", Context.MODE_PRIVATE));
                outputStreamWriter.write(reader.toString());
                outputStreamWriter.close();
            }
            catch (IOException | JSONException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        });
        builder.setNegativeButton("Zrušit", (dialog, id) -> {});
        return builder.create();
    }
}
