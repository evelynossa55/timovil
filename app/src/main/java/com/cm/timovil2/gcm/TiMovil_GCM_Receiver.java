package com.cm.timovil2.gcm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import androidx.legacy.content.WakefulBroadcastReceiver;

public class TiMovil_GCM_Receiver extends WakefulBroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {

        //Se obtiene la ubicación del servicio que manejará el mensaje
        //Se inicia el servicio con startWakefulService, con el propósito de que se
            //mantenga un WAKE LOCK que no permita que el procesador se inactive durante
            //la ejecución del servicio.
        //se retorna un código en representación del resultado de la ejecución de este BroadCastReceiver

        ComponentName service_component = new ComponentName(
                context.getPackageName(), TiMovil_GCM_Service.class.getName()
        );
        startWakefulService(context, (intent.setComponent(service_component)));
        setResultCode(Activity.RESULT_OK);
    }
}
