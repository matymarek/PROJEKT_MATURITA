package com.example.javaapp1.MessageBoxes;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.javaapp1.db.Route;
import com.example.javaapp1.db.RouteDAO;

public class MessageBoxSaveRoute extends DialogFragment {
    RouteDAO routeDAO;
    Route route;
    public MessageBoxSaveRoute(RouteDAO routeDAO, Route route){
        this.routeDAO = routeDAO;
        this.route = route;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Uložit záznam?");
        builder.setMessage("Chcete uložit tento záznam?");
        builder.setPositiveButton("OK", (dialog, id) -> {
            routeDAO.insertRoute(route);
            Toast.makeText(getContext(), "Trasa uložena", Toast.LENGTH_LONG).show();
        }).setNegativeButton("Zrušit", (dialog, id) -> {
        });
        return builder.create();
    }
}
