package com.cm.timovil2.bl.utilities;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;

import java.util.Timer;
import java.util.TimerTask;

/*
 * Created by JORGE ANDRES DAVID CARDONA on 04/06/2015.
 */
public class ControlImeiService extends IntentService{

    private Timer timer;
    private ControlImeiTask task;
    private final ActivityBase context;

    public ControlImeiService() {
        super("ControlImeiService");
        this.context = App.actualActivity;
    }

    private void iniciarTarea() {
        if (timer != null) {
            timer.cancel();
            task.cancel();
        }

        timer = new Timer();
        task = new ControlImeiTask();

        long period = (10000 * 90);//15 minutes
        long delay = 1000; //1 second

        //test
        //long period = (10000 * 3);//30 segundos
        //long delay = 1000; //1 second

        synchronized (this) {
            timer.scheduleAtFixedRate(task, delay, period);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        iniciarTarea();
    }

    private class ControlImeiTask extends TimerTask {


        private ControlImeiTask() {}

        @Override
        public void run() {
            try {
                if (Utilities.isNetworkReachable(getApplicationContext()) &&
                        Utilities.isNetworkConnected(getApplicationContext())) {

                    if(context != null){
                        String installationId = context.getInstallationId();
                        ResolucionDAL resolucionDAL = new ResolucionDAL(context);
                        ResolucionDTO resolucionDTO = resolucionDAL.ObtenerResolucion();

                        if(resolucionDTO != null &&  resolucionDTO.IdCliente!=null
                                && resolucionDTO.CodigoRuta!=null &&
                                !resolucionDTO.IdCliente.equals("") &&
                                !resolucionDTO.CodigoRuta.equals("")) {

                            if (installationId == null || installationId.trim().equals("")) {
                                Log.d("ControlImeiService", "Error verificando el IMEI: El id del dispositivo no está disponible");
                            } else {
                                String respuesta;
                                NetWorkHelper netWorkHelper = new NetWorkHelper();
                                String peticion = SincroHelper.getVerificarImeiURL(
                                        resolucionDTO.IdCliente, resolucionDTO.CodigoRuta, installationId);
                                respuesta = netWorkHelper.readService(peticion);
                                respuesta = SincroHelper.procesarImeiJsonEstado(respuesta);

                                if (respuesta != null && (respuesta.trim().equals("B") ||
                                        respuesta.trim().equals("R") || respuesta.trim().equals("OK"))) {
                                    App.guardarConfiguracionEstadoAplicacion(respuesta, context);
                                } else {
                                    Log.d("ControlImeiService", "Error validando el IMEI : respuesta<<" + respuesta + ">>");
                                }
                            }
                        }
                    }else {
                        Log.d("ControlImeiService", "Error verificando el IMEI: context = null");
                    }

                } else {
                    Log.d("ControlImeiService", "No es posible conectar con el servidor");
                }
            } catch (Exception e) {
                Log.d("ControlImeiService", e.toString());
            }
        }
    }
}
