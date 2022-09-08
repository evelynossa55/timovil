package com.cm.timovil2.bl.utilities;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.data.FacturaDAL;
import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.dto.FacturaDTO;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.dto.wsentities.MValFact;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * CREADO POR JORGE ANDRES DAVID CARDONA EL 28/03/2016.
 */
public class ServiceMonitorFacturacion extends IntentService {

    private Timer timer;
    private MonitorFacturacionTask task;
    private ActivityBase context;
    private FacturaDAL facturaDAL;
    private ArrayList<FacturaDTO> facturas;

    public ServiceMonitorFacturacion() {
        super("ServiceMonitorFacturacion");
        this.context = App.actualActivity;

        facturaDAL = new FacturaDAL(context);
        DateTime now = DateTime.now();
        try {
            facturas = facturaDAL.obtenerListado(true, now, now);
        }catch (Exception e){
            facturas = new ArrayList<>();
        }
    }

    public ServiceMonitorFacturacion(ArrayList<FacturaDTO> facturas){
        super("ServiceMonitorFacturacion");
        this.context = App.actualActivity;
        this.facturas = facturas;
    }

    private void iniciarTarea() {

        if (timer != null) {
            timer.cancel();
            task.cancel();
        }

        timer = new Timer();
        task = new MonitorFacturacionTask();

        long period = (10000 * 60);//10 minutes
        long delay = 30000; //30 seconds

        //test
        //long period = (10000 * 3);//30 segundos
        //long delay = 1000; //1 second

        synchronized (this) {
            timer.scheduleAtFixedRate(task, delay, period);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent){
        iniciarTarea();
    }

    public void verificar(boolean revisarTodas){
        try {
            if (Utilities.isNetworkReachable(context) &&
                    Utilities.isNetworkConnected(context)) {

                if(context != null){

                    ResolucionDAL resolucionDAL = new ResolucionDAL(context);
                    ResolucionDTO resolucionDTO = resolucionDAL.ObtenerResolucion();

                    if(resolucionDTO != null
                            &&  resolucionDTO.IdCliente!=null
                            && resolucionDTO.CodigoRuta!=null &&
                            !resolucionDTO.IdCliente.equals("") &&
                            !resolucionDTO.CodigoRuta.equals("")) {

                        if (facturas == null || facturas.size() <= 0) {
                            Log.d("MonitorFacService", "No se han creado facturas");
                        } else {

                            NetWorkHelper netWorkHelper = new NetWorkHelper();

                            for (FacturaDTO factura: facturas){

                                if(factura.Revisada && !revisarTodas) continue;

                                String respuesta;

                                if(resolucionDTO.IdCliente.equals(Utilities.ID_FR) && factura.Remision){
                                    factura.NumeroFactura = facturaDAL.obtenerNumeroFacturaFR(factura.NumeroFactura);
                                }

                                String peticion = SincroHelper.ValidacionFacturaURL(
                                        resolucionDTO.IdCliente, factura.NumeroFactura, resolucionDTO.CodigoRuta);

                                respuesta = netWorkHelper.readService(peticion);
                                MValFact result = SincroHelper.procesarMonitorFacturacionJsonObject(respuesta);
                                if(result != null){

                                    //¿Qué hago con esta factura?
                                    boolean Anular = false, DescargarDetalle = false, Sincronizar = false;

                                    if(result.Existe){

                                        boolean AnuladaLocal = factura.Anulada;
                                        int CantidadDetalleLocal = factura.DetalleFactura.size();

                                        boolean AnuladaServer = result.Anulada;
                                        int CantidadDetalleServer = result.CantidadDetalle;

                                        if(AnuladaLocal && !AnuladaServer){
                                            //Hay que sincronizar anulación, anular en server
                                            Sincronizar = true;
                                            factura.PendienteAnulacion = true;
                                        }else
                                        if(!AnuladaLocal && AnuladaServer){
                                            //Anular Local
                                            Anular = true;
                                        }

                                        if(CantidadDetalleServer != CantidadDetalleLocal){
                                            //Sincronizar detalle en server
                                            DescargarDetalle = true;
                                        }

                                        if(!factura.Sincronizada){
                                            //Existe en server pero su estado local es sin sincronizar
                                            //Cambiamos estado a sincronizada
                                            facturaDAL.actualizarEstadoDescarga(factura.NumeroFactura, true, factura.PendienteAnulacion);
                                        }
                                    }

                                    if(Anular){
                                        factura.ComentarioAnulacion = "La factura ya se encontraba anulada en la base de datos. <Monitor de Facturación>";
                                        respuesta = facturaDAL.anular(factura);

                                        if(respuesta.equals("OK")){
                                            Sincronizar = true;
                                            factura.PendienteAnulacion = true;
                                        }

                                    }else if(DescargarDetalle){
                                        respuesta = facturaDAL.sincronizarDetalleFactura(factura);
                                        if(!respuesta.equals("OK")){
                                            Log.d("ServiceSincrofacturas", respuesta);
                                        }
                                    }

                                    if(Sincronizar){
                                        factura.EnviadaDesde = Utilities.FACTURA_ENVIADA_DESDE_MONITOR;
                                        respuesta = facturaDAL.sincronizarFactura(factura);
                                        if (respuesta.equals("OK")) {
                                            Log.d("ServiceSincrofacturas", "<<Se ha descargado una factura>>");
                                        } else {
                                            Log.d("ServiceSincrofacturas", "<<NO Se ha descargado una factura "+respuesta+">>");
                                        }
                                    }

                                    if(!Sincronizar && !DescargarDetalle && !Anular){
                                        facturaDAL.revisar(factura);
                                    }

                                } else {
                                    Log.d("MonitorFacService", "Error verificando la facturación: result = null");
                                }
                            }
                        }
                    }
                }else {
                    Log.d("MonitorFacService", "Error verificando la facturación: context = null");
                }

            } else {
                Log.d("MonitorFacService", "No es posible conectar con el servidor");
            }
        } catch (Exception e) {
            Log.d("MonitorFacService", e.toString());
        }
    }

    private class MonitorFacturacionTask extends TimerTask {

        MonitorFacturacionTask() {}

        @Override
        public void run() {
            verificar(false);
        }

    }
}
