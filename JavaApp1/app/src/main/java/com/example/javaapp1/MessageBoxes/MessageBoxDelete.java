package com.example.javaapp1.MessageBoxes;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.javaapp1.SavesActivity;
import com.example.javaapp1.db.Route;
import com.example.javaapp1.db.RouteDAO;

import java.util.List;


public class MessageBoxDelete extends DialogFragment {
    int id;
    RouteDAO routeDAO;
    List<Route> dbRoute;

    public MessageBoxDelete(int id, RouteDAO routeDAO, List<Route> dbRoute){
        this.id = id;
        this.routeDAO = routeDAO;
        this.dbRoute = dbRoute;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Smazat záznam?");
        builder.setMessage("Opravdu chcete smazat záznam?");
        builder.setPositiveButton("OK", (dialog, id) -> {
            routeDAO.delete(dbRoute.get(this.id));
            startActivity(new Intent(getContext(), SavesActivity.class));
        }).setNegativeButton("Zrušit", (dialog, id) -> {
        });
        return builder.create();
    }
}
