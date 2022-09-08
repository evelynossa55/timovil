package com.cm.timovil2.bl.utilities;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import android.util.Log;

import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.front.ActivityPedidos;
import com.cm.timovil2.R;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.data.DetallePedidoCallcenterDAL;
import com.cm.timovil2.data.PedidoCallcenterDAL;
import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.data.ResultadoGestionCasoCallcenterDAL;
import com.cm.timovil2.dto.PedidoCallcenterDTO;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.dto.ResultadoGestionCasoCallcenterDTO;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 24/01/17.
 */
public class ServicePedidosCallcenter extends IntentService {

    private Timer timer;
    private ActualizadorDePedidos tarea;
    private final ActivityBase context;

    public ServicePedidosCallcenter() {
        super("ServicePedidosCallcenter");
        this.context = App.actualActivity;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        iniciarTareasProgramadas();
    }

    private void iniciarTareasProgramadas() {

        if (timer != null) {
            timer.cancel();
            tarea.cancel();
        }

        timer = new Timer();
        tarea = new ActualizadorDePedidos(context);

        //long period = (10000 * 90); //15 minutes
        //long period = ((10000 * 6) * 5); //5 minute
        long period = (1000 * 150); //2 minutes and 30 seconds
        long delay = 1000; //1 second

        synchronized (this) {
            timer.scheduleAtFixedRate(tarea, delay, period);
        }
    }

    private class ActualizadorDePedidos extends TimerTask {

        final PedidoCallcenterDAL pedidoCallcenterDAL;
        final DetallePedidoCallcenterDAL detallePedidoCallcenterDAL;
        final ResultadoGestionCasoCallcenterDAL resultadoGestionCasoCallcenterDAL;

        private ActualizadorDePedidos(ActivityBase context) {
            pedidoCallcenterDAL = new PedidoCallcenterDAL(context);
            detallePedidoCallcenterDAL = new DetallePedidoCallcenterDAL(context);
            resultadoGestionCasoCallcenterDAL = new ResultadoGestionCasoCallcenterDAL(context);
        }

        @Override
        public void run() {
            try {

                if (Utilities.isNetworkReachable(getApplicationContext()) &&
                        Utilities.isNetworkConnected(getApplicationContext())) {

                    ResolucionDTO resolucion = new ResolucionDAL(context).ObtenerResolucion();

                    if(resolucion == null) return;

                    ArrayList<PedidoCallcenterDTO> listadoPedidos;
                    ArrayList<ResultadoGestionCasoCallcenterDTO> listadoMotivosNegativos;

                    NetWorkHelper netWorkHelper = new NetWorkHelper();

                    String request = SincroHelper.ObtenerPedidosCallcenterURL(resolucion.IdCliente, resolucion.CodigoRuta);
                    String respuesta = netWorkHelper.readService(request);
                    listadoPedidos = SincroHelper.procesarJsonPedidoCallcenter(respuesta);

                    request = SincroHelper.ObtenerMotivosNegativosURL(resolucion.IdCliente, resolucion.CodigoRuta, context.getInstallationId());
                    respuesta = netWorkHelper.readService(request);
                    listadoMotivosNegativos = SincroHelper.procesarJsonResultadosGestionPedidosCallcenter(respuesta);

                    int cantidadInsertada = 0;
                    detallePedidoCallcenterDAL.Eliminar();
                    pedidoCallcenterDAL.Eliminar();
                    resultadoGestionCasoCallcenterDAL.Eliminar();

                    StringBuilder idCasos = new StringBuilder();
                    if(listadoPedidos != null && listadoPedidos.size()>0){
                        for (PedidoCallcenterDTO p:listadoPedidos) {
                            pedidoCallcenterDAL.Insertar(p);
                            idCasos.append(p.IdCaso).append("|");
                            cantidadInsertada++;
                        }
                    }

                    request = SincroHelper.ObtenerNotificaRecepcionPedidoURL(resolucion.IdCliente, idCasos.toString(), resolucion.CodigoRuta);
                    respuesta = netWorkHelper.readService(request);
                    String respuestaNotificar = SincroHelper.procesarOkJson(respuesta);
                    Log.d("PedidosService", "Notificar recibidos: " + idCasos.toString() + " -->" + respuestaNotificar);
                    Log.d("PedidosService", "Cantidad Pedidos nuevos: " + cantidadInsertada);
                    if(cantidadInsertada > 0){
                        notificar();
                    }

                    if(listadoMotivosNegativos != null && listadoMotivosNegativos.size() > 0){
                        for (ResultadoGestionCasoCallcenterDTO r: listadoMotivosNegativos) {
                            resultadoGestionCasoCallcenterDAL.Insertar(r);
                        }
                    }

                } else {
                    Log.d("PedidosService", "No es posible conectar con el servidor");
                }

            } catch (Exception e) {
                if (!Utilities.isNetworkReachable(getApplicationContext()) ||
                        !Utilities.isNetworkConnected(getApplicationContext())) {
                    Log.d("PedidosService", "Error obteniendo los pedidos"
                            + " << Causa: " + e.getMessage() + " >>");
                }
            }
        }

        private void notificar(){
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.icon_launcher)
                            .setContentTitle("TiMovil")
                            .setContentText("Han ingresado nuevos pedidos ")
                            .setSound(alarmSound)
                            .setAutoCancel(true);

            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(context, ActivityPedidos.class);
            resultIntent.putExtra("esNotificacion", true);
            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(ActivityPedidos.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);

            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // mId allows you to update the notification later on.
            int mId = 45821;
            if(mNotificationManager != null)
                mNotificationManager.notify(mId, mBuilder.build());
        }

    }


}
