package com.cm.timovil2.location_service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 18/01/17.
 */
public class AlarmReceiverTimovil extends BroadcastReceiver{

    public static final int REQUEST_CODE = 12345;
    public static final String ACTION = "co.com.timovil.location_service.alarm";

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action != null && action.equals(ACTION)){
            Log.i("LocationService", "Se ha iniciado el servicio desde AlarmReceiverTimovil...");
            Intent intent_service = new Intent(context, LocationService.class);
            context.startService(intent_service);
        }
    }
}
