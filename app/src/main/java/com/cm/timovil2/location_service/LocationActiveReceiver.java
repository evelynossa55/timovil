package com.cm.timovil2.location_service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cm.timovil2.bl.app.App;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 8/02/17.
 */
public class LocationActiveReceiver  extends BroadcastReceiver {

    public static final String ACTION = "com.cm.timovil2.location_service.LOCATION_ACTIVE";

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {

        boolean active = intent.getBooleanExtra("active", false);
        Log.i("LocationService", "Location active..." + active);
        App.isLocationServiceActive = active;
    }
}