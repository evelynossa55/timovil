package com.cm.timovil2.bl.utilities;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.data.AbonoFacturaDAL;
import com.cm.timovil2.data.FacturaDAL;
import com.cm.timovil2.data.GuardarMotivoNoVentaDAL;
import com.cm.timovil2.data.GuardarMotivoNoVentaPedidoDAL;
import com.cm.timovil2.data.NotaCreditoFacturaDAL;
import com.cm.timovil2.data.RemisionDAL;
import com.cm.timovil2.front.ActivityBase;

import java.util.Timer;
import java.util.TimerTask;

public class ServiceSincrofacturas extends IntentService {

    private Timer timer;
    private ActualizadorDeFacturas tarea;
    private final ActivityBase context;

    public ServiceSincrofacturas() {
        super("ServiceSincrofacturas");
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
        tarea = new ActualizadorDeFacturas(context);

        long period = (10000 * 90);//15 minutes
        long delay = 1000; //1 second
        //long period = (10000 * 6);//1 minute
        //long delay = 1000; //1 second
        synchronized (this) {
            timer.scheduleAtFixedRate(tarea, delay, period);
        }
    }

    private class ActualizadorDeFacturas extends TimerTask {

        final FacturaDAL facturaDAL;
        final AbonoFacturaDAL abonoFacturaDAL;
        final RemisionDAL remisionDAL;

        private ActualizadorDeFacturas(ActivityBase context) {
            facturaDAL = new FacturaDAL(context);
            abonoFacturaDAL = new AbonoFacturaDAL(context);
            remisionDAL = new RemisionDAL(context);
        }

        @Override
        public void run() {
            try {
                if (Utilities.isNetworkReachable(getApplicationContext()) &&
                        Utilities.isNetworkConnected(getApplicationContext())) {

                    FacturaDAL facturaDal = new FacturaDAL(context);
                    AbonoFacturaDAL abonoFacturaDal = new AbonoFacturaDAL(context);
                    GuardarMotivoNoVentaPedidoDAL guardarMotivoNoVentaPedidoDal = new GuardarMotivoNoVentaPedidoDAL(context);
                    GuardarMotivoNoVentaDAL guardarMotivoNoVentaDal = new GuardarMotivoNoVentaDAL(context);
                    RemisionDAL remisionDal = new RemisionDAL(context);
                    NotaCreditoFacturaDAL notaCreditoFacturaDal = new NotaCreditoFacturaDAL(context);

                    int listadoPendientes = facturaDal.obtenerListadoPendientes().size();
                    int listadoAbonoPendientes = abonoFacturaDal.obtenerListadoPendientes().size();
                    int listadoNoVentaPedidoPendientes = guardarMotivoNoVentaPedidoDal.obtenerListadoPendientes().size();
                    int listadoNoVentaPendientes = guardarMotivoNoVentaDal.obtenerListadoPendientes().size();
                    int remPendientes = remisionDal.obtenerListadoPendientes().size();
                    int notasCreditoPendientes = notaCreditoFacturaDal.obtenerListadoPendientes().size();

                    int totalPendientes =
                            listadoPendientes + listadoAbonoPendientes + listadoNoVentaPedidoPendientes
                            + listadoNoVentaPendientes + remPendientes + notasCreditoPendientes;

                    if (totalPendientes > 0) {

                        if (listadoPendientes > 0) {
                            Log.i("ServiceSincrofacturas", "Descargando facturas pendientes");
                            Log.i("ServiceSincrofacturas", "\nFacturas: ");
                            Log.i("ServiceSincrofacturas", facturaDal.descargarFacturas());
                        }
                        if (listadoAbonoPendientes > 0) {
                            Log.i("ServiceSincrofacturas", "Descargando abonos pendientes");
                            Log.i("ServiceSincrofacturas", "\nCréditos: ");
                            Log.i("ServiceSincrofacturas", abonoFacturaDal.descargarAbonos());
                        }
                        if (listadoNoVentaPendientes > 0) {
                            Log.i("ServiceSincrofacturas", "Descargando registros de no venta");
                            Log.i("ServiceSincrofacturas", "\nNo venta: ");
                            Log.i("ServiceSincrofacturas", guardarMotivoNoVentaDal.descargarPendientes());
                        }
                        if (listadoNoVentaPedidoPendientes > 0) {
                            Log.i("ServiceSincrofacturas", "Descargando registros de no venta para pedidos");
                            Log.i("ServiceSincrofacturas", "\nNo venta, pedidos: ");
                            Log.i("ServiceSincrofacturas", guardarMotivoNoVentaPedidoDal.descargarPendientes());
                        }
                        if (remPendientes > 0) {
                            Log.i("ServiceSincrofacturas", "Remisiones pendientes");
                            Log.i("ServiceSincrofacturas", "Descargando remisiones pendientes");
                            Log.i("ServiceSincrofacturas", remisionDal.descargarRemisiones());
                        }
                        if(notasCreditoPendientes > 0){
                            Log.i("ServiceSincrofacturas", "Notas crédito pendientes");
                            Log.i("ServiceSincrofacturas", "\nNotas Crédito: ");
                            Log.i("ServiceSincrofacturas", notaCreditoFacturaDal.descargarPendientes());
                        }

                    } else {
                        Log.i("ServiceSincrofacturas", "--* NO HAY FACTURAS PENDIENTES PARA DESCARGAR --*");
                    }

                    //VERIFICAR LAS FACTURAS
                    new ServiceMonitorFacturacion().verificar(true);

                } else {
                    Log.i("ServiceSincrofacturas", "No es posible conectar con el servidor");
                }
            } catch (Exception e) {
                if (!Utilities.isNetworkReachable(getApplicationContext()) ||
                        !Utilities.isNetworkConnected(getApplicationContext())) {
                    Log.i("ServiceSincrofacturas", "No se han podido descargar los archivos pendientes"
                            + " << Causa: " + e.getMessage() + " >>");
                }
            }
        }
    }
}
